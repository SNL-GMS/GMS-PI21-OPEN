/* eslint-disable react/destructuring-assignment */
import { IconNames } from '@blueprintjs/icons';
import React from 'react';

import { DeprecatedToolbar, DeprecatedToolbarTypes } from '../components';
import { ToolbarPopover } from './toolbar-popover';

interface ToolbarExampleState {
  popoverSwitch: boolean;
  dropdownSelected: string;
  toolbarSwitch: boolean;
  numericValue: number;
  toolbarWidthPx: number;
  startDate: number;
  endDate: number;
}

enum ExampleEnum {
  ExampleValue1 = 'Example Value One',
  ExampleValue2 = 'Example Value Two',
  ExampleValue3 = 'Example Value Three'
}
/**
 * Example of using the form that actually accepts input
 */
export class ToolbarExample extends React.Component<unknown, ToolbarExampleState> {
  private popoverRef: ToolbarPopover;

  public constructor(props: unknown) {
    super(props);
    this.state = {
      popoverSwitch: false,
      dropdownSelected: ExampleEnum.ExampleValue1,
      toolbarSwitch: false,
      numericValue: 10,
      toolbarWidthPx: 1000,
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      startDate: Date.now(),
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      endDate: Date.now()
    };
  }

  /**
   * React render method
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const popover = (
      <ToolbarPopover
        ref={ref => {
          if (ref) {
            this.popoverRef = ref;
          }
        }}
        defaultValue={this.state.popoverSwitch}
        onChange={val => {
          this.setState({ popoverSwitch: val });
        }}
      />
    );
    const items: DeprecatedToolbarTypes.ToolbarItem[] = [];
    const numericInput: DeprecatedToolbarTypes.NumericInputItem = {
      rank: 1,
      value: this.state.numericValue,
      minMax: { min: 1, max: 100 },
      step: 1,
      type: DeprecatedToolbarTypes.ToolbarItemType.NumericInput,
      onChange: value => {
        this.setState({ numericValue: value });
      },
      label: 'Numeric Input',
      tooltip: 'Accepts values between 1 and 100'
    };
    items.push(numericInput);
    const popoverItem: DeprecatedToolbarTypes.PopoverItem = {
      rank: 3,
      type: DeprecatedToolbarTypes.ToolbarItemType.Popover,
      popoverContent: popover,
      onPopoverDismissed: () => {
        if (this.popoverRef) {
          const popoverState = this.popoverRef.getState();
          this.setState({ popoverSwitch: popoverState.switch });
        }
      },
      label: 'Popover',
      tooltip: 'A checkbox in a popover to demonstrate how we do popovers'
    };
    items.push(popoverItem);
    const buttonItemWithIcon: DeprecatedToolbarTypes.ButtonItem = {
      rank: 4,
      type: DeprecatedToolbarTypes.ToolbarItemType.Button,
      label: 'Generic Button',
      icon: IconNames.THUMBS_UP,
      tooltip: 'Button with Icon',
      onClick: () => {
        // eslint-disable-next-line no-alert
        alert('Button Pressed');
      }
    };
    items.push(buttonItemWithIcon);
    const iconButton: DeprecatedToolbarTypes.ButtonItem = {
      rank: 5,
      type: DeprecatedToolbarTypes.ToolbarItemType.Button,
      label: 'Just an icon',
      icon: IconNames.THUMBS_DOWN,
      onlyShowIcon: true,
      tooltip: 'Just an icon',
      onClick: () => {
        // eslint-disable-next-line no-alert
        alert('Thumbs down button pressed');
      }
    };
    items.push(iconButton);
    const switchItem: DeprecatedToolbarTypes.SwitchItem = {
      label: 'On/Off',
      menuLabel: this.state.toolbarSwitch ? 'Turn Off' : 'Turn On',
      value: this.state.toolbarSwitch,
      tooltip: 'Toggle a switch',
      type: DeprecatedToolbarTypes.ToolbarItemType.Switch,
      rank: 6,
      onChange: value => {
        this.setState({ toolbarSwitch: value });
      }
    };
    items.push(switchItem);
    return (
      <div>
        <div
          className="ag-dark"
          style={{
            flex: '1 1 auto',
            position: 'relative',
            width: `${this.state.toolbarWidthPx + 4}px`,
            display: 'flex',
            justifyContent: 'flex-begin'
          }}
        >
          <DeprecatedToolbar
            toolbarWidthPx={this.state.toolbarWidthPx}
            // eslint-disable-next-line @typescript-eslint/no-magic-numbers
            minWhiteSpacePx={100}
            itemsLeft={[
              {
                rank: 1,
                type: DeprecatedToolbarTypes.ToolbarItemType.Dropdown,
                tooltip: 'Select something from a dropdown',
                label: 'enum',
                widthPx: 200
              }
            ]}
            itemsRight={items}
          />
          {/* eslint-disable-next-line jsx-a11y/no-static-element-interactions */}
          <div
            style={{
              borderRight: '2px white solid',
              paddingRight: '2px'
            }}
            className="toolbar-example__divider"
            onMouseDown={this.onThumbnailDividerDrag}
          />
        </div>
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Toolbar Width: ${this.state.toolbarWidthPx}`}
          <br />
        </div>
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Toggle: ${this.state.toolbarSwitch}`}
          <br />
        </div>
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Popover: ${this.state.popoverSwitch}`}
          <br />
        </div>
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Dropdown: ${this.state.dropdownSelected}`}
          <br />
        </div>
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Numeric Input: ${this.state.numericValue}`}
          <br />
        </div>
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Start Date: ${String(this.state.startDate)}`}
          <br />
        </div>
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`End Date: ${String(this.state.endDate)}`}
          <br />
        </div>
      </div>
    );
  }

  /**
   * Start a drag on mouse down on the divider
   */
  private readonly onThumbnailDividerDrag = (e: React.MouseEvent<HTMLDivElement>) => {
    let prevPosition = e.clientX;
    let currentPos = e.clientX;
    let diff = 0;
    const minWidthPx = 100;
    const onMouseMove = (e2: MouseEvent) => {
      currentPos = e2.clientX;
      diff = prevPosition - currentPos;
      prevPosition = currentPos;
      const widthPx = this.state.toolbarWidthPx - diff;
      if (widthPx >= minWidthPx) {
        this.setState({ toolbarWidthPx: widthPx });
      }
    };

    const onMouseUp = () => {
      document.body.removeEventListener('mousemove', onMouseMove);
      document.body.removeEventListener('mouseup', onMouseUp);
    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  };
}
