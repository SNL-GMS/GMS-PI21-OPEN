/* eslint-disable jest/expect-expect */
import {
  systemMessageDefinitionApiSlice,
  useGetSystemMessageDefinitionQuery
} from '../../../../src/ts/app/api/system-message-definition';
import { expectQueryHookToMakeAxiosRequest } from '../query-test-util';

describe('System Message Definition API Slice', () => {
  it('provides', () => {
    expect(useGetSystemMessageDefinitionQuery).toBeDefined();
    expect(systemMessageDefinitionApiSlice).toBeDefined();
  });

  it('hook queries for system message definitions', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetSystemMessageDefinitionQuery);
  });
});
