import type { IconName } from '@blueprintjs/core';
import { Icon } from '@blueprintjs/core';
import React from 'react';

/**
 * The type of the props for the {@link SubMenuButton} component
 */
export interface SubMenuButtonProps {
  disabled: boolean;
  handleClick: React.MouseEventHandler<HTMLDivElement>;
  handleKeyDown: React.KeyboardEventHandler<HTMLDivElement>;
  iconName: IconName;
}

/**
 * Creates an icon that functions as a button, which calls the callback handleClick when clicked, and handleKeydown when the enter key is pressed.
 */
export function SubMenuButton(props: SubMenuButtonProps) {
  const { disabled, handleClick, handleKeyDown, iconName } = props;
  return (
    !disabled && (
      <div
        className="app-menu-item__sub-menu"
        role="button"
        onClick={handleClick}
        onKeyDown={e => {
          if (e.key === 'Enter') {
            handleKeyDown(e);
          }
        }}
        tabIndex={-1}
      >
        <Icon icon={iconName} />
      </div>
    )
  );
}
