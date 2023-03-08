/* eslint-disable react/destructuring-assignment */
import { UILogger } from '@gms/ui-util';
import React from 'react';

import type { PopoverButton } from '../popover-button';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import * as Renderers from './toolbar-item-renderers';
import type {
  ButtonGroupItem,
  ButtonItem,
  CheckboxDropdownItem,
  CustomItem,
  DateRangePickerItem,
  DeprecatedToolbarItemBase,
  DropdownItem,
  IntervalPickerItem,
  LabelValueItem,
  LoadingSpinnerItem,
  NumericInputItem,
  PopoverItem,
  SwitchItem,
  ToolbarItem
} from './types';
import { ToolbarItemType } from './types';

const logger = UILogger.create('GMS_LOG_TOOLBAR', process.env.GMS_LOG_TOOLBAR);

export interface ToolbarItemRendererProps {
  item: ToolbarItem;
  hasIssue: boolean;
  addToPopoverMap(rank: number, ref: PopoverButton): void;
}

/**
 * Selects the correct function to render a toolbar item
 */
// eslint-disable-next-line complexity
export function ToolbarItemRenderer(props: ToolbarItemRendererProps): JSX.Element {
  const itemTypes = ToolbarItemType;
  switch ((props.item as DeprecatedToolbarItemBase).type) {
    case itemTypes.Dropdown: {
      return Renderers.renderDropdown(props.item as DropdownItem);
    }
    case itemTypes.NumericInput: {
      return Renderers.renderNumeric(props.item as NumericInputItem);
    }
    case itemTypes.IntervalPicker: {
      return Renderers.renderIntervalPicker(props.item as IntervalPickerItem);
    }
    case itemTypes.DateRangePicker: {
      return Renderers.renderDateRangePicker(props.item as DateRangePickerItem);
    }
    case itemTypes.Popover: {
      return Renderers.renderPopoverButton(props.item as PopoverItem, (key, val) =>
        props.addToPopoverMap(key, val)
      );
    }
    case itemTypes.Switch: {
      return Renderers.renderSwitch(props.item as SwitchItem);
    }
    case itemTypes.Button: {
      return Renderers.renderButton(props.item as ButtonItem);
    }
    case itemTypes.ButtonGroup: {
      return Renderers.renderButtonGroup(props.item as ButtonGroupItem);
    }
    case itemTypes.LabelValue: {
      return Renderers.renderLabelValue(props.item as LabelValueItem, props.hasIssue);
    }
    case itemTypes.CheckboxList:
      return Renderers.renderCheckboxDropdown(props.item as CheckboxDropdownItem, (key, val) =>
        props.addToPopoverMap(key, val)
      );
    case itemTypes.LoadingSpinner:
      return Renderers.renderLoadingSpinner(props.item as LoadingSpinnerItem);
    case itemTypes.CustomItem:
      return (props.item as CustomItem).element;
    default: {
      logger.warn(`default error`);
      return <div />;
    }
  }
}
