import type { ChannelTypes, CommonTypes, LegacyEventTypes, StationTypes } from '@gms/common-model';
import {
  ConfigurationTypes,
  EventTypes,
  SignalDetectionTypes,
  WorkflowTypes
} from '@gms/common-model';
import type { EventLocation } from '@gms/common-model/lib/event';
import type { Location } from '@gms/common-model/lib/station-definitions/channel-definitions/channel-definitions';
import type { EventStatus } from '@gms/ui-state';
import { getDistance, getGreatCircleBearing } from 'geolib';
import flatMap from 'lodash/flatMap';
import memoize from 'nano-memoize';

import { userPreferences } from '~analyst-ui/config/user-preferences';
import { semanticColors } from '~scss-config/color-preferences';

/**
 * Get the current open event
 *
 * @param openEventId Id of open event
 * @param events the events
 */
export function getOpenEvent(
  openEventId: string | undefined,
  events: EventTypes.Event[]
): EventTypes.Event | undefined {
  const event =
    openEventId && events && events.length > 0 ? events.find(e => e.id === openEventId) : undefined;
  return event && event.overallPreferred ? event : undefined;
}

/**
 * Calculate the distance in kilometers between an event and a station.
 *
 * @param source The source location for which to calculate distance
 * @param destination The destination location for which to calculate distance
 * @returns calculated distance in kilometers
 */
export function getLocationToEventDistance(
  source: Location,
  destination: EventLocation
): CommonTypes.Distance {
  const accuracy = 1000;
  const degreePrecision = 1000;
  const KM = 1000;

  const KM_TO_DEGREES = 111.1949266;
  const dist: number = getDistance(
    { latitude: source.latitudeDegrees, longitude: source.longitudeDegrees },
    {
      latitude: destination.latitudeDegrees,
      longitude: destination.longitudeDegrees
    },
    accuracy
  );
  const kmDistance = dist / KM;
  // return distance as degrees and km
  return {
    degrees: Math.round((kmDistance / KM_TO_DEGREES) * degreePrecision) / degreePrecision,
    km: kmDistance
  };
}

export const memoizedLocationToEventDistance = memoize(getLocationToEventDistance);

/**
 * Calculate a location to event location azimuth
 *
 * @param source The source location for which to calculate azimuth
 * @param destination The destination location for which to calculate azimuth
 * @returns calculated distance in kilometers
 */
export function getLocationToLocationAzimuth(source: Location, destination: EventLocation): number {
  const origin = {
    latitude: source.latitudeDegrees,
    longitude: source.longitudeDegrees
  };
  const dest = {
    latitude: destination.latitudeDegrees,
    longitude: destination.longitudeDegrees
  };
  return getGreatCircleBearing(origin, dest);
}

export const memoizedLocationToEventAzimuth = memoize(getLocationToLocationAzimuth);

/**
 * We want display the latest preferredEventHypothesis need to use the stageNames order
 * from the workflowQuery so that if the preferredEventHypothesisByStage is not present, will
 * try and grab the previous stage in the list e.x open 'AL1' but preferredEventHypothesisByStage only has
 * 'Auto Network' so will end up using 'Auto Network'
 *
 * @param preferredEventHypothesisByStage list of stages
 * @param openStageName open stage
 * @returns a preferredEventHypothesis
 */
export function getPreferredHypothesisInStage(
  preferredEventHypothesisByStage: EventTypes.PreferredEventHypothesis[],
  openStageName: string
): EventTypes.PreferredEventHypothesis {
  let preferredEventHypothesis: EventTypes.PreferredEventHypothesis;
  const stageNameIndex = WorkflowTypes.stageNames.indexOf(openStageName);
  for (let i = stageNameIndex; i > -1; i -= 1) {
    preferredEventHypothesis = preferredEventHypothesisByStage.find(
      hypothesis => hypothesis.stage.name === WorkflowTypes.stageNames[i]
    );

    if (preferredEventHypothesis) {
      break;
    }
  }
  return preferredEventHypothesis;
}

/**
 * Return a list of EventHypothesis associated with the detection or an empty list
 *
 * @param detection signal detection
 * @param events Event[]
 * @return EventHypothesis[]
 */
export function findEventHypothesisForDetection(
  detection: SignalDetectionTypes.SignalDetection,
  events: EventTypes.Event[]
): EventTypes.EventHypothesis[] {
  if (
    events &&
    detection?.signalDetectionHypotheses &&
    detection?.signalDetectionHypotheses?.length > 0
  ) {
    // If the event has the detection in its associated signal detection
    return flatMap(flatMap(events.map(evt => evt.eventHypotheses))).filter(eventHypothesis =>
      eventHypothesis.associatedSignalDetectionHypotheses.find(
        assoc =>
          assoc.id.signalDetectionId ===
          SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses).id
            .signalDetectionId
      )
    );
  }
  return [];
}

/**
 * Gets the distance and azimuth to all stations and channels for the given location solution id
 *
 * @param event event with a preferred location solution
 * @param stations array of stations to calculated distances for
 * @param openStageName string name of the open stage in order to find the preferred hypothesis
 */
export function getDistanceToStationsForPreferredLocationSolutionId(
  event: EventTypes.Event,
  stations: StationTypes.Station[],
  openStageName: string,
  signalDetections: SignalDetectionTypes.SignalDetection[],
  allChannels: ChannelTypes.Channel[]
): EventTypes.LocationDistance[] {
  if (!event) {
    return [];
  }
  const distances: EventTypes.LocationDistance[] = [];

  const { preferredEventHypothesisByStage, eventHypotheses } = event;

  const preferredEventHypothesis = getPreferredHypothesisInStage(
    preferredEventHypothesisByStage,
    openStageName
  );
  if (preferredEventHypothesis) {
    const locationSolution = EventTypes.findPreferredLocationSolution(
      preferredEventHypothesis.preferred.id.hypothesisId,
      eventHypotheses
    );
    if (stations) {
      stations.forEach(station => {
        distances.push({
          azimuth: memoizedLocationToEventAzimuth(station.location, locationSolution.location),
          distance: memoizedLocationToEventDistance(station.location, locationSolution.location),
          id: station.name
        });
        if (allChannels) {
          station.allRawChannels.forEach(channel => {
            const fullChannel = allChannels.find(c => channel.name === c.name);
            if (fullChannel !== undefined) {
              distances.push({
                azimuth: memoizedLocationToEventAzimuth(
                  station.location,
                  locationSolution.location
                ),
                distance: memoizedLocationToEventDistance(
                  fullChannel.location,
                  locationSolution.location
                ),
                id: fullChannel.name
              });
            }
          });
        }
      });
    }
  }
  return distances;
}

/**
 * Determine if a signal detection is associated to an event.
 *
 * @param detection signal detection
 * @returns boolean
 */
export function isSignalDetectionOpenAssociated(
  detection: SignalDetectionTypes.SignalDetection,
  events: EventTypes.Event[],
  currentOpenEventId: string
): boolean {
  if (!currentOpenEventId || !events) {
    return false;
  }
  const associatedEventHypo = findEventHypothesisForDetection(detection, events);
  return (
    associatedEventHypo.find(
      assoc => assoc.id.eventId === currentOpenEventId && !assoc.rejected
    ) !== undefined
  );
}

/**
 * Determine if a signal detection is complete.
 *
 * @param detection signal detection
 * @returns boolean
 */
export function isSignalDetectionCompleteAssociated(
  detection: SignalDetectionTypes.SignalDetection,
  events: EventTypes.Event[],
  eventsStatuses: Record<string, EventStatus>
): boolean {
  if (!events) {
    return false;
  }
  const associatedEventIds = findEventHypothesisForDetection(detection, events).map(
    hypo => hypo.id.eventId
  );
  let isComplete = false;
  events.forEach(event => {
    isComplete =
      isComplete ||
      (associatedEventIds.find(id => event.id === id) &&
        eventsStatuses &&
        eventsStatuses[event.id] !== undefined &&
        eventsStatuses[event.id] !== null &&
        eventsStatuses[event.id].eventStatusInfo.eventStatus === EventTypes.EventStatus.COMPLETE);
  });
  return isComplete;
}

/**
 * Search the events if an association is found to an event (but not the currently open event)
 * then return true
 *
 * @param detection signal detection
 * @param events The events in time range
 * @return boolean
 */
export function isSignalDetectionOtherAssociated(
  detection: SignalDetectionTypes.SignalDetection,
  events: EventTypes.Event[],
  currentOpenEventId: string
): boolean {
  // If any of the events has the detection in it's associated signal detection
  if (events) {
    const associatedEventHypo = findEventHypothesisForDetection(detection, events);
    return (
      associatedEventHypo.find(
        assoc => assoc.id.eventId !== currentOpenEventId && !assoc.rejected
      ) !== undefined
    );
  }
  return false;
}

/**
 * Search the events if an association is found to an event (but not the currently open event)
 * then return true
 *
 * @param detection signal detection
 * @param events The events in time range
 * @return boolean
 */
export function isSignalDetectionAssociated(
  detection: SignalDetectionTypes.SignalDetection,
  events: EventTypes.Event[]
): boolean {
  // If any of the events has the detection in it's associated signal detection
  if (events) {
    const associatedEventHypo = findEventHypothesisForDetection(detection, events);
    return associatedEventHypo.length > 0;
  }
  return false;
}

/**
 * Returns a CONST string representing the provided signal detection association status
 *
 * @param signalDetection
 * @param events
 * @param openEventId
 * @param eventsStatuses
 */
export function getSignalDetectionAssociationStatus(
  signalDetection: SignalDetectionTypes.SignalDetection,
  events: EventTypes.Event[],
  openEventId: string,
  eventsStatuses: Record<string, EventStatus>
): string {
  if (isSignalDetectionOpenAssociated(signalDetection, events, openEventId)) {
    return EventTypes.AssociationStatus.OPEN_ASSOCIATED;
  }
  // determine if associated to a complete event
  if (isSignalDetectionCompleteAssociated(signalDetection, events, eventsStatuses)) {
    return EventTypes.AssociationStatus.COMPLETE_ASSOCIATED;
  }
  // determine if associated to another event
  if (isSignalDetectionOtherAssociated(signalDetection, events, openEventId)) {
    return EventTypes.AssociationStatus.OTHER_ASSOCIATED;
  }
  // else it is unassociated
  return EventTypes.AssociationStatus.UNASSOCIATED;
}

/* LegacyEvent Methods
 * Methods below are saved for use in legacy components
 */

/**
 * Get the current open event using the legacy COI to maintain legacy code build
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param openEventId Id of open event
 * @param events the events
 */
export function getLegacyOpenEvent(
  openEventId: string | undefined,
  events: LegacyEventTypes.Event[]
): LegacyEventTypes.Event | undefined {
  const event =
    openEventId && events && events.length > 0 ? events.find(e => e.id === openEventId) : undefined;
  return event && event.currentEventHypothesis && event.currentEventHypothesis.eventHypothesis
    ? event
    : undefined;
}

/**
 * Gets the signal detections associated to the given event
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param event the open even to get sd's for
 * @param signalDetections signalDetections to look through
 */
export function getAssocSdsLegacy(
  event: LegacyEventTypes.Event,
  signalDetections: SignalDetectionTypes.SignalDetection[]
): SignalDetectionTypes.SignalDetection[] {
  return event && event.currentEventHypothesis && event.currentEventHypothesis.eventHypothesis
    ? flatMap(event.currentEventHypothesis.eventHypothesis.signalDetectionAssociations, assocSD => {
        const maybeSD = signalDetections.find(
          sd =>
            assocSD.signalDetectionHypothesis.id ===
              SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).id.id &&
            !assocSD.rejected
        );
        if (maybeSD) {
          return maybeSD;
        }
        return undefined;
      }).filter(assocSD => assocSD !== undefined)
    : [];
}

/**
 * Returns the latest location solution set for the provided event.
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param event an event
 */
export function getLatestLocationSolutionSetLegacy(
  event: LegacyEventTypes.Event
): LegacyEventTypes.LocationSolutionSet | undefined {
  return event && event.currentEventHypothesis && event.currentEventHypothesis.eventHypothesis
    ? // eslint-disable-next-line @typescript-eslint/no-use-before-define
      getLatestLocationSolutionSetForEventHypothesisLegacy(
        event.currentEventHypothesis.eventHypothesis
      )
    : undefined;
}

/**
 * Returns the latest location solution set for the provided event hypothesis.
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param eventHypothesis an event hypothesis
 */
export function getLatestLocationSolutionSetForEventHypothesisLegacy(
  eventHypothesis: LegacyEventTypes.EventHypothesis
): LegacyEventTypes.LocationSolutionSet {
  return eventHypothesis &&
    eventHypothesis.locationSolutionSets &&
    eventHypothesis.locationSolutionSets.length > 0
    ? eventHypothesis.locationSolutionSets.reduce((prev, curr) => {
        if (curr.count > prev.count) {
          return curr;
        }
        return prev;
      }, eventHypothesis.locationSolutionSets[0])
    : undefined;
}

/**
 * Gets the default preferred location id for an event based off the config
 * Or if not found returns the id for the preferred location solution
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param locationSolutionSet Location Solution Set to  get default preferred location solution from
 */
export function getPreferredDefaultLocationIdLegacy(
  locationSolutionSet: LegacyEventTypes.LocationSolutionSet
): string | undefined {
  if (!locationSolutionSet.locationSolutions || locationSolutionSet.locationSolutions.length < 1) {
    return undefined;
  }

  let toReturn: string;
  // A for loop is used so we can break
  // eslint-disable-next-line @typescript-eslint/prefer-for-of
  for (
    let i = 0;
    i < userPreferences.location.preferredLocationSolutionRestraintOrder.length;
    // eslint-disable-next-line no-plusplus
    i++
  ) {
    const dr = userPreferences.location.preferredLocationSolutionRestraintOrder[i];
    const maybeLS = locationSolutionSet.locationSolutions.find(
      ls => ls.locationRestraint.depthRestraintType === dr
    );
    if (maybeLS) {
      toReturn = maybeLS.id;
      break;
    }
  }
  if (toReturn) {
    return toReturn;
  }
  return locationSolutionSet[0].id;
}

/**
 * Gets the default preferred location id for an event based off the config
 * Or if not found returns the id for the preferred location solution
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param eventHypothesis Event hypothesis to get default preferred location solution from
 */
export function getPreferredLocationSolutionIdFromEventHypothesisLegacy(
  eventHypothesis: LegacyEventTypes.EventHypothesis
): string {
  if (!eventHypothesis.locationSolutionSets || eventHypothesis.locationSolutionSets.length < 1) {
    return undefined;
  }
  const set = getLatestLocationSolutionSetForEventHypothesisLegacy(eventHypothesis);
  return getPreferredDefaultLocationIdLegacy(set);
}

/**
 * Gets the distance to all stations for the given location solution id
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param event event with location solution
 * @param locationSolutionId location solution id
 */
export function getDistanceToStationsForLocationSolutionIdLegacy(
  event: LegacyEventTypes.Event,
  locationSolutionId: string
): LegacyEventTypes.LocationToStationDistance[] {
  if (!event) {
    return [];
  }
  const locationSolutions = flatMap(
    event.currentEventHypothesis.eventHypothesis.locationSolutionSets,
    lss => lss.locationSolutions
  );
  const maybeLs = locationSolutions.find(ls => ls.id === locationSolutionId);
  if (maybeLs) {
    return maybeLs.locationToStationDistances;
  }
  return event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.locationSolution
    .locationToStationDistances;
}

/**
 * Checks if event hypothesis has changed or if the number of location solution sets has changed
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param event current props event
 * @param prevEvent previous props event
 *
 * @returns boolean if changes have occurred
 */
export function shouldUpdateSelectedLocationSolutionLegacy(
  prevEvent: LegacyEventTypes.Event,
  event: LegacyEventTypes.Event
): boolean {
  return (
    prevEvent.currentEventHypothesis.eventHypothesis.id !==
      event.currentEventHypothesis.eventHypothesis.id ||
    prevEvent.currentEventHypothesis.eventHypothesis.locationSolutionSets.length !==
      event.currentEventHypothesis.eventHypothesis.locationSolutionSets.length
  );
}

/**
 * Search the associated event hypothesis for one pointing at the open event id,
 * else return first in list or undefined
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param detection signal detection
 * @return EventHypothesis | undefined
 */
export function findEventHypothesisForDetectionLegacy(
  detection: SignalDetectionTypes.SignalDetection,
  events: LegacyEventTypes.Event[]
): LegacyEventTypes.EventHypothesis[] {
  if (events) {
    // If the event has the detection in it's associated signal detection
    return flatMap(events.map(evt => evt.currentEventHypothesis.eventHypothesis)).filter(event =>
      event.signalDetectionAssociations.find(
        assoc =>
          assoc.signalDetectionHypothesis.id ===
          SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses).id.id
      )
    );
  }
  return [];
}

/**
 * Determine if a signal detection is associated to an event.
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param detection signal detection
 * @returns boolean
 */
export function determineIfAssociatedLegacy(
  detection: SignalDetectionTypes.SignalDetection,
  events: LegacyEventTypes.Event[],
  currentOpenEventId: string
): boolean {
  if (!currentOpenEventId || !events) {
    return false;
  }
  const associatedEventHypo = findEventHypothesisForDetectionLegacy(detection, events);
  return (
    associatedEventHypo.find(assoc => assoc.event.id === currentOpenEventId && !assoc.rejected) !==
    undefined
  );
}

/**
 * Determine if a signal detection is complete.
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param detection signal detection
 * @returns boolean
 */
export function determineIfCompleteLegacy(
  detection: SignalDetectionTypes.SignalDetection,
  events: LegacyEventTypes.Event[]
): boolean {
  if (!events) {
    return false;
  }
  const associatedEventHypos = findEventHypothesisForDetectionLegacy(detection, events);
  let isComplete = false;
  associatedEventHypos.forEach(assocEvent => {
    isComplete = isComplete || assocEvent.event.status === 'Complete';
  });
  return isComplete;
}

/**
 * Search the events if no event association is found then the SD is unassociated
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param detection signal detection
 * @param events The events in time range
 * @return boolean
 */
export function isSignalDetectionUnassociatedLegacy(
  detection: SignalDetectionTypes.SignalDetection,
  events: LegacyEventTypes.Event[]
): boolean {
  // If any of the events has the detection in it's associated signal detection
  if (events) {
    // Look through the event's signalDetectionAssociations to find a reference to the SD Hypo
    return (
      events.find(
        event =>
          event.currentEventHypothesis.eventHypothesis.signalDetectionAssociations.find(
            assoc =>
              assoc.signalDetectionHypothesis.id ===
              SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses).id
                .id
          ) !== undefined
      ) === undefined
    );
  }
  return true;
}

/**
 * Search the events if an association is found to an event (but not the currently open event)
 * then return true
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param detection signal detection
 * @param events The events in time range
 * @return boolean
 */
export function isSignalDetectionOtherAssociatedLegacy(
  detection: SignalDetectionTypes.SignalDetection,
  events: LegacyEventTypes.Event[],
  currentOpenEventId: string
): boolean {
  // If any of the events has the detection in it's associated signal detection
  if (events) {
    // Look through the event's signalDetectionAssociations to find a reference to the SD Hypo
    return (
      events.find(
        event =>
          event.id !== currentOpenEventId &&
          event.currentEventHypothesis.eventHypothesis.signalDetectionAssociations.find(
            assoc =>
              assoc.signalDetectionHypothesis.id ===
              SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses).id
                .id
          ) !== undefined
      ) !== undefined
    );
  }
  return false;
}

/**
 * Determine the color for the detection list marker based on the state of the detection.
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param detection signal detection
 */
export function determineDetectionColorLegacy(
  detection: SignalDetectionTypes.SignalDetection,
  events: LegacyEventTypes.Event[],
  currentOpenEventId: string,
  unassociatedSDColor = ConfigurationTypes.defaultColorTheme.unassociatedSDColor
): string {
  let color = unassociatedSDColor;
  if (!detection || !events || events.length === 0 || !currentOpenEventId) {
    return color;
  }
  const associatedEventHypos = findEventHypothesisForDetectionLegacy(detection, events);
  const isComplete = determineIfCompleteLegacy(detection, events);
  const isSelectedEvent = determineIfAssociatedLegacy(detection, events, currentOpenEventId);

  if (isSelectedEvent) {
    color = semanticColors.analystOpenEvent;
  } else if (isComplete) {
    color = semanticColors.analystComplete;
  } else if (associatedEventHypos.length > 0) {
    color = semanticColors.analystToWork;
  }

  return color;
}

/**
 * Returns true if te provided signal detection hypothesis is associated
 * to the provided event; false otherwise.
 *
 * @deprecated: This needs to be redone with the new COI but is currently used in legacy components
 *
 * @param sdHypothesis the signal detection hypothesis to check
 * @param event the event to check association status
 * @returns true if the signal detection hypothesis is associated
 * to the provided event; false otherwise.
 */
export function isAssociatedToCurrentEventHypothesisLegacy(
  sdHypothesis: SignalDetectionTypes.SignalDetectionHypothesis,
  event: LegacyEventTypes.Event
): boolean {
  if (!event) {
    return false;
  }
  let isAssociated = false;
  event.currentEventHypothesis.eventHypothesis.signalDetectionAssociations.forEach(sdAssoc => {
    if (sdAssoc.signalDetectionHypothesis.id === sdHypothesis.id.id && !sdAssoc.rejected) {
      isAssociated = true;
    }
  });
  return isAssociated;
}
