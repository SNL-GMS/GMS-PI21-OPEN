import React from 'react';

import { PopoverButton } from '../../popover-button';
import type { ToolbarItemBase, ToolbarItemElement } from '../types';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isPopoverButtonToolbarItem(
  object: unknown
): object is PopoverButtonToolbarItemProps {
  return (object as PopoverButtonToolbarItemProps).popoverContent !== undefined;
}

/**
 * Properties to pass to the {@link PopoverButtonToolbarItem}
 *
 * @see {@link ToolbarItemBase} for base properties.
 */
export interface PopoverButtonToolbarItemProps extends ToolbarItemBase {
  /** content of the popover */
  popoverContent: JSX.Element;

  /** callback when the popover is closed */
  onPopoverDismissed?: () => unknown;
}

/**
 * Represents a Popover(similar to a tooltip) used within a toolbar
 *
 * @param popoverButtonItem the item to display {@link PopoverButtonItem}
 */
// eslint-disable-next-line react/function-component-definition
export const PopoverButtonToolbarItem: React.FC<PopoverButtonToolbarItemProps> = ({
  popoverContent,
  onPopoverDismissed,
  style,
  label,
  tooltip,
  icon,
  onlyShowIcon,
  disabled,
  widthPx,
  popoverButtonMap,
  cyData
}: PopoverButtonToolbarItemProps): ToolbarItemElement => {
  const handleRef = React.useCallback(
    (ref: PopoverButton, buttonMap: Map<number, PopoverButton>): void => {
      if (ref && buttonMap) {
        buttonMap.set(1, ref);
      }
    },
    []
  );
  return (
    <div style={style}>
      <PopoverButton
        label={label}
        tooltip={tooltip}
        icon={icon}
        onlyShowIcon={onlyShowIcon}
        disabled={disabled}
        popupContent={popoverContent}
        onPopoverDismissed={onPopoverDismissed}
        widthPx={widthPx}
        ref={ref => handleRef(ref, popoverButtonMap)}
        data-cy={cyData}
      />
    </div>
  );
};
