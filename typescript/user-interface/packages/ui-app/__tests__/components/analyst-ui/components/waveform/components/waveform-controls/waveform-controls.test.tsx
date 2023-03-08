/* eslint-disable react/jsx-no-constructed-context-values */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable react/jsx-props-no-spreading */
import type { EventTypes } from '@gms/common-model';
import { CommonTypes } from '@gms/common-model';
import { AnalystWorkspaceTypes, getStore } from '@gms/ui-state';
import { processingAnalystConfiguration } from '@gms/ui-state/__tests__/__data__/processing-analyst-configuration';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import * as React from 'react';
import { Provider } from 'react-redux';

import { WaveformControls } from '../../../../../../../src/ts/components/analyst-ui/components/waveform/components/waveform-controls';
import { AmplitudeScalingOptions } from '../../../../../../../src/ts/components/analyst-ui/components/waveform/components/waveform-controls/scaling-options';
import type { WaveformControlsProps } from '../../../../../../../src/ts/components/analyst-ui/components/waveform/components/waveform-controls/types';
import { WaveformLoadingIndicator } from '../../../../../../../src/ts/components/analyst-ui/components/waveform/components/waveform-loading-indicator';
import { getAlignablePhases } from '../../../../../../../src/ts/components/analyst-ui/components/waveform/weavess-stations-util';
import { BaseDisplayContext } from '../../../../../../../src/ts/components/common-ui/components/base-display';
import { useQueryStateResult } from '../../../../../../__data__/test-util-data';

const MOCK_TIME = 123456789;
const MOCK_TIME_STR = '2021-01-20 02:34:31';

const mockDate: any = new Date(MOCK_TIME);
mockDate.now = () => MOCK_TIME;
Date.constructor = jest.fn(() => new Date(MOCK_TIME));
jest.spyOn(global, 'Date').mockImplementation(() => mockDate);
Date.now = jest.fn(() => MOCK_TIME);
Date.UTC = jest.fn(() => MOCK_TIME);

const featurePredictions: EventTypes.FeaturePrediction[] = [];
jest.mock('moment-precise-range-plugin', () => {
  return {};
});

jest.mock('moment', () => {
  // mock chain builder pattern
  const mMoment = {
    utc: jest.fn(() => mMoment),
    format: jest.fn(() => MOCK_TIME_STR),
    preciseDiff: jest.fn(() => '')
  };

  // mock the constructor and to modify instance methods
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const fn: any = jest.fn(() => {
    mMoment.format = jest.fn(() => MOCK_TIME_STR);
    mMoment.preciseDiff = jest.fn(() => '');
    return mMoment;
  });

  // mock moment methods that depend on moment not on a moment instance
  fn.unix = () => ({ utc: () => mMoment });
  return fn;
});

const processingAnalystConfigurationQuery = cloneDeep(useQueryStateResult);
processingAnalystConfigurationQuery.data = processingAnalystConfiguration;

const operationalTimeRange: CommonTypes.TimeRange = {
  startTimeSecs: 0,
  endTimeSecs: 48 * 3600 // 48 hours
};
const operationalTimePeriodConfigurationQuery = cloneDeep(useQueryStateResult);
operationalTimePeriodConfigurationQuery.data = operationalTimeRange;

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetProcessingAnalystConfigurationQuery: jest.fn(() => ({
      ...processingAnalystConfigurationQuery,
      data: {
        mOpenAnythingDuration: 7200
      }
    })),
    useGetOperationalTimePeriodConfigurationQuery: jest.fn(
      () => operationalTimePeriodConfigurationQuery
    )
  };
});

describe('WaveformControls', () => {
  const currentOpenEventId = 'TEST_EVENT_ID';
  const FIFTEEN_MINUTES = 15 * 60;
  const currentTimeInterval = {
    startTimeSecs: MOCK_TIME,
    endTimeSecs: MOCK_TIME + 10000
  };
  const viewableTimeInterval = {
    startTimeSecs: MOCK_TIME - FIFTEEN_MINUTES,
    endTimeSecs: MOCK_TIME + 10000 + FIFTEEN_MINUTES
  };
  it('matches a snapshot when given basic props', () => {
    const props: WaveformControlsProps = {
      defaultSignalDetectionPhase: CommonTypes.PhaseType.P,
      currentSortType: AnalystWorkspaceTypes.WaveformSortType.stationNameAZ,
      currentOpenEventId,
      currentTimeInterval,
      viewableTimeInterval,
      analystNumberOfWaveforms: 20,
      showPredictedPhases: false,
      maskDisplayFilters: {
        ANALYST_DEFINED: { color: 'tomato', visible: false, name: 'ANALYST_DEFINED' },
        CHANNEL_PROCESSING: { color: 'tomato', visible: false, name: 'CHANNEL_PROCESSING' },
        DATA_AUTHENTICATION: { color: 'tomato', visible: false, name: 'DATA_AUTHENTICATION' },
        REJECTED: { color: 'tomato', visible: false, name: 'REJECTED' },
        STATION_SOH: { color: 'tomato', visible: false, name: 'STATION_SOH' },
        WAVEFORM_QUALITY: { color: 'tomato', visible: false, name: 'WAVEFORM_QUALITY' }
      },
      alignWaveformsOn: AlignWaveformsOn.TIME,
      defaultPhaseAlignment: CommonTypes.PhaseType.P,
      phaseToAlignOn: CommonTypes.PhaseType.P,
      alignablePhases: getAlignablePhases(featurePredictions),
      measurementMode: {
        mode: AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT,
        entries: undefined
      },
      setDefaultSignalDetectionPhase: jest.fn(),
      setWaveformAlignment: jest.fn(),
      setSelectedSortType: jest.fn(),
      setAnalystNumberOfWaveforms: jest.fn(),
      setMaskDisplayFilters: jest.fn(),
      setMode: jest.fn(),
      toggleMeasureWindow: jest.fn(),
      pan: jest.fn(),
      onKeyPress: jest.fn(),
      isMeasureWindowVisible: false,
      amplitudeScaleOption: AmplitudeScalingOptions.AUTO,
      fixedScaleVal: 1,
      setAmplitudeScaleOption: jest.fn(),
      setFixedScaleVal: jest.fn(),
      zoomAlignSort: jest.fn(),
      featurePredictionQueryDataUnavailable: false
    };
    const { container } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: undefined,
            widthPx: 1920,
            heightPx: 1080
          }}
        >
          <WaveformControls {...props} />
          <WaveformLoadingIndicator />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('matches a snapshot when given missing props', () => {
    const props: WaveformControlsProps = {
      defaultSignalDetectionPhase: CommonTypes.PhaseType.P,
      currentSortType: AnalystWorkspaceTypes.WaveformSortType.stationNameAZ,
      currentOpenEventId,
      currentTimeInterval,
      viewableTimeInterval,
      analystNumberOfWaveforms: 20,
      showPredictedPhases: false,
      maskDisplayFilters: {
        ANALYST_DEFINED: { color: 'tomato', visible: false, name: 'ANALYST_DEFINED' },
        CHANNEL_PROCESSING: { color: 'tomato', visible: false, name: 'CHANNEL_PROCESSING' },
        DATA_AUTHENTICATION: { color: 'tomato', visible: false, name: 'DATA_AUTHENTICATION' },
        REJECTED: { color: 'tomato', visible: false, name: 'REJECTED' },
        STATION_SOH: { color: 'tomato', visible: false, name: 'STATION_SOH' },
        WAVEFORM_QUALITY: { color: 'tomato', visible: false, name: 'WAVEFORM_QUALITY' }
      },
      alignWaveformsOn: AlignWaveformsOn.PREDICTED_PHASE,
      phaseToAlignOn: CommonTypes.PhaseType.P,
      defaultPhaseAlignment: CommonTypes.PhaseType.P,
      alignablePhases: undefined,
      measurementMode: {
        mode: AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT,
        entries: undefined
      },
      setDefaultSignalDetectionPhase: jest.fn(),
      setWaveformAlignment: jest.fn(),
      setSelectedSortType: jest.fn(),
      setAnalystNumberOfWaveforms: jest.fn(),
      setMaskDisplayFilters: jest.fn(),
      setMode: jest.fn(),
      toggleMeasureWindow: jest.fn(),
      pan: jest.fn(),
      onKeyPress: jest.fn(),
      isMeasureWindowVisible: false,
      amplitudeScaleOption: AmplitudeScalingOptions.AUTO,
      fixedScaleVal: 1,
      setAmplitudeScaleOption: jest.fn(),
      setFixedScaleVal: jest.fn(),
      zoomAlignSort: jest.fn(),
      featurePredictionQueryDataUnavailable: false
    };
    const { container } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: undefined,
            widthPx: 1920,
            heightPx: 1080
          }}
        >
          <WaveformControls {...props} />
          <WaveformLoadingIndicator />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
