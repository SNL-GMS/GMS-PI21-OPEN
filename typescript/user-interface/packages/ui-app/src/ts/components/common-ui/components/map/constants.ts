import * as Cesium from 'cesium';

import { gmsColors } from '~scss-config/color-preferences';

// minimum height of viewer
export const MIN_HEIGHT = 300;

// distances between to show the label of the entity
export const NEAR = 0;
export const MEDIUM = 100000;
export const FAR = 5000000;
export const VERYFAR = 12000000;
// a cesium version of the gms blue selection color
export const SELECT_BLUE = Cesium.Color.fromCssColorString(gmsColors.gmsSelection);

// passing an empty string to get the default cesium label color
export const SELECT_TRANSPARENT = Cesium.Color.fromCssColorString('');

// eyeoffset heights for entities on the map, lower values bring the item closer to the top/camera
export const LABEL_HEIGHT_UNSELECTED = -3;
export const LABEL_HEIGHT_SELECTED = -10;
export const TOOLTIP_HEIGHT = -50;
export const BILLBOARD_HEIGHT_SELECTED = -10;
export const BILLBOARD_HEIGHT_UNSELECTED_STATION = -8;
export const BILLBOARD_HEIGHT_UNSELECTED_CHANNEL = -1;
// TODO: add event circle selected height of -12 when event selection is implemented
export const EVENT_CIRCLE_HEIGHT_UNSELECTED = -11;

// z-index height, not to be confused with the eyeoffsets above. For items that support a true z-index, higher values bring items to the top
export const SELECTED_SIGNAL_DETECTION_ZINDEX = 20;

// Amount in pixels to add to the provided width after selection a Signal Detection
export const SELECTED_SIGNAL_DETECTION_WIDTH_OFFSET = 4;

// Width of selected Signal Detection glowing outline
export const SELECTED_SIGNAL_DETECTION_GLOW_OUTLINE_WIDTH = 4;
// Alpha level of Signal detection glow. 0 is completely transparent, 1 is completely opaque
export const SELECTED_SIGNAL_DETECTION_GLOW_ALPHA = 0.2;

export const FIFTEEN_MINUTES_IN_SECONDS = 900;

export const eventDistanceDisplayCondition: Cesium.DistanceDisplayCondition = new Cesium.DistanceDisplayCondition(
  NEAR,
  VERYFAR
);

export const stationDistanceDisplayCondition: Cesium.DistanceDisplayCondition = new Cesium.DistanceDisplayCondition(
  NEAR,
  VERYFAR
);
export const channelGroupDistanceDisplayCondition: Cesium.DistanceDisplayCondition = new Cesium.DistanceDisplayCondition(
  NEAR,
  MEDIUM
);
export const lineDistanceDisplayCondition: Cesium.DistanceDisplayCondition = new Cesium.DistanceDisplayCondition(
  NEAR,
  MEDIUM
);
export const alwaysDisplayDistanceDisplayCondition: Cesium.DistanceDisplayCondition = new Cesium.DistanceDisplayCondition(
  NEAR,
  Number.MAX_SAFE_INTEGER
);
export const confidenceEllipseDisplayCondition: Cesium.DistanceDisplayCondition = new Cesium.DistanceDisplayCondition(
  NEAR,
  VERYFAR
);

export const coverageEllipseDisplayCondition: Cesium.DistanceDisplayCondition = new Cesium.DistanceDisplayCondition(
  NEAR,
  VERYFAR
);

/**
 * Hardcoded Cartesian3 coordinates for event circles
 */
// TODO: add logic to change Z component when event selection is implemented
export const eventEyeOffset = new Cesium.ConstantPositionProperty(
  new Cesium.Cartesian3(0.0, 0.0, EVENT_CIRCLE_HEIGHT_UNSELECTED)
);

/**
 * Value for event coverage ellipse polyline width in pixels
 */
export const coverageEllipseWidthPx = 1.5;

/**
 * Value for event confidence ellipse polyline width in pixels
 */
export const confidenceEllipseWidthPx = 1.5;
