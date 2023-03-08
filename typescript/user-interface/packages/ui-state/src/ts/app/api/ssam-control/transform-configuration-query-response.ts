import type { ConfigurationTypes } from '@gms/common-model';
import { convertDurationToMilliseconds, convertDurationToSeconds } from '@gms/common-util';

/**
 * Custom axios response transformer that transforms SohConfiguration
 * to UiSohConfiguration.
 *
 * !converts durations types to seconds or milliseconds
 *
 * @param response the original SohConfiguration query response
 *
 * @returns the transformed (converted) response data
 */
export const transformConfigurationQueryResponse = (
  response: ConfigurationTypes.SohConfiguration
): ConfigurationTypes.UiSohConfiguration => {
  return {
    reprocessingPeriodSecs: convertDurationToSeconds(
      response.stationSohControlConfiguration.reprocessingPeriod
    ),
    displayedStationGroups: response.stationSohControlConfiguration.displayedStationGroups,
    rollupStationSohTimeToleranceMs: convertDurationToMilliseconds(
      response.stationSohControlConfiguration.rollupStationSohTimeTolerance
    ),
    redisplayPeriodMs: convertDurationToMilliseconds(
      response.stationSohMonitoringDisplayParameters.redisplayPeriod
    ),
    acknowledgementQuietMs: convertDurationToMilliseconds(
      response.stationSohMonitoringDisplayParameters.acknowledgementQuietDuration
    ),
    availableQuietTimesMs: response.stationSohMonitoringDisplayParameters.availableQuietDurations.map(
      convertDurationToMilliseconds
    ),
    sohStationStaleMs: convertDurationToMilliseconds(
      response.stationSohMonitoringDisplayParameters.sohStationStaleDuration
    ),
    sohHistoricalTimesMs: response.stationSohMonitoringDisplayParameters.sohHistoricalDurations.map(
      convertDurationToMilliseconds
    ),
    historicalSamplesPerChannel: response.stationSohMonitoringDisplayParameters.samplesPerChannel,
    maxHistoricalQueryIntervalSizeMs:
      response.stationSohMonitoringDisplayParameters.maxQueryIntervalSize
  };
};
