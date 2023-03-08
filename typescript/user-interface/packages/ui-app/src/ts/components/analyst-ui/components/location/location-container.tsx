import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { analystActions, AnalystWorkspaceOperations } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { Location } from './location-component';
import type { LocationReduxProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<LocationReduxProps> => ({
  currentTimeInterval: state.app.workflow ? state.app.workflow.timeRange : undefined,
  analysisMode: state.app.workflow ? state.app.workflow.analysisMode : undefined,
  openEventId: state.app.analyst.openEventId,
  selectedSdIds: state.app.analyst.selectedSdIds,
  measurementMode: state.app.analyst.measurementMode,
  sdIdsToShowFk: state.app.analyst.sdIdsToShowFk,
  location: state.app.analyst.location
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<LocationReduxProps> =>
  bindActionCreators(
    {
      setSelectedSdIds: analystActions.setSelectedSdIds,
      setOpenEventId: AnalystWorkspaceOperations.setOpenEventId,
      setSelectedEventIds: analystActions.setSelectedEventIds,
      setSdIdsToShowFk: analystActions.setSdIdsToShowFk,
      setMeasurementModeEntries: AnalystWorkspaceOperations.setMeasurementModeEntries,
      setSelectedLocationSolution: AnalystWorkspaceOperations.setSelectedLocationSolution,
      setSelectedPreferredLocationSolution:
        AnalystWorkspaceOperations.setSelectedPreferredLocationSolution
    },
    dispatch
  );

/**
 * A new redux component, that's wrapping the Location component and injecting in the redux state.
 */
export const ReduxLocationContainer: React.ComponentClass<Pick<any, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(Location);
