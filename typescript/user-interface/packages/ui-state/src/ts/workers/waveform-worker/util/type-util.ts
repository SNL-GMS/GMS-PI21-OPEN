import type { AxiosRequestConfig } from 'axios';

import type { WaveformAxiosRequestConfig } from '../types';

/**
 * TypeGuard that checks an axios configuration's internal data to verify that it is a correctly
 * formatted request config for waveform data. Checks for
 *   startTime: string;
 *   endTime: string;
 *   channels: FacetedTypes.VersionReference<ChannelTypes.Channel>[];
 *
 * @param requestConfig The Axios Request Configuration that should be checked to see if its
 * request data is of the type WaveformRequest
 */
export function isWaveformRequest(
  requestConfig: AxiosRequestConfig
): requestConfig is WaveformAxiosRequestConfig {
  return (
    requestConfig?.data &&
    typeof requestConfig.data?.startTime === 'number' &&
    typeof requestConfig.data?.endTime === 'number' &&
    Array.isArray(requestConfig.data?.channels) &&
    (requestConfig.data.channels.length === 0 ||
      (requestConfig.data.channels[0]?.name &&
        typeof requestConfig.data.channels[0].name === 'string'))
  );
}
