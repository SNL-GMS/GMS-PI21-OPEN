/* eslint-disable jest/expect-expect */
import {
  signalEnhancementConfigurationApiSlice,
  useGetFilterListsDefinitionQuery
} from '../../../../src/ts/app/api/signal-enhancement-configuration';
import { expectQueryHookToMakeAxiosRequest } from '../query-test-util';

describe('Signal Enhancement API Slice', () => {
  it('provides', () => {
    expect(signalEnhancementConfigurationApiSlice).toBeDefined();
    expect(useGetFilterListsDefinitionQuery).toBeDefined();
  });

  it('hook makes a request', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetFilterListsDefinitionQuery);
  });
});
