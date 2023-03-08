import { MenuItem } from '@blueprintjs/core';
import React from 'react';

import type { HideStationMenuItemProps } from './types';

/**
 * Menu item intended to be used with the waveform display to show/hide stations
 *
 * @param props
 * @constructor
 */
export function HideStationMenuItem(props: HideStationMenuItemProps) {
  const { stationName, hideStationCallback, showHideText, disabled } = props;
  return (
    <MenuItem
      disabled={disabled}
      data-cy={`hide-${stationName}`}
      className="hide-station-menu-item"
      onClick={hideStationCallback}
      text={showHideText ?? `Hide ${stationName}`}
    />
  );
}
