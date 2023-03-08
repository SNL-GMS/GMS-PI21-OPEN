/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { ContextMenu, MenuItem } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { UILogger } from '@gms/ui-util';
import * as React from 'react';

import { CheckboxList } from '../checkbox-list/checkbox-list';
import { DateRangePicker } from '../date-range-picker';
import { IntervalPicker } from '../interval-picker';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { renderNumeric } from './toolbar-item-renderers';
import type {
  ButtonGroupItem,
  ButtonItem,
  CheckboxDropdownItem,
  CustomItem,
  DateRangePickerItem,
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

export interface ToolbarMenuItemRendererProps {
  item: ToolbarItem;
  hasIssue: boolean;
  menuKey: string;
}

/**
 * Renders items for the toolbar overflow menu
 */
// eslint-disable-next-line complexity, react/function-component-definition
export const ToolbarMenuItemRenderer: React.FunctionComponent<ToolbarMenuItemRendererProps> = props => {
  const itemTypes = ToolbarItemType;
  const { item } = props;
  const { hasIssue } = props;
  switch (props.item.type) {
    case itemTypes.NumericInput:
      // eslint-disable-next-line no-case-declarations
      const numericItem = item as NumericInputItem;
      // eslint-disable-next-line no-case-declarations
      const renderedNumeric = renderNumeric(numericItem);
      return (
        <MenuItem
          text={item.menuLabel ? item.menuLabel : item.label}
          icon={item.icon}
          key={props.menuKey}
          disabled={item.disabled}
        >
          {renderedNumeric}
        </MenuItem>
      );
    case itemTypes.IntervalPicker: {
      const intervalItem = item as IntervalPickerItem;
      return (
        <MenuItem text={item.label} icon={item.icon} key={props.menuKey} disabled={item.disabled}>
          <IntervalPicker
            renderStacked
            startDate={intervalItem.startDate}
            endDate={intervalItem.endDate}
            shortFormat={intervalItem.shortFormat}
            onNewInterval={(startDate, endDate) => intervalItem.onChange(startDate, endDate)}
            onApply={(startDate: Date, endDate: Date) => {
              intervalItem.onApplyButton(startDate, endDate);
              ContextMenu.hide();
            }}
            defaultIntervalInHours={intervalItem.defaultIntervalInHours}
          />
        </MenuItem>
      );
    }
    case itemTypes.DateRangePicker: {
      const dateRangeItem = item as DateRangePickerItem;
      return (
        <MenuItem
          text={dateRangeItem.label}
          icon={dateRangeItem.icon}
          key={props.menuKey}
          disabled={dateRangeItem.disabled}
        >
          <div className="date-range-picker__menu-popover">
            <DateRangePicker
              startTimeMs={dateRangeItem.startTimeMs}
              endTimeMs={dateRangeItem.endTimeMs}
              format={dateRangeItem.format}
              durations={dateRangeItem.durations}
              minStartTimeMs={dateRangeItem.minStartTimeMs}
              maxEndTimeMs={dateRangeItem.maxEndTimeMs}
              onNewInterval={(startTimeMs: number, endTimeMs: number) =>
                dateRangeItem.onChange(startTimeMs, endTimeMs)
              }
              onApply={(startTimeMs: number, endTimeMs: number) => {
                dateRangeItem.onApplyButton(startTimeMs, endTimeMs);
                ContextMenu.hide();
              }}
            />
          </div>
        </MenuItem>
      );
    }
    case itemTypes.Popover:
      // eslint-disable-next-line no-case-declarations
      const popoverItem = item as PopoverItem;
      return (
        <MenuItem
          text={item.menuLabel ? item.menuLabel : item.label}
          icon={item.icon}
          key={props.menuKey}
          disabled={item.disabled}
        >
          {popoverItem.popoverContent}
        </MenuItem>
      );
    case itemTypes.Button:
      // eslint-disable-next-line no-case-declarations
      const buttonItem = item as ButtonItem;
      return (
        <MenuItem
          text={item.label}
          icon={item.icon}
          disabled={buttonItem.disabled}
          onClick={() => buttonItem.onClick()}
          key={props.menuKey}
        />
      );
    case itemTypes.Switch:
      // eslint-disable-next-line no-case-declarations
      const switchItem = item as SwitchItem;
      // eslint-disable-next-line no-case-declarations
      const label = item.menuLabel ? item.menuLabel : item.label;
      return (
        <MenuItem
          text={label}
          icon={item.icon}
          key={props.menuKey}
          disabled={item.disabled}
          onClick={() => switchItem.onChange(!switchItem.value)}
        />
      );
    case itemTypes.Dropdown:
      // eslint-disable-next-line no-case-declarations
      const dropdownItem = item as DropdownItem;
      return (
        <MenuItem text={item.label} icon={item.icon} key={props.menuKey} disabled={item.disabled}>
          {dropdownItem.dropdownOptions
            ? Object.keys(dropdownItem.dropdownOptions).map(ekey => (
                <MenuItem
                  text={dropdownItem.dropdownOptions[ekey]}
                  key={ekey}
                  onClick={() => dropdownItem.onChange(dropdownItem.dropdownOptions[ekey])}
                  icon={
                    dropdownItem.value === dropdownItem.dropdownOptions[ekey]
                      ? IconNames.TICK
                      : undefined
                  }
                />
              ))
            : null}
        </MenuItem>
      );
    case itemTypes.ButtonGroup: {
      const buttonGroupItem = item as ButtonGroupItem;
      return (
        <MenuItem text={item.label} icon={item.icon} key={props.menuKey} disabled={item.disabled}>
          {buttonGroupItem.buttons
            ? buttonGroupItem.buttons.map(button => (
                <MenuItem
                  text={button.label}
                  icon={button.icon}
                  key={button.label}
                  disabled={button.disabled}
                  onClick={() => {
                    button.onClick();
                  }}
                />
              ))
            : null}
        </MenuItem>
      );
    }
    case itemTypes.LabelValue: {
      const lvItem = item as LabelValueItem;
      return (
        <MenuItem
          className={hasIssue ? 'toolbar-item--issue' : ''}
          title={hasIssue && item.tooltipForIssue ? item.tooltipForIssue : item.tooltip}
          key={props.menuKey}
          text={`${item.label && item.label.length > 0 ? `${item.label}: ` : ''}${lvItem.value}`}
          disabled={item.disabled}
        />
      );
    }
    case itemTypes.CheckboxList: {
      const cboxDropdown = item as CheckboxDropdownItem;
      return (
        <MenuItem
          text={item.menuLabel ? item.menuLabel : item.label}
          icon={item.icon}
          key={props.menuKey}
          disabled={item.disabled}
        >
          <CheckboxList
            enumToCheckedMap={cboxDropdown.values}
            enumToColorMap={cboxDropdown.colors}
            enumKeysToDisplayStrings={cboxDropdown.enumKeysToDisplayStrings}
            checkboxEnum={cboxDropdown.enumOfKeys}
            onChange={value => cboxDropdown.onChange(value)}
          />
        </MenuItem>
      );
    }
    case itemTypes.LoadingSpinner: {
      const lsItem = item as LoadingSpinnerItem;
      const displayString = `Loading ${lsItem.itemsToLoad} ${item.label}`;
      return <MenuItem key={props.menuKey} text={displayString} disabled={item.disabled} />;
    }
    case itemTypes.CustomItem: {
      const customItem = item as CustomItem;
      return (
        <MenuItem
          key={props.menuKey}
          text={customItem.label}
          disabled={customItem.disabled}
          icon={customItem.icon}
        >
          {customItem.element}
        </MenuItem>
      );
    }
    default:
      logger.error('Invalid type for menu item');
      return <MenuItem />;
  }
};
