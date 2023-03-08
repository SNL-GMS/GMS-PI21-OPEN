import { Button, Classes, H6, Popover, PopoverInteractionKind } from '@blueprintjs/core';
import * as React from 'react';

import { BaseCheckbox } from './base-checkbox';
import { DividerEntry } from './divider-checkbox-entry';
import type { CheckboxListEntryButton } from './types';

export interface ButtonCheckboxEntryProps {
  entry: CheckboxListEntryButton;
  onChange: (entryName: string) => void;
  isChecked: boolean;
}

/**
 * Determines if there is an onClick action to execute
 *
 * @param onClick method to execute
 * @param event button click event
 */
const determineOnClick = (
  onClick: (event: React.MouseEvent<HTMLButtonElement>) => void,
  event: React.MouseEvent<HTMLButtonElement>
) => {
  if (onClick) {
    onClick(event);
  }
};

/**
 * Checkbox list entry that is a icon button with an option to display a popover
 *
 * @param props button checkbox entry, onChange, and is checked
 * @returns a checkbox with a icon button
 */
// eslint-disable-next-line react/function-component-definition
export const ButtonCheckboxEntry: React.FunctionComponent<ButtonCheckboxEntryProps> = (
  props: ButtonCheckboxEntryProps
) => {
  const { entry, isChecked, onChange } = props;
  return (
    <React.Fragment key={entry.name}>
      {entry.headerTitle ? <H6 className={Classes.HEADING}> {entry.headerTitle}</H6> : undefined}
      <div className="checkbox-list__row">
        <BaseCheckbox
          name={entry.name}
          color={entry.color}
          isChecked={isChecked}
          onChange={onChange}
        />
        {entry.iconButton.popover ? (
          <Popover
            content={entry.iconButton.popover.content}
            interactionKind={PopoverInteractionKind.CLICK}
            position={entry.iconButton.popover.position}
            usePortal={entry.iconButton.popover.usePortal ?? false}
            minimal={entry.iconButton.popover.minimal ?? false}
          >
            <Button
              icon={entry.iconButton.iconName}
              // eslint-disable-next-line @typescript-eslint/unbound-method
              onClick={e => determineOnClick(entry.iconButton.onClick, e)}
            />
          </Popover>
        ) : (
          <Button
            icon={entry.iconButton.iconName}
            // eslint-disable-next-line @typescript-eslint/unbound-method
            onClick={e => determineOnClick(entry.iconButton.onClick, e)}
          />
        )}
      </div>
      {entry.divider ? <DividerEntry /> : undefined}
    </React.Fragment>
  );
};
