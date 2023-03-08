import type { DataPayload } from '../cache/types';
import type { TimeSeries } from '../channel-segment/types';
import type { SignalDetection } from '../signal-detection/types';
import type { Waveform } from '../waveform/types';

// ***************************************
// Mutations
// ***************************************

export interface FkInput {
  stationId: string;
  signalDetectionId: string;
  signalDetectionHypothesisId: string;
  phase: string;
  frequencyBand: {
    minFrequencyHz: number;
    maxFrequencyHz: number;
  };
  windowParams: {
    windowType: string;
    leadSeconds: number;
    lengthSeconds: number;
    stepSize: number;
  };
  configuration: FkConfiguration;
}

/**
 * Params to mark fks review
 */
export interface MarkFksReviewedInput {
  signalDetectionIds: string[];
  reviewed: boolean;
}

export interface ComputeFksMutationArgs {
  input: FkInput[];
}

export interface ComputeFksMutationData {
  computeFks: DataPayload;
}

export interface ComputeFksMutationResult {
  data: ComputeFksMutationData;
}

export interface ComputeFrequencyFkThumbnailsInput {
  fkInput: FkInput;
}

/**
 * Arguments provided to the mark fks reviewed mutation
 */
export interface MarkFksReviewedMutationArgs {
  markFksReviewedInput: MarkFksReviewedInput;
}

export interface MarkFksReviewedMutationData {
  markFksReviewed: DataPayload;
}

export interface MarkFksReviewedMutationResult {
  data: MarkFksReviewedMutationData;
}

// ***************************************
// Subscriptions
// ***************************************

/**
 * Data structure for detectionUpdated subscription callback
 */
export interface FksCreatedSubscription {
  fksCreated: SignalDetection[];
}

// ***************************************
// Queries
// ***************************************

// ***************************************
// Model
// ***************************************

export interface FrequencyBand {
  minFrequencyHz: number;
  maxFrequencyHz: number;
}

export interface WindowParameters {
  leadSeconds: number;
  lengthSeconds: number;
  stepSize: number;
}

export interface FstatData {
  azimuthWf: Waveform;
  slownessWf: Waveform;
  fstatWf: Waveform;
}

export interface FkPowerSpectra extends TimeSeries {
  // TO ADD
  // channel segment id
  id: string;
  contribChannels: {
    id: string;
    name: string;
    site: {
      name: string;
    };
  }[];
  // TO CONSIDER
  // windowType:string = 'hanning'
  // TO KEEP
  windowLead: number;
  windowLength: number;
  stepSize: number;
  lowFrequency: number;
  highFrequency: number;
  metadata: {
    phaseType: string;
    slowStartX: number;
    slowDeltaX: number;
    slowStartY: number;
    slowDeltaY: number;
  };
  slowCountX: number;
  slowCountY: number;
  reviewed: boolean;
  peakSpectrum?: FkPowerSpectrum;
  spectrums: FkPowerSpectrum[];
  fstatData: FstatData;
  configuration: FkConfiguration;
}

export interface FkPowerSpectrum {
  power: number[][];
  fstat: number[][];
  quality: number;
  attributes: FkAttributes;
  configuration: FkConfiguration;
}

export interface FkAttributes {
  peakFStat: number;
  azimuth: number;
  slowness: number;
  azimuthUncertainty: number;
  slownessUncertainty: number;
}
/**
 * FkFrequencyThumbnail preview Fk at a preset FrequencyBand
 */
export interface FkFrequencyThumbnail {
  frequencyBand: FrequencyBand;
  fkSpectra: FkPowerSpectra;
}

/**
 * Collection of thumbnails by signal detection id
 */
export interface FkFrequencyThumbnailBySDId {
  signalDetectionId: string;
  fkFrequencyThumbnails: FkFrequencyThumbnail[];
}

/**
 * Tracks whether a channel is used to calculate fk
 */

export interface ContributingChannelsConfiguration {
  id: string;
  enabled: boolean;
  name: string;
}
/**
 * Holds the configuration used to calculate an Fk
 */
export interface FkConfiguration {
  maximumSlowness: number;
  mediumVelocity: number;
  numberOfPoints: number;
  normalizeWaveforms: boolean;
  useChannelVerticalOffset: boolean;
  leadFkSpectrumSeconds: number;
  contributingChannelsConfiguration: ContributingChannelsConfiguration[];
}
