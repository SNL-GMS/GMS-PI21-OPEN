import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * Waveform request config definition
 */
export interface WaveformRequestConfig extends RequestConfig {
  readonly waveform: {
    readonly baseUrl: string;
    readonly services: {
      readonly getChannelSegment: ServiceDefinition;
    };
  };
}

const baseUrl = `${UI_URL}${Endpoints.WaveformManagerServiceUrls.baseUrl}`;

/**
 * The Waveform request config for all services.
 */
export const config: WaveformRequestConfig = {
  waveform: {
    baseUrl,
    // Service endpoints for this component
    services: {
      getChannelSegment: {
        requestConfig: {
          baseURL: baseUrl,
          method: 'post',
          url: Endpoints.WaveformManagerServiceUrls.getChannelSegment,
          proxy: false,
          headers: {
            // configure to receive msgpack encoded data
            accept: 'application/msgpack',
            'content-type': 'application/json'
          },
          timeout: 180000 // 3 mins until we figure out batching...
        }
      }
    }
  }
};
