import type { IconName } from '@blueprintjs/icons';
import { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import * as React from 'react';

/**
 * Creates a legend icon hook that keeps it's toggle state
 *
 * @returns a toolbar button and booleans for if legend is visible.
 */
export const useShowLegend = (
  tooltipMsg: string,
  blueprintIcon: IconName
): [DeprecatedToolbarTypes.ButtonItem, boolean, (value: boolean) => void] => {
  // custom legend state (boolean)
  // if true legend will show else it wont
  const [isLegendVisible, setShowLegend] = React.useState(false);
  const legend: DeprecatedToolbarTypes.ButtonItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.Button,
    rank: 1,
    tooltip: tooltipMsg,
    widthPx: 8,
    label: '',
    onClick: () => {
      setShowLegend(!isLegendVisible);
    },
    icon: blueprintIcon
  };
  return [legend, isLegendVisible, setShowLegend];
};
