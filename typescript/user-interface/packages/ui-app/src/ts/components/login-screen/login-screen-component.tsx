/* eslint-disable react/destructuring-assignment */
import { Button, Classes, H2, InputGroup, Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { ConfigurationTypes } from '@gms/common-model';
import type { AuthenticationStatus } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import delay from 'lodash/delay';
import React from 'react';
import { Navigate, useParams } from 'react-router';
import { Slide, ToastContainer } from 'react-toastify';

import { injectTheme } from '~app/initializers/ui-theme-wrapper';
import { legalNotice } from '~config/legal-notice';

import type { LoginScreenProps, LoginScreenState } from './types';

const logger = UILogger.create('GMS_LOG_LOGIN', process.env.GMS_LOG_LOGIN);

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const logo = require('../../../../resources/images/gms-logo-with-text.png');

const LOGO_WIDTH_PX = 370;

// the number of milliseconds between reconnects
const RECONNECT_TIMEOUT_MS = 1000;

/**
 * Checks login status. If the user is logged in, it routes to the page above it. Otherwise it displays a login page
 */
export class LoginScreenComponent extends React.Component<LoginScreenProps, LoginScreenState> {
  /** unique timer id used for attempting reconnects to the gateway */
  private reconnectTimerId: number = undefined;

  public constructor(props: LoginScreenProps) {
    super(props);
    this.state = {
      username: undefined,
      isDarkMode: this.getPreviouslyLoadedUiTheme()?.isDarkMode ?? true
    };
  }

  /**
   * Called immediately after a component is mounted.
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount(): void {
    const uiTheme = this.getPreviouslyLoadedUiTheme();
    if (uiTheme) {
      injectTheme(uiTheme);
      this.setState({ isDarkMode: uiTheme.isDarkMode });
    }
    if (!this.props.authenticationCheckComplete) {
      // check if the user is authenticated
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.props.authenticator
        .checkIsAuthenticated()
        .then(this.setAppAuthenticationStatus)
        .catch(logger.warn);
    }
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(): void {
    if (this.props.failedToConnect) {
      // attempt to reconnect if the app failed to connect to the gateway
      this.reconnect();
    }
    const uiTheme = this.getPreviouslyLoadedUiTheme();
    if (uiTheme && uiTheme.isDarkMode !== this.state.isDarkMode) {
      injectTheme(uiTheme);
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ isDarkMode: uiTheme.isDarkMode });
    }
  }

  /**
   * Called immediately before a component is destroyed. Perform any necessary
   * cleanup in this method, such as canceled network requests,
   * or cleaning up any DOM elements created in componentDidMount.
   */
  public componentWillUnmount(): void {
    if (this.reconnectTimerId) {
      // destroy reconnect timer
      clearTimeout(this.reconnectTimerId);
      this.reconnectTimerId = undefined;
    }
  }

  /**
   * @returns the UI theme that was last loaded from local storage, or the default theme is not found.
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly getPreviouslyLoadedUiTheme = (): ConfigurationTypes.UITheme => {
    const serializedLocalTheme = localStorage.getItem('uiTheme');
    if (!serializedLocalTheme || serializedLocalTheme === 'undefined') {
      return undefined;
    }
    return JSON.parse(serializedLocalTheme);
  };

  /**
   * Display a loading spinner
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    // check the undefined string because the hooks returns it as a string not undefined
    const from = {
      pathname: `/${
        this.props.redirectPath && this.props.redirectPath !== 'undefined'
          ? this.props.redirectPath
          : ''
      }`
    };

    // error state: failed to connect to the gateway
    if (this.props.failedToConnect) {
      return (
        <NonIdealState
          icon={IconNames.ERROR}
          action={<Spinner intent={Intent.DANGER} />}
          title="No connection to server"
          description="Attempting to connect..."
        />
      );
    }

    // attempting to login
    if (!this.props.authenticationCheckComplete) {
      return (
        <NonIdealState
          action={<Spinner intent={Intent.PRIMARY} />}
          title="Checking login"
          description="Attempting to login..."
        />
      );
    }

    // user is authenticated; redirect to the requested page
    if (this.props.authenticated) {
      return <Navigate to={from} />;
    }

    // display the login page
    return (
      <>
        <div
          className={`${this.state.isDarkMode ? Classes.DARK : 'gms-light-mode'} login-screen-body`}
        >
          <div className="login-container">
            <form>
              <img src={logo} width={LOGO_WIDTH_PX} alt="" id="GMSLogo" />
              {legalNotice !== '' ? <H2>Legal Notice</H2> : null}
              <div className="login-legal">{legalNotice}</div>
              <div className="login-row user">
                <div className="login-label">Username:</div>
                <div className="login-input">
                  <InputGroup
                    autoFocus
                    type="text"
                    className="login-input"
                    data-cy="username-input"
                    value={this.state.username || ''}
                    onChange={this.updateState}
                    onKeyDown={this.stopPropagationAndLogin}
                  />
                </div>
              </div>
              <div className="login-row password">
                <div className="login-label">Password:</div>
                <div className="login-input">
                  <InputGroup
                    type="password"
                    className="login-input"
                    disabled
                    value={undefined}
                    onKeyDown={this.stopPropagationAndLogin}
                    autoComplete="off"
                  />
                </div>
              </div>
              <div className="login-row login-button">
                <Button
                  onClick={this.login}
                  text="Login"
                  data-cy="login-btn"
                  disabled={this.state.username === ''}
                />
              </div>
            </form>
          </div>
        </div>
        <ToastContainer
          transition={Slide}
          autoClose={4000}
          position="bottom-right"
          theme={this.state.isDarkMode ? 'dark' : 'light'}
        />
      </>
    );
  }

  /**
   * Updates the state with the username at login
   */
  private readonly updateState = (e: React.ChangeEvent<HTMLInputElement>): void => {
    this.setState({ username: e.target.value });
  };

  /**
   * Performs the login
   */
  private readonly login = () => {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.props.authenticator
      .authenticateWith(this.state.username)
      .then(this.setAppAuthenticationStatus)
      .catch(logger.warn);
  };

  /**
   * Attempts to reconnect to the gateway.
   */
  private readonly reconnect = () => {
    // attempt to reconnect to the gateway
    if (!this.reconnectTimerId) {
      this.delayedReconnect();
    }
  };

  /**
   * Checks if able to connect if not tries to reconnect otherwise updates the auth status
   *
   * @param result result of the auth status
   */
  private readonly ifFailedToConnectTryAgain = (result: AuthenticationStatus): void => {
    if (result.failedToConnect) {
      this.reconnectTimerId = undefined;
      this.reconnect();
    } else {
      this.setAppAuthenticationStatus(result);
    }
  };

  /**
   * Checks if user is authenticated and attempts to connect
   */
  private readonly reconnectIfNotAuthenticated = () => {
    if (this.props.failedToConnect) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.props.authenticator
        .checkIsAuthenticated()
        .then(this.ifFailedToConnectTryAgain)
        .catch(logger.warn);
    }
  };

  /**
   * Timer delay to reattempt to login
   */
  private readonly delayedReconnect = () => {
    delay(this.reconnectIfNotAuthenticated, RECONNECT_TIMEOUT_MS);
  };

  /**
   * Set the login status.
   *
   * @param user the user's authentication status
   */
  private readonly setAppAuthenticationStatus = (user: AuthenticationStatus): void => {
    this.props.setAppAuthenticationStatus(user);
  };

  /**
   * Stops the propagation of the onEnter keypress and calls login
   */
  private readonly stopPropagationAndLogin = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && this.state.username !== '') {
      e.stopPropagation();
      this.login();
    }
  };
}

export function UrlParamGetter(props: LoginScreenProps) {
  const { redirectUrl } = useParams();

  // eslint-disable-next-line react/jsx-props-no-spreading
  return <LoginScreenComponent {...props} redirectPath={redirectUrl} />;
}
