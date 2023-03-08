/* eslint-disable react/jsx-no-useless-fragment */
/* eslint-disable react/function-component-definition */
import type { CommonTypes } from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import { uuid } from '@gms/common-util';
import { enableMapSet } from 'immer';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import { dataSlice, useAppSelector, useInterval } from '../../../src/ts/app';
import { useGetSignalDetections } from '../../../src/ts/app/hooks/signal-detection-hooks';
import { waveformActions, workflowActions } from '../../../src/ts/app/state';
import {
  signalDetectionsActions,
  signalDetectionsInitialState
} from '../../../src/ts/app/state/signal-detections/signal-detections-slice';
import { Operations } from '../../../src/ts/app/state/waveform/operations';
import { getStore } from '../../../src/ts/app/store';
import type { SignalDetectionWithSegmentsFetchResults } from '../../../src/ts/workers/waveform-worker/operations/fetch-signal-detections-segments-by-stations-time';
import { signalDetectionsData, uiChannelSegment } from '../../__data__';
import { expectHookToCallWorker } from '../../test-util';

const signalDetectionWithSegmentsFetchResults: SignalDetectionWithSegmentsFetchResults = {
  signalDetections: signalDetectionsData,
  uiChannelSegments: [uiChannelSegment]
};

signalDetectionWithSegmentsFetchResults.signalDetections.forEach(sd => {
  // Set first SD arrival to pre transformed since signalDetection fetch results is post transform
  let fixedArrivalTimeFM = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  fixedArrivalTimeFM.measurementValue = {
    arrivalTime: {
      value: '2019-01-05T19:04:14.200Z',
      standardDeviation: '1.162'
    },
    travelTime: null
  };

  // Set second SD arrival to null feature measurement value
  fixedArrivalTimeFM = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(
    SignalDetectionTypes.Util.getCurrentHypothesis(
      signalDetectionWithSegmentsFetchResults.signalDetections[1].signalDetectionHypotheses
    ).featureMeasurements
  );
  fixedArrivalTimeFM.measurementValue = null;

  // Set third SD arrival time value to null
  fixedArrivalTimeFM = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(
    SignalDetectionTypes.Util.getCurrentHypothesis(
      signalDetectionWithSegmentsFetchResults.signalDetections[2].signalDetectionHypotheses
    ).featureMeasurements
  );
  fixedArrivalTimeFM.measurementValue = {
    arrivalTime: {
      value: null,
      standardDeviation: '1.162'
    },
    travelTime: null
  };
});

enableMapSet();
const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

// mock the uuid
uuid.asString = jest.fn().mockImplementation(() => '12345789');

jest.mock('worker-rpc', () => ({
  RpcProvider: jest.fn().mockImplementation(() => {
    // eslint-disable-next-line no-var
    var mockRpc = jest.fn(async () => {
      return new Promise(resolve => {
        resolve(signalDetectionWithSegmentsFetchResults);
      });
    });
    return { rpc: mockRpc };
  })
}));

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
const now = 1234567890 / 1000;
const timeRange: CommonTypes.TimeRange = {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  startTimeSecs: now - 3600,
  endTimeSecs: now
};

const anotherTimeRange: CommonTypes.TimeRange = {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  startTimeSecs: now * 1000 - 3600,
  endTimeSecs: now * 1000
};

const store = getStore();

describe('signal detection hooks', () => {
  describe('useGetSignalDetections', () => {
    it('exists', () => {
      expect(useGetSignalDetections).toBeDefined();
    });

    it('calls useGetSignalDetections', async () => {
      const useTestHook = () => useGetSignalDetections({ startTimeSecs: 0, endTimeSecs: 1000 });
      const result = await expectHookToCallWorker(useTestHook, store);
      expect(result).toMatchSnapshot();
    });

    it('returns the signal detections in the viewable interval when not synced', () => {
      store.dispatch(
        signalDetectionsActions.updateDisplayedSignalDetectionConfiguration({
          ...signalDetectionsInitialState.displayedSignalDetectionConfiguration,
          syncWaveform: false
        })
      );
      store.dispatch(waveformActions.setViewableInterval(timeRange));
      store.dispatch(dataSlice.actions.addSignalDetections(signalDetectionsData));

      const Component: React.FC = () => {
        const isSynced = useAppSelector(
          state => state.app.signalDetections.displayedSignalDetectionConfiguration.syncWaveform
        );
        const [interval] = useInterval(isSynced);
        const result = useGetSignalDetections(interval);
        return <>{result.data}</>;
      };

      expect(
        create(
          <Provider store={store}>
            <Component />
          </Provider>
        ).toJSON()
      ).toMatchSnapshot();
    });

    it('returns the signal detections in the zoom interval when synced', () => {
      store.dispatch(
        signalDetectionsActions.updateDisplayedSignalDetectionConfiguration({
          ...signalDetectionsInitialState.displayedSignalDetectionConfiguration,
          syncWaveform: true
        })
      );
      store.dispatch(waveformActions.setViewableInterval(anotherTimeRange));
      store.dispatch(Operations.setZoomInterval(timeRange));
      store.dispatch(dataSlice.actions.addSignalDetections(signalDetectionsData));

      const Component: React.FC = () => {
        const isSynced = useAppSelector(
          state => state.app.signalDetections.displayedSignalDetectionConfiguration.syncWaveform
        );
        const [interval] = useInterval(isSynced);
        const result = useGetSignalDetections(interval);
        return <>{result.data}</>;
      };

      expect(
        create(
          <Provider store={store}>
            <Component />
          </Provider>
        ).toJSON()
      ).toMatchSnapshot();
    });

    it('hook query for signal detections for current stations with initial state', () => {
      const Component: React.FC = () => {
        const [interval] = useInterval(false);
        const result = useGetSignalDetections(interval);
        return <>{result.data}</>;
      };

      expect(
        create(
          <Provider store={store}>
            <Component />
          </Provider>
        ).toJSON()
      ).toMatchSnapshot();
    });

    it('hook query for signal detections for current stations', () => {
      store.dispatch(workflowActions.setTimeRange(timeRange));
      store.dispatch(dataSlice.actions.addSignalDetections(signalDetectionsData));

      const Component: React.FC = () => {
        const [interval] = useInterval(false);
        const result = useGetSignalDetections(interval);
        return <>{result.data}</>;
      };

      expect(
        create(
          <Provider store={store}>
            <Component />
          </Provider>
        ).toJSON()
      ).toMatchSnapshot();
    });

    it('hook query for signal detections', () => {
      store.dispatch(workflowActions.setTimeRange(timeRange));
      const Component: React.FC = () => {
        const [interval] = useInterval(false);
        const result = useGetSignalDetections(interval);
        return <>{result.data}</>;
      };

      expect(
        create(
          <Provider store={store}>
            <Component />
          </Provider>
        ).toJSON()
      ).toMatchSnapshot();

      expect(
        create(
          <Provider store={store}>
            <Component />
          </Provider>
        ).toJSON()
      ).toMatchSnapshot();
    });
  });
});
