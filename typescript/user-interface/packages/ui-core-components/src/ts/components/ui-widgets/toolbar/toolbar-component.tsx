// TODO: update the Popover to use Popover2
import { Button, Menu, Popover } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';

import { getSizeOfAllRenderedItems, renderOverflowMenuItem } from './toolbar-utils';
import type * as ToolbarTypes from './types';

const WIDTH_OF_OVERFLOW_BUTTON_PX = 46;

/**
 * The toolbar component (aka <Toolbar>)
 */
export class ToolbarComponent extends React.Component<
  ToolbarTypes.ToolbarProps,
  ToolbarTypes.ToolbarState
> {
  private toolbarItemRightRefs: HTMLElement[] = [];

  private toolbarItemLeftRefs: HTMLElement[] = [];

  private prevItemsSize = -1;

  private justResized = false;

  private constructor(props: ToolbarTypes.ToolbarProps) {
    super(props);

    const { minWhiteSpacePx } = props;

    this.state = {
      checkSizeOnNextDidMountOrDidUpdate: true,
      rightIndicesToOverflow: [],
      leftIndicesToOverflow: [],
      whiteSpaceAllotmentPx: minWhiteSpacePx || 0
    };
  }

  public componentDidMount(): void {
    const { checkSizeOnNextDidMountOrDidUpdate } = this.state;
    if (checkSizeOnNextDidMountOrDidUpdate) {
      this.handleResize();
    }
  }

  public componentDidUpdate(prevProps: ToolbarTypes.ToolbarProps): void {
    const { whiteSpaceAllotmentPx } = this.state;

    if (this.shouldHandleResize(prevProps)) {
      this.handleResize();
      this.justResized = true;
    } else {
      this.justResized = false;
    }

    this.prevItemsSize = getSizeOfAllRenderedItems(
      this.toolbarItemLeftRefs,
      this.toolbarItemRightRefs,
      whiteSpaceAllotmentPx
    );
  }

  /**
   * @param prevProps The previous props, from componentDidUpdate
   * @returns true if something may have changed the size or order of the toolbar items
   */
  private readonly shouldHandleResize = (prevProps: ToolbarTypes.ToolbarProps): boolean => {
    const { checkSizeOnNextDidMountOrDidUpdate } = this.state;
    const { toolbarWidthPx } = this.props;

    const haveItemsChanged =
      this.haveDifferentNumberOfItems(prevProps) ||
      this.haveItemsChangedOrder() ||
      this.haveItemsChangedSize();

    return (
      checkSizeOnNextDidMountOrDidUpdate ||
      (!this.justResized && (prevProps.toolbarWidthPx !== toolbarWidthPx || haveItemsChanged))
    );
  };

  /**
   * @returns true if the total size of all rendered toolbar items (not in overflow) has changed since
   * the last @function componentDidUpdate call.
   */
  private readonly haveItemsChangedSize = () => {
    const { whiteSpaceAllotmentPx } = this.state;
    return (
      this.prevItemsSize !==
      getSizeOfAllRenderedItems(
        this.toolbarItemLeftRefs,
        this.toolbarItemRightRefs,
        whiteSpaceAllotmentPx
      )
    );
  };

  /**
   * @param prevProps The previous props, from componentDidUpdate
   * @returns true if the items have different ranks, false otherwise.
   *       for now, return false as changing order is not yet supported
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly haveItemsChangedOrder = (): boolean => {
    return false;
  };

  /**
   * @param prevProps The previous props, from componentDidUpdate
   * @returns true if the number of items has changed, false otherwise.
   */
  private readonly haveDifferentNumberOfItems = (prevProps: ToolbarTypes.ToolbarProps): boolean => {
    const { itemsLeft, itemsRight } = this.props;

    // Check left items
    const leftChanged: boolean = prevProps.itemsLeft.length !== itemsLeft.length;

    // Check right items
    const rightChanged: boolean = prevProps.itemsRight?.length !== itemsRight?.length;

    return leftChanged || rightChanged;
  };

  /** Handles toolbar re-sizing to ensure elements are always accessible */
  private readonly handleResize = (): void => {
    const { whiteSpaceAllotmentPx, rightIndicesToOverflow, leftIndicesToOverflow } = this.state;
    const { toolbarWidthPx, minWhiteSpacePx, parentContainerPaddingPx } = this.props;
    // Account for parent padding
    const actualToolbarWidth = toolbarWidthPx - parentContainerPaddingPx;
    // Calculate the width of all rendered elements in the toolbar - our 'pixel debt'
    const totalWidth = getSizeOfAllRenderedItems(
      this.toolbarItemLeftRefs,
      this.toolbarItemRightRefs,
      whiteSpaceAllotmentPx
    );

    // Check to see how many pixels "over budget" the toolbar is
    const overflowWidthPx = totalWidth - actualToolbarWidth;
    let reduceWhiteSpaceTo = minWhiteSpacePx || 0;

    if (overflowWidthPx > 0) {
      // The first priority is to sacrifice whitespace, until the whitespace allocation === minWhiteSpacePx
      if (whiteSpaceAllotmentPx > reduceWhiteSpaceTo) {
        // The maximum amount of whitespace we can get rid of
        const reducibleWhiteSpacePx = whiteSpaceAllotmentPx - reduceWhiteSpaceTo;
        const reduceWhiteSpaceByPx =
          reducibleWhiteSpacePx <= overflowWidthPx ? reducibleWhiteSpacePx : overflowWidthPx;
        reduceWhiteSpaceTo = whiteSpaceAllotmentPx - reduceWhiteSpaceByPx;
        this.setState({
          whiteSpaceAllotmentPx: reduceWhiteSpaceTo,
          checkSizeOnNextDidMountOrDidUpdate: true
        });
      } else {
        // The next priority is to overflow right-aligned menu items into an overflow button
        this.handleOverflowItems(overflowWidthPx, rightIndicesToOverflow, leftIndicesToOverflow);
      }
    } else if (
      overflowWidthPx < 0 &&
      rightIndicesToOverflow.length === 0 &&
      leftIndicesToOverflow.length === 0
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

  /**
   * Helper function called by {@link handleResize} which handles
   * overflowing items in the event that there are too many items in the
   * toolbar to display all at once.
   *
   * @param overflowWidthPx Amount of pixels that the toolbar is "over budget."
   * @param rightIndicesToOverflow Right-aligned indices that should be overflowed.
   * @param leftIndicesToOverflow Left-aligned indices that should be overflowed.
   */
  private readonly handleOverflowItems = (
    overflowWidthPx: number,
    rightIndicesToOverflow: number[],
    leftIndicesToOverflow: number[]
  ): void => {
    // When we create an overflow button, it also takes up space, so we account for that
    let overflowWidthAndButtonPx = overflowWidthPx + WIDTH_OF_OVERFLOW_BUTTON_PX;

    // Loop backwards through our toolbar (higher rank = lower priority to render)
    for (
      let i = this.toolbarItemRightRefs.length - 1;
      i >= 0 && overflowWidthAndButtonPx > 0;
      i -= 1
    ) {
      // If the item is already overflowed, then removing it won't reduce our 'debt'
      if (rightIndicesToOverflow.indexOf(i) < 0) {
        overflowWidthAndButtonPx -=
          this.toolbarItemRightRefs[i]?.getBoundingClientRect()?.width ?? 0;
        // Push item to overflow list
        rightIndicesToOverflow.push(i);
      }
    }
    if (overflowWidthAndButtonPx > 0) {
      for (
        let i = this.toolbarItemLeftRefs.length - 1;
        i >= 0 && overflowWidthAndButtonPx > 0;
        i -= 1
      ) {
        // If the item is already overflowed, then removing it won't reduce our 'debt'
        if (leftIndicesToOverflow.indexOf(i) < 0) {
          overflowWidthAndButtonPx -=
            this.toolbarItemLeftRefs[i]?.getBoundingClientRect()?.width ?? 0;
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
  };

  /** Renders the left side of the toolbar */
  private renderLeft(): JSX.Element {
    const { itemsLeft } = this.props;
    const { leftIndicesToOverflow } = this.state;
    return (
      <div className="toolbar__left-group">
        {itemsLeft
          ? itemsLeft.map((item, index) => {
              if (leftIndicesToOverflow.indexOf(index) < 0) {
                return (
                  <div
                    key={item.key}
                    className="toolbar-item toolbar-item__left"
                    ref={ref => {
                      if (ref) {
                        this.toolbarItemLeftRefs.push(ref);
                      }
                    }}
                  >
                    {item}
                  </div>
                );
              }
              return undefined;
            })
          : null}
      </div>
    );
  }

  /** renders the right side of the toolbar */
  private renderRight(): JSX.Element {
    const { itemsLeft, itemsRight, overflowIcon } = this.props;
    const { leftIndicesToOverflow, rightIndicesToOverflow } = this.state;

    return (
      <div className="toolbar__right-group">
        {itemsRight?.map((item, index) => {
          if (rightIndicesToOverflow.indexOf(index) < 0) {
            return (
              <div
                key={item.key}
                className={`toolbar-item
                  ${item.props.hasIssue ? 'toolbar-item--issue' : ''}`}
                ref={ref => {
                  if (ref) {
                    this.toolbarItemRightRefs.push(ref);
                  }
                }}
              >
                {item}
              </div>
            );
          }
          return undefined;
        })}
        {/* Render overflow items within a Popover */}
        {(leftIndicesToOverflow.length > 0 || rightIndicesToOverflow.length > 0) && (
          <Popover
            captureDismiss
            content={
              <Menu>
                {leftIndicesToOverflow.length > 0 &&
                  itemsLeft
                    ?.slice(itemsLeft.length - leftIndicesToOverflow.length)
                    .map(i => renderOverflowMenuItem(i, i.key))}
                {rightIndicesToOverflow.length > 0 &&
                  itemsRight
                    ?.slice(itemsRight.length - rightIndicesToOverflow.length)
                    .map(i => renderOverflowMenuItem(i, i.key))}
              </Menu>
            }
          >
            <Button
              icon={overflowIcon || IconNames.MORE}
              className="toolbar-overflow-menu-button"
              data-cy="overflow-button"
              style={
                overflowIcon ? { width: '30px' } : { width: '30px', transform: 'rotate(90deg)' }
              }
            />
          </Popover>
        )}
      </div>
    );
  }

  /**
   * React component lifecycle.
   */
  public render(): JSX.Element {
    const { toolbarWidthPx, parentContainerPaddingPx } = this.props;
    const { whiteSpaceAllotmentPx } = this.state;
    this.toolbarItemRightRefs = [];
    this.toolbarItemLeftRefs = [];

    return (
      <div
        className="toolbar"
        style={{
          width: `${toolbarWidthPx - parentContainerPaddingPx}px`
        }}
      >
        {/* Left group */}
        {this.renderLeft()}
        {/* Center group */}
        {whiteSpaceAllotmentPx > 0 && (
          <div className="toolbar__center-group">
            <div className="toolbar__whitespace" style={{ width: `${whiteSpaceAllotmentPx}px` }} />
          </div>
        )}
        {/* Right group */}
        {this.renderRight()}
      </div>
    );
  }
}
