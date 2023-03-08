import type { AxiosResponse } from 'axios';
import Axios from 'axios';

/**
 * Defines an item to perform a health check on
 */
export interface HealthCheckItem {
  /** unique id */
  id: string;
  /** the path */
  path: string;
}

/**
 * Defines a check response.
 */
export interface CheckResponse {
  status: HealthStatus;
  code: number;
  value: string;
}

/**
 * Defines a health check response.
 */
export interface HealthCheckResponse extends CheckResponse {
  path: string;
}

/**
 * Health Check Status
 */
export enum HealthStatus {
  OK = 'OK',
  FAILED = 'Failed'
}

/**
 * Handles the response for the provided Axios requests
 *
 * @param promises axios requests
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const check = async (promises: Promise<AxiosResponse<any>>[]): Promise<CheckResponse[]> => {
  const wrappedPromises = promises.map(async p =>
    Promise.resolve(p)
      .then(val => ({
        status: HealthStatus.OK,
        code: val.status,
        value: val.data
      }))
      .catch(err => {
        if (err.response) {
          return {
            status: HealthStatus.FAILED,
            code: err.response.status,
            value: JSON.parse(JSON.stringify(err.response.data).replace(/(<([^>]+)>)|\\n/gi, ''))
          };
        }

        if (err.request) {
          // the request was made but no response was received
          return {
            status: HealthStatus.FAILED,
            code: 500,
            value: err.request
          };
        }

        // something happened in setting up the request that triggered an Error
        return {
          status: HealthStatus.FAILED,
          code: 500,
          value: err.message
        };
      })
  );
  return Promise.all(wrappedPromises);
};

/**
 * Performs a health check for each id/path provided.
 *
 * @param items the items to check
 */
export const healthChecks = async (items: HealthCheckItem[]): Promise<HealthCheckResponse[]> => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const checkPromises: Promise<AxiosResponse<any>>[] = [];
  items.forEach(item => checkPromises.push(Axios.get(item.path)));
  const checkResults: HealthCheckResponse[] = [];
  return check(checkPromises).then(results => {
    results.forEach((result, index) => {
      checkResults.push({
        path: items[index].id,
        status: result.status,
        code: result.code,
        value: result.value
      });
    });
    return checkResults;
  });
};
