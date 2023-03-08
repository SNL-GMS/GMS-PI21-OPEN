import { Classes, H6 } from '@blueprintjs/core';
import * as React from 'react';

import { BaseCheckbox } from './base-checkbox';
import { DividerEntry } from './divider-checkbox-entry';
import type { CheckboxListEntryElement } from './types';

export interface ElementCheckboxEntryProps {
  entry: CheckboxListEntryElement;
  onChange: (entryName: string) => void;
  isChecked: boolean;
}

/**
 * Checkbox list entry that has an element for the icon
 *
 * @param props element checkbox entry, onChange, and is checked
 * @returns a checkbox with an element for the icon
 */
// eslint-disable-next-line react/function-component-definition
export const ElementCheckboxEntry: React.FunctionComponent<ElementCheckboxEntryProps> = (
  props: ElementCheckboxEntryProps
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
        {entry.element}
      </div>
      {entry.divider ? <DividerEntry /> : undefined}
    </React.Fragment>
  );
};
