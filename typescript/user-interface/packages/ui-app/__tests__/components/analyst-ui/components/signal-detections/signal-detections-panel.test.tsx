/* eslint-disable react/jsx-no-constructed-context-values */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable react/function-component-definition */
import { H1 } from '@blueprintjs/core';
import type { CommonTypes } from '@gms/common-model';
import type { SignalDetectionFetchResult } from '@gms/ui-state';
import {
  getStore,
  SignalDetectionColumn,
  signalDetectionsActions,
  signalDetectionsInitialState
} from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__/signal-detections-data';
import { render } from '@testing-library/react';
import Immutable from 'immutable';
import * as React from 'react';
import { Provider } from 'react-redux';

import { SignalDetectionsPanel } from '../../../../../src/ts/components/analyst-ui/components/signal-detections/signal-detections-panel';
import { convertMapToObject } from '../../../../../src/ts/components/common-ui/common/table-utils';
import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';

const updatedState = {
  ...signalDetectionsInitialState.displayedSignalDetectionConfiguration,
  syncWaveform: false
};

jest.mock(
  '../../../../../src/ts/components/analyst-ui/components/signal-detections/signal-detections-component',
  () => {
    const MockSignalDetections = () => {
      return <H1 />;
    };
    return { SignalDetections: () => MockSignalDetections() };
  }
);

const SDQueryResult: SignalDetectionFetchResult = {
  data: signalDetectionsData,
  isLoading: false,
  pending: 0,
  isError: false,
  fulfilled: 0,
  rejected: 0
};

// Mock data setup
let loading = false;
const isLoading = () => {
  return loading;
};

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
const now = 1234567890 / 1000;
const timeRange: CommonTypes.TimeRange = {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  startTimeSecs: now - 3600,
  endTimeSecs: now
};

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetSignalDetections: jest.fn(() => ({
      data: signalDetectionsData,
      isLoading: isLoading()
    })),
    useViewableInterval: jest.fn(() => [timeRange, jest.fn]),
    useZoomInterval: jest.fn(() => [timeRange, jest.fn])
  };
});

describe('ui ian signal detections', () => {
  test('is defined', () => {
    expect(SignalDetectionsPanel).toBeDefined();
  });

  test('can mount signal detections', () => {
    const { container } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{ glContainer: {} as any, widthPx: 200, heightPx: 200 }}
        >
          <SignalDetectionsPanel signalDetectionsQuery={SDQueryResult} />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  test('signal detections mounts with "loading" non-ideal state', () => {
    const store = getStore();
    store.dispatch(
      signalDetectionsActions.updateDisplayedSignalDetectionConfiguration(updatedState)
    );

    loading = true;

    const { container } = render(
      <Provider store={store}>
        <BaseDisplayContext.Provider
          value={{ glContainer: {} as any, widthPx: 200, heightPx: 200 }}
        >
          <SignalDetectionsPanel signalDetectionsQuery={SDQueryResult} />
        </BaseDisplayContext.Provider>
      </Provider>
    );

    expect(container).toBeDefined();
  });
  test('signal detections mounts with data', () => {
    const store = getStore();
    store.dispatch(
      signalDetectionsActions.updateDisplayedSignalDetectionConfiguration(updatedState)
    );

    loading = false;

    const { container } = render(
      <Provider store={store}>
        <BaseDisplayContext.Provider
          value={{ glContainer: {} as any, widthPx: 200, heightPx: 200 }}
        >
          <SignalDetectionsPanel signalDetectionsQuery={SDQueryResult} />
        </BaseDisplayContext.Provider>
      </Provider>
    );

    expect(container).toMatchSnapshot();
  });
  test('map to object conversion', () => {
    const signalDetectionsColumnsToDisplay = Immutable.Map<SignalDetectionColumn, boolean>([
      [SignalDetectionColumn.unsavedChanges, true],
      [SignalDetectionColumn.assocStatus, true],
      [SignalDetectionColumn.conflict, true],
      [SignalDetectionColumn.station, true],
      [SignalDetectionColumn.channel, true],
      [SignalDetectionColumn.phase, true],
      [SignalDetectionColumn.phaseConfidence, false],
      [SignalDetectionColumn.time, true],
      [SignalDetectionColumn.timeStandardDeviation, true],
      [SignalDetectionColumn.azimuth, true],
      [SignalDetectionColumn.azimuthStandardDeviation, true],
      [SignalDetectionColumn.slowness, true],
      [SignalDetectionColumn.slownessStandardDeviation, true],
      [SignalDetectionColumn.amplitude, true],
      [SignalDetectionColumn.period, true],
      [SignalDetectionColumn.sNR, true],
      [SignalDetectionColumn.rectilinearity, false],
      [SignalDetectionColumn.emergenceAngle, false],
      [SignalDetectionColumn.shortPeriodFirstMotion, false],
      [SignalDetectionColumn.longPeriodFirstMotion, false],
      [SignalDetectionColumn.rejected, true]
    ]);
    const expected = {
      unsavedChanges: true,
      assocStatus: true,
      conflict: true,
      station: true,
      channel: true,
      phase: true,
      phaseConfidence: false,
      time: true,
      timeStandardDeviation: true,
      azimuth: true,
      azimuthStandardDeviation: true,
      slowness: true,
      slownessStandardDeviation: true,
      amplitude: true,
      period: true,
      sNR: true,
      rectilinearity: false,
      emergenceAngle: false,
      shortPeriodFirstMotion: false,
      longPeriodFirstMotion: false,
      rejected: true
    };
    const actual = convertMapToObject(signalDetectionsColumnsToDisplay);
    expect(expected).toEqual(actual);
  });
});
