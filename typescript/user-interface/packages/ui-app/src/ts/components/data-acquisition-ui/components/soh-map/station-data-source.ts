import type { CommonTypes, ProcessingStationTypes, SohTypes } from '@gms/common-model';
import {
  BillboardGraphics,
  Cartesian3,
  Color,
  ColorMaterialProperty,
  ConstantProperty,
  DistanceDisplayCondition,
  Entity,
  HorizontalOrigin,
  LabelGraphics,
  LabelStyle,
  VerticalOrigin
} from 'cesium';

import { buildStationTriangle } from '~analyst-ui/components/map/img/station-triangle';
import {
  FAR,
  NEAR,
  SELECT_BLUE as SELECTED_BLUE
} from '~components/common-ui/components/map/constants';
import { getWorstCapabilityRollup } from '~components/data-acquisition-ui/shared/table/utils';

import {
  colorDictionary,
  fontStyle,
  imageScale,
  imageScaleSelected,
  selectedPixelOffset,
  unselectedPixelOffset
} from './constants';
/**
 * Given a Station or ChannelGroup Location, returns a Cesium Cartesian3 position
 *
 * @param location
 */
export function createCartesianFromSohLocation(location: CommonTypes.Location): Cartesian3 {
  return Cartesian3.fromDegrees(
    location.longitudeDegrees,
    location.latitudeDegrees,
    location.elevationKm
  );
}

/**
 * Map the stations to cesium entity list
 *
 * @param stations station list
 * @param stationSoh soh station list
 * @param selectedStationIds selected string names
 * @param isShowSohStatus should we show the soh status
 * @returns entity list
 */
export const mapStationsToSohEntities = (
  stations: ProcessingStationTypes.ProcessingStation[],
  stationSoh: SohTypes.UiStationSoh[],
  selectedStationIds: string[],
  isShowSohStatus: boolean
): Entity[] => {
  return stationSoh
    .map((stationAsSoh: SohTypes.UiStationSoh) => {
      const station: ProcessingStationTypes.ProcessingStation = stations?.find(
        s => s.name === stationAsSoh.stationName
      );
      if (!station) {
        return undefined;
      }
      const status = isShowSohStatus
        ? stationAsSoh?.sohStatusSummary
        : getWorstCapabilityRollup(stationAsSoh.stationGroups);
      const isSelected = selectedStationIds.indexOf(station.name) > -1;
      const entityProperties = {
        selected: isSelected,
        status,
        color: colorDictionary[status],
        location: station.location
      };
      const entityOptions: Entity.ConstructorOptions = {
        id: station.name,
        name: station.name,
        show: true,
        properties: entityProperties,
        billboard: new BillboardGraphics(),
        label: new LabelGraphics({
          text: station.name,
          font: fontStyle,
          scale: 1.5,
          backgroundColor: SELECTED_BLUE,
          fillColor: Color.WHITE,
          outlineColor: Color.BLACK,
          outlineWidth: 2,
          style: new ConstantProperty(LabelStyle.FILL_AND_OUTLINE),
          distanceDisplayCondition: new ConstantProperty(new DistanceDisplayCondition(NEAR, FAR))
        }),
        position: createCartesianFromSohLocation(station.location)
      };

      const currentEntity = new Entity(entityOptions);
      const describeString = `<span>Status: ${currentEntity.properties.status}</span>`;
      currentEntity.description = new ConstantProperty(describeString);
      currentEntity.billboard.image = new ConstantProperty(buildStationTriangle());
      currentEntity.billboard.scale = new ConstantProperty(imageScale);
      // triangle pins should have the center of lat/long in middle of shape
      currentEntity.billboard.horizontalOrigin = new ConstantProperty(HorizontalOrigin.CENTER);
      currentEntity.billboard.verticalOrigin = new ConstantProperty(VerticalOrigin.CENTER);
      currentEntity.billboard.color = colorDictionary[currentEntity.properties.status];
      currentEntity.label.verticalOrigin = new ConstantProperty(VerticalOrigin.TOP);
      currentEntity.label.pixelOffset = new ConstantProperty(unselectedPixelOffset);
      if (currentEntity.properties.selected.getValue()) {
        currentEntity.label.distanceDisplayCondition = new ConstantProperty(
          new DistanceDisplayCondition(0, Number.MAX_SAFE_INTEGER)
        );
        currentEntity.label.backgroundColor = new ColorMaterialProperty(SELECTED_BLUE);
        currentEntity.label.showBackground = new ConstantProperty(true);
        currentEntity.label.pixelOffset = new ConstantProperty(selectedPixelOffset);
        currentEntity.billboard.scale = new ConstantProperty(imageScaleSelected);
      }
      currentEntity.show = true;

      return currentEntity;
    })
    .filter(entity => entity !== undefined);
};
