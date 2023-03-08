import type { LegacyEventTypes } from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import cloneDeep from 'lodash/cloneDeep';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import type { LocationSDRow } from '~analyst-ui/components/location/components/location-signal-detections/types';
import {
  DefiningChange,
  DefiningTypes
} from '~analyst-ui/components/location/components/location-signal-detections/types';
import type {
  DefiningStatus,
  SignalDetectionSnapshotWithDiffs,
  SignalDetectionTableRowChanges
} from '~analyst-ui/components/location/types';
import { gmsColors } from '~scss-config/color-preferences';

import { getLatestLocationSolutionSetLegacy } from './event-util';

/**
 * Helper function to lookup the location behavior in the event's location behavior list
 * that corresponds to that Feature Measurement Id
 *
 * @param locationBehaviors Event's Location Behaviors list
 * @param definingType which isDefining value to update Arrival Time, Slowness or Azimuth
 * @param sd SignalDetection which has the FeatureMeasurements to search
 *
 * @returns LocationBehavior
 */
export function getLocationBehavior(
  definingType: DefiningTypes,
  sd: SignalDetectionTypes.SignalDetection,
  locationBehaviors: LegacyEventTypes.LocationBehavior[]
): LegacyEventTypes.LocationBehavior {
  let fmt: SignalDetectionTypes.FeatureMeasurementType;
  if (definingType === DefiningTypes.AZIMUTH) {
    fmt = SignalDetectionTypes.FeatureMeasurementType.RECEIVER_TO_SOURCE_AZIMUTH;
  } else if (definingType === DefiningTypes.ARRIVAL_TIME) {
    fmt = SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME;
  } else if (definingType === DefiningTypes.SLOWNESS) {
    fmt = SignalDetectionTypes.FeatureMeasurementType.SLOWNESS;
  } else {
    fmt = undefined;
  }
  return locationBehaviors.find(
    lb => lb.signalDetectionId === sd.id && lb.featureMeasurementType === fmt
  );
}

/**
 * Helper function for {@link getNewDefiningForSD} to determine the {@link DefiningChange}
 * value of a given {@link DefiningTypes}
 *
 * @param definingType Type of location behavior to change
 * @param expectedDefiningType Expected type of location behavior to change
 * @param setDefining Whether defining will change to true or false
 * @param originalDefining Location behavior to a specific Feature Measurement Id
 * @param defaultDefiningChange Default location behavior if {@link definingType}
 * and {@link expectedDefiningType} do not match
 * @returns A {@link DefiningChange} value determined by the given parameters.
 */
function determineDefiningChange(
  definingType: DefiningTypes,
  expectedDefiningType: keyof typeof DefiningTypes,
  setDefining: boolean,
  originalDefining: boolean,
  defaultDefiningChange: DefiningChange
): DefiningChange {
  let definingChange = defaultDefiningChange;
  if (definingType === expectedDefiningType) {
    if (setDefining !== originalDefining) {
      if (setDefining) {
        definingChange = DefiningChange.CHANGED_TO_TRUE;
      } else {
        definingChange = DefiningChange.CHANGED_TO_FALSE;
      }
    } else {
      definingChange = DefiningChange.NO_CHANGE;
    }
  }
  return definingChange;
}

/**
 * Gets the defining settings for an sd based on a new set of defining
 *
 * @param definingType Type of location behavior to change
 * @param setDefining Whether defining will change to true or false
 * @param signalDetection The signal detection being affected
 * @param sdRowChanges The current state of defining settings in the ui
 * @param openEvent The open event
 */
export function getNewDefiningForSD(
  definingType: DefiningTypes,
  setDefining: boolean,
  signalDetection: SignalDetectionTypes.SignalDetection,
  sdRowChanges: SignalDetectionTableRowChanges,
  openEvent: LegacyEventTypes.Event
): SignalDetectionTableRowChanges {
  const {
    locationBehaviors
  } = openEvent.currentEventHypothesis.eventHypothesis.preferredLocationSolution.locationSolution;

  const originalAzimuthDefining = getLocationBehavior(
    DefiningTypes.AZIMUTH,
    signalDetection,
    locationBehaviors
  )
    ? getLocationBehavior(DefiningTypes.AZIMUTH, signalDetection, locationBehaviors).defining
    : false;
  const originalArrivalTimeDefining = getLocationBehavior(
    DefiningTypes.ARRIVAL_TIME,
    signalDetection,
    locationBehaviors
  )
    ? getLocationBehavior(DefiningTypes.ARRIVAL_TIME, signalDetection, locationBehaviors).defining
    : false;
  const originalSlownessDefining = getLocationBehavior(
    DefiningTypes.SLOWNESS,
    signalDetection,
    locationBehaviors
  )
    ? getLocationBehavior(DefiningTypes.SLOWNESS, signalDetection, locationBehaviors).defining
    : false;

  return {
    arrivalTimeDefining: determineDefiningChange(
      definingType,
      DefiningTypes.ARRIVAL_TIME,
      setDefining,
      originalArrivalTimeDefining,
      sdRowChanges.arrivalTimeDefining
    ),
    azimuthDefining: determineDefiningChange(
      definingType,
      DefiningTypes.AZIMUTH,
      setDefining,
      originalAzimuthDefining,
      sdRowChanges.azimuthDefining
    ),
    slownessDefining: determineDefiningChange(
      definingType,
      DefiningTypes.SLOWNESS,
      setDefining,
      originalSlownessDefining,
      sdRowChanges.slownessDefining
    ),
    signalDetectionId: signalDetection.id
  };
}

/**
 * Helper function to set the Time values in the new SD Table Row
 *
 * @param row LocationSDRow to populate
 * @param sd Signal Detection to find location behavior to populate from Slowness
 * @param locationBehaviors LocationBehaviors list from current selected event
 */
export function getArrivalTimeValues(
  sd: SignalDetectionTypes.SignalDetection,
  locationBehaviors: LegacyEventTypes.LocationBehavior[]
): { obs: number; res: number } {
  const fmValue = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  const locBehavior = getLocationBehavior(DefiningTypes.ARRIVAL_TIME, sd, locationBehaviors);
  return {
    obs: fmValue.arrivalTime.value,
    res: locBehavior ? locBehavior.residual : undefined
  };
}

/**
 * Helper function to set the Azimiuth values in the new SD Table Row
 *
 * @param row LocationSDRow to populate
 * @param sd Signal Detection to find location behavior to populate from Slowness
 * @param locationBehaviors LocationBehavors list from current selected event
 */
export function getAzimuthValues(
  sd: SignalDetectionTypes.SignalDetection,
  locationBehaviors: LegacyEventTypes.LocationBehavior[]
): { obs: number; res: number } {
  const fmValue = SignalDetectionUtils.findAzimuthFeatureMeasurementValue(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  const locBehavior = getLocationBehavior(DefiningTypes.AZIMUTH, sd, locationBehaviors);
  return {
    obs: fmValue ? fmValue.measuredValue.value : undefined,
    res: locBehavior ? locBehavior.residual : undefined
  };
}

/**
 * Helper function to set the Slowness values in the new SD Table Row
 *
 * @param row LocationSDRow to populate
 * @param sd Signal Detection to find location behavior to populate from Slowness
 * @param locationBehaviors LocationBehavors list from current selected event
 */
export function getSlownessValues(
  sd: SignalDetectionTypes.SignalDetection,
  locationBehaviors: LegacyEventTypes.LocationBehavior[]
): { obs: number; res: number } {
  const fmValue = SignalDetectionUtils.findSlownessFeatureMeasurementValue(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  const locBehavior = getLocationBehavior(DefiningTypes.SLOWNESS, sd, locationBehaviors);
  return {
    obs: fmValue ? fmValue.measuredValue.value : undefined,
    res: locBehavior ? locBehavior.residual : undefined
  };
}

/**
 * Gets the proper cell style for a dif
 *
 * @param definingType Type of cell to mark
 * @param row params from ag grid
 */
export function getDiffStyleForDefining(definingType: DefiningTypes, row: LocationSDRow): any {
  switch (definingType) {
    case DefiningTypes.ARRIVAL_TIME:
      return {
        'background-color': row.timeDefiningDiff ? gmsColors.gmsTableChangeMarker : ''
      };
    case DefiningTypes.AZIMUTH:
      return {
        'background-color': row.azimuthDefiningDiff ? gmsColors.gmsTableChangeMarker : ''
      };
    case DefiningTypes.SLOWNESS:
      return {
        'background-color': row.slownessDefiningDiff ? gmsColors.gmsTableChangeMarker : ''
      };
    default:
      return {};
  }
}
/**
 * Gets the channel name for an sd
 *
 * @param signalDetection Signal detection to get channel name from
 */
export function getChannelName(signalDetection: SignalDetectionTypes.SignalDetection): string {
  const maybeArrivalTime = SignalDetectionTypes.Util.getCurrentHypothesis(
    signalDetection.signalDetectionHypotheses
  ).featureMeasurements.find(
    fm => fm.featureMeasurementType === SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME
  );
  if (maybeArrivalTime) {
    if (maybeArrivalTime?.channel.name) {
      return maybeArrivalTime.channel.name;
    }
  }
  return '';
}

/**
 *
 * @param sd Signal detection to convert
 * @param locationBehaviors  location behaviors for the sd as sent fromt the gateway
 * @param sdDefiningFromTable defining states for the sd as sent from the location sd table
 */
export function convertSignalDetectionToSnapshotWithDiffs(
  sd: SignalDetectionTypes.SignalDetection,
  locationBehaviors: LegacyEventTypes.LocationBehavior[],
  sdDefiningFromTable: DefiningStatus | undefined
): SignalDetectionSnapshotWithDiffs {
  /** Helper function, determine if a given definingChange is defining */
  const determineDefining = (definingChange: DefiningChange, definingType: DefiningTypes) => {
    let isDefining = false;
    if (definingChange === DefiningChange.CHANGED_TO_TRUE) {
      isDefining = true;
    } else if (getLocationBehavior(definingType, sd, locationBehaviors)) {
      isDefining = getLocationBehavior(definingType, sd, locationBehaviors).defining;
    }
    return isDefining;
  };
  const arrivalTimeValues = getArrivalTimeValues(sd, locationBehaviors);
  const slownessValues = getSlownessValues(sd, locationBehaviors);
  const azimuthValues = getAzimuthValues(sd, locationBehaviors);

  const isDefiningTime = determineDefining(
    sdDefiningFromTable.arrivalTimeDefining,
    DefiningTypes.ARRIVAL_TIME
  );

  const isDefiningAzimuth = determineDefining(
    sdDefiningFromTable.azimuthDefining,
    DefiningTypes.AZIMUTH
  );

  const isDefiningSlowness = determineDefining(
    sdDefiningFromTable.slownessDefining,
    DefiningTypes.SLOWNESS
  );

  return {
    signalDetectionId: sd.id,
    signalDetectionHypothesisId: SignalDetectionTypes.Util.getCurrentHypothesis(
      sd.signalDetectionHypotheses
    ).id.id,
    stationName: sd.station.name,
    channelName: getChannelName(sd),
    phase: SignalDetectionUtils.findPhaseFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
        .featureMeasurements
    ).value,
    time: {
      defining: isDefiningTime,
      observed: arrivalTimeValues.obs,
      residual: arrivalTimeValues.res,
      correction: undefined
    },
    slowness: {
      defining: isDefiningSlowness,
      observed: slownessValues.obs,
      residual: slownessValues.res,
      correction: undefined
    },
    azimuth: {
      defining: isDefiningAzimuth,
      observed: azimuthValues.obs,
      residual: azimuthValues.res,
      correction: undefined
    },
    diffs: {
      isAssociatedDiff: undefined,
      slownessDefining: sdDefiningFromTable.slownessDefining,
      arrivalTimeDefining: sdDefiningFromTable.arrivalTimeDefining,
      azimuthDefining: sdDefiningFromTable.azimuthDefining
    },
    rejectedOrUnassociated: undefined
  };
}
/**
 * Creates new snapshots with false for all diffs
 *
 * @snaps Snapshots to add false diffs to
 */
export function generateFalseDiffs(
  snaps: LegacyEventTypes.SignalDetectionSnapshot[]
): SignalDetectionSnapshotWithDiffs[] {
  return snaps.map(snap => ({
    ...snap,
    diffs: {
      isAssociatedDiff: false,
      arrivalTimeDefining: DefiningChange.NO_CHANGE,
      azimuthDefining: DefiningChange.NO_CHANGE,
      slownessDefining: DefiningChange.NO_CHANGE
    },
    rejectedOrUnassociated: false
  }));
}

export function getSnapshotsWithDiffs(
  associatedSdSnapshots: SignalDetectionSnapshotWithDiffs[],
  locationSnapshots: LegacyEventTypes.SignalDetectionSnapshot[]
): SignalDetectionSnapshotWithDiffs[] {
  // loop through the associated
  // ones that arent in location are added to our new master with "associatedDiff" true
  // ones in locationSnaps but no associated are added to master with "associatedDiff" true and rejected/unnassoc true
  const masterSnapshotList: SignalDetectionSnapshotWithDiffs[] = [];
  associatedSdSnapshots.forEach(assocSnap => {
    const maybeSameSD = locationSnapshots.find(
      ls => ls.signalDetectionId === assocSnap.signalDetectionId
    );
    if (maybeSameSD) {
      masterSnapshotList.push({
        ...assocSnap,
        diffs: {
          ...assocSnap.diffs,
          isAssociatedDiff: false,
          arrivalTimeDiff: assocSnap.time.observed !== maybeSameSD.time.observed,
          phaseDiff: assocSnap.phase !== maybeSameSD.phase,
          slownessObsDiff: assocSnap.slowness.observed !== maybeSameSD.slowness.observed,
          azimuthObsDiff: assocSnap.azimuth.observed !== maybeSameSD.azimuth.observed
        },
        rejectedOrUnassociated: false
      });
    } else {
      masterSnapshotList.push({
        ...assocSnap,
        diffs: {
          isAssociatedDiff: true,
          azimuthDefining: DefiningChange.NO_CHANGE,
          slownessDefining: DefiningChange.NO_CHANGE,
          arrivalTimeDefining: DefiningChange.NO_CHANGE
        },
        rejectedOrUnassociated: false
      });
    }
  });
  locationSnapshots.forEach(locationSnap => {
    const maybeSameSD = associatedSdSnapshots.find(
      as => as.signalDetectionId === locationSnap.signalDetectionId
    );
    if (!maybeSameSD) {
      masterSnapshotList.push({
        ...locationSnap,
        diffs: {
          isAssociatedDiff: true,
          azimuthDefining: DefiningChange.NO_CHANGE,
          slownessDefining: DefiningChange.NO_CHANGE,
          arrivalTimeDefining: DefiningChange.NO_CHANGE
        },
        rejectedOrUnassociated: true
      });
    }
  });
  return masterSnapshotList;
}

/**
 * Adds the SignalDetectionTableRowChanges to the State initially as well as when the props change.
 *
 * @param props Current props to build the entries from.
 * @parm sdStates Current list of SignalDetectionTableRowChanges in the State
 *
 * @returns New list of SignalDetectionTableRowChanges to set in the State
 */
export function initializeSDDiffs(
  signalDetections: SignalDetectionTypes.SignalDetection[]
): SignalDetectionTableRowChanges[] {
  return signalDetections.map(sd => ({
    signalDetectionId: sd.id,
    arrivalTimeDefining: DefiningChange.NO_CHANGE,
    slownessDefining: DefiningChange.NO_CHANGE,
    azimuthDefining: DefiningChange.NO_CHANGE
  }));
}

/**
 * @param sdId id of signal detection to look up
 */
export function getDefiningStatusForSdId(
  sdId: string,
  sdDefiningChanges: SignalDetectionTableRowChanges[]
): DefiningStatus {
  const maybeRow = sdDefiningChanges.find(sdr => sdr.signalDetectionId === sdId);
  if (maybeRow) {
    return {
      arrivalTimeDefining: maybeRow.arrivalTimeDefining,
      slownessDefining: maybeRow.slownessDefining,
      azimuthDefining: maybeRow.azimuthDefining
    };
  }
  return {
    arrivalTimeDefining: DefiningChange.NO_CHANGE,
    slownessDefining: DefiningChange.NO_CHANGE,
    azimuthDefining: DefiningChange.NO_CHANGE
  };
}

/**
 * Gets the signal detection snapshots, including diffs, for the latest locatino
 *
 * @param openEvent The currently open event
 * @param selectedLocationSolutionId The id for the selected location solution
 * @param sdDefiningChanges The defining changes from the location sd table
 * @param associatedSignalDetections The signal detections associated to the event
 */
export function generateSnapshotsForLatestLocation(
  openEvent: LegacyEventTypes.Event,
  selectedLocationSolutionId: string,
  sdDefiningChanges: SignalDetectionTableRowChanges[],
  associatedSignalDetections: SignalDetectionTypes.SignalDetection[]
): SignalDetectionSnapshotWithDiffs[] {
  const latestLSS = getLatestLocationSolutionSetLegacy(openEvent);
  const locationSolution =
    // eslint-disable-next-line no-nested-ternary
    selectedLocationSolutionId && latestLSS
      ? latestLSS.locationSolutions.find(ls => ls.id === selectedLocationSolutionId)
        ? latestLSS.locationSolutions.find(ls => ls.id === selectedLocationSolutionId)
        : openEvent.currentEventHypothesis.eventHypothesis.preferredLocationSolution
            .locationSolution
      : undefined;

  const lastCalculatedLSS = getLatestLocationSolutionSetLegacy(openEvent);
  if (!lastCalculatedLSS || !locationSolution) {
    return [];
  }

  const { locationBehaviors } = locationSolution;
  const signalDetectionSnapshots = locationSolution.snapshots;
  const assocSDSnapshots = associatedSignalDetections.map(sd => {
    if (sd) {
      return convertSignalDetectionToSnapshotWithDiffs(
        sd,
        locationBehaviors,
        getDefiningStatusForSdId(sd.id, sdDefiningChanges)
      );
    }
    return undefined;
  });
  const mergedSnapshots = getSnapshotsWithDiffs(assocSDSnapshots, signalDetectionSnapshots);
  return mergedSnapshots;
}
/**
 * Generates snapshots for a previously calculated location
 *
 * @param openEvent The currently open event
 * @param selectedLocationSolutionSetId The id of the location solution set to pull snapshots from
 * @param selectedLocationSolutionId The id of the location solution to pull snapshots from
 */
export function generateSnapshotsForPreviousLocation(
  openEvent: LegacyEventTypes.Event,
  selectedLocationSolutionSetId: string,
  selectedLocationSolutionId: string
): SignalDetectionSnapshotWithDiffs[] {
  const maybeSelectedLSS = openEvent.currentEventHypothesis.eventHypothesis.locationSolutionSets.find(
    lss => lss.id === selectedLocationSolutionSetId
  );
  if (!maybeSelectedLSS) {
    return [];
  }

  const location = maybeSelectedLSS.locationSolutions.find(
    ls => ls.id === selectedLocationSolutionId
  );
  if (!location) {
    return [];
  }
  return generateFalseDiffs(location.snapshots);
}
/**
 * Helper function to get the correct snapshots for the rendering state
 */
export function getSnapshots(
  historicalMode: boolean,
  openEvent: LegacyEventTypes.Event,
  selectedLocationSolutionSetId: string,
  selectedLocationSolutionId: string,
  sdDefiningChanges: SignalDetectionTableRowChanges[],
  associatedSignalDetections: SignalDetectionTypes.SignalDetection[]
): SignalDetectionSnapshotWithDiffs[] {
  if (historicalMode) {
    return generateSnapshotsForPreviousLocation(
      openEvent,
      selectedLocationSolutionSetId,
      selectedLocationSolutionId
    );
  }
  return generateSnapshotsForLatestLocation(
    openEvent,
    selectedLocationSolutionId,
    sdDefiningChanges,
    associatedSignalDetections
  );
}
/**
 * Returns a new location behavior with an updated defining status
 *
 * @param behavior The Location Behavior to update
 * @param definingTableChange The change reported from the location sd table
 * @param behaviorDefiningKey The name of key in the location behavior object to udpate
 */
export function updateLocBehaviorFromTableChanges(
  behavior: LegacyEventTypes.LocationBehavior,
  definingTableChange: SignalDetectionTableRowChanges,
  behaviorDefiningKey: string
): LegacyEventTypes.LocationBehavior {
  if (definingTableChange[behaviorDefiningKey] === undefined) {
    throw new Error('value for behaviorDefiningKey not found in definingTableChange');
  }
  const change = definingTableChange[behaviorDefiningKey];
  const newBehavior = cloneDeep(behavior);
  newBehavior.defining =
    // eslint-disable-next-line no-nested-ternary
    change === DefiningChange.CHANGED_TO_FALSE
      ? false
      : change === DefiningChange.CHANGED_TO_TRUE
      ? true
      : newBehavior.defining;
  return newBehavior;
}
