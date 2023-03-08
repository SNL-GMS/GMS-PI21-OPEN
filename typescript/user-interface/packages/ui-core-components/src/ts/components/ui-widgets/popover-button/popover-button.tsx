/* eslint-disable react/destructuring-assignment */
import { Alignment, Button, ContextMenu, Icon, MenuItem } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';

import type { PopoverProps, PopoverState } from './types';
/**
 * Renders button in toolbar that creates and dismisses popovers
 * Not for external use
 */
export class PopoverButtonComponent extends React.Component<PopoverProps, PopoverState> {
  /** Internal reference to the button container */
  private internalRef: HTMLDivElement;

  private constructor(props) {
    super(props);
    this.state = {
      isExpanded: false
    };
  }

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const widthStr = this.props.widthPx ? `${this.props.widthPx}px` : undefined;
    const element = (
      <div
        ref={ref => {
          if (ref) {
            this.internalRef = ref;
          }
        }}
        data-cy={this.props.cyData}
      >
        {this.props.renderAsMenuItem ? (
          <MenuItem
            disabled={this.props.disabled}
            icon={IconNames.MENU_OPEN}
            text={this.props.label}
            label="opens dialog"
            onClick={event => {
              event.stopPropagation();
              this.togglePopover();
            }}
          />
        ) : (
          <Button
            title={this.props.tooltip}
            disabled={this.props.disabled}
            onClick={() => {
              this.togglePopover();
              if (this.props.onClick) {
                this.props.onClick(this.internalRef);
              }
            }}
            active={this.state.isExpanded}
            style={{ width: widthStr }}
            alignText={this.props.onlyShowIcon ? Alignment.CENTER : Alignment.LEFT}
            className={this.props.onlyShowIcon ? 'toolbar-button--icon-only' : 'toolbar-button'}
          >
            <span>{this.props.onlyShowIcon ? null : this.props.label}</span>
            <Icon title={false} icon={this.props.icon ? this.props.icon : IconNames.CHEVRON_DOWN} />
          </Button>
        )}
      </div>
    );
    return element;
  }

  /**
   * Returns if the popover is expanded
   *
   * @returns boolean
   */
  public isExpanded = (): boolean => this.state.isExpanded;

  /**
   * Toggles the popover
   *
   * @param leftOff left offset to render popover
   * @param topSet top offset to render popover
   */
  public togglePopover = (leftOffset?: number, topOffset?: number): void => {
    if (this.state.isExpanded) {
      ContextMenu.hide();
      this.setState({ isExpanded: false });
    } else {
      const left = this.props.renderAsMenuItem
        ? // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
          this.internalRef.getBoundingClientRect().left + this.internalRef.scrollWidth
        : this.internalRef.getBoundingClientRect().left;
      // The plus four is a chosen offset - has no real world meaning
      const top = this.props.renderAsMenuItem
        ? this.internalRef.getBoundingClientRect().top
        : // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
          this.internalRef.getBoundingClientRect().top + this.internalRef.scrollHeight + 4;
      ContextMenu.hide();
      // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
      ContextMenu.show(
        this.props.popupContent,
        {
          left: leftOffset || left,
          top: topOffset || top
        },
        () => {
          // TODO need to onblur for the popup itself
          if (this.props.onPopoverDismissed) {
            this.props.onPopoverDismissed();
          }
          this.setState({ isExpanded: false });
        },
        true
      );
      this.setState({ isExpanded: true });
    }
  };
}
