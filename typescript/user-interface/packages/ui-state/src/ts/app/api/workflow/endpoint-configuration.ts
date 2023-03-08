import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * Workflow request config definition
 */
export interface WorkflowRequestConfig extends RequestConfig {
  readonly workflow: {
    readonly baseUrl: string;
    readonly services: {
      readonly workflow: ServiceDefinition;
      readonly stageIntervalsByIdAndTime: ServiceDefinition;
      readonly updateActivityIntervalStatus: ServiceDefinition;
      readonly updateStageIntervalStatus: ServiceDefinition;
    };
  };
}

/**
 * The workflow request config for all services.
 */
export const config: WorkflowRequestConfig = {
  workflow: {
    baseUrl: `${UI_URL}${Endpoints.WorkflowManagerServiceUrls.baseUrl}`,
    services: {
      workflow: {
        requestConfig: {
          method: 'post',
          url: Endpoints.WorkflowManagerServiceUrls.workflow,
          responseType: 'json',
          proxy: false,
          headers: {
            accept: 'application/json',
            'content-type': 'text/plain'
          },
          timeout: 180000,
          data: `"PlaceHolder"`
        }
      },
      stageIntervalsByIdAndTime: {
        requestConfig: {
          method: 'post',
          url: Endpoints.WorkflowManagerServiceUrls.stageIntervalsByIdAndTime,
          responseType: 'json',
          proxy: false,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          timeout: 180000
        }
      },
      updateActivityIntervalStatus: {
        requestConfig: {
          method: 'post',
          url: Endpoints.WorkflowManagerServiceUrls.updateActivityIntervalStatus,
          responseType: 'json',
          proxy: false,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          timeout: 60000
        }
      },
      updateStageIntervalStatus: {
        requestConfig: {
          method: 'post',
          url: Endpoints.WorkflowManagerServiceUrls.updateStageIntervalStatus,
          responseType: 'json',
          proxy: false,
          headers: {
            'content-type': 'application/json'
          },
          timeout: 60000
        }
      }
    }
  }
};
