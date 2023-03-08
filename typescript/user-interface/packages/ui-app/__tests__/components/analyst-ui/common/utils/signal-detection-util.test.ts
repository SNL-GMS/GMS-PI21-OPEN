/* eslint-disable @typescript-eslint/no-magic-numbers */
import { CommonTypes, SignalDetectionTypes, WaveformTypes } from '@gms/common-model';
import type {
  FeatureMeasurement,
  SignalDetectionHypothesis
} from '@gms/common-model/lib/signal-detection';
import { FeatureMeasurementType } from '@gms/common-model/lib/signal-detection';
import { AnalystWorkspaceTypes, defaultTheme } from '@gms/ui-state';
import { signalDetectionsData, uiChannelSegmentData } from '@gms/ui-state/__tests__/__data__';
import cloneDeep from 'lodash/cloneDeep';

import {
  AMPLITUDE_VALUES,
  FREQUENCY_VALUES
} from '../../../../../src/ts/components/analyst-ui/common/utils/amplitude-scale-constants';
import {
  calculateAmplitudeMeasurementValue,
  findAmplitudeFeatureMeasurement,
  findAmplitudeFeatureMeasurementValue,
  findAzimuthFeatureMeasurement,
  findAzimuthFeatureMeasurementValue,
  findEmergenceAngleFeatureMeasurementValue,
  findFeatureMeasurementChannelName,
  findFilteredBeamFeatureMeasurement,
  findLongPeriodFirstMotionFeatureMeasurementValue,
  findMinMaxAmplitudeForPeakTrough,
  findPhaseFeatureMeasurement,
  findPhaseFeatureMeasurementValue,
  findRectilinearityFeatureMeasurementValue,
  findShortPeriodFirstMotionFeatureMeasurementValue,
  findSlownessFeatureMeasurement,
  findSlownessFeatureMeasurementValue,
  getAssocStatusColor,
  getAssocStatusString,
  getChannelSegmentStringForCurrentHypothesis,
  getSignalDetectionChannelName,
  getWaveformValueForTime,
  isPeakTroughInWarning,
  parseBeamType,
  parseStationNameFromChannelSegment,
  scaleAmplitudeForPeakTrough,
  scaleAmplitudeMeasurementValue,
  sortAndOrderSignalDetections
} from '../../../../../src/ts/components/analyst-ui/common/utils/signal-detection-util';
import { systemConfig } from '../../../../../src/ts/components/analyst-ui/config';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

/**
 * Tests the ability to check if the peak trough is in warning
 */
describe('isPeakTroughInWarning', () => {
  const { min } = systemConfig.measurementMode.peakTroughSelection.warning;
  const { max } = systemConfig.measurementMode.peakTroughSelection.warning;
  const mid = (max - min) / 2 + min;
  const { startTimeOffsetFromSignalDetection } = systemConfig.measurementMode.selection;
  const { endTimeOffsetFromSignalDetection } = systemConfig.measurementMode.selection;

  it('check [min] period value', () => {
    expect(
      isPeakTroughInWarning(
        0,
        min,
        startTimeOffsetFromSignalDetection,
        endTimeOffsetFromSignalDetection
      )
    ).toEqual(false);
  });

  it('check [max] period value', () => {
    expect(
      isPeakTroughInWarning(
        0,
        max,
        startTimeOffsetFromSignalDetection,
        endTimeOffsetFromSignalDetection
      )
    ).toEqual(false);
  });

  it('check [min, max] period value', () => {
    expect(
      isPeakTroughInWarning(
        0,
        mid,
        startTimeOffsetFromSignalDetection,
        endTimeOffsetFromSignalDetection
      )
    ).toEqual(false);
  });

  it('check bad [min] period value', () => {
    expect(
      isPeakTroughInWarning(
        0,
        min - 0.1,
        startTimeOffsetFromSignalDetection,
        endTimeOffsetFromSignalDetection
      )
    ).toEqual(true);
  });

  it('check bad [max] period value', () => {
    expect(
      isPeakTroughInWarning(
        0,
        max + 0.1,
        startTimeOffsetFromSignalDetection,
        endTimeOffsetFromSignalDetection
      )
    ).toEqual(true);
  });

  it('check trough greater than peak', () => {
    expect(
      isPeakTroughInWarning(
        0,
        mid,
        endTimeOffsetFromSignalDetection,
        startTimeOffsetFromSignalDetection
      )
    ).toEqual(true);
  });

  it('check trough out of range', () => {
    expect(
      isPeakTroughInWarning(
        0,
        mid,
        startTimeOffsetFromSignalDetection - 0.1,
        endTimeOffsetFromSignalDetection
      )
    ).toEqual(true);
  });

  it('check peak out of range', () => {
    expect(
      isPeakTroughInWarning(
        0,
        mid,
        startTimeOffsetFromSignalDetection,
        endTimeOffsetFromSignalDetection + 0.1
      )
    ).toEqual(true);
  });

  it('check peak trough out of range', () => {
    expect(
      isPeakTroughInWarning(
        0,
        mid,
        startTimeOffsetFromSignalDetection - 0.1,
        endTimeOffsetFromSignalDetection + 0.1
      )
    ).toEqual(true);
  });
});

/**
 * Tests the ability to find the [min,max] for the peak trough
 */
describe('findMinMaxForPeakTrough', () => {
  it('find [min,max] with a bad starting index', () => {
    const samples = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

    let result = findMinMaxAmplitudeForPeakTrough(-1, samples);
    expect(result.min.value).toEqual(0);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(0);
    expect(result.max.index).toEqual(0);

    result = findMinMaxAmplitudeForPeakTrough(samples.length, samples);
    expect(result.min.value).toEqual(0);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(0);
    expect(result.max.index).toEqual(0);
  });

  it('find [min,max] with a bad data samples', () => {
    let result = findMinMaxAmplitudeForPeakTrough(0, undefined);
    expect(result.min.value).toEqual(0);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(0);
    expect(result.max.index).toEqual(0);

    result = findMinMaxAmplitudeForPeakTrough(0, []);
    expect(result.min.value).toEqual(0);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(0);
    expect(result.max.index).toEqual(0);
  });

  it('find [min,max] for a flat line', () => {
    const samples = [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2];
    const result = findMinMaxAmplitudeForPeakTrough(4, samples);
    expect(result.min.value).toEqual(2);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(2);
    expect(result.max.index).toEqual(samples.length - 1);
  });

  it('find [min,max] for a partial flat line', () => {
    const samples = [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5];
    const result = findMinMaxAmplitudeForPeakTrough(4, samples);
    expect(result.min.value).toEqual(2);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(5);
    expect(result.max.index).toEqual(samples.length - 1);
  });

  it('find [min,max] for another partial flat line', () => {
    const samples = [7, 2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5];
    const result = findMinMaxAmplitudeForPeakTrough(10, samples);
    expect(result.min.value).toEqual(2);
    expect(result.min.index).toEqual(1);
    expect(result.max.value).toEqual(5);
    expect(result.max.index).toEqual(samples.length - 1);
  });

  it('find [min,max] with good data', () => {
    const samples = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 9, 8, 7, 6, 5, 4];

    let result = findMinMaxAmplitudeForPeakTrough(0, samples);
    expect(result.min.value).toEqual(1);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(10);
    expect(result.max.index).toEqual(9);

    result = findMinMaxAmplitudeForPeakTrough(9, samples);
    expect(result.min.value).toEqual(1);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(10);
    expect(result.max.index).toEqual(9);

    result = findMinMaxAmplitudeForPeakTrough(13, samples);
    expect(result.min.value).toEqual(4);
    expect(result.min.index).toEqual(15);
    expect(result.max.value).toEqual(10);
    expect(result.max.index).toEqual(9);

    result = findMinMaxAmplitudeForPeakTrough(15, samples);
    expect(result.min.value).toEqual(4);
    expect(result.min.index).toEqual(15);
    expect(result.max.value).toEqual(10);
    expect(result.max.index).toEqual(9);
  });
});

/**
 * Tests the ability scale an amplitude measurement value
 */
describe('scaleAmplitudeMeasurementValue', () => {
  it('expect scale of amplitude measurement too throw exception with bad data', () => {
    expect(() => {
      scaleAmplitudeMeasurementValue(undefined);
    }).toThrow();
  });

  it('scale amplitude measurement value', () => {
    const amplitude = 5;
    const periodValues = FREQUENCY_VALUES.map(freq => 1 / freq);

    const amplitudeMeasurementValue: SignalDetectionTypes.AmplitudeMeasurementValue = {
      amplitude: {
        value: amplitude,
        standardDeviation: 0,
        units: CommonTypes.Units.UNITLESS
      },
      period: periodValues[8],
      startTime: 500
    };

    const scaledAmplitudeMeasurementValue = scaleAmplitudeMeasurementValue(
      amplitudeMeasurementValue
    );
    const normalizedAmplitude = AMPLITUDE_VALUES[8] / AMPLITUDE_VALUES[348];
    expect(scaledAmplitudeMeasurementValue.amplitude.value).toEqual(
      amplitude / normalizedAmplitude
    );
    expect(scaledAmplitudeMeasurementValue.amplitude.standardDeviation).toEqual(0);
    expect(scaledAmplitudeMeasurementValue.period).toEqual(periodValues[8]);
    expect(scaledAmplitudeMeasurementValue.startTime).toEqual(500);
  });
});

/**
 * Tests the ability calculate the scaled amplitude
 */
describe('scaleAmplitudeForPeakTrough', () => {
  it('expect calculation to throw exception with bad data', () => {
    expect(() => {
      scaleAmplitudeForPeakTrough(0, 0, 1, [], []);
    }).toThrow();

    expect(() => {
      scaleAmplitudeForPeakTrough(0, 0, 1, [1, 2, 3], [1, 2]);
    }).toThrow();
  });

  it('scale amplitude value appropriately when nominal calibration period is equal to 1', () => {
    const amplitude = 5;
    const periodValues = FREQUENCY_VALUES.map(freq => 1 / freq);
    const normalizedAmplitude = AMPLITUDE_VALUES[5] / AMPLITUDE_VALUES[348];
    expect(scaleAmplitudeForPeakTrough(amplitude, periodValues[5])).toEqual(
      amplitude / normalizedAmplitude
    );
  });

  it('scale amplitude value appropriately with nominal calibration period is equal to 2', () => {
    const amplitude = 5;
    const periodValues = FREQUENCY_VALUES.map(freq => 1 / freq);
    const nominalCalibrationPeriod = 2;
    const expectedFoundPeriod: { index: number; value: number } = {
      index: 5,
      value: 909.090909090909
    };
    const expectedNormalizedAmplitude = AMPLITUDE_VALUES[expectedFoundPeriod.index];
    const expectedFoundPeriodForCalibration: {
      index: number;
      value: number;
    } = { value: 2.004008016032064, index: 313 };
    const normalizedAmplitude =
      expectedNormalizedAmplitude / AMPLITUDE_VALUES[expectedFoundPeriodForCalibration.index];

    expect(
      scaleAmplitudeForPeakTrough(amplitude, periodValues[5], nominalCalibrationPeriod)
    ).toEqual(amplitude / normalizedAmplitude);
  });
});

/**
 * Tests the ability calculate an Amplitude measurement
 */
describe('calculateAmplitudeMeasurementValue', () => {
  const peakAmplitude = 4;
  const troughAmplitude = 2;
  const peakTime = 4;
  const troughTime = 2;
  const expectedResult: SignalDetectionTypes.AmplitudeMeasurementValue = {
    amplitude: {
      value: 1,
      standardDeviation: 0,
      units: CommonTypes.Units.UNITLESS
    },
    period: 4,
    startTime: Math.min(troughTime, peakTime)
  };
  it('expect calculation to set samples to return correct result', () => {
    const result: SignalDetectionTypes.AmplitudeMeasurementValue = calculateAmplitudeMeasurementValue(
      peakAmplitude,
      troughAmplitude,
      peakTime,
      troughTime
    );

    expect(result).toEqual(expectedResult);
  });

  it('should recalculate with the expected results', () => {
    const expectedResults: SignalDetectionTypes.AmplitudeMeasurementValue = {
      amplitude: {
        standardDeviation: 0,
        value: 1,
        units: CommonTypes.Units.UNITLESS
      },
      period: 2,
      startTime: 1553022096
    };
    const input = {
      startTime: 1553022096,
      peakAmplitude: 2,
      troughAmplitude: 0,
      peakTime: 1553022096,
      troughTime: 1553022097
    };
    const result = calculateAmplitudeMeasurementValue(
      input.peakAmplitude,
      input.troughAmplitude,
      input.peakTime,
      input.troughTime
    );
    expect(result).toBeDefined();

    expect(result).toEqual(expectedResults);
  });
});

/**
 * Tests the ability to get the waveform value for the given waveform data
 */
describe('getWaveformValueForTime', () => {
  const timeSecs = 0;

  it('expect calculation to return undefined when no waveforms are given', () => {
    // eslint-disable-next-line
    expect(getWaveformValueForTime(undefined, timeSecs)).toBeUndefined;
  });

  describe('find FeatureMeasurement', () => {
    it('undefined feature measurement type', () => {
      const result = SignalDetectionTypes.Util.findFeatureMeasurementByType([], null);
      expect(result).toBeUndefined();
    });

    it('undefined feature measurement list', () => {
      const result = SignalDetectionTypes.Util.findFeatureMeasurementByType(null, null);
      expect(result).toBeUndefined();
    });

    it('find each feature measurement type', () => {
      // eslint-disable-next-line guard-for-in
      Object.values(FeatureMeasurementType).forEach((value: FeatureMeasurementType) => {
        const fm: FeatureMeasurement = {
          channel: null,
          featureMeasurementType: value,
          measuredChannelSegment: null,
          snr: null,
          measurementValue: null
        };
        const result = SignalDetectionTypes.Util.findFeatureMeasurementByType([fm], value);
        expect(result.featureMeasurementType).toEqual(fm.featureMeasurementType);
      });
    });
  });

  describe('find FeatureMeasurement and Values', () => {
    const sdHypo = SignalDetectionTypes.Util.getCurrentHypothesis(
      signalDetectionsData[0].signalDetectionHypotheses
    );

    it('find ArrivalTime feature measurement and value', () => {
      expect(
        SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(sdHypo.featureMeasurements)
      ).toMatchSnapshot();
      expect(
        SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(sdHypo.featureMeasurements)
      ).toMatchSnapshot();
    });

    it('find Azimuth feature measurement and value', () => {
      expect(findAzimuthFeatureMeasurement(sdHypo.featureMeasurements)).toMatchSnapshot();
      expect(findAzimuthFeatureMeasurementValue(sdHypo.featureMeasurements)).toMatchSnapshot();
    });

    it('find Slowness feature measurement and value', () => {
      expect(findSlownessFeatureMeasurement(sdHypo.featureMeasurements)).toMatchSnapshot();
      expect(findSlownessFeatureMeasurementValue(sdHypo.featureMeasurements)).toMatchSnapshot();
    });

    it('find Amplitude feature measurement and value', () => {
      expect(
        findAmplitudeFeatureMeasurement(
          sdHypo.featureMeasurements,
          SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE_A5_OVER_2
        )
      ).toMatchSnapshot();

      expect(
        findAmplitudeFeatureMeasurementValue(
          sdHypo.featureMeasurements,
          SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE_A5_OVER_2
        )
      ).toMatchSnapshot();
    });

    it('find Phase feature measurement and value', () => {
      expect(findPhaseFeatureMeasurement(sdHypo.featureMeasurements)).toMatchSnapshot();
      expect(findPhaseFeatureMeasurementValue(sdHypo.featureMeasurements)).toMatchSnapshot();
    });

    it('find Rectilinearity feature measurementvalue', () => {
      expect(
        findRectilinearityFeatureMeasurementValue(sdHypo.featureMeasurements)
      ).toMatchSnapshot();
      expect(findRectilinearityFeatureMeasurementValue([])).toBeUndefined();
    });

    it('find Emergence Angle feature measurementvalue', () => {
      expect(
        findEmergenceAngleFeatureMeasurementValue(sdHypo.featureMeasurements)
      ).toMatchSnapshot();
      expect(findEmergenceAngleFeatureMeasurementValue([])).toBeUndefined();
    });

    it('find LongPeriodFirstMotionFeatureMeasurementValue', () => {
      expect(
        findLongPeriodFirstMotionFeatureMeasurementValue(sdHypo.featureMeasurements)
      ).toMatchSnapshot();
      expect(findLongPeriodFirstMotionFeatureMeasurementValue([])).toBeUndefined();
    });
    it('find ShortPeriodFirstMotionFeatureMeasurementValue', () => {
      expect(
        findShortPeriodFirstMotionFeatureMeasurementValue(sdHypo.featureMeasurements)
      ).toMatchSnapshot();
      expect(findShortPeriodFirstMotionFeatureMeasurementValue([])).toBeUndefined();
    });
  });

  /**
   * Test the ability to get the current hypothesis from a set of hypotheses.
   */
  describe('SignalDetectionTypes.Util.getCurrentHypothesis', () => {
    it('empty hypotheses set', () => {
      const hypotheses: SignalDetectionHypothesis[] = undefined;
      expect(SignalDetectionTypes.Util.getCurrentHypothesis(hypotheses)).toBeUndefined();
    });

    it('correct hypothesis returned', () => {
      const hypotheses = [];

      const hypothesis1: SignalDetectionHypothesis = {
        id: {
          id: 'TEST1',
          signalDetectionId: null
        },
        featureMeasurements: [],
        monitoringOrganization: 'GMS',
        parentSignalDetectionHypothesis: null,
        rejected: false
      };

      const hypothesis2: SignalDetectionHypothesis = {
        id: {
          id: 'TEST2',
          signalDetectionId: null
        },
        featureMeasurements: [],
        monitoringOrganization: 'GMS',
        parentSignalDetectionHypothesis: null,
        rejected: false
      };

      hypotheses.push(hypothesis1, hypothesis2);

      const result = SignalDetectionTypes.Util.getCurrentHypothesis(hypotheses);
      expect(result.id.id).toEqual('TEST2');
    });
  });
});

/**
 *
 */
describe('can get use signal detection utils and get expected results', () => {
  test('getSignalDetectionChannelName', () => {
    const sd = cloneDeep(signalDetectionsData[0]);
    expect(getSignalDetectionChannelName(sd)).toMatchSnapshot();

    // Set arrival time feature measurement to undefined
    const index = sd.signalDetectionHypotheses[0].featureMeasurements.findIndex(
      fm => fm.featureMeasurementType === FeatureMeasurementType.ARRIVAL_TIME
    );
    sd.signalDetectionHypotheses[0].featureMeasurements[index] = undefined;
    expect(getSignalDetectionChannelName(sd)).toBeUndefined();

    // Test when there are no feature measurements
    sd.signalDetectionHypotheses[0].featureMeasurements = undefined;
    expect(getSignalDetectionChannelName(sd)).toBeUndefined();
    // set the sd hyp to undefined
    sd.signalDetectionHypotheses = undefined;
    expect(getSignalDetectionChannelName(sd)).toBeUndefined();
  });

  test('getChannelSegmentStringForCurrentHypothesis', () => {
    const sd = cloneDeep(signalDetectionsData[0]);
    expect(getChannelSegmentStringForCurrentHypothesis(sd)).toMatchSnapshot();

    // Set arrival time feature measurement to undefined
    const index = sd.signalDetectionHypotheses[0].featureMeasurements.findIndex(
      fm => fm.featureMeasurementType === FeatureMeasurementType.ARRIVAL_TIME
    );
    sd.signalDetectionHypotheses[0].featureMeasurements[index] = undefined;
    expect(getChannelSegmentStringForCurrentHypothesis(sd)).toBeUndefined();

    // Test when there are no feature measurements
    sd.signalDetectionHypotheses[0].featureMeasurements = undefined;
    expect(getChannelSegmentStringForCurrentHypothesis(sd)).toBeUndefined();
    // set the sd hyp to undefined
    sd.signalDetectionHypotheses = undefined;
    expect(getChannelSegmentStringForCurrentHypothesis(sd)).toBeUndefined();
  });

  test('parseStationNameFromChannelSegment', () => {
    // test cs is undefined
    expect(parseStationNameFromChannelSegment(undefined)).toBeUndefined();
    const cs = cloneDeep(uiChannelSegmentData.ASAR[WaveformTypes.UNFILTERED][0]);
    expect(parseStationNameFromChannelSegment(cs.channelSegment)).toMatchSnapshot();

    // Test channel name undefined
    cs.channelSegment.channelName = undefined;
    expect(parseStationNameFromChannelSegment(cs.channelSegment)).toBeUndefined();
  });

  test('parseBeamType from channel name', () => {
    expect(parseBeamType(undefined)).toBeUndefined();
    expect(parseBeamType('foobar')).toEqual('Raw channel');
    expect(parseBeamType('ASAR.AS01.BHZ')).toEqual('Raw channel');
    let channelName =
      'KSRS.beam.SHZ/beam,fk,coherent/steer,az_104.325deg,slow_13.808s_per_deg/33689b9f-8e74-36a2-a9eb-115ade4d6d9a';
    expect(parseBeamType(channelName)).toEqual('Fk beam');
    channelName =
      'KSRS.beam.SHZ/beam,event,coherent/steer,az_104.325deg,slow_13.808s_per_deg/33689b9f-8e74-36a2-a9eb-115ade4d6d9a';
    expect(parseBeamType(channelName)).toEqual('Event beam');
    channelName =
      'KSRS.beam.SHZ/beam,detection,coherent/steer,az_104.325deg,slow_13.808s_per_deg/33689b9f-8e74-36a2-a9eb-115ade4d6d9a';
    expect(parseBeamType(channelName)).toEqual('Detection beam');
    channelName =
      'KSRS.beam.SHZ/beam,foobar,coherent/steer,az_104.325deg,slow_13.808s_per_deg/33689b9f-8e74-36a2-a9eb-115ade4d6d9a';
    expect(parseBeamType(channelName)).toBeUndefined();
    expect(parseBeamType('KSRS.beam.SHZ/')).toBeUndefined();
  });
});

describe('Find feature measurement channel name', () => {
  const expectedChannelName = 'ASAR.beam.SHZ';
  const expectedUndefinedChannelName = undefined;
  const { featureMeasurements } = signalDetectionsData[0].signalDetectionHypotheses[0];
  const featureMeasurement = featureMeasurements[0];
  const {
    measuredChannelSegment,
    measurementValue,
    featureMeasurementType,
    snr
  } = featureMeasurements[0];

  test('return channel name', () => {
    const result = findFeatureMeasurementChannelName(featureMeasurements);
    expect(result).toEqual(expectedChannelName);
  });
  test('return undefined, valid channel name and invalid channel name', () => {
    const featureMeasurementCopy = {
      channel: { name: '', effectiveAt: undefined },
      measuredChannelSegment,
      measurementValue,
      featureMeasurementType,
      snr
    };
    const result = findFeatureMeasurementChannelName([featureMeasurement, featureMeasurementCopy]);
    expect(result).toEqual(expectedChannelName);
  });
  test('return undefined, empty feature measurement collection', () => {
    const result = findFeatureMeasurementChannelName([]);
    expect(result).toEqual(expectedUndefinedChannelName);
  });
  test('return undefined, undefined feature measurement collection', () => {
    const result = findFeatureMeasurementChannelName(undefined);
    expect(result).toEqual(expectedUndefinedChannelName);
  });
  test('return undefined, undefined channel', () => {
    const featureMeasurementCopy = {
      channel: undefined,
      measuredChannelSegment,
      measurementValue,
      featureMeasurementType,
      snr
    };
    const result = findFeatureMeasurementChannelName([featureMeasurementCopy]);
    expect(result).toEqual(expectedUndefinedChannelName);
  });
  test('return undefined, null channel', () => {
    const featureMeasurementCopy = {
      channel: null,
      measuredChannelSegment,
      measurementValue,
      featureMeasurementType,
      snr
    };
    const result = findFeatureMeasurementChannelName([featureMeasurementCopy]);
    expect(result).toEqual(expectedUndefinedChannelName);
  });
  test('return undefined, null channel name', () => {
    const featureMeasurementCopy = {
      channel: { name: null, effectiveAt: undefined },
      measuredChannelSegment,
      measurementValue,
      featureMeasurementType,
      snr
    };
    const result = findFeatureMeasurementChannelName([featureMeasurementCopy]);
    expect(result).toEqual(expectedUndefinedChannelName);
  });
  test('return undefined, undefined channel name', () => {
    const featureMeasurementCopy = {
      channel: { name: undefined, effectiveAt: undefined },
      measuredChannelSegment,
      measurementValue,
      featureMeasurementType,
      snr
    };
    const result = findFeatureMeasurementChannelName([featureMeasurementCopy]);
    expect(result).toEqual(expectedUndefinedChannelName);
  });
  test('return undefined, zero length channel name', () => {
    const featureMeasurementCopy = {
      channel: { name: '', effectiveAt: undefined },
      measuredChannelSegment,
      measurementValue,
      featureMeasurementType,
      snr
    };
    const result = findFeatureMeasurementChannelName([featureMeasurementCopy]);
    expect(result).toEqual(expectedUndefinedChannelName);
  });
});

describe('Sort and order signal detections', () => {
  test('Sort by distance, empty distances', () => {
    const distances = [];
    const result = sortAndOrderSignalDetections(
      signalDetectionsData,
      AnalystWorkspaceTypes.WaveformSortType.distance,
      distances
    );
    expect(result).toEqual(signalDetectionsData);
  });
  test('Sort by station name, empty distances', () => {
    const distances = [];
    const result = sortAndOrderSignalDetections(
      signalDetectionsData,
      AnalystWorkspaceTypes.WaveformSortType.stationNameAZ,
      distances
    );
    expect(result).toEqual(signalDetectionsData);
  });
});

describe('findFilteredBeamFeatureMeasurement', () => {
  it('returns undefined for empty input', () => {
    const result = findFilteredBeamFeatureMeasurement([], 'aoeu');
    expect(result).toBeUndefined();
  });

  it('returns undefined for undefined input', () => {
    const result = findFilteredBeamFeatureMeasurement(undefined, 'aoeu');
    expect(result).toBeUndefined();
  });
  it('returns undefined for null input', () => {
    const result = findFilteredBeamFeatureMeasurement(null, 'aoeu');
    expect(result).toBeUndefined();
  });

  it('returns undefined with empty feature definition', () => {
    const fm: FeatureMeasurement = {
      channel: null,
      featureMeasurementType: FeatureMeasurementType.FILTERED_BEAM,
      measuredChannelSegment: null,
      snr: null,
      measurementValue: { value: '13' }
    };
    const result = findFilteredBeamFeatureMeasurement([fm], '');
    expect(result).toBeUndefined();
  });

  describe('getAssocStatusColor', () => {
    it('is defined', () => {
      expect(getAssocStatusColor).toBeDefined();
    });

    it('returns associated to open event', () => {
      expect(getAssocStatusColor('Open', defaultTheme)).toBe(defaultTheme.colors.openEventSDColor);
    });

    it('returns associated to completed event', () => {
      expect(getAssocStatusColor('Completed', defaultTheme)).toBe(
        defaultTheme.colors.completeEventSDColor
      );
    });

    it('returns associated to other event', () => {
      expect(getAssocStatusColor('Other', defaultTheme)).toBe(
        defaultTheme.colors.otherEventSDColor
      );
    });
  });

  describe('getAssocStatusString', () => {
    it('is defined', () => {
      expect(getAssocStatusString).toBeDefined();
    });

    it('returns associated to open event', () => {
      expect(getAssocStatusString('Open')).toBe('Associated to open event');
    });

    it('returns associated to completed event', () => {
      expect(getAssocStatusString('Completed')).toBe('Associated to completed event');
    });

    it('returns associated to other event', () => {
      expect(getAssocStatusString('Other')).toBe('Associated to other event');
    });

    it('returns unknown', () => {
      expect(getAssocStatusString('this should not happen')).toBe('Unknown');
    });
  });
});
