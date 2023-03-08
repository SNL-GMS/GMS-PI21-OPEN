import type { ChannelTypes, SignalDetectionTypes, StationTypes } from '@gms/common-model';
import { ConfigurationTypes, EventTypes } from '@gms/common-model';
import type { AnalystWaveformTypes } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import {
  Cartesian3,
  Cartographic,
  Color,
  ColorMaterialProperty,
  ConstantPositionProperty,
  EllipseOutlineGeometry,
  Entity,
  Math as CesiumMath,
  PolylineDashMaterialProperty
} from 'cesium';

import {
  findEventHypothesisForDetection,
  getLocationToEventDistance
} from '~analyst-ui/common/utils/event-util';
import {
  createBillboard,
  createLabel,
  createPolyline,
  getSDPolylineMaterial
  // eslint-disable-next-line import/namespace
} from '~analyst-ui/components/map/cesium-map-utils';
import { buildStationTriangle } from '~analyst-ui/components/map/img/station-triangle';
import {
  alwaysDisplayDistanceDisplayCondition,
  BILLBOARD_HEIGHT_SELECTED,
  BILLBOARD_HEIGHT_UNSELECTED_CHANNEL,
  BILLBOARD_HEIGHT_UNSELECTED_STATION,
  channelGroupDistanceDisplayCondition,
  confidenceEllipseDisplayCondition,
  confidenceEllipseWidthPx,
  coverageEllipseDisplayCondition,
  coverageEllipseWidthPx,
  eventDistanceDisplayCondition,
  eventEyeOffset,
  lineDistanceDisplayCondition,
  stationDistanceDisplayCondition
} from '~common-ui/components/map/constants';
import { gmsColors } from '~scss-config/color-preferences';

import { EdgeTypes } from '../events/types';
import {
  createCartesianFromLocationZeroElevation,
  destGivenBearingStartDistance,
  formatNumberForTooltipDisplay,
  getSignalDetectionEntityProps,
  getStationLocation,
  stationTypeToFriendlyNameMap
} from './ian-map-utils';
import { buildEventCircle } from './img/event-circle';
import { buildSelectedEventCircle } from './img/selected-event-circle';
import type { MapEventSource, MapSDConditions, MapSDEntityValues } from './types';

const logger = UILogger.create('CREATE_IAN_ENTITIES', process.env.GMS_LOG_TOOLBAR);

/**
 * Returns the configured color for a station or site on the map from the current theme as a Color. If the current theme
 * does not have a value configured for mapStationDefault or mapVisibleStation, uses the value from the default theme
 *
 * @param isVisibleStation sites are not visible stations, pass false for this if getting site color
 * @param uiTheme current UITheme
 */
export function getStationOrSiteColor(
  isVisibleStation: boolean,
  uiTheme: ConfigurationTypes.UITheme
): Color {
  const defaultColorString =
    uiTheme?.colors?.mapStationDefault ?? ConfigurationTypes.defaultColorTheme.mapStationDefault;
  const visibleColorString =
    uiTheme?.colors?.mapVisibleStation ?? ConfigurationTypes.defaultColorTheme.mapVisibleStation;

  return Color.fromCssColorString(isVisibleStation ? visibleColorString : defaultColorString);
}

/**
 * Given a station, return a Cesium map Entity containing a label and a billboard (icon)
 *
 * @param station
 * @param selectedStations
 * @param stationVisibility
 * @param uiTheme for determining Station billboard color
 */
export function createMapEntityFromStation(
  station: StationTypes.Station,
  selectedStations: string[],
  stationVisibility: boolean,
  uiTheme: ConfigurationTypes.UITheme
): Entity {
  const isSelected = selectedStations?.indexOf(station.name) > -1;
  const entityProperties = {
    name: station.name,
    type: 'Station',
    selected: isSelected,
    coordinates: {
      longitude: formatNumberForTooltipDisplay(station.location.longitudeDegrees),
      latitude: formatNumberForTooltipDisplay(station.location.latitudeDegrees),
      elevation: formatNumberForTooltipDisplay(station.location.elevationKm)
    },
    statype: stationTypeToFriendlyNameMap.get(station.type)
  };

  const eyeOffSet = isSelected
    ? new ConstantPositionProperty(new Cartesian3(0.0, 0.0, BILLBOARD_HEIGHT_SELECTED))
    : new ConstantPositionProperty(new Cartesian3(0.0, 0.0, BILLBOARD_HEIGHT_UNSELECTED_STATION));

  const color = getStationOrSiteColor(stationVisibility, uiTheme);
  const entityOptions: Entity.ConstructorOptions = {
    id: station.name,
    name: station.name,
    show: true,
    label: createLabel(station, stationDistanceDisplayCondition, isSelected),
    billboard: createBillboard(isSelected, eyeOffSet, color, buildStationTriangle()),
    properties: entityProperties,
    position: createCartesianFromLocationZeroElevation(station.location)
  };
  return new Entity(entityOptions);
}

/**
 * Given a channelGroup, return a Cesium map Entity containing a billboard that represents the channelgroup location,
 * and a polyline that connects it back to the station.
 *
 * @param channelGroup
 * @param stationPosition
 * @param uiTheme For Site billboard color
 */
export function createMapEntityFromChannelGroup(
  channelGroup: ChannelTypes.ChannelGroup,
  stationPosition: Cartesian3,
  uiTheme: ConfigurationTypes.UITheme
): Entity {
  const isSelected = false; // cannot select channelGroups
  const entityProperties = {
    name: channelGroup.name,
    type: 'ChannelGroup',
    selected: isSelected,
    coordinates: {
      longitude: formatNumberForTooltipDisplay(channelGroup.location.longitudeDegrees),
      latitude: formatNumberForTooltipDisplay(channelGroup.location.latitudeDegrees),
      elevation: formatNumberForTooltipDisplay(channelGroup.location.elevationKm)
    }
  };
  // elevation is set to zero to prevent bug where polylines don't appear on MapMode2D.ROTATE
  const channelGroupPosition = createCartesianFromLocationZeroElevation(channelGroup.location);
  const eyeOffSet = isSelected
    ? new ConstantPositionProperty(new Cartesian3(0.0, 0.0, BILLBOARD_HEIGHT_SELECTED))
    : new ConstantPositionProperty(new Cartesian3(0.0, 0.0, BILLBOARD_HEIGHT_UNSELECTED_CHANNEL));
  const entityOptions: Entity.ConstructorOptions = {
    id: channelGroup.name,
    name: channelGroup.name,
    show: true,
    label: createLabel(channelGroup, channelGroupDistanceDisplayCondition, isSelected),
    billboard: createBillboard(
      isSelected,
      eyeOffSet,
      getStationOrSiteColor(false, uiTheme),
      buildStationTriangle()
    ),
    properties: entityProperties,
    position: channelGroupPosition,
    polyline: createPolyline(
      [channelGroupPosition, stationPosition],
      lineDistanceDisplayCondition,
      new ColorMaterialProperty(Color.fromCssColorString(gmsColors.gmsBackground)),
      1
    )
  };
  return new Entity(entityOptions);
}

/**
 * Given a station, return an array of Cesium Entities containing data for each of the station's uniquely
 * located (by lat, long) channel groups
 *
 * @param station - Station containing the sites to be converted into entities
 * @param uiTheme - For Station/site color configuration
 */
export function processChannelGroups(
  station: StationTypes.Station,
  uiTheme: ConfigurationTypes.UITheme
): Entity[] {
  if (!station.channelGroups || station.channelGroups.length === 0) return [];

  const stationLocation = createCartesianFromLocationZeroElevation(station.location);
  return station.channelGroups.map(channelGroup =>
    createMapEntityFromChannelGroup(channelGroup, stationLocation, uiTheme)
  );
}

/**
 * Given two points and a signal detection ID
 * Return an entity with a polyline representing a great circle path
 *
 * @param signalDetectionId
 * @param stationLocation
 * @param sourceLocation
 * @param SDColor color for the polyline as a CSS color string
 * @param isSelected affects highlighting and appearance of the resulting entity's polyline
 */
export function createCirclePathEntity(
  signalDetectionId: string,
  stationLocation: Cartesian3,
  sourceLocation: Cartesian3,
  SDColor: string,
  isSelected: boolean,
  sdEntityProps: MapSDEntityValues
): Entity {
  const entityProperties = {
    id: signalDetectionId,
    type: 'Signal detection',
    ...sdEntityProps
  };

  const entityOptions: Entity.ConstructorOptions = {
    id: signalDetectionId,
    name: signalDetectionId,
    show: true,
    position: stationLocation,
    polyline: createPolyline(
      [stationLocation, sourceLocation],
      alwaysDisplayDistanceDisplayCondition,
      getSDPolylineMaterial(
        isSelected,
        new ColorMaterialProperty(Color.fromCssColorString(SDColor))
      ),
      1,
      isSelected
    ),
    properties: entityProperties
  };

  return new Entity(entityOptions);
}

/**
 * Given a signal detection
 * Return a cesium entity with a great circle path polyline
 *
 * @param signalDetection
 * @param stations
 * @param signalDetectionColor
 * @param signalDetectionLengthMeters
 * @param isSelected affects the styling of the resulting polyline if true
 * @param sdConditions open/complete/other status and association
 * @param associatedEventTime
 */
export function processSignalDetection(
  signalDetection: SignalDetectionTypes.SignalDetection,
  stations: StationTypes.Station[],
  signalDetectionColor: string,
  signalDetectionLengthMeters: number,
  isSelected: boolean,
  sdConditions: MapSDConditions,
  associatedEventTime?: number
): Entity {
  // get most recent hypothesis
  if (
    !signalDetection?.signalDetectionHypotheses ||
    signalDetection.signalDetectionHypotheses.length === 0
  ) {
    return null;
  }

  // determine station location
  const stationLocation = getStationLocation(signalDetection.station.name, stations);
  if (!stationLocation) return null;

  const sdEntityProps = associatedEventTime
    ? getSignalDetectionEntityProps(
        signalDetection,
        signalDetectionColor,
        sdConditions.status,
        sdConditions.edgeSDType,
        associatedEventTime
      )
    : getSignalDetectionEntityProps(
        signalDetection,
        signalDetectionColor,
        sdConditions.status,
        sdConditions.edgeSDType
      );

  // calculate source location
  const sourceLocation = Cartographic.toCartesian(
    destGivenBearingStartDistance(
      sdEntityProps.azimuth.azimuthValue,
      signalDetectionLengthMeters,
      stationLocation.latitudeDegrees,
      stationLocation.longitudeDegrees
    )
  );
  const stationCartesian = createCartesianFromLocationZeroElevation(stationLocation);
  // create entity containing polyline
  return createCirclePathEntity(
    signalDetection.id,
    stationCartesian,
    sourceLocation,
    signalDetectionColor,
    isSelected,
    sdEntityProps
  );
}

/**
 * Given an array of Stations, parses through the array to create a Cesium Entity array for each Station
 * with proper labels, icons, and distanceDisplayConditions
 *
 * @param stations
 * @param selectedStations
 * @param stationsVisibility
 * @param uiTheme for determining Station billboard color
 */
export function createStationEntitiesFromStationArray(
  stations: StationTypes.Station[],
  selectedStations: string[],
  stationsVisibility: AnalystWaveformTypes.StationVisibilityChangesDictionary,
  uiTheme: ConfigurationTypes.UITheme
): Entity[] {
  const entities: Entity[] = [];

  if (!stations || !stations.length) {
    return entities;
  }

  stations.forEach(station => {
    entities.push(
      createMapEntityFromStation(
        station,
        selectedStations,
        stationsVisibility ? stationsVisibility[station.name]?.visibility : undefined,
        uiTheme
      )
    );
  });
  return entities;
}

/**
 * Given an array of Stations, parses through the array to create a Cesium Entity array for each
 * uniquely located (by long, lat) ChannelGroup with proper labels, icons, and distanceDisplayConditions
 *
 * @param stations containing the Sites/ChannelGroups that we want to create entities for
 * @param uiTheme for determining Site Billboard Color
 */
export function createSiteEntitiesFromStationArray(
  stations: StationTypes.Station[],
  uiTheme: ConfigurationTypes.UITheme
): Entity[] {
  const entities: Entity[] = [];

  if (!!stations && stations.length > 0) {
    stations.forEach(station => {
      processChannelGroups(station, uiTheme).forEach(entity => entities.push(entity));
    });
  }

  return entities;
}

/**
 * Given an array of signal detections
 * Return an array of Cesium entities containing polylines that represent great circle paths
 *
 * @param signalDetections
 * @param stations
 * @param signalDetectionColor
 * @param signalDetectionLengthMeters
 * @param selectedSdIds string[] containing the ids of currently selected SDs from the application redux store
 * @param edgeSDType
 */
export function createUnassociatedSignalDetectionEntities(
  signalDetections: SignalDetectionTypes.SignalDetection[],
  stations: StationTypes.Station[],
  signalDetectionColor: string,
  signalDetectionLengthMeters: number,
  selectedSdIds: string[],
  edgeSDType: EdgeTypes
): Entity[] {
  const entities: Entity[] = [];
  const sdConditions: MapSDConditions = {
    status: 'Unassociated',
    edgeSDType
  };

  if (!!signalDetections && signalDetections.length > 0) {
    signalDetections.forEach(detection => {
      const entity = processSignalDetection(
        detection,
        stations,
        signalDetectionColor,
        signalDetectionLengthMeters,
        !!selectedSdIds?.includes(detection.id),
        sdConditions
      );
      if (entity) entities.push(entity);
    });
  }

  return entities;
}

/**
 * Given an array of signal detections
 * Return an array of Cesium entities containing polylines that represent great circle paths
 *
 * @param signalDetections
 * @param stations
 * @param signalDetectionColor
 * @param events list of events to calculate station to event distance with
 * @param selectedSdIds String array of selected SD IDs
 * @param edgeSDType
 * @param status
 */
export function createAssociatedSignalDetectionEntities(
  signalDetections: SignalDetectionTypes.SignalDetection[],
  stations: StationTypes.Station[],
  signalDetectionColor: string,
  events: EventTypes.Event[],
  selectedSdIds: string[],
  status: string,
  edgeSDType: EdgeTypes
): Entity[] {
  const entities: Entity[] = [];
  const sdConditions = {
    status,
    edgeSDType
  };

  if (!!signalDetections && signalDetections.length > 0) {
    signalDetections.forEach(detection => {
      const eventHypothesis: EventTypes.EventHypothesis = findEventHypothesisForDetection(
        detection,
        events
      )[0];
      if (!eventHypothesis) {
        // shouldn't ever happen but if it does log an error and move on instead of blowing up
        logger.error(
          `No associated eventHypothesis found for provided signal detection id: ${detection.id}`
        );
        return;
      }
      const locationSolution = eventHypothesis.locationSolutions.find(
        ls => ls.id === eventHypothesis.preferredLocationSolution.id
      );

      const sourceToEventDistance = getLocationToEventDistance(
        stations.find(station => station.name === detection.station.name).location,
        locationSolution.location
      );
      const associatedEventTime = locationSolution.location.time;

      const entity = processSignalDetection(
        detection,
        stations,
        signalDetectionColor,
        sourceToEventDistance.km * 1000,
        !!selectedSdIds?.includes(detection.id),
        sdConditions,
        associatedEventTime
      );
      if (entity) entities.push(entity);
    });
  }

  return entities;
}

/**
 * Given a map event source and a UI theme
 * Return the appropriate color from the theme
 *
 * @param event
 * @param uiTheme
 * @returns css string
 */
function getEventColor(event: MapEventSource, uiTheme: ConfigurationTypes.UITheme): string {
  if (event.isOpen) return uiTheme.colors.openEventSDColor;
  if (event.status === EventTypes.EventStatus.COMPLETE) return uiTheme.colors.completeEventSDColor;
  return uiTheme.colors.otherEventSDColor;
}

/**
 * Given an event location within the selected time interval
 * Return a Cesium ellipse entity
 *
 * @param event
 * @param uiTheme
 */
export function processEventLocation(
  event: MapEventSource,
  uiTheme: ConfigurationTypes.UITheme,
  preferred: boolean,
  selectedEvents: string[]
): Entity {
  const entityProperties = {
    id: event.id,
    type: 'Event location',
    event
  };
  const strokeOpacity =
    event.edgeEventType === EdgeTypes.INTERVAL ? 1 : uiTheme.display.edgeEventOpacity;
  let fillOpacity =
    event.edgeEventType === EdgeTypes.INTERVAL ? 1 : uiTheme.display.edgeEventOpacity;
  if (!preferred) fillOpacity = 0;
  const color = preferred ? Color.fromCssColorString(getEventColor(event, uiTheme)) : Color.BLACK;
  const isSelected = selectedEvents?.indexOf(event.id) > -1;
  const entityOptions: Entity.ConstructorOptions = {
    id: event.id,
    name: event.id,
    label: createLabel(event, eventDistanceDisplayCondition, isSelected),
    billboard: createBillboard(
      isSelected,
      eventEyeOffset,
      color,
      isSelected
        ? buildSelectedEventCircle(strokeOpacity, fillOpacity)
        : buildEventCircle(strokeOpacity, fillOpacity)
    ),
    properties: entityProperties,
    position: createCartesianFromLocationZeroElevation({
      latitudeDegrees: event.latitudeDegrees,
      longitudeDegrees: event.longitudeDegrees,
      elevationKm: 0,
      depthKm: 0
    }),
    show: true
  };

  return new Entity(entityOptions);
}

/**
 * Given an array of events and an edge type, parses through the array to create a Cesium entity for each event location that matches the edge type
 *
 * @param events
 * @param uiTheme
 * @param preferred
 * @param eventType
 */
export function createEventLocationEntities(
  events: MapEventSource[],
  uiTheme: ConfigurationTypes.UITheme,
  preferred: boolean,
  eventType: EdgeTypes,
  selectedEvents: string[]
): Entity[] {
  const entities: Entity[] = [];
  if (events?.length > 0) {
    events.forEach(event => {
      if (event.edgeEventType === eventType) {
        const entity = processEventLocation(event, uiTheme, preferred, selectedEvents);
        if (entity) entities.push(entity);
      }
    });
  }

  return entities;
}

/**
 * Given an event coverage ellipse within the selected time interval
 * rotation uses a negative value because our data is clockwise in degrees from north and Cesium expects counter-clockwise
 * rotationOffset rotates semi-major axis to north/south as Cesium defaults it to east/west
 * toRadians converts our data in degrees to radians
 * Return a Cesium ellipse entity
 *
 * @param event
 * @param uiTheme
 */
export function processEventCoverageEllipse(
  event: MapEventSource,
  uiTheme: ConfigurationTypes.UITheme
): Entity {
  const rotationOffset = 90;
  const geometry = EllipseOutlineGeometry.createGeometry(
    new EllipseOutlineGeometry({
      center: createCartesianFromLocationZeroElevation({
        latitudeDegrees: event.latitudeDegrees,
        longitudeDegrees: event.longitudeDegrees,
        elevationKm: 0,
        depthKm: 0
      }),
      // Converting kilometers to meters
      semiMajorAxis: (event.coverageSemiMajorAxis ?? 0) * 1000,
      semiMinorAxis: (event.coverageSemiMinorAxis ?? 0) * 1000,
      height: 1,
      rotation: CesiumMath.toRadians(
        -Math.abs((event.coverageSemiMajorAxisTrend ?? 0) + rotationOffset)
      ),
      granularity: 0.0025
    })
  );
  const entityProperties = {
    id: event.id,
    type: 'EventCoverageEllipse',
    coordinates: {
      longitude: formatNumberForTooltipDisplay(event.longitudeDegrees),
      latitude: formatNumberForTooltipDisplay(event.latitudeDegrees)
    }
  };
  const entityOptions: Entity.ConstructorOptions = {
    id: event.id,
    name: event.id,
    position: createCartesianFromLocationZeroElevation({
      latitudeDegrees: event.latitudeDegrees,
      longitudeDegrees: event.longitudeDegrees,
      elevationKm: 0,
      depthKm: 0
    }),
    properties: entityProperties,
    show: true
  };
  if (geometry) {
    const color = Color.fromCssColorString(getEventColor(event, uiTheme));
    color.alpha = uiTheme.display.edgeEventOpacity;
    entityOptions.polyline = createPolyline(
      Cartesian3.unpackArray(Array.from(geometry.attributes.position.values)),
      coverageEllipseDisplayCondition,
      new ColorMaterialProperty(color),
      coverageEllipseWidthPx
    );
  }
  return new Entity(entityOptions);
}

/**
 * Given an array of events and an edge type, parses through the array to create a Cesium entity for each event coverage ellipse that matches the edge type
 *
 * @param events
 * @param uiTheme
 * @param eventType
 *
 */
export function createEventCoverageEntities(
  events: MapEventSource[],
  uiTheme: ConfigurationTypes.UITheme,
  eventType: EdgeTypes
): Entity[] {
  const entities: Entity[] = [];
  if (events?.length > 0) {
    events.forEach(event => {
      if (event.edgeEventType === eventType) {
        const entity = processEventCoverageEllipse(event, uiTheme);
        if (entity) entities.push(entity);
      }
    });
  }
  return entities;
}

/**
 * Given an event confidence ellipse within the selected time interval
 * rotation uses a negative value because our data is clockwise in degrees from north and Cesium expects counter-clockwise
 * rotationOffset rotates semi-major axis to north/south as Cesium defaults it to east/west
 * toRadians converts our data in degrees to radians
 * Return a Cesium ellipse entity
 *
 * @param event
 */
export function processEventConfidenceEllipse(
  event: MapEventSource,
  uiTheme: ConfigurationTypes.UITheme
): Entity {
  const rotationOffset = 90;
  const geometry = EllipseOutlineGeometry.createGeometry(
    new EllipseOutlineGeometry({
      center: createCartesianFromLocationZeroElevation({
        latitudeDegrees: event.latitudeDegrees,
        longitudeDegrees: event.longitudeDegrees,
        elevationKm: 0,
        depthKm: 0
      }),
      // Converting kilometers to meters
      semiMajorAxis: (event.confidenceSemiMajorAxis ?? 0) * 1000,
      semiMinorAxis: (event.confidenceSemiMinorAxis ?? 0) * 1000,
      height: 1,
      rotation: CesiumMath.toRadians(
        -Math.abs((event.confidenceSemiMajorAxisTrend ?? 0) + rotationOffset)
      ),
      granularity: 0.0025
    })
  );
  const entityProperties = {
    id: event.id,
    type: 'EventConfidenceEllipse',
    coordinates: {
      longitude: formatNumberForTooltipDisplay(event.longitudeDegrees),
      latitude: formatNumberForTooltipDisplay(event.latitudeDegrees)
    }
  };
  const entityOptions: Entity.ConstructorOptions = {
    id: event.id,
    name: event.id,
    position: createCartesianFromLocationZeroElevation({
      latitudeDegrees: event.latitudeDegrees,
      longitudeDegrees: event.longitudeDegrees,
      elevationKm: 0,
      depthKm: 0
    }),
    properties: entityProperties,
    show: true
  };
  if (geometry) {
    const color = Color.fromCssColorString(getEventColor(event, uiTheme));
    color.alpha = uiTheme.display.edgeEventOpacity;
    entityOptions.polyline = createPolyline(
      Cartesian3.unpackArray(Array.from(geometry.attributes.position.values)),
      confidenceEllipseDisplayCondition,
      new PolylineDashMaterialProperty({ color }),
      confidenceEllipseWidthPx
    );
  }
  return new Entity(entityOptions);
}

/**
 * Given an array of events and an edge type, parses through the array to create a Cesium entity for each event confidence ellipse that matches the edge type
 *
 * @param events
 * @param uiTheme
 * @param eventType
 *
 */
export function createEventConfidenceEntities(
  events: MapEventSource[],
  uiTheme: ConfigurationTypes.UITheme,
  eventType: EdgeTypes
): Entity[] {
  const entities: Entity[] = [];
  if (events?.length > 0) {
    events.forEach(event => {
      if (event.edgeEventType === eventType) {
        const entity = processEventConfidenceEllipse(event, uiTheme);
        if (entity) entities.push(entity);
      }
    });
  }
  return entities;
}
