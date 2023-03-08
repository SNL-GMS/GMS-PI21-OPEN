import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { AppOperations } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { UrlParamGetter } from './login-screen-component';
import type { LoginScreenProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<LoginScreenProps> => ({
  authenticated: state.app.userSession.authenticationStatus.authenticated,
  authenticationCheckComplete:
    state.app.userSession.authenticationStatus.authenticationCheckComplete,
  failedToConnect: state.app.userSession.authenticationStatus.failedToConnect
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<LoginScreenProps> =>
  bindActionCreators(
    {
      setAppAuthenticationStatus: AppOperations.setAppAuthenticationStatus
    },
    dispatch
  );

/**
 * Connects the login screen to the redux store
 */
export const ReduxLoginScreenContainer = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(UrlParamGetter);
