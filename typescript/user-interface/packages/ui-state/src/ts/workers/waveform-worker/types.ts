import type { AxiosRequestConfig } from 'axios';

import type { GetChannelSegmentsByChannelsQueryArgs } from '../../app/api/data/channel-segment/types';

/**
 * The colors to use when building the weavess channel segments
 */
export interface ChannelSegmentColorOptions {
  waveformColor: string;
  labelTextColor: string;
}

export interface AmplitudeBounds {
  amplitudeMax: number;
  amplitudeMin: number;
  amplitudeTotal?: number;
  totalSamplesCount?: number;
  amplitudeMaxSecs?: number;
  amplitudeMinSecs?: number;
}

/**
 * An axios request known to have a waveform query request in its data.
 */
export type WaveformAxiosRequestConfig = AxiosRequestConfig & {
  data: GetChannelSegmentsByChannelsQueryArgs;
};
