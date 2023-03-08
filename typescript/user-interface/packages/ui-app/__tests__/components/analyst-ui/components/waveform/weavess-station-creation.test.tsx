/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable no-param-reassign */
import { Colors } from '@blueprintjs/core';
import { ConfigurationTypes } from '@gms/common-model';
import { readJsonData } from '@gms/common-util';
import type { AnalystWaveformTypes } from '@gms/ui-state';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import * as path from 'path';

import type { CreateWeavessStationsParameters } from '../../../../../src/ts/components/analyst-ui/components/waveform/weavess-stations-util';
import { createWeavessStations } from '../../../../../src/ts/components/analyst-ui/components/waveform/weavess-stations-util';
import { eventData } from '../../../../__data__/event-data';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('Weavess Station Creation unit tests', () => {
  const basePath = path.resolve(__dirname, './__data__');
  const currentOpenEvent = eventData[0]; // readJsonData(path.resolve(basePath, 'currentOpenEvent.json'))[0];
  const stationDefinitions = readJsonData(path.resolve(basePath, 'defaultStations.json'));
  const defaultWaveformFilters = readJsonData(
    path.resolve(basePath, 'defaultWaveformFilters.json')
  );
  const events = [eventData];
  const featurePredictions = readJsonData(path.resolve(basePath, 'featurePredictions.json'))[0];
  const maskDisplayFilters = readJsonData(path.resolve(basePath, 'maskDisplayFilters.json'))[0];
  const measurementMode: AnalystWorkspaceTypes.MeasurementMode = {
    mode: AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT,
    entries: {}
  };
  const defaultMode: AnalystWorkspaceTypes.MeasurementMode = {
    mode: AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT,
    entries: {}
  };
  const qcMasksByChannelId = readJsonData(path.resolve(basePath, 'qcMasksByChannelId.json'));
  const signalDetections = signalDetectionsData;

  // Create the station visibility map
  const stationVisibilityDictionary: AnalystWaveformTypes.StationVisibilityChangesDictionary = {};
  stationDefinitions.forEach(station => {
    stationVisibilityDictionary[station.name] = {
      stationName: station.name,
      visibility: true,
      isStationExpanded: true,
      hiddenChannels: undefined
    };
  });

  const channelFilters = {};
  const waveformUtilParams: CreateWeavessStationsParameters = {
    channelFilters,
    channelHeight: 24.8,
    currentOpenEvent,
    defaultStations: stationDefinitions,
    defaultWaveformFilters,
    endTimeSecs: 1274400000,
    events,
    featurePredictions,
    maskDisplayFilters,
    measurementMode: defaultMode,
    offsets: [],
    qcMasksByChannelName: qcMasksByChannelId,
    showPredictedPhases: false,
    showSignalDetectionUncertainty: true,
    signalDetections,
    selectedSdIds: [],
    startTimeSecs: 1274392801,
    distances: [],
    uiChannelSegments: {},
    stationVisibilityDictionary,
    stations: stationDefinitions,
    processingAnalystConfiguration: {
      uiThemes: [
        {
          name: 'GMS Default (dark)',
          isDarkMode: true,
          colors: {
            waveformRaw: Colors.COBALT4,
            waveformFilterLabel: Colors.LIGHT_GRAY5
          }
        }
      ]
    } as ConfigurationTypes.ProcessingAnalystConfiguration,
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
    eventStatuses: {}
  };

  let result = createWeavessStations(
    waveformUtilParams,
    AnalystWorkspaceTypes.WaveformSortType.stationNameAZ,
    []
  );
  result.forEach(station => {
    delete station.defaultChannel.waveform.channelSegmentsRecord;
    station.nonDefaultChannels.forEach(channel => delete channel.waveform.channelSegmentsRecord);
  });

  it('When switching to measurement mode, should show only waveforms/channels with associated SD', () => {
    expect(result).toMatchSnapshot();

    waveformUtilParams.measurementMode = measurementMode;

    result = createWeavessStations(
      waveformUtilParams,
      AnalystWorkspaceTypes.WaveformSortType.stationNameAZ,
      []
    );
    result.forEach(station => {
      delete station.defaultChannel.waveform.channelSegmentsRecord;
      station.nonDefaultChannels.forEach(channel => delete channel.waveform.channelSegmentsRecord);
    });
    expect(result).toMatchSnapshot();
  });
});
