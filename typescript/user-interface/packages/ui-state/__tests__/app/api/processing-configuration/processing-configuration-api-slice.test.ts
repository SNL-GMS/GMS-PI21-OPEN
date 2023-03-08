/* eslint-disable jest/expect-expect */
import {
  processingConfigurationApiSlice,
  useGetOperationalTimePeriodConfigurationQuery,
  useGetProcessingAnalystConfigurationQuery,
  useGetProcessingCommonConfigurationQuery,
  useGetProcessingStationGroupNamesConfigurationQuery
} from '../../../../src/ts/app/api/processing-configuration';
import { expectQueryHookToMakeAxiosRequest } from '../query-test-util';

describe('Processing Configuration API Slice', () => {
  it('provides', () => {
    expect(processingConfigurationApiSlice).toBeDefined();
    expect(useGetProcessingCommonConfigurationQuery).toBeDefined();
    expect(useGetProcessingAnalystConfigurationQuery).toBeDefined();
    expect(useGetOperationalTimePeriodConfigurationQuery).toBeDefined();
    expect(useGetProcessingStationGroupNamesConfigurationQuery).toBeDefined();
  });

  it('hook queries for processing common configuration', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetProcessingCommonConfigurationQuery);
  });

  it('hook queries for processing analyst configuration', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetProcessingAnalystConfigurationQuery);
  });

  it('hook queries for operational time period configuration', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetOperationalTimePeriodConfigurationQuery);
  });

  it('hook queries for processing stations group name configuration', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetProcessingStationGroupNamesConfigurationQuery);
  });
});
