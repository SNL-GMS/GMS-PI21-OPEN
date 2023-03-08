import type {
  CommonTypes,
  LegacyEventTypes,
  ProcessingStationTypes,
  SignalDetectionTypes
} from '@gms/common-model';

import {
  getMagnitudeDataForSd,
  getSnapshotsForLssId
} from '~analyst-ui/common/utils/magnitude-util';

import type { StationMagnitudeSdData } from './components/station-magnitude/types';
import type { AmplitudesByStation } from './types';

/**
 * Returns the amplitudes by station data used in the station mag display
 *
 * @param stations Stations to get data for
 * @param stationMagnitudeSDData Station Magnitude data for associated signal detections
 * @param magnitudeTypesForPhase a mapping from signal detection phase to valid magnitude types
 */
export const getAmplitudesByStation = (
  stations: ProcessingStationTypes.ProcessingStation[],
  stationMagnitudeSDData: StationMagnitudeSdData[],
  magnitudeTypesForPhase: Map<CommonTypes.PhaseType, LegacyEventTypes.MagnitudeType[]>
): AmplitudesByStation[] => {
  const amplitudesByStation: AmplitudesByStation[] = stations.map(station => ({
    stationId: station.name,
    stationName: station.name,
    magTypeToAmplitudeMap: new Map<LegacyEventTypes.MagnitudeType, StationMagnitudeSdData>(),
    validSignalDetectionForMagnitude: new Map<LegacyEventTypes.MagnitudeType, boolean>()
  }));
  stationMagnitudeSDData.forEach(sdData => {
    const magnitudeTypes = magnitudeTypesForPhase.get(sdData.phase as CommonTypes.PhaseType);
    if (magnitudeTypes) {
      magnitudeTypes.forEach(mag => {
        const maybeSD = amplitudesByStation
          .find(station => station.stationName === sdData.stationName)
          .magTypeToAmplitudeMap.get(mag);
        if (maybeSD === undefined || sdData.time < maybeSD.time) {
          amplitudesByStation
            .find(station => station.stationName === sdData.stationName)
            .magTypeToAmplitudeMap.set(mag, sdData);
          amplitudesByStation
            .find(station => station.stationName === sdData.stationName)
            .validSignalDetectionForMagnitude.set(mag, true);
        }
      });
    }
  });
  // Filters out stations that lack sd's for the configured mag types
  return amplitudesByStation.filter(abs =>
    [...abs.validSignalDetectionForMagnitude.values()].reduce((accum, val) => accum || val, false)
  );
};

/**
 * Converts signal detections into the StationMagnitudeSdData type sorted by arrival time
 *
 * @param currentlyOpenEvent currently open event
 * @param associatedSignalDetections signal detections associated to the open event
 * @param selectedLocationSolutionSetId the id of the user selected location solution set
 * @param isLatestLssSelected true if the selected location solution set is the last calculated set
 */
export const getSignalDetectionMagnitudeData = (
  currentlyOpenEvent: LegacyEventTypes.Event,
  associatedSignalDetections: SignalDetectionTypes.SignalDetection[],
  selectedLocationSolutionSetId: string,
  isLatestLssSelected: boolean
): StationMagnitudeSdData[] => {
  const sdDataForMagDisplay: StationMagnitudeSdData[] = isLatestLssSelected
    ? associatedSignalDetections.map(getMagnitudeDataForSd)
    : getSnapshotsForLssId(currentlyOpenEvent, selectedLocationSolutionSetId);

  sdDataForMagDisplay.sort((a, b) => {
    const aTime = a.time;
    const bTime = b.time;
    return aTime - bTime;
  });

  return sdDataForMagDisplay;
};
