import { Switch } from '@blueprintjs/core';
import React from 'react';

import type { ToolbarItemBase, ToolbarItemElement } from '../types';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isSwitchToolbarItem(object: unknown): object is SwitchToolbarItemProps {
  return (object as SwitchToolbarItemProps).switchValue !== undefined;
}

/**
 * Properties to pass to the {@link SwitchToolbarItem}
 *
 * @see {@link ToolbarItemBase} for base properties.
 */
export interface SwitchToolbarItemProps extends ToolbarItemBase {
  /** value - either on or off */
  switchValue: boolean;
  /** callback when the value changes */
  onChange(value: boolean);
}

/**
 * Represents a switch (on/off) used within a toolbar
 *
 * @param switchItem the item to display {@link SwitchItem}
 */
// eslint-disable-next-line react/function-component-definition
export const SwitchToolbarItem: React.FC<SwitchToolbarItemProps> = ({
  switchValue,
  onChange,
  style,
  label,
  tooltip,
  disabled,
  cyData
}: SwitchToolbarItemProps): ToolbarItemElement => {
  const handleChange = (event: React.FormEvent<HTMLInputElement>): void => {
    onChange(event.currentTarget.checked);
  };
  return (
    <div className="toolbar-switch" title={tooltip} style={style ?? {}}>
      {label && <div className="toolbar-switch-label">{`${label}:`}</div>}
      <Switch
        title={tooltip}
        disabled={disabled}
        className={`toolbar-switch__blueprint ${label?.toLowerCase().replace(' ', '-')}`}
        checked={switchValue}
        large
        onChange={handleChange}
        data-cy={cyData}
      />
    </div>
  );
};
