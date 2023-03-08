/* eslint-disable react/destructuring-assignment */
import { Button, Menu, Popover } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { UILogger } from '@gms/ui-util';
import React from 'react';

import type { PopoverButton } from '../popover-button';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { getOverflowMenuItems, getSizeOfAllRenderedItems, renderItem } from './toolbar-utils';
import type { ToolbarItem, ToolbarProps, ToolbarState } from './types';

const logger = UILogger.create('GMS_LOG_TOOLBAR', process.env.GMS_LOG_TOOLBAR);

const WIDTH_OF_OVERFLOW_BUTTON_PX = 46;
const AMOUNT_OF_SPACE_TO_RESERVE_PX = 16;
export class ToolbarComponent extends React.Component<ToolbarProps, ToolbarState> {
  private toolbarItemRightRefs: HTMLElement[] = [];

  private toolbarItemLeftRefs: HTMLElement[] = [];

  private popoverButtonMap: Map<number, PopoverButton>;

  private prevItemsSize = -1;

  private justResized = false;

  private constructor(props) {
    super(props);
    this.props.itemsRight.forEach((item, index) => {
      this.props.itemsRight.forEach((itemB, indexB) => {
        if (index !== indexB && item.rank === itemB.rank) {
          logger.warn('Toolbar Error: Item ranks must be unique - change item ranks to be unique');
        }
      });
    });
    this.state = {
      checkSizeOnNextDidMountOrDidUpdate: true,
      rightIndicesToOverflow: [],
      leftIndicesToOverflow: [],
      whiteSpaceAllotmentPx: this.props.minWhiteSpacePx ? this.props.minWhiteSpacePx : 0
    };
    this.popoverButtonMap = new Map<number, PopoverButton>();
  }

  public componentDidMount(): void {
    if (this.state.checkSizeOnNextDidMountOrDidUpdate) {
      this.handleResize();
    }
  }

  public componentDidUpdate(prevProps: ToolbarProps): void {
    if (this.shouldHandleResize(prevProps)) {
      this.handleResize();
      this.justResized = true;
    } else {
      this.justResized = false;
    }
    this.prevItemsSize = getSizeOfAllRenderedItems(
      this.toolbarItemLeftRefs,
      this.toolbarItemRightRefs,
      this.state.whiteSpaceAllotmentPx
    );
  }

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const sortedItems = [...this.props.itemsRight].sort((a, b) => a.rank - b.rank);
    this.toolbarItemRightRefs = [];
    this.toolbarItemLeftRefs = [];
    this.popoverButtonMap = new Map<number, PopoverButton>();
    return (
      <div
        className="toolbar"
        style={{
          width: `${this.props.toolbarWidthPx}px`
        }}
      >
        <div className="toolbar__left-group">
          {this.props.itemsLeft
            ? this.props.itemsLeft.map((item, index) => {
                if (this.state.leftIndicesToOverflow.indexOf(index) < 0) {
                  return (
                    <div
                      key={item.rank}
                      className="toolbar-item toolbar-item__left"
                      ref={ref => {
                        if (ref) {
                          this.toolbarItemLeftRefs.push(ref);
                        }
                      }}
                    >
                      {renderItem(item as ToolbarItem, this.popoverButtonMap)}
                    </div>
                  );
                }
                return undefined;
              })
            : null}
        </div>
        {this.props.itemsRight && (
          <div className="toolbar__center-group">
            <div
              className="toolbar__whitespace"
              style={{
                width: `${this.state.whiteSpaceAllotmentPx}px`
              }}
            />
          </div>
        )}
        <div className="toolbar__right-group">
          {sortedItems.map((item: ToolbarItem, index: number) => {
            if (this.state.rightIndicesToOverflow.indexOf(index) < 0) {
              return (
                <div
                  key={item.rank}
                  className={`toolbar-item
                    ${item.hasIssue ? 'toolbar-item--issue' : ''}`}
                  ref={ref => {
                    if (ref) {
                      this.toolbarItemRightRefs.push(ref);
                    }
                  }}
                >
                  {renderItem(item, this.popoverButtonMap, item.hasIssue)}
                </div>
              );
            }
            return undefined;
          })}
          {(this.state.rightIndicesToOverflow.length > 0 ||
            this.state.leftIndicesToOverflow.length > 0) && (
            <Popover
              captureDismiss
              content={
                <Menu>
                  {getOverflowMenuItems(
                    this.props.itemsRight as ToolbarItem[],
                    this.props.itemsLeft as ToolbarItem[],
                    this.state.rightIndicesToOverflow,
                    this.state.leftIndicesToOverflow
                  )}
                </Menu>
              }
            >
              <Button
                icon={this.props.overflowIcon ? this.props.overflowIcon : IconNames.MORE}
                className={`toolbar-overflow-menu-button ${
                  // Checks if any of the overflowed items have an issue
                  this.state.rightIndicesToOverflow
                    .map(index => sortedItems[index].hasIssue)
                    .reduce((accum, val) => accum || val, false)
                    ? 'toolbar-overflow-menu-button--issue'
                    : ''
                }`}
                data-cy="overflow-button"
                style={
                  this.props.overflowIcon
                    ? { width: '30px' }
                    : { width: '30px', transform: 'rotate(90deg)' }
                }
              />
            </Popover>
          )}
        </div>
      </div>
    );
  }

  /**
   * @param prevProps The previous props, from componentDidUpdate
   * @returns true if something may have changed the size or order of the toolbar items
   */
  private readonly shouldHandleResize = (prevProps: ToolbarProps): boolean => {
    const haveItemsChanged =
      this.haveDifferentNumberOfItems(prevProps) ||
      this.haveItemsChangedOrder(prevProps) ||
      this.haveItemsChangedSize();

    return (
      this.state.checkSizeOnNextDidMountOrDidUpdate ||
      (!this.justResized &&
        (prevProps.toolbarWidthPx !== this.props.toolbarWidthPx || haveItemsChanged))
    );
  };

  /**
   * @returns true if the total size of all rendered toolbar items (not in overflow) has changed since
   * the last @function componentDidUpdate call.
   */
  private readonly haveItemsChangedSize = () => {
    return (
      this.prevItemsSize !==
      getSizeOfAllRenderedItems(
        this.toolbarItemLeftRefs,
        this.toolbarItemRightRefs,
        this.state.whiteSpaceAllotmentPx
      )
    );
  };

  /**
   * @param prevProps The previous props, from componentDidUpdate
   * @returns true if the items have different ranks, false otherwise.
   */
  private readonly haveItemsChangedOrder = (prevProps: ToolbarProps): boolean => {
    return prevProps.itemsRight.reduce<boolean>(
      (hasChanged: boolean, prevItem: ToolbarItem, index: number) => {
        if (hasChanged) {
          return true;
        }
        const currItem = this.props.itemsRight[index];
        return prevItem.rank !== currItem.rank;
      },
      false
    );
  };

  /**
   * @param prevProps The previous props, from componentDidUpdate
   * @returns true if the number of items has changed, false otherwise.
   */
  private readonly haveDifferentNumberOfItems = (prevProps: ToolbarProps): boolean =>
    prevProps.itemsRight.length !== this.props.itemsRight.length;

  /* Handles toolbar re-sizing to ensure elements are always accessible */
  // eslint-disable-next-line complexity
  private readonly handleResize = () => {
    // Calculate the width of all rendered elements in the toolbar - our 'pixel debt'
    const totalWidth = getSizeOfAllRenderedItems(
      this.toolbarItemLeftRefs,
      this.toolbarItemRightRefs,
      this.state.whiteSpaceAllotmentPx
    );
    // Check to see how many pixels "over budget" the toolbar is
    let overflowWidthPx = totalWidth - this.props.toolbarWidthPx + AMOUNT_OF_SPACE_TO_RESERVE_PX;
    let reduceWhiteSpaceTo = this.props.minWhiteSpacePx ? this.props.minWhiteSpacePx : 0;

    if (overflowWidthPx > 0) {
      // The first priority is to sacrifice whitespace, until the whitespace allocation === minWhiteSpacePx
      if (this.state.whiteSpaceAllotmentPx > reduceWhiteSpaceTo) {
        // The maximum amount of whitespace we can get rid of
        const reducibleWhiteSpacePx = this.state.whiteSpaceAllotmentPx - reduceWhiteSpaceTo;
        const reduceWhiteSpaceByPx =
          reducibleWhiteSpacePx <= overflowWidthPx ? reducibleWhiteSpacePx : overflowWidthPx;
        reduceWhiteSpaceTo = this.state.whiteSpaceAllotmentPx - reduceWhiteSpaceByPx;
        this.setState({
          whiteSpaceAllotmentPx: reduceWhiteSpaceTo,
          checkSizeOnNextDidMountOrDidUpdate: true
        });
      } else {
        // The next priority is to overflow right-aligned menu items into an overflow button
        // When we create an overflow button, it also takes up space, so we account for that
        overflowWidthPx += WIDTH_OF_OVERFLOW_BUTTON_PX;
        const { rightIndicesToOverflow, leftIndicesToOverflow } = this.state;
        // Loop backwards through our toolbar (higher rank = lower priority to render)
        for (let i = this.toolbarItemRightRefs.length - 1; i >= 0 && overflowWidthPx > 0; i -= 1) {
          // If the item is already overflowed, then removing it won't reduce our 'debt'
          if (this.state.rightIndicesToOverflow.indexOf(i) < 0) {
            overflowWidthPx -= this.toolbarItemRightRefs[i]?.getBoundingClientRect()?.width ?? 0;
            // Push item to overflow list
            rightIndicesToOverflow.push(i);
          }
        }
        if (overflowWidthPx > 0) {
          for (let i = this.toolbarItemLeftRefs.length - 1; i >= 0 && overflowWidthPx > 0; i -= 1) {
            // If the item is already overflowed, then removing it won't reduce our 'debt'
            if (this.state.leftIndicesToOverflow.indexOf(i) < 0) {
              overflowWidthPx -= this.toolbarItemLeftRefs[i]?.getBoundingClientRect()?.width ?? 0;
              // Push item to overflow list
              leftIndicesToOverflow.push(i);
            }
          }
        }
        this.setState({
          rightIndicesToOverflow,
          leftIndicesToOverflow,
          checkSizeOnNextDidMountOrDidUpdate: false
        });
      }
    } else if (
      overflowWidthPx < 0 &&
      this.state.rightIndicesToOverflow.length === 0 &&
      this.state.leftIndicesToOverflow.length === 0
    ) {
      // If we have excess overflow to start, then we add to whitespace and end
      const surplus = Math.floor(Math.abs(overflowWidthPx));
      this.setState(prevState => ({
        rightIndicesToOverflow: [],
        leftIndicesToOverflow: [],
        whiteSpaceAllotmentPx: prevState.whiteSpaceAllotmentPx + surplus,
        checkSizeOnNextDidMountOrDidUpdate: false
      }));
    } else if (overflowWidthPx !== 0) {
      this.setState(preState => ({
        rightIndicesToOverflow: [],
        leftIndicesToOverflow: [],
        whiteSpaceAllotmentPx: reduceWhiteSpaceTo,
        checkSizeOnNextDidMountOrDidUpdate: !preState.checkSizeOnNextDidMountOrDidUpdate
      }));
    } else {
      this.setState({ checkSizeOnNextDidMountOrDidUpdate: false });
    }
  };
}
