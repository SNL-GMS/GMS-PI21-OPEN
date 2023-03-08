/* eslint-disable react/jsx-props-no-spreading */
import type { StationTypes } from '@gms/common-model';
import { CommonTypes, ConfigurationTypes, WorkflowTypes } from '@gms/common-model';
import { readJsonData, toEpochSeconds } from '@gms/common-util';
import {
  analystActions,
  AnalystWaveformOperations,
  AnalystWorkspaceOperations,
  AnalystWorkspaceTypes,
  commonActions,
  getStore,
  setSelectedStationIds,
  waveformActions
} from '@gms/ui-state';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import { mount } from 'enzyme';
import * as path from 'path';
import * as React from 'react';
import { Provider } from 'react-redux';

import type { WaveformComponentProps } from '../../../../../src/ts/components/analyst-ui/components/waveform/types';
import { WaveformComponent } from '../../../../../src/ts/components/analyst-ui/components/waveform/waveform-component';
import { getAlignablePhases } from '../../../../../src/ts/components/analyst-ui/components/waveform/weavess-stations-util';

const currentTimeInterval: CommonTypes.TimeRange = {
  startTimeSecs: toEpochSeconds('2010-05-20T22:00:00.000Z'),
  endTimeSecs: toEpochSeconds('2010-05-20T23:59:59.000Z')
};
const currentStageName = 'AL1';

const basePath = path.resolve(__dirname, './__data__');

const featurePredictions = readJsonData(path.resolve(basePath, 'featurePredictions.json'))[0];

const waveformProps: WaveformComponentProps = {
  viewableInterval: {
    startTimeSecs: 1,
    endTimeSecs: 100
  },
  pan: jest.fn(),
  setViewableInterval: jest.fn(),
  setZoomInterval: jest.fn(),
  setMaximumOffset: jest.fn(),
  maximumOffset: 0,
  setMinimumOffset: jest.fn(),
  minimumOffset: 0,
  baseStationTime: 0,
  stationsVisibility: {},
  currentTimeInterval,
  currentStageName,
  shouldShowTimeUncertainty: true,
  shouldShowPredictedPhases: true,
  featurePredictionQuery: undefined,
  stationsQuery: undefined,
  processingAnalystConfigurationQuery: undefined,
  qcMaskQuery: undefined,
  markAmplitudeMeasurementReviewed: undefined,
  setMode: AnalystWorkspaceOperations.setMode,
  setOpenEventId: analystActions.setOpenEventId,
  setSelectedSdIds: analystActions.setSelectedSdIds,
  setSelectedStationIds,
  setSdIdsToShowFk: analystActions.setSdIdsToShowFk,
  setMeasurementModeEntries: analystActions.setMeasurementModeEntries,
  setChannelFilters: analystActions.setChannelFilters,
  setDefaultSignalDetectionPhase: analystActions.setDefaultSignalDetectionPhase,
  setSelectedSortType: analystActions.setSelectedSortType,
  setKeyPressActionQueue: commonActions.setKeyPressActionQueue,
  setStationsVisibility: waveformActions.setStationsVisibility,
  isStationVisible: jest.fn().mockReturnValue(true),
  isStationExpanded: jest.fn().mockReturnValue(false),
  getVisibleStationsFromStationList: jest
    .fn()
    .mockImplementation((stations: StationTypes.Station[]) => stations),
  setStationVisibility: AnalystWaveformOperations.setStationVisibility,
  setStationExpanded: AnalystWaveformOperations.setStationExpanded,
  setChannelVisibility: AnalystWaveformOperations.setChannelVisibility,
  setBaseStationTime: waveformActions.setBaseStationTime,
  setShouldShowTimeUncertainty: jest.fn(),
  setShouldShowPredictedPhases: jest.fn(),
  showAllChannels: jest.fn(),
  location: {
    selectedLocationSolutionId: '',
    selectedPreferredLocationSolutionId: '',
    selectedPreferredLocationSolutionSetId: '',
    selectedLocationSolutionSetId: ''
  },
  defaultSignalDetectionPhase: CommonTypes.PhaseType.P,
  currentOpenEventId: '1',
  selectedSdIds: [],
  selectedStationIds: [],
  selectedSortType: AnalystWorkspaceTypes.WaveformSortType.stationNameAZ,
  analysisMode: WorkflowTypes.AnalysisMode.EVENT_REVIEW,
  measurementMode: {
    mode: AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT,
    entries: undefined
  } as AnalystWorkspaceTypes.MeasurementMode,
  sdIdsToShowFk: [],
  channelFilters: {},
  openEventId: undefined,
  keyPressActionQueue: {},
  uiTheme: {
    name: 'mockTheme',
    isDarkMode: true,
    colors: ConfigurationTypes.defaultColorTheme,
    display: {
      edgeEventOpacity: 0.35,
      edgeSDOpacity: 0.2,
      predictionSDOpacity: 0.1
    }
  },
  alignablePhases: getAlignablePhases([featurePredictions]),
  eventStatuses: {},
  distances: [],
  phaseToAlignOn: undefined,
  alignWaveformsOn: AlignWaveformsOn.TIME,
  setAlignWaveformsOn: jest.fn(),
  setPhaseToAlignOn: jest.fn(),
  stationsAssociatedWithCurrentOpenEvent: [],
  setStationsAssociatedWithCurrentOpenEvent: jest.fn()
};

describe('Waveform Component', () => {
  test('Can mount waveform component', () => {
    const wrapper = mount(
      <Provider store={getStore()}>
        <WaveformComponent {...waveformProps} />
      </Provider>
    );
    expect(wrapper).toBeDefined();
  });
});
