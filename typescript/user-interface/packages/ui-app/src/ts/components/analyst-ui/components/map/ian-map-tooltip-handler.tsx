import * as Cesium from 'cesium';
import * as React from 'react';
import { ScreenSpaceEvent, ScreenSpaceEventHandler } from 'resium';

import type { MapHandlerProps } from '~common-ui/components/map/types';

import {
  ianMapEventTooltipLabel,
  ianMapStationTooltipLabel,
  ianMapTooltipHandleAltClick,
  ianMapTooltipHandleMouseMove
} from './ian-map-tooltip-utils';

/**
 * This component creates and ScreenSpaceEventHandler along with a ScreenSpaceEvent of type mousemove
 * so that when an entity on the map has been hovered over a tooltip will appear.
 *
 * @param props the props
 */
// eslint-disable-next-line react/function-component-definition
export const IanMapTooltipHandler: React.FunctionComponent<MapHandlerProps> = ({
  viewer
}: MapHandlerProps) => {
  if (viewer) {
    // check to see if we have a station tooltip entity to work with if not we add it
    if (!viewer.entities.getById('hoverLabelEntity')) {
      viewer.entities.add(ianMapStationTooltipLabel);
    }
    // check to see if we have an event tooltip entity to work with if not we add it
    if (!viewer.entities.getById('eventLabelEntity')) {
      viewer.entities.add(ianMapEventTooltipLabel);
    }
    return (
      <ScreenSpaceEventHandler key="IMTHandlers">
        <ScreenSpaceEvent
          action={async position => ianMapTooltipHandleMouseMove(position, viewer)}
          type={Cesium.ScreenSpaceEventType.MOUSE_MOVE}
        />
        <ScreenSpaceEvent
          action={position => ianMapTooltipHandleAltClick(position, viewer)}
          type={Cesium.ScreenSpaceEventType.LEFT_DOWN}
          modifier={Cesium.KeyboardEventModifier.ALT}
        />
      </ScreenSpaceEventHandler>
    );
  }
  return null;
};
