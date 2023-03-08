import { Classes, H6, Icon } from '@blueprintjs/core';
import * as React from 'react';

import { BaseCheckbox } from './base-checkbox';
import { DividerEntry } from './divider-checkbox-entry';
import type { CheckboxListEntryIcon } from './types';

export interface IconCheckboxEntryProps {
  entry: CheckboxListEntryIcon;
  onChange: (entryName: string) => void;
  isChecked: boolean;
}

/**
 * Checkbox list entry that has an icon
 *
 * @param props button checkbox entry, onChange, and is checked
 * @returns a checkbox with an icon
 */
// eslint-disable-next-line react/function-component-definition
export const IconCheckboxEntry: React.FunctionComponent<IconCheckboxEntryProps> = (
  props: IconCheckboxEntryProps
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
        <Icon icon={entry.iconName} color={entry.iconColor ?? ''} />
      </div>
      {entry.divider ? <DividerEntry /> : undefined}
    </React.Fragment>
  );
};
