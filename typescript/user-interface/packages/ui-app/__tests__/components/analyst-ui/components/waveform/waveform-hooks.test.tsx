/* eslint-disable @typescript-eslint/no-magic-numbers */
import { Units } from '@gms/common-model/lib/common/types';
import { FeatureMeasurementType } from '@gms/common-model/lib/signal-detection';
import type { CheckboxSearchListTypes } from '@gms/ui-core-components';
import type { AnalystWaveformTypes, AppState } from '@gms/ui-state';
import { getStore, usePredictFeaturesForEventLocationQuery, waveformSlice } from '@gms/ui-state';
import { appState, waitForComponentToPaint } from '@gms/ui-state/__tests__/test-util';
import { renderHook } from '@testing-library/react-hooks';
import * as Enzyme from 'enzyme';
import * as React from 'react';
import { Provider } from 'react-redux';

import {
  buildReceiversForFeaturePredictionQuery,
  MAX_FEATURE_PREDICTION_REQUEST,
  useFeaturePredictionQueryByLocationForWaveformDisplay,
  useRawChannelNamesForFeaturePredictions,
  useStationsVisibilityFromCheckboxState
} from '../../../../../src/ts/components/analyst-ui/components/waveform/waveform-hooks';
import { useQueryStateResult } from '../../../../__data__/test-util-data';
import { data } from '../station-properties/mock-station-data';

const { station } = data;
const validDict: AnalystWaveformTypes.StationVisibilityChangesDictionary = {};

validDict[station.name] = { visibility: true, stationName: station.name, isStationExpanded: false };

jest.mock('@gms/ui-state', () => {
  const actualRedux = jest.requireActual('@gms/ui-state');
  return {
    ...actualRedux,
    useAppDispatch: jest.fn(() => jest.fn()),
    useAppSelector: jest.fn((stateFunc: (state: AppState) => any) => {
      const stationsVisibility: AnalystWaveformTypes.StationVisibilityChangesDictionary = {};
      stationsVisibility.name = {
        visibility: true,
        stationName: 'station-name',
        isStationExpanded: false
      };
      const state: AppState = appState;
      const range = { startTimeSecs: 100, endTimeSecs: 200 };
      state.app.workflow.timeRange = range;
      state.app.workflow.openIntervalName = 'AL1';
      state.app.waveform.viewableInterval = range;
      state.app.waveform.stationsVisibility = stationsVisibility;
      state.app.common.selectedStationIds = ['station-name'];
      return stateFunc(state);
    }),
    useGetAllStationsQuery: () => ({ data: [station] }),
    useGetStationsWithChannelsQuery: () => ({ data: [station] }),
    useEffectiveTime: () => 1,
    useVisibleStations: () => [station],
    useGetChannelsByNamesQuery: () => ({ ...useQueryStateResult, data: station.allRawChannels }),
    useWorkflowQuery: jest.fn(() => ({
      isSuccess: true,
      data: { stages: [{ name: 'Auto Network' }, { name: 'AL1' }] }
    })),
    usePredictFeaturesForEventLocationQuery: jest.fn(),
    useGetProcessingAnalystConfigurationQuery: jest.fn(() => ({
      data: {
        priorityPhases: ['P', 'S']
      }
    }))
  };
});

jest.mock('~analyst-ui/components/map/ian-map-hooks', () => {
  const actualHooks = jest.requireActual('~analyst-ui/components/map/ian-map-hooks');
  return {
    ...actualHooks,
    useStationData: jest.fn(() => [station])
  };
});

describe('waveform-hooks', () => {
  test('functions are defined', () => {
    expect(useStationsVisibilityFromCheckboxState).toBeDefined();
    expect(useFeaturePredictionQueryByLocationForWaveformDisplay).toBeDefined();
    expect(useRawChannelNamesForFeaturePredictions).toBeDefined();
  });

  const expectUseStationsVisibilityFromCheckboxStateHookToMatchSnapshot = async () => {
    const getUpdatedCheckboxItemsList = jest.fn(
      (
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        previousList: CheckboxSearchListTypes.CheckboxItem[]
      ): CheckboxSearchListTypes.CheckboxItem[] => {
        return [
          { name: 'name', id: 'name', checked: false },
          { name: 'name2', id: 'name2', checked: true }
        ];
      }
    );

    function TestComponent() {
      const checkboxItemsList: CheckboxSearchListTypes.CheckboxItem[] = [
        { name: 'name', id: 'name', checked: true },
        { name: 'name2', id: 'name2', checked: true }
      ];
      const stationsVisibility: AnalystWaveformTypes.StationVisibilityChangesDictionary = {};
      stationsVisibility.name = {
        visibility: true,
        stationName: 'name',
        isStationExpanded: false
      };
      const setStationsVisibilityFromCheckboxState = useStationsVisibilityFromCheckboxState(
        checkboxItemsList
      );
      setStationsVisibilityFromCheckboxState(getUpdatedCheckboxItemsList);
      return null;
    }

    const store = getStore();
    store.dispatch(waveformSlice.actions.setStationsVisibility(validDict));

    // Mounting may call the request, if React decides to run it soon.
    const wrapper = Enzyme.mount(
      <Provider store={store}>
        <TestComponent />
      </Provider>
    );

    // This ensures that the axios request will have been called.
    await waitForComponentToPaint(wrapper);
    expect(getUpdatedCheckboxItemsList).toHaveBeenCalledTimes(1);
  };
  it('useStationsVisibilityFromCheckboxState matches the snapshot', async () => {
    await expectUseStationsVisibilityFromCheckboxStateHookToMatchSnapshot();
    expect(useStationsVisibilityFromCheckboxState).toBeDefined();
  });

  describe('buildReceiversForFeaturePredictionQuery', () => {
    it('builds receivers for channels with each band type split', () => {
      expect(
        buildReceiversForFeaturePredictionQuery(data.station.allRawChannels, undefined)
      ).toEqual([
        {
          receiverBandType: 'B',
          receiverDataType: null,
          receiverLocationsByName: {
            'BPPPP.BPP01.CNN': {
              depthKm: 100,
              elevationKm: 5500,
              latitudeDegrees: 35.45,
              longitudeDegrees: -125.2345
            },
            'BPPPP.BPP01.BBC': {
              depthKm: 100,
              elevationKm: 5500,
              latitudeDegrees: 35,
              longitudeDegrees: -125
            }
          }
        }
      ]);
    });

    it('builds receivers for stations', () => {
      expect(
        buildReceiversForFeaturePredictionQuery(data.station.allRawChannels, undefined)
      ).toEqual([
        {
          receiverBandType: 'B',
          receiverDataType: null,
          receiverLocationsByName: {
            'BPPPP.BPP01.BBC': {
              depthKm: 100,
              elevationKm: 5500,
              latitudeDegrees: 35,
              longitudeDegrees: -125
            },
            'BPPPP.BPP01.CNN': {
              depthKm: 100,
              elevationKm: 5500,
              latitudeDegrees: 35.45,
              longitudeDegrees: -125.2345
            }
          }
        }
      ]);
    });

    it('splits receiver collections that would have greater then the configured number of receivers', () => {
      const channels = [];

      for (let i = 0; i < MAX_FEATURE_PREDICTION_REQUEST + 10; i += 1) {
        channels.push({
          ...data.station.allRawChannels[0],
          name: 'test channel'.concat(i.toString())
        });
      }
      const receiverCollections = buildReceiversForFeaturePredictionQuery(channels, undefined);
      expect(Object.entries(receiverCollections[0].receiverLocationsByName)).toHaveLength(
        MAX_FEATURE_PREDICTION_REQUEST
      );
      expect(Object.entries(receiverCollections[1].receiverLocationsByName)).toHaveLength(10);
    });
  });

  describe('useFeaturePredictionQueryByLocationForWaveformDisplay', () => {
    const stationReceivers = [
      {
        receiverBandType: 'B',
        receiverDataType: null,
        receiverLocationsByName: {
          'BPPPP.BPP01.CNN': {
            depthKm: 100,
            elevationKm: 5500,
            latitudeDegrees: 35.45,
            longitudeDegrees: -125.2345
          },
          'BPPPP.BPP01.BBC': {
            depthKm: 100,
            elevationKm: 5500,
            latitudeDegrees: 35,
            longitudeDegrees: -125
          }
        }
      },
      {
        receiverBandType: null,
        receiverDataType: null,
        receiverLocationsByName: {
          STA: {
            depthKm: 50,
            elevationKm: 10,
            latitudeDegrees: 35.647,
            longitudeDegrees: 100
          }
        }
      }
    ];
    it('passes in undefined as a locationSolution if not given data', () => {
      renderHook(() =>
        useFeaturePredictionQueryByLocationForWaveformDisplay(
          { data: undefined } as any,
          'testEvent'
        )
      );
      expect(usePredictFeaturesForEventLocationQuery as jest.Mock).toHaveBeenCalledWith({
        phases: ['P', 'S'],
        receivers: stationReceivers,
        sourceLocation: undefined
      });
    });
    it('passes in locationSolutionForOpenEvent as locationSolution if given an event result', () => {
      const mockLocation = { lat: 0, lon: 1 };
      const mockPreferredLocationSolution = {
        id: 'a solution for the location',
        location: mockLocation
      };
      renderHook(() =>
        useFeaturePredictionQueryByLocationForWaveformDisplay(
          {
            data: [
              { id: 'notIt' },
              {
                id: 'testEvent',
                overallPreferred: { preferredLocationSolution: mockPreferredLocationSolution },
                preferredEventHypothesisByStage: [
                  {
                    stage: { name: 'AL1' },
                    preferred: { id: { hypothesisId: '' } }
                  }
                ],
                eventHypotheses: [
                  {
                    preferredLocationSolution: mockPreferredLocationSolution,
                    locationSolutions: [mockPreferredLocationSolution],
                    id: { hypothesisId: '' }
                  }
                ]
              }
            ]
          } as any,
          'testEvent'
        )
      );
      expect(usePredictFeaturesForEventLocationQuery as jest.Mock).toHaveBeenCalledWith({
        phases: ['P', 'S'],
        receivers: stationReceivers,
        sourceLocation: mockLocation
      });
    });
  });

  describe('useRawChannelNamesForFeaturePredictions', () => {
    const signalDetectionResult = [
      {
        id: '012de1b9-8ae3-3fd4-800d-58665c3152cc',
        monitoringOrganization: 'GMS',
        signalDetectionHypotheses: [
          {
            id: {
              id: '20cc9505-efe3-3068-b7d5-59196f37992c',
              signalDetectionId: '012de1b9-8ae3-3fd4-800d-58665c3152cc'
            },
            parentSignalDetectionHypothesis: null,
            rejected: false,
            monitoringOrganization: 'GMS',
            featureMeasurements: [
              {
                channel: {
                  name:
                    'BPPPP.BPP01/beam,fk,coherent/steer,az_90.142deg,slow_7.122s_per_deg/06c0cb24-ab8f-3853-941d-bdf5e73a51b4',
                  effectiveAt: 1636503404
                },
                measuredChannelSegment: {
                  id: {
                    channel: {
                      name:
                        'BPPPP.BPP01/beam,fk,coherent/steer,az_90.142deg,slow_7.122s_per_deg/06c0cb24-ab8f-3853-941d-bdf5e73a51b4',
                      effectiveAt: 1636503404
                    },
                    startTime: 1636503404,
                    endTime: 1636503704,
                    creationTime: 1636503404
                  }
                },
                measurementValue: {
                  arrivalTime: {
                    value: 1636503404,
                    standardDeviation: 1.162
                  },
                  travelTime: null
                },
                snr: {
                  value: 8.9939442,
                  standardDeviation: null,
                  units: Units.DECIBELS
                },
                featureMeasurementType: FeatureMeasurementType.ARRIVAL_TIME
              }
            ]
          }
        ],
        station: {
          name: 'BPPPP',
          effectiveAt: 1
        }
      }
    ];
    it('returns raw channel names from signal detection query', () => {
      const { result } = renderHook(() =>
        useRawChannelNamesForFeaturePredictions({
          ...useQueryStateResult,
          data: signalDetectionResult,
          pending: 0,
          fulfilled: 1,
          rejected: 0,
          isLoading: false,
          isError: false
        })
      );
      expect(result.current).toEqual(['BPPPP.BPP01.CNN', 'BPPPP.BPP01.BBC']);
    });
  });
});
