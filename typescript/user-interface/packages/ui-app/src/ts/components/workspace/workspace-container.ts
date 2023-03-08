import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { AppOperations, commonActions } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import type { WorkspaceProps } from './types';
import { Workspace } from './workspace-component';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<WorkspaceProps> => ({
  userSessionState: state.app.userSession,
  keyPressActionQueue: state.app.common.keyPressActionQueue
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<any> =>
  bindActionCreators(
    {
      setAppAuthenticationStatus: AppOperations.setAppAuthenticationStatus,
      setKeyPressActionQueue: commonActions.setKeyPressActionQueue
    },
    dispatch
  );

export const ReduxWorkspaceContainer: React.ComponentClass<Partial<WorkspaceProps>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(Workspace);
