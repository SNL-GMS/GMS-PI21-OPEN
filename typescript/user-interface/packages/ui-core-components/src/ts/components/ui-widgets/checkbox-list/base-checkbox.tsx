import { Checkbox } from '@blueprintjs/core';
import * as React from 'react';

export interface CheckboxProps {
  name: string;
  color: string;
  onChange: (entryName: string) => void;
  isChecked: boolean;
}

/**
 * Checkbox with style and classnames
 *
 * @param props name, color, onChange, and is checked
 * @returns a checkbox with desired style and formatting
 */
// eslint-disable-next-line react/function-component-definition
export const BaseCheckbox: React.FunctionComponent<CheckboxProps> = (props: CheckboxProps) => {
  const { name, color, isChecked, onChange } = props;
  return (
    <div className="checkbox-list__box-and-label">
      <Checkbox
        className="checkbox-list__checkbox"
        data-cy={`checkbox-item-${name}`}
        onChange={() => onChange(name)}
        checked={isChecked}
      >
        <div className="checkbox-list__label">{name}</div>
        {color ? (
          <div
            className="checkbox-list__legend-box"
            style={{
              backgroundColor: color
            }}
          />
        ) : undefined}
      </Checkbox>
    </div>
  );
};
