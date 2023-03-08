/* eslint-disable react/jsx-props-no-spreading */
import React from 'react';

import { authenticator } from '../../src/ts/app/authentication';
import { LoginScreenComponent } from '../../src/ts/components/login-screen/login-screen-component';
import type { LoginScreenReduxProps } from '../../src/ts/components/login-screen/types';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

const reduxProps: LoginScreenReduxProps = {
  redirectPath: '',
  authenticated: false,
  authenticationCheckComplete: false,
  failedToConnect: true,
  setAppAuthenticationStatus: jest.fn()
};

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();
describe('Login screen', () => {
  it('should be defined', () => {
    expect(LoginScreenComponent).toBeDefined();
  });

  const loginScreenNonIdealState: any = Enzyme.mount(
    <LoginScreenComponent authenticator={authenticator} {...reduxProps} />
  );

  it('failed to connect should return non ideal state', () => {
    expect(loginScreenNonIdealState).toMatchSnapshot();
  });

  reduxProps.failedToConnect = false;

  const loginScreenNonIdealState2: any = Enzyme.mount(
    <LoginScreenComponent authenticator={authenticator} {...reduxProps} />
  );

  it('failed authentication check complete should return non ideal state', () => {
    expect(loginScreenNonIdealState2).toMatchSnapshot();
  });

  reduxProps.authenticationCheckComplete = true;
  reduxProps.authenticated = false;

  const loginScreen: any = Enzyme.mount(
    <LoginScreenComponent authenticator={authenticator} {...reduxProps} />
  );

  it('Connected, authentication check complete, and not authenticated should return login page', () => {
    expect(loginScreen).toMatchSnapshot();
  });

  const loginScreen2: any = Enzyme.shallow(
    <LoginScreenComponent authenticator={authenticator} {...reduxProps} />
  );

  loginScreen2.instance().setAppAuthenticationStatus({
    userName: 'someUser',
    authenticated: false,
    authenticationCheckComplete: true,
    failedToConnect: jest.fn()
  });
  // eslint-disable-next-line @typescript-eslint/unbound-method, jest/no-standalone-expect
  expect(reduxProps.setAppAuthenticationStatus).toHaveBeenCalled();

  reduxProps.failedToConnect = true;
  const loginScreen3: any = Enzyme.shallow(
    <LoginScreenComponent authenticator={authenticator} {...reduxProps} />
  );

  reduxProps.authenticated = true;
  reduxProps.authenticationCheckComplete = true;
  reduxProps.failedToConnect = false;

  const loginRedirect: any = Enzyme.shallow(
    <LoginScreenComponent authenticator={authenticator} {...reduxProps} />
  );

  it('Authenticated and accessing login page should return redirect', () => {
    expect(loginRedirect).toMatchSnapshot();
  });

  it('Private methods work as expected', async () => {
    loginScreen3.instance().state.username = 'someUser';
    const input = { key: 'Enter', stopPropagation: jest.fn() };
    loginScreen3.instance().stopPropagationAndLogin(input);
    expect(input.stopPropagation).toHaveBeenCalled();

    loginScreen3.instance().reconnectTimerId = undefined;
    loginScreen3.instance().forceUpdate();
    loginScreen3.instance().reconnect();
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(reduxProps.setAppAuthenticationStatus).toHaveBeenCalled();

    await loginScreen3.instance().reconnectIfNotAuthenticated();
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(reduxProps.setAppAuthenticationStatus).toHaveBeenCalled();

    const authStatus: any = { failedToConnect: true, authenticated: true };
    loginScreen3.instance().ifFailedToConnectTryAgain(authStatus);
    // eslint-disable-next-line
    expect(loginScreen3.instance().reconnectTimerId).toBeUndefined;

    authStatus.failedToConnect = false;
    loginScreen3.instance().ifFailedToConnectTryAgain(authStatus);
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(reduxProps.setAppAuthenticationStatus).toHaveBeenCalled();

    loginScreen3.instance().setAppAuthenticationStatus(authStatus);
    loginScreen3.instance().setState = jest.fn();
    loginScreen3.instance().updateState({ target: { value: 'someUsername' } });
    expect(loginScreen3.instance().setState).toHaveBeenCalled();
  });

  it('React methods work as expected', () => {
    loginScreen3.instance().reconnectTimerId = 'someId';
    loginScreen3.instance().componentWillUnmount();
    expect(loginScreen3.instance().reconnectTimerId).toBeUndefined();
  });
});
