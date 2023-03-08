import type { CommonTypes, SignalDetectionTypes } from '@gms/common-model';
import { act, renderHook } from '@testing-library/react-hooks';
import axios from 'axios';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';
import type Redux from 'redux';

import {
  getStationNamesFromAssociatedSignalDetections,
  useEventStatusQuery,
  useGetEvents,
  useQueryArgsForGetEventsWithDetectionsAndSegmentsByTime,
  useStationsAssociatedWithCurrentOpenEvent,
  useUpdateVisibleStationsForCloseEvent,
  useUpdateVisibleStationsForOpenEvent
} from '../../../src/ts/app/hooks/event-manager-hooks';
import { workflowActions } from '../../../src/ts/app/state/workflow/workflow-slice';
import type { AppState } from '../../../src/ts/app/store';
import { getStore } from '../../../src/ts/app/store';
import { eventResultsWithOverallPreferredHypothesis } from '../../__data__/event-data';
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
describe('Event Manager API Slice', () => {
  it('provides', () => {
    expect(useGetEvents).toBeDefined();
    expect(useEventStatusQuery).toBeDefined();
    expect(useQueryArgsForGetEventsWithDetectionsAndSegmentsByTime).toBeDefined();
    expect(useStationsAssociatedWithCurrentOpenEvent).toBeDefined();
    expect(useUpdateVisibleStationsForOpenEvent).toBeDefined();
    expect(useUpdateVisibleStationsForCloseEvent).toBeDefined();
  });

  it('has a hook for query arg generation', () => {
    const store = getStore();
    store.dispatch(workflowActions.setOpenIntervalName('AL1'));

    function Wrapper({ children }) {
      return <Provider store={store}>{children}</Provider>;
    }
    const timeInterval: CommonTypes.TimeRange = { startTimeSecs: 1, endTimeSecs: 6 };
    const { result } = renderHook(
      () => useQueryArgsForGetEventsWithDetectionsAndSegmentsByTime(timeInterval),
      {
        wrapper: Wrapper
      }
    );
    expect(result.current).toMatchInlineSnapshot(`
  Object {
    "endTime": 6,
    "stageId": Object {
      "name": "AL1",
    },
    "startTime": 1,
  }
  `);
  });

  it('hook queries for find event status', () => {
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
    const store = getStore();
    store.dispatch(workflowActions.setTimeRange({ startTimeSecs: 200, endTimeSecs: 400 }));
    store.dispatch(workflowActions.setOpenIntervalName('test'));

    function Component() {
      const result = useEventStatusQuery();
      return <>{JSON.stringify(result.data)}</>;
    }

    expect(
      // eslint-disable-next-line @typescript-eslint/await-thenable
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('event status hook does not query with no stage name', () => {
    const store = getStore();
    store.dispatch(workflowActions.setTimeRange({ startTimeSecs: 200, endTimeSecs: 400 }));
    store.dispatch(workflowActions.setOpenIntervalName(undefined));

    function Component() {
      const result = useEventStatusQuery();
      return <>{JSON.stringify(result.data)}</>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('useGetChannelSegmentsByChannels returns an object with loading values', () => {
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
    const store = getStore();
    store.dispatch(workflowActions.setTimeRange({ startTimeSecs: 200, endTimeSecs: 400 }));

    function Wrapper({ children }) {
      return <Provider store={store}>{children}</Provider>;
    }
    const { result } = renderHook(() => useGetEvents(), {
      wrapper: Wrapper
    });
    expect(result.current).toMatchSnapshot();
  });

  describe('useStationsAssociatedWithCurrentOpenEvent', () => {
    let store: Redux.Store<AppState>;
    beforeEach(() => {
      store = configureNewStore();
    });
    it('should be able to get and set stations associated with current open event', () => {
      const { result } = renderHook(() => useStationsAssociatedWithCurrentOpenEvent(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          <Provider store={store}>{props.children}</Provider>
        )
      });
      expect(result.current[0]).toStrictEqual([]);
      const newValue = ['STA1'];
      act(() => {
        result.current[1](newValue);
      });
      expect(result.current[0]).toBe(newValue);
    });
  });

  describe('getStationNamesFromAssociatedSignalDetections', () => {
    it('should get station names from associated signal detections', () => {
      const signalDetectionsFromStore: Record<string, SignalDetectionTypes.SignalDetection> = {
        record1: signalDetectionsData[0]
      };
      const actual = getStationNamesFromAssociatedSignalDetections(
        'eventID',
        eventResultsWithOverallPreferredHypothesis,
        signalDetectionsFromStore
      );
      expect(actual).toEqual(['ASAR']);
    });
  });
});
