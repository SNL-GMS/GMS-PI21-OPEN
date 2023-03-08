/* eslint-disable jest/expect-expect */
import {
  useGetUserProfileQuery,
  userManagerApiSlice,
  useSetUserProfileMutation
} from '../../../../src/ts/app/api/user-manager';
import { expectQueryHookToMakeAxiosRequest } from '../query-test-util';

jest.mock('@gms/common-util', () => {
  const actual = jest.requireActual('@gms/common-util');
  return {
    ...actual,
    IS_MODE_SOH: true,
    IS_MODE_IAN: false
  };
});

describe('User Manager Profile SOH', () => {
  it('provides', () => {
    expect(useGetUserProfileQuery).toBeDefined();
    expect(useSetUserProfileMutation).toBeDefined();
    expect(userManagerApiSlice).toBeDefined();
  });

  it('hook queries for user profile (soh)', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetUserProfileQuery);
  });
});
