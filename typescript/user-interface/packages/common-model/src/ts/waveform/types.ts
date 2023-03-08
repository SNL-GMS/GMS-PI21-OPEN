import type { ChannelSegment, TimeSeries } from '../channel-segment/types';

export interface Waveform extends TimeSeries {
  samples: number[] | Float32Array;
}

export interface FilteredChannelSegment extends ChannelSegment<Waveform> {
  sourceChannelId: string;
  wfFilterId: string;
}

export interface WaveformFilter {
  id: string;
  name: string;
  description: string;
  filterType: string; // FIR_HAMMING
  filterPassBandType: string; // BAND_PASS, HIGH_PASS
  lowFrequencyHz: number;
  highFrequencyHz: number;
  order: number;
  filterSource: string; // SYSTEM
  filterCausality: string; // CAUSAL
  zeroPhase: boolean;
  sampleRate: number;
  sampleRateTolerance: number;
  validForSampleRate: boolean;
  aCoefficients?: number[];
  bCoefficients?: number[];
  groupDelaySecs: number;
}

export const DEFAULT_SAMPLE_RATE = 1;

export const UNFILTERED = 'unfiltered';

export const UNFILTERED_FILTER: Partial<WaveformFilter> = {
  id: UNFILTERED,
  name: UNFILTERED,
  sampleRate: undefined
};

// Enum to clarify pan button interactions
export enum PanType {
  Left,
  Right
}
