import { H1 } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { render } from '@testing-library/react';
import Immutable from 'immutable';
import React from 'react';

import { ToolbarComponent } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-component';
import { ButtonGroupToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/button-group-item';
import type { ButtonToolbarItemProps } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/button-item';
import { ButtonToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/button-item';
import { CheckboxDropdownToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/checkbox-dropdown-item';
import { DateRangePickerToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/date-range-picker-item';
import { DropdownToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/dropdown-item';
import { IntervalPickerToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/interval-picker-item';
import { LabelValueToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/label-value-item';
import { LoadingSpinnerToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { NumericInputToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/numeric-input-item';
import { PopoverButtonToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/popover-button-item';
import { SwitchToolbarItem } from '../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/switch-item';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const element = <H1>Test</H1>;

const buttonProps: ButtonToolbarItemProps = {
  key: 'button-item-1',
  disabled: false,
  label: 'Pan Left',
  tooltip: 'Pan waveforms to the left',
  icon: IconNames.ARROW_LEFT,
  onlyShowIcon: true,
  onButtonClick: () => jest.fn()
};

describe('Toolbar component', () => {
  test('Toolbar builds empty', () => {
    const { container } = render(
      <ToolbarComponent toolbarWidthPx={100} parentContainerPaddingPx={0} itemsLeft={[]} />
    );

    expect(container).toMatchSnapshot();
  });

  test('Toolbar builds left and right, no overflow', () => {
    const { container } = render(
      <ToolbarComponent
        toolbarWidthPx={1000}
        parentContainerPaddingPx={0}
        itemsLeft={[
          <ButtonToolbarItem
            key="button"
            onButtonClick={jest.fn()}
            tooltip="Button Hello"
            hasIssue={false}
          />
        ]}
        itemsRight={[
          <ButtonGroupToolbarItem
            key="buttongroup"
            buttons={[buttonProps]}
            tooltip="Button Group Hello"
            hasIssue={false}
          />
        ]}
      />
    );

    expect(container).toMatchSnapshot();
  });

  test('Toolbar builds and overflows left side', () => {
    const { container } = render(
      <ToolbarComponent
        toolbarWidthPx={10}
        parentContainerPaddingPx={0}
        itemsLeft={[
          <ButtonToolbarItem
            key="button"
            onButtonClick={jest.fn()}
            tooltip="Button Hello"
            hasIssue={false}
            widthPx={50}
          />,
          <ButtonGroupToolbarItem
            key="buttongroup"
            buttons={[buttonProps]}
            tooltip="Button Group Hello"
            hasIssue={false}
            widthPx={50}
          />,
          <CheckboxDropdownToolbarItem
            key="checkboxdropdown"
            values={Immutable.Map<string, boolean>()}
            enumOfKeys={undefined}
            onChange={jest.fn()}
            tooltip="Hello CheckboxDropdown"
            hasIssue={false}
            widthPx={50}
          />,
          <DateRangePickerToolbarItem
            key="datepicker"
            startTimeMs={100}
            endTimeMs={1000}
            onChange={jest.fn()}
            format={undefined}
            onApplyButton={jest.fn()}
            tooltip="Hello Date Range"
            hasIssue={false}
            widthPx={50}
          />,
          <DropdownToolbarItem
            key="dropdown"
            dropdownOptions={[]}
            value="Hello Dropdown"
            onChange={jest.fn()}
            tooltip="Hello Dropdown"
            hasIssue={false}
            widthPx={50}
          />,
          <IntervalPickerToolbarItem
            key="intervalpicker"
            defaultIntervalInHours={12}
            startDate={new Date(100)}
            endDate={new Date(1000)}
            onChange={jest.fn()}
            onApplyButton={jest.fn()}
            tooltip="Hello Interval"
            widthPx={50}
          />,
          <LabelValueToolbarItem
            key="labelvalue"
            labelValue=""
            tooltip="Hello label"
            label="label"
            menuLabel="menu label"
            widthPx={50}
          />,
          <LoadingSpinnerToolbarItem
            key="loadingspinner"
            itemsToLoad={1}
            tooltip="Hello LoadingSpinner"
            hasIssue={false}
            widthPx={50}
          />,
          <NumericInputToolbarItem
            key="numericinput"
            numericValue={42}
            minMax={{
              max: 43,
              min: 41
            }}
            onChange={jest.fn()}
            tooltip="Hello Numeric"
            hasIssue={false}
            widthPx={50}
          />,
          <PopoverButtonToolbarItem
            key="popover"
            popoverContent={element}
            onPopoverDismissed={jest.fn()}
            tooltip="Hello Popover"
            hasIssue={false}
            widthPx={50}
          />,
          <SwitchToolbarItem key="switch-test" switchValue onChange={jest.fn()} widthPx={50} />
        ]}
      />
    );

    expect(container).toMatchSnapshot();
  });

  test('Toolbar builds and overflows right side', () => {
    const { container } = render(
      <ToolbarComponent
        toolbarWidthPx={10}
        parentContainerPaddingPx={0}
        itemsLeft={[]}
        itemsRight={[
          <ButtonToolbarItem
            key="button"
            onButtonClick={jest.fn()}
            tooltip="Button Hello"
            hasIssue={false}
            widthPx={50}
          />,
          <ButtonGroupToolbarItem
            key="buttongroup"
            buttons={[buttonProps]}
            tooltip="Button Group Hello"
            hasIssue={false}
            widthPx={50}
          />,
          <CheckboxDropdownToolbarItem
            key="checkboxdropdown"
            values={Immutable.Map<string, boolean>()}
            enumOfKeys={undefined}
            onChange={jest.fn()}
            tooltip="Hello CheckboxDropdown"
            hasIssue={false}
            widthPx={50}
          />,
          <DateRangePickerToolbarItem
            key="datepicker"
            startTimeMs={100}
            endTimeMs={1000}
            onChange={jest.fn()}
            format={undefined}
            onApplyButton={jest.fn()}
            tooltip="Hello Date Range"
            hasIssue={false}
            widthPx={50}
          />,
          <DropdownToolbarItem
            key="dropdown"
            dropdownOptions={[]}
            value="Hello Dropdown"
            onChange={jest.fn()}
            tooltip="Hello Dropdown"
            hasIssue={false}
            widthPx={50}
          />,
          <IntervalPickerToolbarItem
            key="intervalpicker"
            defaultIntervalInHours={12}
            startDate={new Date(100)}
            endDate={new Date(1000)}
            onChange={jest.fn()}
            onApplyButton={jest.fn()}
            tooltip="Hello Interval"
            widthPx={50}
          />,
          <LabelValueToolbarItem
            key="labelvalue"
            labelValue=""
            tooltip="Hello label"
            label="label"
            menuLabel="menu label"
            widthPx={50}
          />,
          <LoadingSpinnerToolbarItem
            key="loadingspinner"
            itemsToLoad={1}
            tooltip="Hello LoadingSpinner"
            hasIssue={false}
            widthPx={50}
          />,
          <NumericInputToolbarItem
            key="numericinput"
            numericValue={42}
            minMax={{
              max: 43,
              min: 41
            }}
            onChange={jest.fn()}
            tooltip="Hello Numeric"
            hasIssue={false}
            widthPx={50}
          />,
          <PopoverButtonToolbarItem
            key="popover"
            popoverContent={element}
            onPopoverDismissed={jest.fn()}
            tooltip="Hello Popover"
            hasIssue={false}
            widthPx={50}
          />,
          <SwitchToolbarItem key="switch-test" switchValue onChange={jest.fn()} widthPx={50} />
        ]}
      />
    );

    expect(container).toMatchSnapshot();
  });

  test('Toolbar builds left and right, overflow both sides', () => {
    const { container } = render(
      <ToolbarComponent
        toolbarWidthPx={10}
        parentContainerPaddingPx={0}
        itemsLeft={[
          <ButtonToolbarItem
            key="button"
            onButtonClick={jest.fn()}
            tooltip="Button Hello"
            hasIssue={false}
            widthPx={50}
          />,
          <ButtonGroupToolbarItem
            key="buttongroup"
            buttons={[buttonProps]}
            tooltip="Button Group Hello"
            hasIssue={false}
            widthPx={50}
          />,
          <CheckboxDropdownToolbarItem
            key="checkboxdropdown"
            values={Immutable.Map<string, boolean>()}
            enumOfKeys={undefined}
            onChange={jest.fn()}
            tooltip="Hello CheckboxDropdown"
            hasIssue={false}
            widthPx={50}
          />,
          <DateRangePickerToolbarItem
            key="datepicker"
            startTimeMs={100}
            endTimeMs={1000}
            onChange={jest.fn()}
            format={undefined}
            onApplyButton={jest.fn()}
            tooltip="Hello Date Range"
            hasIssue={false}
            widthPx={50}
          />,
          <DropdownToolbarItem
            key="dropdown"
            dropdownOptions={[]}
            value="Hello Dropdown"
            onChange={jest.fn()}
            tooltip="Hello Dropdown"
            hasIssue={false}
            widthPx={50}
          />
        ]}
        itemsRight={[
          <IntervalPickerToolbarItem
            key="intervalpicker"
            defaultIntervalInHours={12}
            startDate={new Date(100)}
            endDate={new Date(1000)}
            onChange={jest.fn()}
            onApplyButton={jest.fn()}
            tooltip="Hello Interval"
            widthPx={50}
          />,
          <LabelValueToolbarItem
            key="labelvalue"
            labelValue=""
            tooltip="Hello label"
            label="label"
            menuLabel="menu label"
            widthPx={50}
          />,
          <LoadingSpinnerToolbarItem
            key="loadingspinner"
            itemsToLoad={1}
            tooltip="Hello LoadingSpinner"
            hasIssue={false}
            widthPx={50}
          />,
          <NumericInputToolbarItem
            key="numericinput"
            numericValue={42}
            minMax={{
              max: 43,
              min: 41
            }}
            onChange={jest.fn()}
            tooltip="Hello Numeric"
            hasIssue={false}
            widthPx={50}
          />,
          <PopoverButtonToolbarItem
            key="popover"
            popoverContent={element}
            onPopoverDismissed={jest.fn()}
            tooltip="Hello Popover"
            hasIssue={false}
            widthPx={50}
          />,
          <SwitchToolbarItem key="switch-test" switchValue onChange={jest.fn()} widthPx={50} />
        ]}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
