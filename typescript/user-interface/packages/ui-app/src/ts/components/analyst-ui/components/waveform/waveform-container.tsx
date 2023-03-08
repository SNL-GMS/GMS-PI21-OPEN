import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import {
  analystActions,
  AnalystWorkspaceOperations,
  commonActions,
  setSelectedStationIds
} from '@gms/ui-state';
import type React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import type { WaveformDisplayReduxProps } from './types';
import { WaveformComponent } from './waveform-component';

// map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<WaveformDisplayReduxProps> => ({
  currentTimeInterval: state.app.workflow.timeRange,
  currentStageName: state.app.workflow.openIntervalName,
  currentOpenEventId: state.app.analyst?.openEventId,
  analysisMode: state.app.workflow ? state.app.workflow.analysisMode : undefined,
  location: state.app.analyst.location,
  measurementMode: state.app.analyst.measurementMode,
  channelFilters: state.app.analyst.channelFilters,
  selectedSortType: state.app.analyst.selectedSortType,
  selectedSdIds: state.app.analyst.selectedSdIds,
  keyPressActionQueue: state.app.common.keyPressActionQueue,
  selectedStationIds: state.app.common.selectedStationIds,
  phaseToAlignOn: state.app.analyst.phaseToAlignOn,
  alignWaveformsOn: state.app.analyst.alignWaveformsOn
});

// map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<WaveformDisplayReduxProps> =>
  bindActionCreators(
    {
      setMode: AnalystWorkspaceOperations.setMode,
      setOpenEventId: analystActions.setOpenEventId,
      setSelectedSdIds: analystActions.setSelectedSdIds,
      setSelectedStationIds,
      setSdIdsToShowFk: analystActions.setSdIdsToShowFk,
      setMeasurementModeEntries: AnalystWorkspaceOperations.setMeasurementModeEntries,
      setChannelFilters: analystActions.setChannelFilters,
      setDefaultSignalDetectionPhase: analystActions.setDefaultSignalDetectionPhase,
      setSelectedSortType: analystActions.setSelectedSortType,
      setKeyPressActionQueue: commonActions.setKeyPressActionQueue,
      setPhaseToAlignOn: analystActions.setPhaseToAlignOn,
      setAlignWaveformsOn: analystActions.setAlignWaveformsOn
    },
    dispatch
  );

/**
 * higher-order component WaveformDisplay
 */
export const WaveformDisplay: React.ComponentClass<Pick<any, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(WaveformComponent);
