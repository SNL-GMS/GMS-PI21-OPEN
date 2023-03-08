import React from 'react';

import type { ToolbarItemBase, ToolbarItemElement } from '../types';
import type { ButtonToolbarItemProps } from './button-item';
import { ButtonToolbarItem } from './button-item';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isButtonGroupToolbarItem(object: unknown): object is ButtonGroupToolbarProps {
  return (object as ButtonGroupToolbarProps).buttons !== undefined;
}

/**
 * Properties to pass to the {@link ButtonGroupToolbarItem}
 *
 * @see {@link ToolbarItemBase} for base properties.
 */
export interface ButtonGroupToolbarProps extends ToolbarItemBase {
  /** Array of props for {@link ButtonToolbarItem} items to be rendered. */
  buttons: ButtonToolbarItemProps[];
}

/**
 * Represents a group of buttons used within a toolbar
 *
 * @param groupItem Properties to pass to the ButtonGroup {@link ButtonGroupToolbarProps}
 */
// eslint-disable-next-line react/function-component-definition
export const ButtonGroupToolbarItem: React.FC<ButtonGroupToolbarProps> = ({
  buttons,
  style,
  cyData
}: ButtonGroupToolbarProps): ToolbarItemElement => {
  const indexOfLastButton = buttons.length - 1;
  return (
    <div className="toolbar-button-group" data-cy={cyData} style={style ?? {}}>
      {buttons.map((button, index) => {
        return (
          <ButtonToolbarItem
            key={button.key}
            widthPx={button.widthPx}
            onlyShowIcon={button.onlyShowIcon}
            style={button.style}
            disabled={button.disabled}
            label={button.label}
            labelRight={button.labelRight}
            tooltip={button.tooltip}
            icon={button.icon}
            onButtonClick={() => button.onButtonClick()}
            onMouseEnter={button.onMouseEnter ? () => button.onMouseEnter() : undefined}
            onMouseOut={button.onMouseOut ? () => button.onMouseOut() : undefined}
            marginRight={index !== indexOfLastButton ? 2 : 0}
            cyData={button.cyData}
          />
        );
      })}
    </div>
  );
};
