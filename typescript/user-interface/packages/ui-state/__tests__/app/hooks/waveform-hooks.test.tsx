/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/display-name */
/* eslint-disable @typescript-eslint/no-floating-promises */
/* eslint-disable @typescript-eslint/await-thenable */
/* eslint-disable jest/expect-expect */
import type { ChannelTypes, StationTypes } from '@gms/common-model';
import { WaveformTypes } from '@gms/common-model';
import { act, renderHook } from '@testing-library/react-hooks';
import axios from 'axios';
import * as React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';
import type Redux from 'redux';

import { useGetChannelSegments } from '../../../src/ts/app/hooks/channel-segment-hooks';
import type { PanningOptions } from '../../../src/ts/app/hooks/waveform-hooks';
import {
  useBaseStationTime,
  useMaximumOffset,
  useMinimumOffset,
  usePan,
  useShouldShowPredictedPhases,
  useShouldShowTimeUncertainty,
  useStationsVisibility,
  useViewableInterval,
  useZoomInterval
} from '../../../src/ts/app/hooks/waveform-hooks';
import { isChannelVisible } from '../../../src/ts/app/state/waveform/util';
import {
  waveformInitialState,
  waveformSlice
} from '../../../src/ts/app/state/waveform/waveform-slice';
import type { AppState } from '../../../src/ts/app/store';
import { processingAnalystConfiguration } from '../../__data__/processing-analyst-configuration';
import { configureNewStore, HookChecker } from '../../test-util';

axios.request = jest.fn().mockImplementation();

jest.mock(
  '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice',
  () => {
    const actual = jest.requireActual(
      '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice'
    );

    return {
      ...actual,
      processingConfigurationApiSlice: {
        middleware: actual.processingConfigurationApiSlice.middleware,
        endpoints: {
          getProcessingAnalystConfiguration: {
            select: jest.fn(() =>
              jest.fn(() => ({
                data: processingAnalystConfiguration
              }))
            )
          }
        }
      }
    };
  }
);

function checkHookReturnValue<HookReturnType = unknown>(
  storeToUse: Redux.Store<AppState>,
  useHookToTest: () => HookReturnType,
  assertion: (result: HookReturnType) => void
): void {
  create(
    <Provider store={storeToUse}>
      <HookChecker<ReturnType<typeof useHookToTest>> useHook={useHookToTest}>
        {assertion}
      </HookChecker>
    </Provider>
  ).toJSON();
}

async function expectThatAsyncHookUpdates<HookReturnType = unknown>(
  storeToUse: Redux.Store<AppState>,
  useHookToTest: () => [HookReturnType, (result: HookReturnType) => void],
  assertion: (result: ReturnType<typeof useHookToTest>) => Promise<void>
) {
  return new Promise<void>(done => {
    create(
      <Provider store={storeToUse}>
        <HookChecker<ReturnType<typeof useHookToTest>> useHook={useHookToTest}>
          {async (result: ReturnType<typeof useHookToTest>) => {
            await assertion(result);
            done(); // resolve the promise
          }}
        </HookChecker>
      </Provider>
    ).toJSON();
  });
}

const toyInterval = {
  startTimeSecs: 123,
  endTimeSecs: 456
};

function assertReturnsInitialStateAndSetter<HookReturnType>(expectedValue: HookReturnType) {
  return ([value, setValue]) => {
    expect(value).toEqual(expectedValue);
    expect(typeof setValue).toEqual('function');
  };
}

function assertSetterWorks(valueToSet) {
  return async ([value, setValue]: [typeof valueToSet, any]) => {
    if (value == null) {
      await act(() => {
        setValue(valueToSet);
      });
    } else {
      // we use a conditional expect here because it must be hit to consider this a success.
      // if the expect is not hit, the `done` function call on the next line will not be either,
      // ensuring that the test will fail (promise won't resolve and it will time out).
      // throw new Error(JSON.stringify({ value, valueToSet }));
      // eslint-disable-next-line jest/no-conditional-expect
      expect(value).toEqual(valueToSet);
    }
  };
}

describe('UI State Waveform Hooks', () => {
  describe('useViewableInterval', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('returns the initial state and a setter', () => {
      checkHookReturnValue(
        store,
        useViewableInterval,
        assertReturnsInitialStateAndSetter(waveformInitialState.viewableInterval)
      );
    });
    it('sets a value using the setter', async () => {
      await expectThatAsyncHookUpdates(store, useViewableInterval, assertSetterWorks(toyInterval));
    });
  });

  describe('useMinimumOffset', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('returns the initial state and a setter', () => {
      const { result } = renderHook(() => useMinimumOffset(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toBe(waveformInitialState.minimumOffset);
    });
    it('sets a value using the setter', () => {
      const { result } = renderHook(() => useMinimumOffset(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toBe(waveformInitialState.minimumOffset);
      act(() => {
        result.current[1](100);
      });
      expect(result.current[0]).toBe(100);
    });
  });

  describe('useMaximumOffset', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('returns the initial state and a setter', () => {
      const { result } = renderHook(() => useMaximumOffset(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toBe(waveformInitialState.maximumOffset);
    });
    it('sets a value using the setter', () => {
      const { result } = renderHook(() => useMaximumOffset(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toBe(waveformInitialState.maximumOffset);
      act(() => {
        result.current[1](100);
      });
      expect(result.current[0]).toBe(100);
    });
  });

  describe('useZoomInterval', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('returns the initial state and a setter', () => {
      checkHookReturnValue(
        store,
        useZoomInterval,
        assertReturnsInitialStateAndSetter(waveformInitialState.zoomInterval)
      );
    });
    it('sets a value using the setter', async () => {
      // this has to be set to satisfy the error handling in the zoom interval operation
      store.dispatch(waveformSlice.actions.setViewableInterval(toyInterval));
      await expectThatAsyncHookUpdates(
        store,
        useZoomInterval,
        assertSetterWorks({
          startTimeSecs: 234,
          endTimeSecs: 345
        })
      );
    });
  });

  describe('usePan', () => {
    let store: Redux.Store<AppState>;
    const toyZoomInterval = { startTimeSecs: 0, endTimeSecs: 100 };
    beforeEach(() => {
      store = configureNewStore();
      store.dispatch(waveformSlice.actions.setViewableInterval(toyInterval));
      store.dispatch(waveformSlice.actions.setZoomInterval(toyZoomInterval));
    });

    function assertItPans(panDirection: WaveformTypes.PanType, panOptions: PanningOptions) {
      // eslint-disable-next-line no-useless-catch
      return async (
        pan: (panDirection: WaveformTypes.PanType, panOptions: PanningOptions) => void
      ) => {
        expect(typeof pan).toBe('function');
        // eslint-disable-next-line no-useless-catch
        try {
          await act(() => {
            pan(panDirection, panOptions);
          });
        } catch (err) {
          throw err;
        }
        const state = store.getState();
        const { zoomInterval } = state.app.waveform;
        expect(zoomInterval).toMatchSnapshot();
      };
    }

    it('sets the zoom interval', () => {
      const onPanningBoundaryReached = jest.fn();
      checkHookReturnValue(
        store,
        usePan,
        assertItPans(WaveformTypes.PanType.Right, {
          shouldLoadAdditionalData: true,
          onPanningBoundaryReached
        })
      );
    });
  });

  describe('useBaseStationTime', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('returns the initial state and a setter', () => {
      const { result } = renderHook(() => useBaseStationTime(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toBe(waveformInitialState.baseStationTime);
    });
    it('sets a value using the setter', () => {
      const { result } = renderHook(() => useBaseStationTime(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toBe(waveformInitialState.baseStationTime);
      act(() => {
        result.current[1](100);
      });
      expect(result.current[0]).toBe(100);
    });
  });

  describe('Time Uncertainty', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('returns the time uncertainty value and a setter', () => {
      const { result } = renderHook(() => useShouldShowTimeUncertainty(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toBe(waveformInitialState.shouldShowTimeUncertainty);
      const newValue = !waveformInitialState.shouldShowTimeUncertainty;
      act(() => {
        result.current[1](newValue);
      });
      expect(result.current[0]).toBe(newValue);
    });
  });

  describe('Predicted Phases', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('returns the predicted phases value and a setter', () => {
      const { result } = renderHook(() => useShouldShowPredictedPhases(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toBe(waveformInitialState.shouldShowPredictedPhases);
      const newValue = !waveformInitialState.shouldShowPredictedPhases;
      act(() => {
        result.current[1](newValue);
      });
      expect(result.current[0]).toBe(newValue);
    });
  });

  describe('Use Hook for Waveform Query', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('returns the use waveform query for current channels value and a setter', () => {
      store.dispatch(waveformSlice.actions.setViewableInterval(toyInterval));
      const { result } = renderHook(() => useGetChannelSegments(toyInterval), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result).toMatchSnapshot();
    });
  });

  describe('Stations Visibility Hooks', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });

    const mockChannel = { name: 'chan1' } as ChannelTypes.Channel;
    const mockStation = {
      name: 'station1',
      allRawChannels: [mockChannel]
    } as StationTypes.Station;

    it('returns the default station visibility', () => {
      const { result } = renderHook(() => useStationsVisibility(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current.stationsVisibility).toBe(waveformInitialState.stationsVisibility);
    });

    it("can set a station's visibility to true and false", () => {
      const { result } = renderHook(() => useStationsVisibility(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current.stationsVisibility).toBe(waveformInitialState.stationsVisibility);
      act(() => {
        result.current.setStationVisibility(mockStation, true);
      });
      expect(result.current.stationsVisibility[mockStation.name].visibility).toBe(true);
      act(() => {
        // use string for better branch testing
        result.current.setStationVisibility(mockStation.name, false);
      });
      expect(result.current.stationsVisibility[mockStation.name].visibility).toBe(false);
    });

    it('can tell if a station is visible or not', () => {
      const { result } = renderHook(() => useStationsVisibility(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      act(() => {
        result.current.setStationVisibility(mockStation, true);
      });
      expect(result.current.isStationVisible(mockStation)).toBe(true);
      act(() => {
        result.current.setStationVisibility(mockStation, false);
      });
      expect(result.current.isStationVisible(mockStation)).toBe(false);
    });

    it('can set if a station is expanded or not', () => {
      const { result } = renderHook(() => useStationsVisibility(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current.stationsVisibility).toBe(waveformInitialState.stationsVisibility);
      act(() => {
        result.current.setStationExpanded(mockStation, true);
      });
      expect(result.current.stationsVisibility[mockStation.name].isStationExpanded).toBe(true);
      act(() => {
        // use string for better branch testing
        result.current.setStationExpanded(mockStation.name, false);
      });
      expect(result.current.stationsVisibility[mockStation.name].isStationExpanded).toBe(false);
    });

    it('can tell if a station is expanded or not', () => {
      const { result } = renderHook(() => useStationsVisibility(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      act(() => {
        result.current.setStationExpanded(mockStation, true);
      });
      expect(result.current.isStationExpanded(mockStation)).toBe(true);
      act(() => {
        result.current.setStationExpanded(mockStation, false);
      });
      expect(result.current.isStationExpanded(mockStation)).toBe(false);
    });

    it('can determine which stations are visible in a station list', () => {
      const { result } = renderHook(() => useStationsVisibility(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current.stationsVisibility).toBe(waveformInitialState.stationsVisibility);
      act(() => {
        // set up some stations as visible and some that aren't
        result.current.setStationVisibility('a', true);
        result.current.setStationVisibility('b', true);
        result.current.setStationVisibility('c', false);
        result.current.setStationVisibility('d', true);
      });
      let visibleStations;
      act(() => {
        // use string for better branch testing
        visibleStations = result.current.getVisibleStationsFromStationList([
          { name: 'a' } as StationTypes.Station,
          { name: 'c' } as StationTypes.Station,
          { name: 'd' } as StationTypes.Station
        ]);
      });
      expect(JSON.stringify(visibleStations)).toEqual(
        JSON.stringify([
          { name: 'a' } as StationTypes.Station,
          { name: 'd' } as StationTypes.Station
        ])
      );
    });

    it('can set if a channel is visible or not', () => {
      const { result } = renderHook(() => useStationsVisibility(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current.stationsVisibility).toBe(waveformInitialState.stationsVisibility);
      act(() => {
        result.current.setChannelVisibility(mockStation, mockChannel, false);
      });
      expect(
        result.current.stationsVisibility[mockStation.name].hiddenChannels?.includes(
          mockChannel.name
        )
      ).toBe(true);
      act(() => {
        // use string for better branch testing
        result.current.setChannelVisibility(mockStation.name, mockChannel.name, true);
      });
      expect(
        isChannelVisible(mockChannel.name, result.current.stationsVisibility[mockStation.name])
      ).toBe(true);
    });

    it('can show all channels', () => {
      const { result } = renderHook(() => useStationsVisibility(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      const channelList = [
        { name: 'a' } as ChannelTypes.Channel,
        { name: 'c' } as ChannelTypes.Channel,
        { name: 'd' } as ChannelTypes.Channel
      ];
      const mockStation2: StationTypes.Station = {
        name: 'mockStation2',
        allRawChannels: channelList
      } as any;
      expect(result.current.stationsVisibility).toBe(waveformInitialState.stationsVisibility);
      act(() => {
        channelList.forEach(chan => {
          result.current.setChannelVisibility(mockStation2, chan, false);
        });
      });
      channelList.forEach(chan => {
        expect(
          result.current.stationsVisibility[mockStation2.name].hiddenChannels?.includes(chan.name)
        ).toBe(true);
      });
      act(() => {
        // use string for better branch testing
        result.current.showAllChannels(mockStation2.name);
      });
      channelList.forEach(chan => {
        expect(
          isChannelVisible(chan.name, result.current.stationsVisibility[mockStation2.name])
        ).toBe(true);
      });
    });
  });
});
