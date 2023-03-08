import { compose } from '@gms/common-util';
import { WithNonIdealStates } from '@gms/ui-core-components';
import type { AppState, SystemMessageSubscription } from '@gms/ui-state';
import { addSystemMessages, clearAllSystemMessages } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { CommonNonIdealStateDefs } from '../non-ideal-states';
import { SystemMessage } from './system-message-component';
import type { SystemMessageProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (
  state: AppState
): Partial<SystemMessageSubscription.SystemMessageReduxProps> => ({
  systemMessagesState: state.app.systemMessage
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<SystemMessageSubscription.SystemMessageReduxProps> =>
  bindActionCreators(
    {
      addSystemMessages,
      clearAllSystemMessages
    },
    dispatch
  );

/**
 * Renders the system message display, or a non-ideal state from the provided list of
 * non ideal state definitions
 */
const SystemMessageComponentOrNonIdealState = WithNonIdealStates<SystemMessageProps>(
  [...CommonNonIdealStateDefs.baseNonIdealStateDefinitions],
  SystemMessage
);

/**
 * A new redux component, that's wrapping the SystemMessage component and injecting in the redux state
 * and react queries and mutations.
 */
export const ReduxReactSystemMessageContainer = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(SystemMessageComponentOrNonIdealState);
