import type { ChannelTypes, StationTypes } from '@gms/common-model';
import { secondsToString, TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION } from '@gms/common-util';
import type {
  ColorMaterialProperty,
  ConstantPositionProperty,
  DistanceDisplayCondition
} from 'cesium';
import {
  BillboardGraphics,
  Cartesian3,
  Color,
  ConstantProperty,
  HorizontalOrigin,
  JulianDate,
  LabelGraphics,
  LabelStyle,
  PolylineGraphics,
  PolylineOutlineMaterialProperty,
  VerticalOrigin
} from 'cesium';

import {
  alwaysDisplayDistanceDisplayCondition,
  LABEL_HEIGHT_SELECTED,
  LABEL_HEIGHT_UNSELECTED,
  SELECT_BLUE as SELECTED_BLUE,
  SELECT_TRANSPARENT as SELECTED_TRANSPARENT,
  SELECTED_SIGNAL_DETECTION_GLOW_ALPHA,
  SELECTED_SIGNAL_DETECTION_GLOW_OUTLINE_WIDTH,
  SELECTED_SIGNAL_DETECTION_WIDTH_OFFSET
} from '~common-ui/components/map/constants';
// TODO move constants to common
import {
  fontStyle,
  imageScale,
  imageScaleSelected,
  monoFontStyle,
  selectedPixelOffset,
  unselectedPixelOffset
} from '~data-acquisition-ui/components/soh-map/constants';

import type { MapEventSource } from './types';

/**
 * Returns polyline material for Signal Detections based on whether or not the signal detection is selected or not.
 * If the Polyline is not selected, returns just the color
 * If the SD is selected, returns a new PolylineMaterialProperty that includes a semi-transparent outline to give the line a glow effect.
 *
 * @param isSelected
 * @param color
 */
export function getSDPolylineMaterial(
  isSelected: boolean,
  color: ColorMaterialProperty
): ColorMaterialProperty | PolylineOutlineMaterialProperty {
  if (!isSelected) return color;

  const outlineColor = color.color.getValue(JulianDate.now());
  outlineColor.alpha = SELECTED_SIGNAL_DETECTION_GLOW_ALPHA;
  return new PolylineOutlineMaterialProperty({
    color: color.color,
    outlineWidth: SELECTED_SIGNAL_DETECTION_GLOW_OUTLINE_WIDTH,
    outlineColor
  });
}

/**
 * Given a Cesium entity and a display condition, configure a label for that entity
 *
 * @param item - Station/Channel Group/Event location we are creating a label for
 * @param distanceDisplayCondition - DistanceDisplayCondition
 * @param isSelected - is the entity we are creating a label for selected (affects size and color)
 */
export function createLabel(
  item: ChannelTypes.ChannelGroup | StationTypes.Station | MapEventSource,
  distanceDisplayCondition: DistanceDisplayCondition,
  isSelected: boolean
): LabelGraphics {
  let text;
  let font;
  let backgroundColor;
  if ('name' in item) {
    text = item.name;
    font = fontStyle;
    backgroundColor = SELECTED_BLUE;
  } else {
    text = secondsToString(item.time, TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION);
    font = monoFontStyle;
    backgroundColor = SELECTED_TRANSPARENT;
  }
  const options: LabelGraphics.ConstructorOptions = {
    backgroundColor,
    text,
    font,
    fillColor: Color.WHITE,
    outlineColor: Color.BLACK,
    outlineWidth: 2,
    showBackground: isSelected,
    style: new ConstantProperty(LabelStyle.FILL_AND_OUTLINE),
    distanceDisplayCondition: new ConstantProperty(
      isSelected ? alwaysDisplayDistanceDisplayCondition : distanceDisplayCondition
    ),
    verticalOrigin: new ConstantProperty(VerticalOrigin.TOP),
    pixelOffset: new ConstantProperty(isSelected ? selectedPixelOffset : unselectedPixelOffset),
    eyeOffset: new Cartesian3(
      0.0,
      0.0,
      isSelected ? LABEL_HEIGHT_SELECTED : LABEL_HEIGHT_UNSELECTED
    )
  };

  return new LabelGraphics(options);
}

/**
 * Returns a new PolyLineGraphics object from an array of positions with given style
 *
 * @param positions Start position and end position as a Cesium.Cartiesian3[]
 * @param distanceDisplayCondition defines what distances you can see the line from
 * @param material If just specifying a color, use ColorMaterialProperty. If you want a line with outline provide a PolylineMaterialProperty
 * @param width of the polyline in pixels
 * @param isSelected
 */
export function createPolyline(
  positions: Cartesian3[],
  distanceDisplayCondition: DistanceDisplayCondition,
  material: ColorMaterialProperty | PolylineOutlineMaterialProperty,
  width: number,
  isSelected = false
): PolylineGraphics {
  return new PolylineGraphics({
    distanceDisplayCondition: new ConstantProperty(distanceDisplayCondition),
    positions,
    show: true,
    width: isSelected
      ? new ConstantProperty(width + (SELECTED_SIGNAL_DETECTION_WIDTH_OFFSET as number))
      : new ConstantProperty(width),
    material
  });
}

/**
 * Create a Billboard for a station, ChannelGroup, or event (i.e. map icons)
 *
 * @param selected : is the entity we are creating a billboard for selected
 * @param eyeOffset: z-index of the billboard
 * @param color
 * @param image
 */
export function createBillboard(
  selected: boolean,
  eyeOffset: ConstantPositionProperty,
  color: Color,
  image: Document | string
): BillboardGraphics {
  const billboard = new BillboardGraphics();
  billboard.image = new ConstantProperty(image);
  billboard.color = new ConstantProperty(color);
  billboard.scale = selected
    ? new ConstantProperty(imageScaleSelected)
    : new ConstantProperty(imageScale);
  // billboard should have the center of lat/long in middle of shape;
  billboard.horizontalOrigin = new ConstantProperty(HorizontalOrigin.CENTER);
  billboard.verticalOrigin = new ConstantProperty(VerticalOrigin.CENTER);
  // set eye offset (similar to z-index, which is not yet supported)
  billboard.eyeOffset = eyeOffset;
  return billboard;
}
