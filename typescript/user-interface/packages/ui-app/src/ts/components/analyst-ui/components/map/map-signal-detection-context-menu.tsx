import { ContextMenu, Menu, MenuItem } from '@blueprintjs/core';
import React from 'react';

import { MapSignalDetectionDetails } from './map-signal-detection-details';

export interface MapSignalDetectionContextMenuProps {
  readonly sd;
  readonly left: number;
  readonly top: number;
}

/**
 * Given signal detection entity properties and mouse location, renders a popover with metadata and adds global event listener to close it
 *
 * @param sd
 * @param clientX
 * @param clientY
 */
export const showMapSdDetailsPopover = (sd: any, clientX: number, clientY: number): void => {
  ContextMenu.show(
    <MapSignalDetectionDetails sd={sd} />,
    {
      left: clientX,
      top: clientY
    },
    undefined,
    true
  );
};

/**
 * Component that renders the map signal detection context menu
 */
// eslint-disable-next-line react/function-component-definition
export const MapSignalDetectionContextMenu: React.FunctionComponent<MapSignalDetectionContextMenuProps> = (
  props: MapSignalDetectionContextMenuProps
) => {
  const { sd, left, top } = props;

  return (
    <Menu>
      <MenuItem
        className="menu-item-sd-details"
        text="Open signal detection details"
        label="(Alt + click)"
        disabled={!sd}
        shouldDismissPopover={false}
        onClick={() => showMapSdDetailsPopover(sd, left, top)}
      />
    </Menu>
  );
};
