import { Classes, H6 } from '@blueprintjs/core';
import * as React from 'react';

import { BaseCheckbox } from './base-checkbox';
import { DividerEntry } from './divider-checkbox-entry';
import type { CheckboxListEntry } from './types';

export interface CheckboxEntryProps {
  entry: CheckboxListEntry;
  onChange: (entryName: string) => void;
  isChecked: boolean;
}

/**
 * Checkbox list entry with no button or icon
 *
 * @param props checkbox list entry, onChange, and is checked
 * @returns a checkbox
 */
// eslint-disable-next-line react/function-component-definition
export const CheckboxEntry: React.FunctionComponent<CheckboxEntryProps> = (
  props: CheckboxEntryProps
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
      </div>
      {entry.divider ? <DividerEntry /> : undefined}
    </React.Fragment>
  );
};
