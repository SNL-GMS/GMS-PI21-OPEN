// ! this is ignored because there is no default export for the package common-util
// ! importing `* as constants from '@gms/common-util'` does not work with Object.defineProperty
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import constants from '@gms/common-util';

import { ianAuthenticator } from '../../../src/ts/app/authentication/ian-authentication';

jest.mock('@gms/common-util', () => {
  const original = jest.requireActual('@gms/common-util');
  return {
    ...original,
    IS_MODE_IAN: true,
    isIanMode: jest.fn(() => true)
  };
});
// TODO: figure out object.assign error on run
// eslint-disable-next-line jest/no-disabled-tests
describe('ian-authentication', () => {
  test('imports should be defined', () => {
    expect(ianAuthenticator).toBeDefined();
  });

  test('ianAuthenticator should have defined object fields', () => {
    /* eslint-disable @typescript-eslint/unbound-method */
    expect(ianAuthenticator.authenticateWith).toBeDefined();
    expect(ianAuthenticator.checkIsAuthenticated).toBeDefined();
    expect(ianAuthenticator.unAuthenticateWith).toBeDefined();
    expect(ianAuthenticator.logout).toBeDefined();
  });

  test("authenticateWith('test') with IS_MODE_IAN = true should return an authenticated session", async () => {
    /*
      See this github issue: https://github.com/facebook/jest/issues/879
      this, plus using a non-star import of constants, coupled with the jest.mock (to ensure it is defined),
      allows us to overwrite this isIanMode function.
    */
    Object.defineProperty(constants, 'isIanMode', {
      get: function get() {
        return () => true;
      },
      configurable: true
    });
    const result = await ianAuthenticator.authenticateWith('test');

    expect(result.authenticated).toBeTruthy();
    expect(result).toMatchSnapshot();
  });

  test("authenticateWith('test') with IS_MODE_IAN = false should return an authenticated session", async () => {
    Object.defineProperty(constants, 'isIanMode', {
      get: function get() {
        return () => false;
      },
      configurable: true
    });
    const result = await ianAuthenticator.authenticateWith('test');
    expect(result.authenticated).toBeTruthy();
    expect(result).toMatchSnapshot();
  });

  test('authenticateWith() with IS_MODE_IAN = true should return an authenticated session', async () => {
    Object.defineProperty(constants, 'isIanMode', {
      get: function get() {
        return () => true;
      },
      configurable: true
    });
    const result = await ianAuthenticator.authenticateWith('username');
    expect(result.authenticated).toBeTruthy();
    expect(result).toMatchSnapshot();
  });

  test('authenticateWith() with IS_MODE_IAN = false should return an unauthenticated session', async () => {
    Object.defineProperty(constants, 'isIanMode', {
      get: function get() {
        return () => false;
      },
      configurable: true
    });
    const result = await ianAuthenticator.authenticateWith('');
    expect(result.authenticated).toBeFalsy();
    expect(result).toMatchSnapshot();
  });

  test('checkIsAuthenticated should return an authenticated session', async () => {
    Object.defineProperty(constants, 'isIanMode', {
      get: function get() {
        return () => true;
      },
      configurable: true
    });
    const result = await ianAuthenticator.checkIsAuthenticated();
    expect(result.authenticated).toBeTruthy();
    expect(result).toMatchSnapshot();
  });

  test('logout should call back with an unauthenticated session', () => {
    Object.defineProperty(constants, 'isIanMode', {
      get: function get() {
        return () => false;
      },
      configurable: true
    });
    ianAuthenticator.logout(result => {
      expect(result.authenticated).toBeFalsy();
      expect(result).toMatchSnapshot();
    });
  });

  test('checkIsAuthenticated should return an unauthenticated session', async () => {
    Object.defineProperty(constants, 'isIanMode', {
      get: function get() {
        return () => true;
      },
      configurable: true
    });
    const result = await ianAuthenticator.checkIsAuthenticated();
    expect(result.authenticated).toBeFalsy();
    expect(result).toMatchSnapshot();
  });

  test('unauthenticated if logout should fail', () => {
    Object.defineProperty(constants, 'isIanMode', {
      get: function get() {
        return () => true;
      },
      configurable: true
    });
    ianAuthenticator.logout(result => {
      // eslint-disable-next-line no-eval
      eval('💣'); // Exercise the "catch" of the logout
      expect(result.authenticated).toBeFalsy();
    });
  });

  test('logs an error if logout should fail', () => {
    const errorMessage = 'Failed to un - authenticate: SyntaxError: Invalid or unexpected token';
    Object.defineProperty(constants, 'isIanMode', {
      value: true,
      configurable: true
    });
    ianAuthenticator.logout(() => {
      Object.defineProperty(constants, 'isIanMode', {
        get: function get() {
          return () => true;
        },
        configurable: true
      });
      // eslint-disable-next-line no-eval
      eval('💣'); // Exercise the "catch" of the logout
      // eslint-disable-next-line no-console
      console.error = jest.fn();
      // eslint-disable-next-line no-console
      expect(console.error).toHaveBeenCalledWith(errorMessage);
    });
  });
});
