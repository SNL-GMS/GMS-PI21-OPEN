/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { IS_MODE_IAN } from '@gms/common-util';
import { useUiTheme } from '@gms/ui-state';
import type { Viewer as CesiumViewer } from 'cesium';
import {
  buildModuleUrl,
  Cartesian3,
  Color,
  EllipsoidTerrainProvider,
  KeyboardEventModifier,
  ProviderViewModel,
  ScreenSpaceEventType,
  TileMapServiceImageryProvider
} from 'cesium';
import React from 'react';
import type { CesiumComponentRef } from 'resium';
import {
  Camera,
  CameraFlyTo,
  ImageryLayer,
  Scene,
  ScreenSpaceEvent,
  ScreenSpaceEventHandler,
  Viewer
} from 'resium';

import { MIN_HEIGHT } from './constants';
import { baseViewerSettings } from './map-resources';
import { useOnMultipleSelect, useRenderMapOnSelection, useTileLoadingStatus } from './map-util';
import type { MapProps } from './types';

/* Set to true to see map fps */
const DEBUG_FPS = false;

const imageryProviderViewModels: ProviderViewModel[] = [];

imageryProviderViewModels.push(
  new ProviderViewModel({
    name: 'Natural Earth',
    tooltip: 'Natural Earth',
    iconUrl: buildModuleUrl('Widgets/Images/ImageryProviders/naturalEarthII.png'),
    creationFunction: (): TileMapServiceImageryProvider =>
      new TileMapServiceImageryProvider({
        url: buildModuleUrl('Assets/Textures/NaturalEarthII'),
        maximumLevel: 2
      })
  })
);

const naturalEarthImageryProvider = new TileMapServiceImageryProvider({
  url: buildModuleUrl('Assets/Textures/NaturalEarthII'),
  maximumLevel: 2
});

const terrainProviderViewModels = [];

const wgs84Terrain = new ProviderViewModel({
  name: 'WGS84 Ellipsoid',
  iconUrl: buildModuleUrl('Widgets/Images/TerrainProviders/Ellipsoid.png'),
  tooltip: 'WGS84 standard ellipsoid, also known as EPSG:4326',
  creationFunction: () => new EllipsoidTerrainProvider()
});

terrainProviderViewModels.push(wgs84Terrain);

/**
 * common base map component
 *
 * @param props the props
 */
export function MapComponent(props: MapProps) {
  const ref = React.useRef<CesiumComponentRef<CesiumViewer>>(null);
  useRenderMapOnSelection(ref);

  const { doMultiSelect, selectedStations, selectedEvents, selectedSdIds } = props;

  const minHeight = MIN_HEIGHT;

  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  const destination: Cartesian3 = Cartesian3.fromDegrees(0, 0, 40000000);

  /**
   * This function passes all the ScreenSpaceEventHandlers
   * viewer context and then maps them into the viewer
   *
   * @returns jsx ScreenSpaceEventHandlers
   */
  const getViewerHandlers = () => {
    return (
      ref?.current?.cesiumElement &&
      props.handlers?.map(handler => handler({ viewer: ref?.current?.cesiumElement }))
    );
  };

  const tileLoadingState = useTileLoadingStatus(ref?.current?.cesiumElement);

  // Guarantee that after the map-panel rerenders, that the map scene also updates with it
  React.useEffect(() => {
    if (IS_MODE_IAN) ref.current?.cesiumElement?.scene?.requestRender();
  });

  // If the user selects a Station using Ctrl-Click, keep the existing Entities and add the new one.
  const onMultipleSelect = useOnMultipleSelect(
    ref,
    selectedStations,
    selectedEvents,
    selectedSdIds
  );

  /*
   * Disable double-click object selection in the UI.
   * NOTE: code added inline here because sonar lint complained about
   * "using a return value from a Void function" when placed in a utility function.
   */
  ref?.current?.cesiumElement?.screenSpaceEventHandler.removeInputAction(
    ScreenSpaceEventType.LEFT_DOUBLE_CLICK
  );

  const [uiTheme] = useUiTheme();

  return (
    <div
      className="map-sub-wrapper"
      style={{ minHeight }}
      data-tile-loading-status={tileLoadingState}
    >
      <Viewer
        // eslint-disable-next-line react/jsx-props-no-spreading
        {...baseViewerSettings}
        ref={ref}
        full
        infoBox={false}
        selectionIndicator={false}
        homeButton
        terrainProviderViewModels={terrainProviderViewModels}
        baseLayerPicker
        imageryProvider={false}
        imageryProviderViewModels={imageryProviderViewModels}
      >
        <Scene
          debugShowFramesPerSecond={DEBUG_FPS}
          backgroundColor={Color.fromCssColorString(uiTheme.colors.gmsBackground)}
        />
        <Camera defaultZoomAmount={0} />
        <CameraFlyTo duration={0} destination={destination} once />
        <ImageryLayer imageryProvider={naturalEarthImageryProvider} />
        {props.entities?.map(entity => {
          return entity;
        })}
        {props.dataSources?.map(dataSource => {
          return dataSource;
        })}
        {getViewerHandlers()}
        {doMultiSelect && (
          <ScreenSpaceEventHandler>
            <ScreenSpaceEvent
              action={onMultipleSelect}
              type={ScreenSpaceEventType.LEFT_DOWN}
              modifier={KeyboardEventModifier.SHIFT}
            />
          </ScreenSpaceEventHandler>
        )}
      </Viewer>
    </div>
  );
}

export const Map = React.memo(MapComponent);
