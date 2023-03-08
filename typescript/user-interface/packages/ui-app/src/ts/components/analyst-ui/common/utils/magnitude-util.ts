import type { LegacyEventTypes } from '@gms/common-model';
import { CommonTypes, SignalDetectionTypes } from '@gms/common-model';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import type { StationMagnitudeSdData } from '~analyst-ui/components/magnitude/components/station-magnitude/types';
import { systemConfig } from '~analyst-ui/config';

/**
 * Returns the station magnitude data for a signal detection that may not be defined
 *
 * @param sd a signal detection to get mag data from
 */
export function getMagnitudeDataForSd(
  sd: SignalDetectionTypes.SignalDetection
): StationMagnitudeSdData {
  const phaseFmValue = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  const timeFm = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  const arrivalFm = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  // TODO: need to confirm this is an fkb (no longer using ChannelSegmentType)
  // TODO: Fix once the channel segment type definition figures out what type of waveform it is
  // TODO: fkb, detection beam, raw channel signal detection
  const channel = arrivalFm ? 'fkb' : 'Not Sure';
  const maybeAmplitudeMeasurement = phaseFmValue
    ? SignalDetectionUtils.findAmplitudeFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
          .featureMeasurements,
        systemConfig.amplitudeTypeForPhase.get(phaseFmValue.value) as any
      )
    : undefined;
  const maybeAmplitude = maybeAmplitudeMeasurement
    ? maybeAmplitudeMeasurement.amplitude.value
    : undefined;
  const maybePeriod = maybeAmplitudeMeasurement ? maybeAmplitudeMeasurement.period : undefined;
  return {
    amplitudePeriod: maybePeriod,
    amplitudeValue: maybeAmplitude,
    phase: phaseFmValue ? phaseFmValue.value : '',
    // FIXME: What is trim supposed to return?
    channel,
    signalDetectionId: sd.id,
    time: timeFm ? timeFm.arrivalTime.value : Infinity,
    stationName: sd.station.name,
    // Show as needs review if the amplitude measurement hasnt been reviewed and if its eligible for review
    // TODO: current SD definition no longer has a review or  requiresReview fields
    flagForReview: false // !sd.reviewed.amplitudeMeasurement && sd.requiresReview.amplitudeMeasurement
  };
}

/**
 * Returns the station magnitude data for a signal detection snapshot
 *
 * @param maybeSd a signal detection that
 */
export function getMagnitudeDataForSdSnapshot(
  snapshot: LegacyEventTypes.SignalDetectionSnapshot
): StationMagnitudeSdData {
  let amplitude: LegacyEventTypes.AmplitudeSnapshot;

  if (snapshot.phase === CommonTypes.PhaseType.P) {
    amplitude = snapshot.aFiveAmplitude;
  }

  if (snapshot.phase === CommonTypes.PhaseType.LR) {
    amplitude = snapshot.aLRAmplitude;
  }
  return {
    amplitudePeriod: amplitude ? amplitude.amplitudeValue : undefined,
    amplitudeValue: amplitude ? amplitude.period : undefined,
    phase: snapshot.phase,
    channel: snapshot.channelName,
    signalDetectionId: snapshot.signalDetectionId,
    time: snapshot.time.observed,
    stationName: snapshot.stationName,
    flagForReview: false
  };
}

/**
 * Gets snapshots for the given lss
 *
 * @param event event whichs holds the lss
 * @param locationSolutionSetId the id
 */
export function getSnapshotsForLssId(
  event: LegacyEventTypes.Event,
  locationSolutionSetId: string
): StationMagnitudeSdData[] {
  const maybeLss =
    event && event.currentEventHypothesis && event.currentEventHypothesis.eventHypothesis
      ? event.currentEventHypothesis.eventHypothesis.locationSolutionSets.find(
          lss => lss.id === locationSolutionSetId
        )
      : undefined;
  return maybeLss ? maybeLss.locationSolutions[0].snapshots.map(getMagnitudeDataForSdSnapshot) : [];
}

/**
 * Returns the Network Magnitude Solution based on magnitude type
 *
 * @param locationSolution a location solution
 * @param  magnitudeTYpe a magnitude type
 *
 * @returns a NetworkMagnitudeSolution
 */
export function getNetworkMagSolution(
  locationSolution: LegacyEventTypes.LocationSolution,
  magnitudeType: string
): LegacyEventTypes.NetworkMagnitudeSolution {
  return locationSolution.networkMagnitudeSolutions.find(
    netMagSol => netMagSol.magnitudeType === magnitudeType
  );
}
