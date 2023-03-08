import type { AnalystWaveformTypes, AppState } from '@gms/ui-state';
import { getStore, useAppDispatch, useAppSelector } from '@gms/ui-state';
import { processingAnalystConfiguration } from '@gms/ui-state/__tests__/__data__/processing-analyst-configuration';
import { expectQueryHookToMakeAxiosRequest } from '@gms/ui-state/__tests__/app/api/query-test-util';
import { appState } from '@gms/ui-state/__tests__/test-util';
import { act, renderHook } from '@testing-library/react-hooks';
import cloneDeep from 'lodash/cloneDeep';

import {
  useEventOnClickHandler,
  useHideShowContextMenuState,
  useIsMapSyncedToWaveformZoom,
  useMapNonPreferredEventData,
  useMapPreferredEventData,
  useSdOnClickHandler,
  useSetIsMapSyncedToWaveformZoom,
  useSignalDetectionForMap,
  useStationData,
  useStationOnClickHandler
} from '../../../../../src/ts/components/analyst-ui/components/map/ian-map-hooks';
import { useQueryStateResult } from '../../../../__data__/test-util-data';
import { renderReduxHook } from '../../../../utils/render-hook-util';
import { eventResultsWithRejected } from '../events/event-data-types';

const processingAnalystConfigurationQuery = cloneDeep(useQueryStateResult);
processingAnalystConfigurationQuery.data = processingAnalystConfiguration;

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
jest.setTimeout(60000);

jest.mock('@gms/ui-state', () => {
  const actualRedux = jest.requireActual('@gms/ui-state');
  const mockDispatchFunc = jest.fn();
  const mockDispatch = () => mockDispatchFunc;
  const mockUseAppDispatch = jest.fn(mockDispatch);
  return {
    ...actualRedux,
    useAppDispatch: mockUseAppDispatch,
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
      state.app.analyst.selectedSdIds = ['sd-name'];
      return stateFunc(state);
    }),
    useGetProcessingAnalystConfigurationQuery: jest.fn(() => processingAnalystConfigurationQuery),
    useGetEvents: jest.fn(() => ({
      ...eventResultsWithRejected
    })),
    useWorkflowQuery: jest.fn(() => ({
      isSuccess: true,
      data: { stages: [{ name: 'Auto Network' }, { name: 'AL1' }] }
    }))
  };
});

const store = getStore();

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

describe('ui map', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('is defined', () => {
    expect(useHideShowContextMenuState).toBeDefined();
    expect(useStationData).toBeDefined();
    expect(useSignalDetectionForMap).toBeDefined();
    expect(useStationOnClickHandler).toBeDefined();

    expect(useIsMapSyncedToWaveformZoom).toBeDefined();
    expect(useSetIsMapSyncedToWaveformZoom).toBeDefined();

    expect(useMapPreferredEventData).toBeDefined();
    expect(useMapNonPreferredEventData).toBeDefined();
    expect(useEventOnClickHandler).toBeDefined();
  });

  // eslint-disable-next-line jest/expect-expect
  it('call useSignalDetectionForMap', async () => {
    const useTestHook = () => useSignalDetectionForMap({ startTimeSecs: 0, endTimeSecs: 999 });
    await expectQueryHookToMakeAxiosRequest(useTestHook);
  });

  it('call useSetIsMapSyncedToWaveformZoom', () => {
    useSetIsMapSyncedToWaveformZoom(true);
    expect(useAppDispatch).toHaveBeenCalledTimes(1);
  });

  it('call useMapPreferredEventData', () => {
    const result = renderReduxHook(store, () => useMapPreferredEventData());
    expect(result).toMatchSnapshot();
  });

  it('call useMapNonPreferredEventData', () => {
    const result = renderReduxHook(store, () => useMapNonPreferredEventData());
    expect(result).toMatchSnapshot();
  });

  it('call useStationOnClickHandler', () => {
    const mockDispatch = useAppDispatch();
    const { result } = renderHook(() => useStationOnClickHandler());
    act(() => {
      const entity: any = {
        id: 'station-name',
        properties: {
          type: {
            getValue: jest.fn(() => 'Station')
          }
        }
      };
      const onClick = result.current(entity);
      onClick();
    });
    expect(useAppSelector).toHaveBeenCalledTimes(1);
    expect(useAppDispatch).toHaveBeenCalledTimes(2);
    expect(mockDispatch).toHaveBeenCalledTimes(1);
    expect(mockDispatch).toHaveBeenCalledWith(expect.any(Function));
    act(() => {
      const entity: any = {
        id: 'station-wrong-name',
        properties: {
          type: {
            getValue: jest.fn(() => 'Station')
          }
        }
      };
      const onClick = result.current(entity);
      onClick();
    });
    expect(useAppSelector).toHaveBeenCalledTimes(1);
    expect(useAppDispatch).toHaveBeenCalledTimes(2);
    expect(mockDispatch).toHaveBeenCalledTimes(2);
    expect(mockDispatch).toHaveBeenCalledWith(expect.any(Function));
  });

  it('call useEventOnClickHandler', () => {
    const mockDispatch = useAppDispatch();
    const { result } = renderHook(() => useEventOnClickHandler());
    act(() => {
      const entity: any = {
        id: 'event-name',
        properties: {
          type: {
            getValue: jest.fn(() => 'Event location')
          }
        }
      };
      const onClick = result.current(entity);
      onClick();
    });
    expect(useAppSelector).toHaveBeenCalledTimes(1);
    expect(useAppDispatch).toHaveBeenCalledTimes(2);
    expect(mockDispatch).toHaveBeenCalledTimes(1);
    act(() => {
      const entity: any = {
        id: 'event-wrong-name',
        properties: {
          type: {
            getValue: jest.fn(() => 'Event location')
          }
        }
      };
      const onClick = result.current(entity);
      onClick();
    });
    expect(useAppSelector).toHaveBeenCalledTimes(1);
    expect(useAppDispatch).toHaveBeenCalledTimes(2);
    expect(mockDispatch).toHaveBeenCalledTimes(2);
    act(() => {
      const entity: any = {
        id: 'station',
        properties: {
          type: {
            getValue: jest.fn(() => 'Station')
          }
        }
      };
      const onClick = result.current(entity);
      onClick();
    });
    expect(useAppSelector).toHaveBeenCalledTimes(1);
    expect(useAppDispatch).toHaveBeenCalledTimes(2);
    expect(mockDispatch).toHaveBeenCalledTimes(2);
  });

  it('call useSdOnClickHandler', () => {
    const mockDispatch = useAppDispatch();
    const { result } = renderHook(() => useSdOnClickHandler());
    act(() => {
      const entity: any = {
        id: 'sd-name',
        properties: {
          type: {
            getValue: jest.fn(() => 'Signal detection')
          }
        }
      };
      const onClick = result.current(entity);
      onClick();
    });
    expect(useAppSelector).toHaveBeenCalledTimes(1);
    expect(useAppDispatch).toHaveBeenCalledTimes(2);
    expect(mockDispatch).toHaveBeenCalledTimes(1);
    act(() => {
      const entity: any = {
        id: 'sd-wrong-name',
        properties: {
          type: {
            getValue: jest.fn(() => 'Signal detection')
          }
        }
      };
      const onClick = result.current(entity);
      onClick();
    });
    expect(useAppSelector).toHaveBeenCalledTimes(1);
    expect(useAppDispatch).toHaveBeenCalledTimes(2);
    expect(mockDispatch).toHaveBeenCalledTimes(2);
  });
});
