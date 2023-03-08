import type { CommonTypes, ConfigurationTypes, EventTypes, StationTypes } from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import { Logger } from '@gms/common-util';
import type { AnalystWaveformTypes, EventStatus } from '@gms/ui-state';
import {
  MapLayers,
  useAppSelector,
  useEventStatusQuery,
  useGetEvents,
  useGetSelectedSdIds,
  useUiTheme,
  useUnassociatedSignalDetectionLengthInMeters
} from '@gms/ui-state';
import { decimalToHex } from '@gms/ui-util';
import type Cesium from 'cesium';
import React from 'react';
import type { CesiumMovementEvent } from 'resium';

import {
  isSignalDetectionCompleteAssociated,
  isSignalDetectionOpenAssociated,
  isSignalDetectionOtherAssociated
} from '~analyst-ui/common/utils/event-util';

import { EdgeTypes } from '../events/types';
import {
  createAssociatedSignalDetectionEntities,
  createEventConfidenceEntities,
  createEventCoverageEntities,
  createEventLocationEntities,
  createSiteEntitiesFromStationArray,
  createStationEntitiesFromStationArray,
  createUnassociatedSignalDetectionEntities
} from './create-ian-entities';
import { IanMapDataSource } from './ian-map-data-source';
import { useEventOnClickHandler, useSdOnClickHandler } from './ian-map-hooks';
import { useIANMapEventClickHandlers, useIANMapSDClickHandlers } from './ian-map-utils';
import type { MapEventSource } from './types';

const logger = Logger.create(
  'GMS_LOG_IAN_MAP_DATA_SOURCES',
  process.env.GMS_LOG_IAN_MAP_DATA_SOURCES
);

const splitSignalDetectionsByAssociation = (
  detections: SignalDetectionTypes.SignalDetection[],
  events: EventTypes.Event[],
  openEventId: string,
  eventsStatuses: Record<string, EventStatus>
) => {
  const associatedCompleteDetections: SignalDetectionTypes.SignalDetection[] = [];
  const associatedOpenDetections: SignalDetectionTypes.SignalDetection[] = [];
  const associatedOtherDetections: SignalDetectionTypes.SignalDetection[] = [];
  const unassociatedDetections: SignalDetectionTypes.SignalDetection[] = [];
  if (detections) {
    detections.forEach(signalDetection => {
      // determine if associated to the open event
      if (isSignalDetectionOpenAssociated(signalDetection, events, openEventId)) {
        associatedOpenDetections.push(signalDetection);
      }
      // determine if associated to a complete event
      else if (isSignalDetectionCompleteAssociated(signalDetection, events, eventsStatuses)) {
        associatedCompleteDetections.push(signalDetection);
      }
      // determine if associated to another event
      else if (isSignalDetectionOtherAssociated(signalDetection, events, openEventId)) {
        associatedOtherDetections.push(signalDetection);
      }
      // else it is unassociated
      else {
        unassociatedDetections.push(signalDetection);
      }
    });
  }
  return [
    associatedCompleteDetections,
    associatedOpenDetections,
    associatedOtherDetections,
    unassociatedDetections
  ];
};

const getEventVisibility = (
  preferred: boolean,
  layerVisibility: Record<MapLayers, boolean>,
  edgeEventType: EdgeTypes
): boolean => {
  const preferredVisibility = preferred
    ? layerVisibility[MapLayers.preferredLocationSolution]
    : layerVisibility[MapLayers.nonPreferredLocationSolution];

  let eventVisibility = layerVisibility[MapLayers.events];

  switch (edgeEventType) {
    case EdgeTypes.AFTER:
      eventVisibility = eventVisibility && layerVisibility[MapLayers.edgeEventsAfterInterval];
      break;
    case EdgeTypes.BEFORE:
      eventVisibility = eventVisibility && layerVisibility[MapLayers.edgeEventsBeforeInterval];
      break;
    default:
  }

  return preferredVisibility && eventVisibility;
};

/**
 * Takes a nested array of signal detections split by association, checks their current hypothesis's time range against
 * the time range of the open interval, filters the nested array by comparing the two time ranges based on param
 * specifying before, after, or within the interval
 *
 * @param splitDetectionsArr
 * @param edgeSDType
 * @param intervalTimeRange
 *
 */
const filterSignalDetectionsByEdgeType = (
  splitDetectionsArr: SignalDetectionTypes.SignalDetection[][],
  edgeSDType: EdgeTypes,
  intervalTimeRange: CommonTypes.TimeRange
): SignalDetectionTypes.SignalDetection[][] => {
  const compareTimeRanges = (a, b) => {
    let timeRangeCondition;
    if (edgeSDType === EdgeTypes.BEFORE) {
      timeRangeCondition = a.startTimeSecs < b.startTimeSecs;
    } else if (edgeSDType === EdgeTypes.AFTER) {
      timeRangeCondition = a.startTimeSecs > b.endTimeSecs;
    } else {
      timeRangeCondition = a.startTimeSecs >= b.startTimeSecs && a.startTimeSecs <= b.endTimeSecs;
    }
    return timeRangeCondition;
  };

  return splitDetectionsArr.map(detectionArr =>
    detectionArr.filter(detection => {
      const currentHypo = SignalDetectionTypes.Util.getCurrentHypothesis(
        detection.signalDetectionHypotheses
      );
      const detectionTimeRange: CommonTypes.TimeRange = {
        startTimeSecs: currentHypo.featureMeasurements[0]?.measuredChannelSegment.id.startTime,
        endTimeSecs: currentHypo.featureMeasurements[0]?.measuredChannelSegment.id.endTime
      };
      return compareTimeRanges(detectionTimeRange, intervalTimeRange);
    })
  );
};

const generateSignalDetectionSourceProps = (
  edgeSDType: EdgeTypes,
  layerVisibility: Record<MapLayers, boolean>,
  uiTheme: ConfigurationTypes.UITheme
) => {
  let edgeShowCondition = layerVisibility[MapLayers.signalDetections];
  if (edgeSDType === EdgeTypes.BEFORE) {
    edgeShowCondition =
      layerVisibility[MapLayers.signalDetections] && layerVisibility[MapLayers.edgeDetectionBefore];
  } else if (edgeSDType === EdgeTypes.AFTER) {
    edgeShowCondition =
      layerVisibility[MapLayers.signalDetections] && layerVisibility[MapLayers.edgeDetectionAfter];
  }

  // fallback in case edge SD opacity is missing from theme
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  const edgeSDOpacityFallback = 0.2;

  const sdOpacity =
    edgeSDType === EdgeTypes.INTERVAL
      ? decimalToHex(1.0)
      : decimalToHex(uiTheme.display.edgeSDOpacity ?? edgeSDOpacityFallback);

  return {
    edgeShowCondition,
    sdKeyPrefix: edgeSDType.charAt(0),
    sdOpacity
  };
};

/**
 * Custom hook to split up signal detections and build toggle sources
 *
 * @param signalDetections
 * @param signalDetectionMount
 * @param layerVisibility
 * @param stationsResult
 * @returns array of jsx elements containing cesium data sources
 */
export const useMapSignalDetectionSources = (
  signalDetections: SignalDetectionTypes.SignalDetection[],
  signalDetectionMount: () => void,
  layerVisibility: Record<MapLayers, boolean>,
  stationsResult: StationTypes.Station[],
  edgeSDType: EdgeTypes
): JSX.Element[] => {
  const [uiTheme] = useUiTheme();

  const eventResults = useGetEvents();
  const eventStatusQuery = useEventStatusQuery();
  const openEventId = useAppSelector(state => state.app.analyst.openEventId);
  const timeRange = useAppSelector(state => state.app.workflow.timeRange);
  const unassociatedVisibility = layerVisibility[MapLayers.unassociatedDetection];
  const completeVisibility = layerVisibility[MapLayers.associatedCompleteDetection];
  const otherVisibility = layerVisibility[MapLayers.associatedOtherDetection];
  const openVisibility = layerVisibility[MapLayers.associatedOpenDetection];
  const [sdRightClickHandler] = useIANMapSDClickHandlers();
  const sdOnClickHandler = useSdOnClickHandler();

  const unassociatedSignalDetectionLengthMeters = useUnassociatedSignalDetectionLengthInMeters();

  const selectedSdIds = useGetSelectedSdIds();

  const { sdKeyPrefix, edgeShowCondition, sdOpacity } = generateSignalDetectionSourceProps(
    edgeSDType,
    layerVisibility,
    uiTheme
  );

  const [
    associatedCompleteDetections,
    associatedOpenDetections,
    associatedOtherDetections,
    unassociatedDetections
  ] = React.useMemo(() => {
    logger.debug('splitting detections');
    const splitDetectionsArr = splitSignalDetectionsByAssociation(
      signalDetections,
      eventResults.data,
      openEventId,
      eventStatusQuery.data
    );

    return filterSignalDetectionsByEdgeType(splitDetectionsArr, edgeSDType, timeRange);
  }, [
    edgeSDType,
    eventResults.data,
    eventStatusQuery.data,
    openEventId,
    signalDetections,
    timeRange
  ]);

  const unassociatedSignalDetectionDataSource = React.useMemo(() => {
    logger.debug(`building ${edgeSDType} unassociated detections`);
    const unassociatedSignalDetectionEntities: Cesium.Entity[] = createUnassociatedSignalDetectionEntities(
      unassociatedDetections,
      stationsResult,
      `${uiTheme.colors.unassociatedSDColor}${sdOpacity}`,
      unassociatedSignalDetectionLengthMeters,
      selectedSdIds,
      edgeSDType
    );
    return (
      <IanMapDataSource
        key={`${sdKeyPrefix}USDs`}
        entities={unassociatedSignalDetectionEntities}
        leftClickHandler={sdOnClickHandler}
        rightClickHandler={sdRightClickHandler}
        name="UnassociatedSignalDetections"
        onMount={signalDetectionMount}
        show={edgeShowCondition && unassociatedVisibility}
      />
    );
  }, [
    edgeSDType,
    edgeShowCondition,
    sdKeyPrefix,
    sdOnClickHandler,
    sdOpacity,
    sdRightClickHandler,
    selectedSdIds,
    signalDetectionMount,
    stationsResult,
    uiTheme.colors.unassociatedSDColor,
    unassociatedDetections,
    unassociatedSignalDetectionLengthMeters,
    unassociatedVisibility
  ]);

  const associatedCompleteSignalDetectionDataSource = React.useMemo(() => {
    logger.debug(`building ${edgeSDType} complete detections`);
    const associatedCompleteSignalDetectionEntities: Cesium.Entity[] = createAssociatedSignalDetectionEntities(
      associatedCompleteDetections,
      stationsResult,
      `${uiTheme.colors.completeEventSDColor}${sdOpacity}`,
      eventResults.data,
      selectedSdIds,
      'Associated to completed event',
      edgeSDType
    );
    return (
      <IanMapDataSource
        key={`${sdKeyPrefix}ACSDs`}
        entities={associatedCompleteSignalDetectionEntities}
        leftClickHandler={sdOnClickHandler}
        rightClickHandler={sdRightClickHandler}
        name="AssociatedCompleteSignalDetections"
        onMount={signalDetectionMount}
        show={edgeShowCondition && completeVisibility}
      />
    );
  }, [
    edgeSDType,
    associatedCompleteDetections,
    stationsResult,
    uiTheme.colors.completeEventSDColor,
    sdOpacity,
    eventResults.data,
    selectedSdIds,
    sdKeyPrefix,
    sdOnClickHandler,
    sdRightClickHandler,
    signalDetectionMount,
    edgeShowCondition,
    completeVisibility
  ]);
  const associatedOpenSignalDetectionDataSource = React.useMemo(() => {
    logger.debug(`building ${edgeSDType} open detections`);
    const associatedOpenSignalDetectionEntities: Cesium.Entity[] = createAssociatedSignalDetectionEntities(
      associatedOpenDetections,
      stationsResult,
      `${uiTheme.colors.openEventSDColor}${sdOpacity}`,
      eventResults.data,
      selectedSdIds,
      'Associated to open event',
      edgeSDType
    );

    return (
      <IanMapDataSource
        key={`${sdKeyPrefix}AOpSDs`}
        entities={associatedOpenSignalDetectionEntities}
        leftClickHandler={sdOnClickHandler}
        rightClickHandler={sdRightClickHandler}
        name="AssociatedOpenSignalDetections"
        onMount={signalDetectionMount}
        show={edgeShowCondition && openVisibility}
      />
    );
  }, [
    associatedOpenDetections,
    edgeSDType,
    edgeShowCondition,
    eventResults.data,
    openVisibility,
    sdKeyPrefix,
    sdOnClickHandler,
    sdOpacity,
    sdRightClickHandler,
    selectedSdIds,
    signalDetectionMount,
    stationsResult,
    uiTheme.colors.openEventSDColor
  ]);

  const associatedOtherSignalDetectionDataSource = React.useMemo(() => {
    logger.debug(`building ${edgeSDType} other detections`);
    const associatedOtherSignalDetectionEntities: Cesium.Entity[] = createAssociatedSignalDetectionEntities(
      associatedOtherDetections,
      stationsResult,
      `${uiTheme.colors.otherEventSDColor}${sdOpacity}`,
      eventResults.data,
      selectedSdIds,
      'Associated to other event',
      edgeSDType
    );
    return (
      <IanMapDataSource
        key={`${sdKeyPrefix}AOtSDs`}
        entities={associatedOtherSignalDetectionEntities}
        leftClickHandler={sdOnClickHandler}
        rightClickHandler={sdRightClickHandler}
        name="AssociatedOtherSignalDetections"
        onMount={signalDetectionMount}
        show={edgeShowCondition && otherVisibility}
      />
    );
  }, [
    associatedOtherDetections,
    edgeSDType,
    edgeShowCondition,
    eventResults.data,
    otherVisibility,
    sdKeyPrefix,
    sdOnClickHandler,
    sdOpacity,
    sdRightClickHandler,
    selectedSdIds,
    signalDetectionMount,
    stationsResult,
    uiTheme.colors.otherEventSDColor
  ]);

  return [
    unassociatedSignalDetectionDataSource,
    associatedCompleteSignalDetectionDataSource,
    associatedOpenSignalDetectionDataSource,
    associatedOtherSignalDetectionDataSource
  ];
};

const generateLocationDataSourceID = (preferred: boolean, edgeEventType: EdgeTypes) => {
  return (preferred ? 'Preferred' : 'NonPreferred').concat(edgeEventType).concat('EventLocations');
};

const useMapEventLocationSource = (
  layerVisibility: Record<MapLayers, boolean>,
  eventMount: () => void,
  preferred: boolean,
  eventData: MapEventSource[],
  edgeEventType: EdgeTypes,
  setEventId: (eventId: string) => void
): JSX.Element => {
  const [uiTheme] = useUiTheme();

  const selectedEvents = useAppSelector(state => state.app.analyst.selectedEventIds);

  const [eventRightClickHandler, eventDoubleClickHandler] = useIANMapEventClickHandlers(setEventId);

  const eventOnClickHandler = useEventOnClickHandler();

  const visibility = getEventVisibility(preferred, layerVisibility, edgeEventType);

  return React.useMemo(() => {
    logger.debug(
      `building ${edgeEventType} ${preferred ? '' : 'non-'}preferred event location solutions`
    );
    const eventLocationEntities: Cesium.Entity[] = createEventLocationEntities(
      eventData,
      uiTheme,
      preferred,
      edgeEventType,
      selectedEvents
    );

    return (
      <IanMapDataSource
        key={generateLocationDataSourceID(preferred, edgeEventType)}
        entities={eventLocationEntities}
        leftClickHandler={eventOnClickHandler}
        rightClickHandler={eventRightClickHandler}
        doubleClickHandler={eventDoubleClickHandler}
        name="Events"
        onMount={eventMount}
        show={visibility}
      />
    );
  }, [
    eventData,
    uiTheme,
    preferred,
    edgeEventType,
    selectedEvents,
    eventOnClickHandler,
    eventRightClickHandler,
    eventDoubleClickHandler,
    eventMount,
    visibility
  ]);
};

/**
 * Build the data sources for event locations
 *
 * @param layerVisibility Layer visibility for hte map display
 * @param preferredEventMount On mount function for preferred solutions
 * @param preferredEventsResult Preferred location data
 * @param nonPreferredEventMount On mount function for non-preferred solutions
 * @param nonPreferredEventsResult Non-preferred location data
 * @returns array of jsx elements containing IanMapDataSources
 */
export const useMapEventLocationSources = (
  layerVisibility: Record<MapLayers, boolean>,
  preferredEventMount: () => void,
  preferredEventsResult: MapEventSource[],
  nonPreferredEventMount: () => void,
  nonPreferredEventsResult: MapEventSource[],
  setEventId: (eventId: string) => void
): JSX.Element[] => {
  const nonPreferredAfterEventLocationDataSource = useMapEventLocationSource(
    layerVisibility,
    nonPreferredEventMount,
    false,
    nonPreferredEventsResult,
    EdgeTypes.AFTER,
    setEventId
  );

  const nonPreferredIntervalEventLocationDataSource = useMapEventLocationSource(
    layerVisibility,
    nonPreferredEventMount,
    false,
    nonPreferredEventsResult,
    EdgeTypes.INTERVAL,
    setEventId
  );

  const nonPreferredBeforeEventLocationDataSource = useMapEventLocationSource(
    layerVisibility,
    nonPreferredEventMount,
    false,
    nonPreferredEventsResult,
    EdgeTypes.BEFORE,
    setEventId
  );

  const preferredAfterEventLocationDataSource = useMapEventLocationSource(
    layerVisibility,
    preferredEventMount,
    true,
    preferredEventsResult,
    EdgeTypes.AFTER,
    setEventId
  );

  const preferredIntervalEventLocationDataSource = useMapEventLocationSource(
    layerVisibility,
    preferredEventMount,
    true,
    preferredEventsResult,
    EdgeTypes.INTERVAL,
    setEventId
  );

  const preferredBeforeEventLocationDataSource = useMapEventLocationSource(
    layerVisibility,
    preferredEventMount,
    true,
    preferredEventsResult,
    EdgeTypes.BEFORE,
    setEventId
  );
  return [
    nonPreferredAfterEventLocationDataSource,
    nonPreferredBeforeEventLocationDataSource,
    nonPreferredIntervalEventLocationDataSource,
    preferredAfterEventLocationDataSource,
    preferredBeforeEventLocationDataSource,
    preferredIntervalEventLocationDataSource
  ];
};

const generateCoverageEllipseDataSourceID = (preferred: boolean, edgeEventType: EdgeTypes) => {
  return (preferred ? 'Preferred' : 'NonPreferred')
    .concat(edgeEventType)
    .concat('EventCoverageEllipse');
};

const useMapEventCoverageEllipseSource = (
  layerVisibility: Record<MapLayers, boolean>,
  preferred: boolean,
  eventData: MapEventSource[],
  edgeEventType: EdgeTypes,
  setEventId: (eventId: string) => void
): JSX.Element => {
  const [uiTheme] = useUiTheme();

  const [eventRightClickHandler, eventDoubleClickHandler] = useIANMapEventClickHandlers(setEventId);

  const visibility =
    getEventVisibility(preferred, layerVisibility, edgeEventType) &&
    layerVisibility[MapLayers.coverageEllipse];

  return React.useMemo(() => {
    logger.debug(`building ${edgeEventType} coverage ellipses`);
    const coverageEntity: Cesium.Entity[] = createEventCoverageEntities(
      eventData,
      uiTheme,
      edgeEventType
    );

    return (
      <IanMapDataSource
        key={generateCoverageEllipseDataSourceID(preferred, edgeEventType)}
        entities={coverageEntity}
        rightClickHandler={eventRightClickHandler}
        doubleClickHandler={eventDoubleClickHandler}
        name="Event Coverage Ellipses"
        show={visibility}
      />
    );
  }, [
    eventData,
    uiTheme,
    preferred,
    edgeEventType,
    eventRightClickHandler,
    eventDoubleClickHandler,
    visibility
  ]);
};

/**
 * Build the data sources for coverage ellipses
 *
 * @param layerVisibility Layer visibility for hte map display
 * @param preferredEventsResult Preferred location data
 * @param nonPreferredEventsResult Non-preferred location data
 * @returns array of jsx elements containing IanMapDataSources
 */
export const useMapEventCoverageEllipseSources = (
  layerVisibility: Record<MapLayers, boolean>,
  preferredEventsResult: MapEventSource[],
  nonPreferredEventsResult: MapEventSource[],
  setEventId: (eventId: string) => void
): JSX.Element[] => {
  const preferredBeforeEventCoverageDataSource = useMapEventCoverageEllipseSource(
    layerVisibility,
    true,
    preferredEventsResult,
    EdgeTypes.BEFORE,
    setEventId
  );
  const preferredIntervalEventCoverageDataSource = useMapEventCoverageEllipseSource(
    layerVisibility,
    true,
    preferredEventsResult,
    EdgeTypes.INTERVAL,
    setEventId
  );
  const preferredAfterEventCoverageDataSource = useMapEventCoverageEllipseSource(
    layerVisibility,
    true,
    preferredEventsResult,
    EdgeTypes.AFTER,
    setEventId
  );
  const nonPreferredBeforeEventCoverageDataSource = useMapEventCoverageEllipseSource(
    layerVisibility,
    false,
    nonPreferredEventsResult,
    EdgeTypes.BEFORE,
    setEventId
  );
  const nonPreferredIntervalEventCoverageDataSource = useMapEventCoverageEllipseSource(
    layerVisibility,
    false,
    nonPreferredEventsResult,
    EdgeTypes.INTERVAL,
    setEventId
  );
  const nonPreferredAfterEventCoverageDataSource = useMapEventCoverageEllipseSource(
    layerVisibility,
    false,
    nonPreferredEventsResult,
    EdgeTypes.AFTER,
    setEventId
  );

  return [
    nonPreferredAfterEventCoverageDataSource,
    nonPreferredBeforeEventCoverageDataSource,
    nonPreferredIntervalEventCoverageDataSource,
    preferredAfterEventCoverageDataSource,
    preferredBeforeEventCoverageDataSource,
    preferredIntervalEventCoverageDataSource
  ];
};

const generateConfidenceEllipseDataSourceID = (preferred: boolean, edgeEventType: EdgeTypes) => {
  return (preferred ? 'Preferred' : 'NonPreferred')
    .concat(edgeEventType)
    .concat('EventConfidenceEllipse');
};

/**
 * Build the data source for a confidence ellipse group based on preferred and edge type
 *
 * @param layerVisibility layer visibility for hte map display
 * @param preferred Preferred vs non preferred
 * @param eventData Event row data to build sources from
 * @param edgeEventType Edge type to build sources for
 * @returns IanMapDataSource
 */
export const useMapEventConfidenceEllipseSource = (
  layerVisibility: Record<MapLayers, boolean>,
  preferred: boolean,
  eventData: MapEventSource[],
  edgeEventType: EdgeTypes,
  setEventId: (eventId: string) => void
): JSX.Element => {
  const [uiTheme] = useUiTheme();

  const [eventRightClickHandler, eventDoubleClickHandler] = useIANMapEventClickHandlers(setEventId);

  const visibility =
    getEventVisibility(preferred, layerVisibility, edgeEventType) &&
    layerVisibility[MapLayers.confidenceEllipse];

  return React.useMemo(() => {
    logger.debug(`building ${edgeEventType} confidence ellipses`);
    const confidenceEntity: Cesium.Entity[] = createEventConfidenceEntities(
      eventData,
      uiTheme,
      edgeEventType
    );

    return (
      <IanMapDataSource
        key={generateConfidenceEllipseDataSourceID(preferred, edgeEventType)}
        entities={confidenceEntity}
        rightClickHandler={eventRightClickHandler}
        doubleClickHandler={eventDoubleClickHandler}
        name="Event Confidence Ellipses"
        show={visibility}
      />
    );
  }, [
    eventData,
    uiTheme,
    preferred,
    edgeEventType,
    eventRightClickHandler,
    eventDoubleClickHandler,
    visibility
  ]);
};

/**
 * Build the data sources for confidence ellipses
 *
 * @param layerVisibility Layer visibility for hte map display
 * @param preferredEventsResult Preferred location data
 * @param nonPreferredEventsResult Non-preferred location data
 * @returns array of jsx elements containing IanMapDataSources
 */
export const useMapEventConfidenceEllipseSources = (
  layerVisibility: Record<MapLayers, boolean>,
  preferredEventsResult: MapEventSource[],
  nonPreferredEventsResult: MapEventSource[],
  setEventId: (eventId: string) => void
): JSX.Element[] => {
  const preferredBeforeEventConfidenceDataSource = useMapEventConfidenceEllipseSource(
    layerVisibility,
    true,
    preferredEventsResult,
    EdgeTypes.BEFORE,
    setEventId
  );
  const preferredIntervalEventConfidenceDataSource = useMapEventConfidenceEllipseSource(
    layerVisibility,
    true,
    preferredEventsResult,
    EdgeTypes.INTERVAL,
    setEventId
  );
  const preferredAfterEventConfidenceDataSource = useMapEventConfidenceEllipseSource(
    layerVisibility,
    true,
    preferredEventsResult,
    EdgeTypes.AFTER,
    setEventId
  );
  const nonPreferredBeforeEventConfidenceDataSource = useMapEventConfidenceEllipseSource(
    layerVisibility,
    false,
    nonPreferredEventsResult,
    EdgeTypes.BEFORE,
    setEventId
  );
  const nonPreferredIntervalEventConfidenceDataSource = useMapEventConfidenceEllipseSource(
    layerVisibility,
    false,
    nonPreferredEventsResult,
    EdgeTypes.INTERVAL,
    setEventId
  );
  const nonPreferredAfterEventConfidenceDataSource = useMapEventConfidenceEllipseSource(
    layerVisibility,
    false,
    nonPreferredEventsResult,
    EdgeTypes.AFTER,
    setEventId
  );

  return [
    nonPreferredAfterEventConfidenceDataSource,
    nonPreferredBeforeEventConfidenceDataSource,
    nonPreferredIntervalEventConfidenceDataSource,
    preferredAfterEventConfidenceDataSource,
    preferredBeforeEventConfidenceDataSource,
    preferredIntervalEventConfidenceDataSource
  ];
};
/**
 * Build the data sources for confidence ellipses
 *
 * @param layerVisibility Layer visibility for hte map display
 * @param stationsResult stations data
 * @param stationsVisibility station visibility dictionary
 * @param stationMount on mount function
 * @param onStationClickHandler left click handler for stations and sites
 * @param rightClickHandler right click handler for stations and sites
 * @returns jsx element IanMapDataSource
 */
export const useMapStationSource = (
  layerVisibility: Record<MapLayers, boolean>,
  stationsResult: StationTypes.Station[],
  stationsVisibility: AnalystWaveformTypes.StationVisibilityChangesDictionary,

  stationMount: () => void,
  onStationClickHandler: (targetEntity: Cesium.Entity) => () => void,
  rightClickHandler: (movement: CesiumMovementEvent, target: Cesium.Entity) => void
): JSX.Element => {
  const [uiTheme] = useUiTheme();
  const selectedStations = useAppSelector(state => state.app.common?.selectedStationIds);
  const stationLayerVisibility = layerVisibility[MapLayers.stations];

  return React.useMemo(() => {
    logger.debug(`building stations`);

    const stationEntities: Cesium.Entity[] = createStationEntitiesFromStationArray(
      stationsResult,
      selectedStations,
      stationsVisibility,
      uiTheme
    );

    return (
      <IanMapDataSource
        key="Stations"
        entities={stationEntities}
        leftClickHandler={onStationClickHandler}
        rightClickHandler={rightClickHandler}
        name="Stations"
        onMount={stationMount}
        show={stationLayerVisibility}
      />
    );
  }, [
    stationsResult,
    selectedStations,
    stationsVisibility,
    uiTheme,
    onStationClickHandler,
    rightClickHandler,
    stationMount,
    stationLayerVisibility
  ]);
};

/**
 * Build the data sources for confidence ellipses
 *
 * @param layerVisibility Layer visibility for hte map display
 * @param stationsResult stations data
 * @param onStationClickHandler left click handler for stations and sites
 * @param rightClickHandler right click handler for stations and sites
 * @returns jsx element IanMapDataSource
 */
export const useMapSiteSource = (
  layerVisibility: Record<MapLayers, boolean>,
  stationsResult: StationTypes.Station[],
  onStationClickHandler: (targetEntity: Cesium.Entity) => () => void,
  rightClickHandler: (movement: CesiumMovementEvent, target: Cesium.Entity) => void
): JSX.Element => {
  const siteLayerVisibility = layerVisibility.sites;
  const [uiTheme] = useUiTheme();
  return React.useMemo(() => {
    logger.debug(`building sites`);
    const siteEntities: Cesium.Entity[] = createSiteEntitiesFromStationArray(
      stationsResult,
      uiTheme
    );

    return (
      <IanMapDataSource
        key="Sites"
        entities={siteEntities}
        leftClickHandler={onStationClickHandler}
        rightClickHandler={rightClickHandler}
        name="Sites"
        show={siteLayerVisibility}
      />
    );
  }, [stationsResult, onStationClickHandler, rightClickHandler, siteLayerVisibility, uiTheme]);
};
