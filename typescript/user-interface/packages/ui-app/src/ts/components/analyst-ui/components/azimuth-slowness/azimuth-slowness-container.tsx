import { ConfigurationTypes } from '@gms/common-model';
import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { analystActions, AnalystWorkspaceOperations } from '@gms/ui-state';
import type React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { AzimuthSlowness } from './azimuth-slowness-component';
import type { AzimuthSlownessReduxProps } from './types';

/**
 * Container component for Azimuth Slowness
 * Handles mapping of state/props
 */

/**
 *  Mapping between the current redux state and props for the Azimuth Slowness Display
 */
const mapStateToProps = (state: AppState): Partial<AzimuthSlownessReduxProps> => ({
  currentTimeInterval: state.app.workflow ? state.app.workflow.timeRange : undefined,
  analysisMode: state.app.workflow ? state.app.workflow.analysisMode : undefined,
  selectedSdIds: state.app.analyst.selectedSdIds,
  openEventId: state.app.analyst.openEventId,
  sdIdsToShowFk: state.app.analyst.sdIdsToShowFk,
  location: state.app.analyst.location,
  channelFilters: state.app.analyst.channelFilters,
  defaultSignalDetectionPhase: state.app.analyst.defaultSignalDetectionPhase,
  selectedSortType: state.app.analyst.selectedSortType,
  unassociatedSDColor: ConfigurationTypes.defaultColorTheme.unassociatedSDColor
});

/**
 * Map actions dispatch callbacks into this component as props
 */
const mapDispatchToProps = (dispatch): Partial<AzimuthSlownessReduxProps> =>
  bindActionCreators(
    {
      setSelectedSdIds: analystActions.setSelectedSdIds,
      setSdIdsToShowFk: analystActions.setSdIdsToShowFk,
      setChannelFilters: analystActions.setChannelFilters,
      setMeasurementModeEntries: AnalystWorkspaceOperations.setMeasurementModeEntries
    },
    dispatch
  );

/**
 * The higher-order component
 */
export const ReduxAzimuthSlowness: React.ComponentClass<Pick<any, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(AzimuthSlowness);
