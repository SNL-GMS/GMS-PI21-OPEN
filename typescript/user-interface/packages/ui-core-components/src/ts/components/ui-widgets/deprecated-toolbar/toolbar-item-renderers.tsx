import { Alignment, Button, Icon, Switch } from '@blueprintjs/core';
import * as React from 'react';

import { CheckboxList } from '../checkbox-list/checkbox-list';
import { DateRangePicker } from '../date-range-picker';
import { DropDown } from '../drop-down';
import { IntervalPicker } from '../interval-picker';
import { LabelValue } from '../label-value';
import { LoadingSpinner } from '../loading-spinner';
import { NumericInput } from '../numeric-input';
import { PopoverButton } from '../popover-button';
import type {
  ButtonGroupItem,
  ButtonItem,
  CheckboxDropdownItem,
  DateRangePickerItem,
  DropdownItem,
  IntervalPickerItem,
  LabelValueItem,
  LoadingSpinnerItem,
  NumericInputItem,
  PopoverItem,
  SwitchItem
} from './types';

/**
 * TODO: add support for a default value.
 * A collection of "last mile" renderers for toolbar items
 */
export const renderDropdown = (dropdownItem: DropdownItem): JSX.Element => (
  <div style={dropdownItem.style ? dropdownItem.style : {}}>
    <DropDown
      key={dropdownItem.rank}
      onMaybeValue={value => dropdownItem.onChange(value)}
      value={dropdownItem.value}
      custom={dropdownItem.custom}
      dropDownItems={dropdownItem.dropdownOptions}
      dropdownText={dropdownItem.dropdownText}
      disabled={dropdownItem.disabled}
      widthPx={dropdownItem.widthPx}
      title={dropdownItem.tooltip}
      data-cy={dropdownItem.cyData}
      displayLabel={dropdownItem.displayLabel}
      label={dropdownItem.label}
    />
  </div>
);

export const renderNumeric = (numericItem: NumericInputItem): JSX.Element => (
  <div style={numericItem.style ? numericItem.style : {}}>
    <span key={numericItem.rank}>
      {!numericItem.labelRight && numericItem?.label ? (
        <span className="toolbar-numeric__label toolbar-numeric__label-left">
          {numericItem.label}
        </span>
      ) : null}
      <NumericInput
        value={numericItem.value}
        minMax={numericItem.minMax}
        disabled={numericItem.disabled}
        onChange={val => {
          numericItem.onChange(val);
        }}
        widthPx={numericItem.widthPx}
        step={numericItem.step}
        tooltip={numericItem.tooltip}
        cyData={numericItem.cyData}
      />
      {numericItem.labelRight ? (
        <span className="toolbar-numeric__label">{numericItem.labelRight}</span>
      ) : null}
    </span>
  </div>
);

export const renderIntervalPicker = (intervalItem: IntervalPickerItem): JSX.Element => (
  <div
    className="toolbar-button--capsule"
    key={intervalItem.rank}
    style={intervalItem.style ? intervalItem.style : {}}
  >
    <IntervalPicker
      shortFormat={intervalItem.shortFormat}
      startDate={intervalItem.startDate}
      endDate={intervalItem.endDate}
      onNewInterval={(startDate, endDate) => intervalItem.onChange(startDate, endDate)}
      onApply={(startDate: Date, endDate: Date) => intervalItem.onApplyButton(startDate, endDate)}
      defaultIntervalInHours={intervalItem.defaultIntervalInHours}
      data-cy={intervalItem.cyData}
    />
  </div>
);

/**
 * updated render for DateRangePicker
 * leverages legacy toolbar approach
 */
export const renderDateRangePicker = (intervalItem: DateRangePickerItem): JSX.Element => (
  <div
    className="toolbar-button--capsule"
    key={intervalItem.rank}
    style={intervalItem.style ? intervalItem.style : {}}
  >
    <DateRangePicker
      startTimeMs={intervalItem.startTimeMs}
      endTimeMs={intervalItem.endTimeMs}
      format={intervalItem.format}
      durations={intervalItem.durations}
      minStartTimeMs={intervalItem.minStartTimeMs}
      maxEndTimeMs={intervalItem.maxEndTimeMs}
      onNewInterval={(startTimeMs: number, endTimeMs: number) =>
        intervalItem.onChange(startTimeMs, endTimeMs)
      }
      onApply={(startTimeMs: number, endTimeMs: number) =>
        intervalItem.onApplyButton(startTimeMs, endTimeMs)
      }
    />
  </div>
);

export const renderPopoverButton = (
  popoverItem: PopoverItem,
  addToPopoverMap: any
): JSX.Element => (
  <div style={popoverItem.style ? popoverItem.style : {}}>
    <PopoverButton
      label={popoverItem.label}
      tooltip={popoverItem.tooltip}
      key={popoverItem.rank}
      icon={popoverItem.icon}
      onlyShowIcon={popoverItem.onlyShowIcon}
      disabled={popoverItem.disabled}
      popupContent={popoverItem.popoverContent}
      onPopoverDismissed={() => popoverItem.onPopoverDismissed()}
      widthPx={popoverItem.widthPx}
      ref={ref => {
        if (ref) {
          addToPopoverMap(popoverItem.rank, ref);
        }
      }}
      data-cy={popoverItem.cyData}
    />
  </div>
);

// eslint-disable-next-line complexity
export const renderButton = (buttonItem: ButtonItem, marginRight?: number): JSX.Element => {
  const widthAsString = buttonItem.widthPx ? `${buttonItem.widthPx}px` : undefined;
  const width = widthAsString || (buttonItem.onlyShowIcon ? '30px' : undefined);
  return (
    <div style={buttonItem.style ? buttonItem.style : {}} key={buttonItem.rank}>
      <Button
        disabled={buttonItem.disabled}
        key={buttonItem.rank}
        alignText={
          // eslint-disable-next-line no-nested-ternary
          buttonItem.onlyShowIcon
            ? buttonItem.labelRight
              ? Alignment.LEFT
              : Alignment.CENTER
            : Alignment.LEFT
        }
        onClick={() => buttonItem.onClick()}
        title={buttonItem.tooltip}
        onMouseEnter={() => buttonItem.onMouseEnter && buttonItem.onMouseEnter()}
        onMouseOut={() => buttonItem.onMouseOut && buttonItem.onMouseOut()}
        style={{
          width,
          marginRight: marginRight ? `${marginRight}px` : undefined
        }}
        data-cy={buttonItem.cyData}
        className={buttonItem.onlyShowIcon ? 'toolbar-button--icon-only' : 'toolbar-button'}
      >
        {buttonItem.onlyShowIcon ? undefined : <span key="1">{buttonItem.label}</span>}
        {buttonItem.icon ? <Icon icon={buttonItem.icon} title={false} key="2" /> : null}
        {!buttonItem.labelRight ? undefined : <span key="3">{buttonItem.labelRight}</span>}
      </Button>
    </div>
  );
};

export const renderSwitch = (switchItem: SwitchItem): JSX.Element => (
  <div
    className="toolbar-switch"
    title={switchItem.tooltip}
    key={switchItem.rank}
    style={switchItem.style ? switchItem.style : {}}
  >
    <div className="toolbar-switch-label">{`${switchItem.label}:`}</div>
    <Switch
      title={switchItem.tooltip}
      disabled={switchItem.disabled}
      className={`toolbar-switch__blueprint ${switchItem.label.toLowerCase().replace(' ', '-')}`}
      checked={switchItem.value}
      large
      onChange={e => switchItem.onChange(e.currentTarget.checked)}
      data-cy={switchItem.cyData}
    />
  </div>
);

export const renderButtonGroup = (groupItem: ButtonGroupItem): JSX.Element => {
  const indexOfLastButton = groupItem.buttons.length - 1;
  return (
    <div
      className="toolbar-button-group"
      key={groupItem.rank}
      data-cy={groupItem.cyData}
      style={groupItem.style ? groupItem.style : {}}
    >
      {groupItem.buttons
        ? groupItem.buttons.map((button, index) =>
            renderButton(button, index !== indexOfLastButton ? 2 : 0)
          )
        : null}
    </div>
  );
};

export const renderLabelValue = (labelValue: LabelValueItem, hasIssue = false): JSX.Element => (
  <div className="toolbar-label-value" style={labelValue.style ? labelValue.style : {}}>
    <LabelValue
      label={labelValue.label}
      value={labelValue.value}
      ianApp={labelValue.ianApp}
      tooltip={
        hasIssue && labelValue.tooltipForIssue ? labelValue.tooltipForIssue : labelValue.tooltip
      }
      styleForValue={labelValue.styleForValue}
      valueColor={labelValue.valueColor}
      data-cy={labelValue.cyData}
    />
  </div>
);

export const renderCheckboxDropdown = (
  checkboxItem: CheckboxDropdownItem,
  addToPopoverMap: any
): JSX.Element => (
  <div style={checkboxItem.style ? checkboxItem.style : {}}>
    <PopoverButton
      label={checkboxItem.label}
      tooltip={checkboxItem.tooltip}
      key={checkboxItem.rank}
      disabled={checkboxItem.disabled}
      cyData={checkboxItem.cyData}
      popupContent={
        <CheckboxList
          enumToCheckedMap={checkboxItem.values}
          enumToColorMap={checkboxItem.colors}
          checkboxEnum={checkboxItem.enumOfKeys}
          enumKeysToDisplayStrings={checkboxItem.enumKeysToDisplayStrings}
          onChange={value => checkboxItem.onChange(value)}
        />
      }
      onPopoverDismissed={() => {
        if (checkboxItem.onPopoverDismissed) {
          checkboxItem.onPopoverDismissed();
        }
      }}
      widthPx={checkboxItem.widthPx}
      onClick={ref => {
        if (checkboxItem.onPopUp) {
          checkboxItem.onPopUp(ref);
        }
      }}
      ref={ref => {
        if (ref) {
          addToPopoverMap(checkboxItem.rank, ref);
        }
      }}
    />
  </div>
);

export const renderLoadingSpinner = (spinnerItem: LoadingSpinnerItem): JSX.Element => (
  <div style={spinnerItem.style ? spinnerItem.style : {}}>
    <LoadingSpinner
      hideTheWordLoading={spinnerItem.hideTheWordLoading}
      onlyShowSpinner={spinnerItem.onlyShowIcon ? spinnerItem.onlyShowIcon : false}
      itemsLoaded={spinnerItem.itemsLoaded}
      itemsToLoad={spinnerItem.itemsToLoad}
      label={spinnerItem.label}
      widthPx={spinnerItem.widthPx}
      key={spinnerItem.rank}
      data-cy={spinnerItem.cyData}
    />
  </div>
);
