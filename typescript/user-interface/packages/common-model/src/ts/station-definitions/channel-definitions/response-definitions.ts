import type * as CommonTypes from '../../common/types';

/**
 * Channel Response definition
 */
export interface Response {
  readonly calibration: Calibration;
  readonly effectiveAt: number; // epoch seconds
  readonly effectiveUntil: number;
  readonly fapResponse: FrequencyAmplitudePhase;
  readonly id?: string;
}

/**
 * Represents calibration information
 *
 * @JsonProperty("calibrationPeriodSec") double calibrationPeriodSec,
 * @JsonProperty("calibrationTimeShift") Duration calibrationTimeShift,
 * @JsonProperty("calibrationFactor") DoubleValue calibrationFactor)
 */
export interface Calibration {
  readonly calibrationPeriodSec: number;
  readonly calibrationTimeShift: string; // Java Duration
  readonly calibrationFactor: CommonTypes.DoubleValue;
}

/**
 * FrequencyAmplitudePhase definition
 */
export interface FrequencyAmplitudePhase {
  readonly frequencies: number[];
  readonly amplitudeResponseUnits: CommonTypes.Units[];
  readonly amplitudeResponse: number[];
  readonly amplitudeResponseStdDev: number[];
  readonly phaseResponseUnits: CommonTypes.Units[];
  readonly phaseResponse: number[];
  readonly phaseResponseStdDev: number[];
  readonly id?: string;
}
