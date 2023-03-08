import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { analystActions, AnalystWorkspaceOperations } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { Magnitude } from './magnitude-component';
import type { MagnitudeReduxProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<MagnitudeReduxProps> => ({
  currentTimeInterval: state.app.workflow ? state.app.workflow.timeRange : undefined,
  analysisMode: state.app.workflow ? state.app.workflow.analysisMode : undefined,
  openEventId: state.app.analyst.openEventId,
  selectedSdIds: state.app.analyst.selectedSdIds,
  location: state.app.analyst.location
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<MagnitudeReduxProps> =>
  bindActionCreators(
    {
      setSelectedSdIds: analystActions.setSelectedSdIds,
      setSelectedLocationSolution: AnalystWorkspaceOperations.setSelectedLocationSolution
    },
    dispatch
  );

/**
 * A new redux component, that's wrapping the Magnitude component and injecting in the redux state
 * and queries and mutations.
 */
export const ReduxMagnitudeContainer: React.ComponentClass<Pick<any, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(Magnitude);
