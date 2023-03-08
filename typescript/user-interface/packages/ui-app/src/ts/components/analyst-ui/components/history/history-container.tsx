import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { analystActions, commonActions } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { HistoryComponent } from './history-component';
import type { HistoryReduxProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<HistoryReduxProps> => ({
  analysisMode: state.app.workflow ? state.app.workflow.analysisMode : undefined,
  currentTimeInterval: state.app.workflow ? state.app.workflow.timeRange : undefined,
  openEventId: state.app.analyst.openEventId,
  historyActionInProgress: state.app.analyst.historyActionInProgress
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<HistoryReduxProps> =>
  bindActionCreators(
    {
      setKeyPressActionQueue: commonActions.setKeyPressActionQueue,
      incrementHistoryActionInProgress: analystActions.incrementHistoryAction,
      decrementHistoryActionInProgress: analystActions.decrementHistoryAction
    } as any,
    dispatch
  );

/**
 * A new redux component, that's wrapping the History component and injecting in the redux state
 * and queries and mutations.
 */
export const ReactHistoryContainer = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(HistoryComponent);
