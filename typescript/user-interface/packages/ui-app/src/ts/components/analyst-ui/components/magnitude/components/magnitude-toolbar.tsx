/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { DeprecatedToolbar, DeprecatedToolbarTypes } from '@gms/ui-core-components';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';
import React from 'react';

import { MagnitudeConfiguration } from './magnitude-configuration';

const MARGINS_FOR_TOOLBAR_PX = 16;

export interface MagnitudeToolbarProps {
  displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes;
  widthPx: number;
  setDisplayedMagnitudeTypes(
    displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes
  ): void;
}

/**
 * Generates the toolbar to be used in the magnitude display
 *
 * @param props of type MagnitudeToolbarProps
 */
// eslint-disable-next-line react/function-component-definition
export const MagnitudeToolbar: React.FunctionComponent<MagnitudeToolbarProps> = props => {
  const dropdownContent = (
    <MagnitudeConfiguration
      displayedMagnitudeTypes={props.displayedMagnitudeTypes}
      setCategoryAndTypes={types => {
        props.setDisplayedMagnitudeTypes(types);
      }}
    />
  );
  const dropdownItem: DeprecatedToolbarTypes.PopoverItem = {
    popoverContent: dropdownContent,
    label: 'Magnitude Configuration',
    rank: 1,
    widthPx: 204,
    tooltip: 'Configure the displayed magnitude types',
    type: DeprecatedToolbarTypes.ToolbarItemType.Popover,
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    onPopoverDismissed: () => {}
  };
  const toolBarItems: DeprecatedToolbarTypes.ToolbarItem[] = [dropdownItem];
  return (
    <DeprecatedToolbar
      toolbarWidthPx={props.widthPx - MARGINS_FOR_TOOLBAR_PX}
      itemsRight={toolBarItems}
    />
  );
};
