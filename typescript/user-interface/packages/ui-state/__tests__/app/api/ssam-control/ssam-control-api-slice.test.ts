/* eslint-disable jest/expect-expect */
import { SohTypes } from '@gms/common-model';

import {
  ssamControlApiSlice,
  useGetSohConfigurationQuery,
  useRetrieveDecimatedHistoricalStationSohQuery
} from '../../../../src/ts/app/api/ssam-control';
import type { RetrieveDecimatedHistoricalStationSohQueryProps } from '../../../../src/ts/app/api/ssam-control/retrieve-decimated-historical-station-soh';
import { expectQueryHookToMakeAxiosRequest } from '../query-test-util';

describe('SSAM Control API Slice', () => {
  it('provides', () => {
    expect(ssamControlApiSlice).toBeDefined();
    expect(useGetSohConfigurationQuery).toBeDefined();
    expect(useRetrieveDecimatedHistoricalStationSohQuery).toBeDefined();
  });

  it('hook queries for SOH configuration', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetSohConfigurationQuery);
  });

  it('hook queries for decimated historical station soh', async () => {
    const params: RetrieveDecimatedHistoricalStationSohQueryProps = {
      data: {
        startTime: 200,
        endTime: 500,
        samplesPerChannel: 50,
        sohMonitorType: SohTypes.SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
        stationName: 'test station name'
      },
      maxQueryIntervalSize: 200
    };

    const useTestHook = () =>
      ssamControlApiSlice.useRetrieveDecimatedHistoricalStationSohQuery(params);

    await expectQueryHookToMakeAxiosRequest(useTestHook);
  });
});
