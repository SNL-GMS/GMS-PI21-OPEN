import type { CommonTypes, EventTypes } from '@gms/common-model';
import { act, renderHook } from '@testing-library/react-hooks';
import axios from 'axios';
import React from 'react';
import { Provider } from 'react-redux';
import type Redux from 'redux';

import { dataSlice } from '../../../src/ts/app/api/data/data-slice';
import {
  useUpdateVisibleStationsForCloseEvent,
  useUpdateVisibleStationsForOpenEvent
} from '../../../src/ts/app/hooks/event-manager-hooks';
import { eventsActions } from '../../../src/ts/app/state/events/events-slice';
import type { AnalystWaveformTypes } from '../../../src/ts/app/state/waveform/index';
import { waveformActions } from '../../../src/ts/app/state/waveform/waveform-slice';
import { workflowActions } from '../../../src/ts/app/state/workflow/workflow-slice';
import type { AppState } from '../../../src/ts/app/store';
import { eventWithHypothesis } from '../../__data__/event-data';
import { signalDetectionsData } from '../../__data__/signal-detections-data';
import { configureNewStore } from '../../test-util';

jest.mock('axios');
axios.request = jest.fn(() => {
  return {
    method: 'post',
    url: '/resolve',
    responseType: 'json',
    proxy: false,
    headers: { accept: 'application/json', 'content-type': 'application/json' },
    timeout: 60000,
    data: { stages: [], configName: 'ui.analyst-settings', selectors: [] }
  };
}) as any;
jest.mock('../../../src/ts/app/api/workflow/workflow-api-slice', () => {
  const actual = jest.requireActual('../../../src/ts/app/api/workflow/workflow-api-slice');
  return {
    ...actual,
    useWorkflowQuery: jest.fn(() => ({
      isSuccess: true,
      data: { stages: [{ name: 'Auto Network' }, { name: 'AL1' }] }
    }))
  };
});

describe('Update station visibility for currently open event', () => {
  let store: Redux.Store<AppState>;
  beforeEach(() => {
    store = configureNewStore();
    (axios.request as jest.Mock).mockImplementation(
      jest.fn(() => {
        return {
          status: 200,
          statusText: 'OK',
          data: {},
          headers: [],
          config: {}
        };
      })
    );
  });
  it('is defined', () => {
    expect(useUpdateVisibleStationsForOpenEvent).toBeDefined();
    expect(useUpdateVisibleStationsForCloseEvent).toBeDefined();
  });
  it('calls useUpdateVisibleStationsForCloseEvent', () => {
    const stationVisibilityDictionary: AnalystWaveformTypes.StationVisibilityChangesDictionary = {};
    stationVisibilityDictionary.STA1 = {
      stationName: 'STA1',
      visibility: true,
      isStationExpanded: true,
      hiddenChannels: undefined
    };
    store.dispatch(eventsActions.setStationsAssociatedWithCurrentOpenEvent(['STA1']));
    store.dispatch(waveformActions.setStationsVisibility(stationVisibilityDictionary));
    expect(store.getState().app.events.stationsAssociatedWithCurrentOpenEvent).toStrictEqual([
      'STA1'
    ]);
    const { result } = renderHook(() => useUpdateVisibleStationsForCloseEvent(), {
      wrapper: (props: React.PropsWithChildren<unknown>) => (
        <Provider store={store}>{props.children}</Provider>
      )
    });
    act(() => {
      result.current();
    });
    expect(store.getState().app.events.stationsAssociatedWithCurrentOpenEvent).toStrictEqual([]);
    expect(store.getState().app.waveform.stationsVisibility.STA1.visibility).toBe(false);
    expect(result.current).toMatchSnapshot();
  });
  it('calls useUpdateVisibleStationsForOpenEvent to update visibility for an existing station', () => {
    const stationVisibilityDictionary: AnalystWaveformTypes.StationVisibilityChangesDictionary = {};
    stationVisibilityDictionary.ASAR = {
      stationName: 'ASAR',
      visibility: false,
      isStationExpanded: true,
      hiddenChannels: undefined
    };
    const eventArray: EventTypes.Event[] = [];
    const timeRange: CommonTypes.TimeRange = { startTimeSecs: 2000, endTimeSecs: 4000 };
    eventArray.push(eventWithHypothesis);
    store.dispatch(dataSlice.actions.addEvents(eventArray));
    store.dispatch(workflowActions.setOpenIntervalName('AL1'));
    store.dispatch(waveformActions.setViewableInterval(timeRange));
    store.dispatch(eventsActions.setStationsAssociatedWithCurrentOpenEvent([]));
    store.dispatch(waveformActions.setStationsVisibility(stationVisibilityDictionary));
    store.dispatch(dataSlice.actions.addSignalDetections(signalDetectionsData.slice(0, 1)));
    expect(store.getState().app.events.stationsAssociatedWithCurrentOpenEvent).toStrictEqual([]);
    const { result } = renderHook(() => useUpdateVisibleStationsForOpenEvent(), {
      wrapper: (props: React.PropsWithChildren<unknown>) => (
        <Provider store={store}>{props.children}</Provider>
      )
    });
    act(() => {
      const openEventId = 'eventID';
      result.current(openEventId);
    });
    expect(store.getState().app.events.stationsAssociatedWithCurrentOpenEvent).toStrictEqual([
      'ASAR'
    ]);
    expect(store.getState().app.waveform.stationsVisibility.ASAR.visibility).toBe(true);
    expect(result.current).toMatchSnapshot();
  });
  it('calls useUpdateVisibleStationsForOpenEvent to update visibility for a station added due to association with the current open event', () => {
    const stationVisibilityDictionary: AnalystWaveformTypes.StationVisibilityChangesDictionary = {};
    const eventArray: EventTypes.Event[] = [];
    const timeRange: CommonTypes.TimeRange = { startTimeSecs: 2000, endTimeSecs: 4000 };
    eventArray.push(eventWithHypothesis);
    store.dispatch(dataSlice.actions.addEvents(eventArray));
    store.dispatch(workflowActions.setOpenIntervalName('AL1'));
    store.dispatch(waveformActions.setViewableInterval(timeRange));
    store.dispatch(eventsActions.setStationsAssociatedWithCurrentOpenEvent([]));
    store.dispatch(waveformActions.setStationsVisibility(stationVisibilityDictionary));
    store.dispatch(dataSlice.actions.addSignalDetections(signalDetectionsData.slice(0, 1)));
    expect(store.getState().app.events.stationsAssociatedWithCurrentOpenEvent).toStrictEqual([]);
    const { result } = renderHook(() => useUpdateVisibleStationsForOpenEvent(), {
      wrapper: (props: React.PropsWithChildren<unknown>) => (
        <Provider store={store}>{props.children}</Provider>
      )
    });
    act(() => {
      const openEventId = 'eventID';
      result.current(openEventId);
    });
    expect(store.getState().app.events.stationsAssociatedWithCurrentOpenEvent).toStrictEqual([
      'ASAR'
    ]);
    expect(store.getState().app.waveform.stationsVisibility.ASAR.visibility).toBe(true);
    expect(result.current).toMatchSnapshot();
  });
  it('calls useUpdateVisibleStationsForOpenEvent to update station visibility for open event without first closing previously opened event', () => {
    const stationVisibilityDictionary: AnalystWaveformTypes.StationVisibilityChangesDictionary = {};
    stationVisibilityDictionary.STA1 = {
      stationName: 'STA1',
      visibility: true,
      isStationExpanded: true,
      hiddenChannels: undefined
    };
    stationVisibilityDictionary.STA2 = {
      stationName: 'STA2',
      visibility: true,
      isStationExpanded: true,
      hiddenChannels: undefined
    };
    const eventArray: EventTypes.Event[] = [];
    const timeRange: CommonTypes.TimeRange = { startTimeSecs: 2000, endTimeSecs: 4000 };
    eventArray.push(eventWithHypothesis);
    store.dispatch(dataSlice.actions.addEvents(eventArray));
    store.dispatch(workflowActions.setOpenIntervalName('AL1'));
    store.dispatch(waveformActions.setViewableInterval(timeRange));
    store.dispatch(eventsActions.setStationsAssociatedWithCurrentOpenEvent(['STA1', 'STA2']));
    store.dispatch(waveformActions.setStationsVisibility(stationVisibilityDictionary));
    store.dispatch(dataSlice.actions.addSignalDetections(signalDetectionsData.slice(0, 1)));
    const { result } = renderHook(() => useUpdateVisibleStationsForOpenEvent(), {
      wrapper: (props: React.PropsWithChildren<unknown>) => (
        <Provider store={store}>{props.children}</Provider>
      )
    });
    act(() => {
      const openEventId = 'eventID';
      result.current(openEventId);
    });
    expect(store.getState().app.events.stationsAssociatedWithCurrentOpenEvent).toStrictEqual([
      'ASAR'
    ]);
    expect(store.getState().app.waveform.stationsVisibility.STA1.visibility).toBe(false);
    expect(store.getState().app.waveform.stationsVisibility.STA2.visibility).toBe(false);
    expect(store.getState().app.waveform.stationsVisibility.ASAR.visibility).toBe(true);
    expect(result.current).toMatchSnapshot();
  });
});
