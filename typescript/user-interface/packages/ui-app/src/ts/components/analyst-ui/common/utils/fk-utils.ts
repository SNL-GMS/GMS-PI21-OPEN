import type { FkTypes, ProcessingStationTypes } from '@gms/common-model';
import { CommonTypes, SignalDetectionTypes } from '@gms/common-model';
import type Immutable from 'immutable';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import type { FkParams } from '~analyst-ui/components/azimuth-slowness/types';
import { FkUnits } from '~analyst-ui/components/azimuth-slowness/types';
import { systemConfig, userPreferences } from '~analyst-ui/config';

/**
 * Utility functions for the Azimuth Slowness Display
 */

/**
 * Finds Azimuth Feature Measurements for the FkData object
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns FkData or undefined if not found
 */
export function getFkData(
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): FkTypes.FkPowerSpectra | undefined {
  // TODO: Plumb in channel segment map for waveform lookup when FK display is added
  // const azimuthFM = SignalDetectionUtils.findAzimuthFeatureMeasurement(featureMeasurements);

  // if (azimuthFM && azimuthFM?.measuredChannelSegment?.id) {
  //   if (
  //     azimuthFM.channelSegment.timeseries[0] &&
  //     isFkSpectraChannelSegment(azimuthFM.channelSegment)
  //   ) {
  //     return azimuthFM.channelSegment.timeseries[0];
  //   }
  // }
  return undefined;
}

export function getFkParamsForSd(sd: SignalDetectionTypes.SignalDetection): FkParams {
  const fk = getFkData(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  return {
    frequencyPair: {
      maxFrequencyHz: fk.highFrequency,
      minFrequencyHz: fk.lowFrequency
    },
    windowParams: {
      leadSeconds: fk.windowLead,
      lengthSeconds: fk.windowLength,
      stepSize: fk.stepSize
    }
  };
}

/**
 * Returns an empty FK Spectrum configuration. The values are NOT default values,
 * but instead values that will make it obvious within the UI that a correct
 * configuration was never added to the FK
 */
const defaultFkConfiguration: FkTypes.FkConfiguration = {
  contributingChannelsConfiguration: [],
  maximumSlowness: systemConfig.continousFkConfiguration.defaultMaximumSlowness,
  mediumVelocity: 1,
  normalizeWaveforms: false,
  numberOfPoints: systemConfig.continousFkConfiguration.defaultNumberOfPoints,
  useChannelVerticalOffset: false,
  leadFkSpectrumSeconds: userPreferences.azimuthSlowness.defaultLead
};

/**
 * Returns an Fk Configuration for the correct phase
 */
export function getDefaultFkConfigurationForSignalDetection(
  sd: SignalDetectionTypes.SignalDetection,
  contributingChannels: ProcessingStationTypes.ProcessingChannel[]
): FkTypes.FkConfiguration {
  // Check and see if SD is well formed
  if (
    !sd ||
    !SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses) ||
    !SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
      .featureMeasurements
  ) {
    return undefined;
  }
  const phase = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  ).value;
  const phaseAsString = CommonTypes.PhaseType[phase];
  const contributingChannelsConfiguration = contributingChannels.map(channel => ({
    name: channel.name,
    id: channel.name,
    enabled: true
  }));
  let mediumVelocity = 0;
  // eslint-disable-next-line newline-per-chained-call
  if (phaseAsString.toLowerCase().startsWith('p') || phaseAsString.toLowerCase().endsWith('p')) {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    mediumVelocity = 5.8;
    // eslint-disable-next-line newline-per-chained-call
  } else if (
    phaseAsString.toLowerCase().startsWith('s') ||
    phaseAsString.toLowerCase().endsWith('s')
  ) {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    mediumVelocity = 3.6;
  } else if (phaseAsString === CommonTypes.PhaseType.Lg) {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    mediumVelocity = 3.5;
  } else if (phaseAsString === CommonTypes.PhaseType.Rg) {
    // eslint-disable-next-line
    mediumVelocity = 3;
  } else {
    // Cause Tx or N...undefined behavior ok
    mediumVelocity = 1;
  }
  const fkConfiguration: FkTypes.FkConfiguration = {
    ...defaultFkConfiguration,
    mediumVelocity,
    contributingChannelsConfiguration
  };
  return fkConfiguration;
}

/**
 * Gets the user-set fk unit for a given fk id, or returns the default unit
 *
 * @param fkId the id of the fk
 */
export function getFkUnitForSdId(
  sdId: string,
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>
): FkUnits {
  return fkUnitsForEachSdId.has(sdId) ? fkUnitsForEachSdId.get(sdId) : FkUnits.FSTAT;
}

/**
 * Formats a frequency band into a string for the drop down
 *
 * @param band Frequency band to format
 */
export function frequencyBandToString(band: FkTypes.FrequencyBand): string {
  return `${band.minFrequencyHz} - ${band.maxFrequencyHz} Hz`;
}
