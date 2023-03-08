import type Immutable from 'immutable';
import React from 'react';

import { CheckboxList } from '../../checkbox-list';
import { PopoverButton } from '../../popover-button';
import type { ToolbarItemBase, ToolbarItemElement } from '../types';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isCheckboxDropdownToolbarItem(
  object: unknown
): object is CheckboxDropdownToolbarItemProps {
  return (object as CheckboxDropdownToolbarItemProps).enumOfKeys !== undefined;
}

/**
 * props for {@link CheckboxDropdownToolbarItem}
 *
 * @see {@link ToolbarItemBase} for base properties.
 */
export interface CheckboxDropdownToolbarItemProps extends ToolbarItemBase {
  /** map for checkbox values (checked/unchecked) */
  values: Immutable.Map<any, boolean>;

  /** map of background colors */
  colors?: Immutable.Map<any, string>;

  /** map of keys to display strings (labels) */
  enumKeysToDisplayStrings?: Immutable.Map<string, string>;

  enumKeysToRenderDividers?: Immutable.Map<string, boolean>;

  enumKeysToLabelStrings?: Immutable.Map<string, string>;

  /** keys */
  enumOfKeys: any;

  /** callback to change event */
  onChange(value: any): void;

  /** callback to onPopUp (list appearing) event */
  onPopUp?(ref?: HTMLDivElement): void;

  /** callback to onPopoverDismissed (list disappears) event */
  onPopoverDismissed?(): void;
}

/**
 * Represents a dropdown list of checkbox items used within a toolbar
 *
 * @param checkboxItem the checkboxItem to display {@link CheckboxDropdownItem}
 */
// eslint-disable-next-line react/function-component-definition
export const CheckboxDropdownToolbarItem: React.FC<CheckboxDropdownToolbarItemProps> = ({
  values,
  colors,
  enumKeysToDisplayStrings,
  enumKeysToLabelStrings,
  enumKeysToRenderDividers,
  enumOfKeys,
  onChange,
  onPopUp,
  onPopoverDismissed,
  style,
  label,
  tooltip,
  disabled,
  widthPx,
  popoverButtonMap,
  cyData
}: CheckboxDropdownToolbarItemProps): ToolbarItemElement => {
  const handleRef = (ref: PopoverButton, buttonMap: Map<number, PopoverButton>): void => {
    if (ref && buttonMap) {
      buttonMap.set(1, ref);
    }
  };

  return (
    <div style={style ?? {}}>
      <PopoverButton
        label={label}
        tooltip={tooltip}
        disabled={disabled}
        cyData={cyData}
        popupContent={
          <CheckboxList
            enumToCheckedMap={values}
            enumToColorMap={colors}
            checkboxEnum={enumOfKeys}
            enumKeysToDisplayStrings={enumKeysToDisplayStrings}
            enumKeysToDividerMap={enumKeysToRenderDividers}
            enumKeysToLabelMap={enumKeysToLabelStrings}
            onChange={onChange}
          />
        }
        onPopoverDismissed={onPopoverDismissed}
        widthPx={widthPx}
        onClick={onPopUp}
        ref={ref => handleRef(ref, popoverButtonMap)}
      />
    </div>
  );
};
