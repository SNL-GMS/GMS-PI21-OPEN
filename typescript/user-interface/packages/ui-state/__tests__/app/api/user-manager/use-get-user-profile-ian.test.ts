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
    IS_MODE_SOH: false,
    IS_MODE_IAN: true
  };
});

describe('User Manager Profile IAN', () => {
  it('provides', () => {
    expect(useGetUserProfileQuery).toBeDefined();
    expect(useSetUserProfileMutation).toBeDefined();
    expect(userManagerApiSlice).toBeDefined();
  });

  it('hook queries for user profile (ian)', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetUserProfileQuery);
  });
});
