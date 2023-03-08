/* eslint-disable class-methods-use-this */
/* eslint-disable react/destructuring-assignment */
import {
  DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  secondsToString
} from '@gms/common-util';
import type {
  CellClickedEvent,
  GridReadyEvent,
  StringValueGetter,
  StringValueGetterParams
} from '@gms/ui-core-components';
import { InfiniteTable, useDatasource } from '@gms/ui-core-components';
import defer from 'lodash/defer';
import delay from 'lodash/delay';
import isEqual from 'lodash/isEqual';
import throttle from 'lodash/throttle';
import * as React from 'react';

import { getHeaderHeight, getRowHeightWithBorder } from '~components/common-ui/common/table-utils';

import { buildColumnDefs, defaultColumnDefinition } from './column-definitions';
import { NewMessageIndicator } from './new-message-indicator';
import type {
  SystemMessageColumnDefinition,
  SystemMessageInfiniteTableDataType,
  SystemMessageTableProps,
  SystemMessageTableState
} from './types';

/** The system message table component */
export class SystemMessageTable extends React.Component<
  SystemMessageTableProps,
  SystemMessageTableState
> {
  /**
   * The ID applied to the div wrapping the AG Grid table
   */
  private static readonly TABLE_ID: string = 'system-message-table';

  private static hasUnseenMessages = false;

  /** A reference to the table component. */
  private table: InfiniteTable<{ id: string; severity: string }, unknown>;

  /** The table column definitions */
  private readonly columnDefinitions: SystemMessageColumnDefinition[];

  /** The timer for auto scrolling - used to indicate when auto scrolling is complete */
  private readonly autoScrollTimer: ReturnType<typeof setTimeout>;

  /**
   * An amount of time to wait before scrolling after a sort.
   * This allows sort to finish and display before an auto-scroll event happens.
   * This is necessary because AG Grid fires the onSortChanged event before
   * the table has been full sorted, for some reason. The upshot of this
   * is that we cannot call tableApi.ensureIndexIsVisible when onSortChanged
   * fires, because that index doesn't exist.
   */
  private readonly AUTO_SCROLL_AFTER_SORT_DELAY_MS: number = 200;

  /**
   * The amount of time to delay auto-scrolling after the datasource has updated.
   * This allows the grid to catch up and "know" how many rows exist.
   */
  private readonly SCROLL_TO_VISIBLE_TIMEOUT_MS: number = 64;

  /**
   * The number of px that a user must scroll up in order to disable auto scroll
   */
  private readonly SCROLL_UP_THRESHOLD_PX: number = 40;

  /**
   * A queue of callbacks to call after sorting has finished.
   */
  private readonly actionQueueAfterSort: (() => void)[] = [];

  /**
   * The number of times we'll call the function that tries to auto-scroll to the most recent system message
   */
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  private SET_TIMEOUT_RETRIES = 10;

  /**
   * constructor
   */
  public constructor(props: SystemMessageTableProps) {
    super(props);
    this.columnDefinitions = buildColumnDefs(
      this.timeValueGetter,
      this.categoryValueGetter,
      this.subcategoryValueGetter,
      this.severityValueGetter,
      this.messageValueGetter
    );
    this.state = {
      systemMessages: [],
      prevScrollPositions: [0, 0],
      isScrolledToLatest: true
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Updates the derived state from the next props.
   *
   * @param nextProps The next (new) props
   * @param prevState The previous state
   */
  public static getDerivedStateFromProps(
    nextProps: SystemMessageTableProps,
    prevState: SystemMessageTableState
  ): Partial<SystemMessageTableState> {
    const stateModifications: Partial<SystemMessageTableState> = {
      systemMessages: nextProps.systemMessages
    };

    const messagesHaveChanged = !isEqual(nextProps.systemMessages, prevState.systemMessages);

    const messagesHaveBeenCleared =
      nextProps.systemMessages?.length < prevState.systemMessages?.length && messagesHaveChanged;

    const isScrolledToLatest = prevState.isScrolledToLatest || messagesHaveBeenCleared;

    if (nextProps.systemMessages?.length && !isScrolledToLatest && messagesHaveChanged) {
      // if we are not scrolled to the end and we have unseen messages
      SystemMessageTable.hasUnseenMessages = true;
      stateModifications.prevScrollPositions = [0, 0];
    } else if (messagesHaveBeenCleared) {
      // if we have had messages removed
      stateModifications.prevScrollPositions = [0, 0];
      stateModifications.isScrolledToLatest = true;
      if (nextProps.systemMessages.length === 0) {
        SystemMessageTable.hasUnseenMessages = false;
      }
    }
    return stateModifications;
  }

  /**
   * React lifecycle `shouldComponentUpdate`.
   * Determines if the component should update based on the next props passed in.
   *
   * @param nextProps props for the System Message Table
   *
   * @returns boolean
   */
  public shouldComponentUpdate(
    nextProps: SystemMessageTableProps,
    nextState: SystemMessageTableState
  ): boolean {
    // the component should only update (render) if the data has changed
    const messagesHaveChanged = !isEqual(this.state.systemMessages, nextState.systemMessages);

    return (
      messagesHaveChanged ||
      nextProps.isAutoScrollingEnabled !== this.props.isAutoScrollingEnabled ||
      SystemMessageTable.hasUnseenMessages
    );
  }

  /**
   * React lifecycle `componentDidUpdate`.
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   */
  // eslint-disable-next-line complexity
  public componentDidUpdate(
    prevProps: SystemMessageTableProps,
    prevState: SystemMessageTableState
  ): void {
    // We have unseen messages if we aren't scrolled to the end and if the messages have changed.
    const haveMessagesChanged = !isEqual(prevState.systemMessages, this.state.systemMessages);
    const messagesHaveBeenCleared =
      this.state.systemMessages?.length < prevState.systemMessages?.length && haveMessagesChanged;

    if (messagesHaveBeenCleared) {
      if (this.table && this.table.getTableApi()) {
        this.table.getTableApi().refreshInfiniteCache();
      }
      this.forceUpdate();
    }
    const isScrolledToLatest = this.isScrolledToLatest();
    // if auto-scrolling has turned on since last update, or if we have unseen messages and
    // auto scrolling is on
    if (
      this.table &&
      this.props.isAutoScrollingEnabled &&
      (prevProps.isAutoScrollingEnabled !== this.props.isAutoScrollingEnabled ||
        SystemMessageTable.hasUnseenMessages)
    ) {
      // auto scroll to the latest
      this.scrollToLatest();
    } else if (this.state.isScrolledToLatest !== isScrolledToLatest) {
      // only not an infinite loop because we shouldComponentUpdate prevents this from causing a rerender
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ isScrolledToLatest });
    }
    this.trackScrollDelta();
  }

  public componentWillUnmount(): void {
    clearTimeout(this.autoScrollTimer);
  }

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div
        id={SystemMessageTable.TABLE_ID}
        className="system-message-table"
        data-cy={SystemMessageTable.TABLE_ID}
      >
        <this.TableWithDatasource>
          <InfiniteTable<SystemMessageInfiniteTableDataType, any>
            ref={ref => {
              this.table = ref;
            }}
            debug={false}
            rowData={this.state.systemMessages}
            alwaysShowVerticalScroll
            headerHeight={getHeaderHeight()}
            rowHeight={getRowHeightWithBorder()}
            suppressCellFocus
            defaultColDef={defaultColumnDefinition}
            columnDefs={this.columnDefinitions}
            onBodyScroll={throttle(this.onBodyScroll)}
            onCellContextMenu={this.onCellContextMenu}
            onCellClicked={this.onCellClicked}
            overlayNoRowsTemplate="No System Messages"
            pagination={false}
            suppressScrollOnNewData
            onGridReady={this.onGridReady}
            onSortChanged={this.onSortChanged}
          />
        </this.TableWithDatasource>
        <NewMessageIndicator
          isVisible={this.isNewMessageIndicatorVisible()}
          handleNewMessageIndicatorClick={() => this.onNewMessageIndicatorClick()}
        />
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * A wrapper component that provides a datasource as a prop to its child.
   */
  private readonly TableWithDatasource: React.FunctionComponent<
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    React.PropsWithChildren<any>
  > = props => {
    // !FIX ESLINT REACT HOOK HOOKS MUST BE IN FUNCTIONAL COMPONENT
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const datasource = useDatasource(this.state.systemMessages);
    /**
     * We store the child in a variable so we can check its type with the
     * isValidElement type guard below.
     */
    const child: React.ReactNode = props.children;

    /**
     * Verify that the children are indeed a React Element (not just a node)
     */
    if (React.isValidElement(child)) {
      /**
       * React.cloneElement is used to inject the new props into the child. This injects the
       * the datasource as a prop into the child.
       * CloneElement should be reasonably performant.
       * See https://stackoverflow.com/questions/54922160/react-cloneelement-in-list-performance
       */
      return <>{React.cloneElement(child, { datasource })}</>;
    }
    return undefined;
  };

  /**
   * Event handler - on grid ready event for the system messages table
   *
   * @param params the event parameters
   */
  private readonly onGridReady = (params: GridReadyEvent): void => {
    params.columnApi.autoSizeColumn('message');
  };

  /**
   * Sorts the table by the time column in the order provided. Defaults to 'asc'.
   * Orders are 'asc' and 'desc'
   */
  private readonly sortByTime = (order: 'asc' | 'desc' = 'asc') => {
    if (this.table.getColumnApi()) {
      this.table.getColumnApi().applyColumnState({
        state: [{ colId: 'time', sort: order }],
        defaultState: { sort: null }
      });
    }
  };

  /**
   * Event handler - on sort order changed for the system messages
   *
   * @param params the event parameters
   */
  private readonly onSortChanged = (): void => {
    const sort = this.getSortOrderForTime();
    if (sort !== 'asc') {
      // no longer sorting by time time first; disable auto scrolling
      this.props.setIsAutoScrollingEnabled(false);
    }
    while (this.actionQueueAfterSort.length > 0) {
      const callback = this.actionQueueAfterSort.pop();
      defer(callback);
    }
    // perform a force update to ensure that new message indicator is updated
    this.forceUpdate();
  };

  /**
   * sorts by ascending order if there are messages
   * that are not already sorted in asc order
   */
  private readonly maybeSortByAsc = () => {
    const sort = this.getSortOrderForTime();
    if (this.state.systemMessages && sort !== 'asc') {
      this.sortByTime('asc');
    }
  };

  /**
   * Tries to scroll to the latest row. This will try to re-schedule the call to auto-scroll
   * if an exception was thrown because the table is unavailable. This will only be attempted
   * ten times.
   */
  private readonly timeout = () => {
    const tableApi = this.table.getTableApi();
    /**
     * We need to delay execution of this function
     * so that we can be sure the grid has updated from
     * and the rows are "available" to auto-scroll to.
     */
    if (this.props.isAutoScrollingEnabled) {
      this.SET_TIMEOUT_RETRIES -= 1;
      try {
        tableApi.ensureIndexVisible(this.state.systemMessages.length - 1, 'bottom');
      } catch {
        if (this.SET_TIMEOUT_RETRIES > 0) {
          setTimeout(() => this.timeout(), this.SCROLL_TO_VISIBLE_TIMEOUT_MS);
        }
      }
    }
  };

  /**
   * Scrolls to the latest row. Sorts by asc if needed.
   */
  private readonly scrollToLatest = () => {
    this.setState({ isScrolledToLatest: true });
    SystemMessageTable.hasUnseenMessages = false;
    this.SET_TIMEOUT_RETRIES = 10;
    if (this.state.systemMessages) {
      this.maybeSortByAsc();
      setTimeout(() => this.timeout(), this.SCROLL_TO_VISIBLE_TIMEOUT_MS);
    }
  };

  /**
   * Event called when the table scrolls. Pauses scrolling if the
   * scroll event is not an auto-scroll event.
   * If it sees a scroll up, assumes it must be a user-intended scroll, and pauses, even
   * if this occurs during auto scroll. This is to ensure that a user
   * who forcefully scrolls up during an auto-scroll can disable scrolling.
   *
   * @param event table scroll event
   */
  private readonly onBodyScroll = (): void => {
    this.trackScrollDelta();
    if (this.state.systemMessages?.length) {
      if (this.getScrollDirection() === 'up') {
        if (SystemMessageTable.hasUnseenMessages) {
          SystemMessageTable.hasUnseenMessages = !this.isScrolledToLatest();
        }
        this.props.setIsAutoScrollingEnabled(false);
      }
    }
  };

  /**
   * Looks at the last tracked scroll event to see in what direction it moved
   */
  private readonly getScrollDirection = (
    a: number = this.state.prevScrollPositions[0],
    b: number = this.state.prevScrollPositions[1]
  ): 'up' | 'down' | 'unchanged' => {
    if (a < b) return 'down';
    if (b < a - this.SCROLL_UP_THRESHOLD_PX) return 'up';
    return 'unchanged';
  };

  /**
   * Track the amount the scroll position has changed, if we are in auto-scroll mode.
   * This looks to see if we have scrolled up more than a configured threshold, and if so,
   * disables auto scroll.
   */
  private readonly trackScrollDelta = () => {
    // We don't need to watch for auto scroll
    if (!this.props.isAutoScrollingEnabled) return;

    const scrollContainer = this.getScrollContainer();
    if (!scrollContainer) return;

    const currentScrollPosPx = scrollContainer.scrollTop;
    const farthestScrollPositionPx = Math.max(
      this.state.prevScrollPositions[0],
      currentScrollPosPx
    );

    // If we have scrolled up more than the threshold amount
    if (farthestScrollPositionPx - currentScrollPosPx > this.SCROLL_UP_THRESHOLD_PX) {
      this.setState({ prevScrollPositions: [currentScrollPosPx, currentScrollPosPx] });
      this.props.setIsAutoScrollingEnabled(false);
    } else {
      this.setState({ prevScrollPositions: [farthestScrollPositionPx, currentScrollPosPx] });
    }
  };

  /**
   * AG Grid does not provide a ref to the scroll container DOM Node. Therefore, in order
   * to determine the amount scrolled, we need to reference the DOM directly.
   */
  private readonly getScrollContainer = (): HTMLElement =>
    document.getElementById(SystemMessageTable.TABLE_ID)?.querySelector('.ag-body-viewport');

  /**
   * @returns true if the table is scrolled to the bottom of the current page; false otherwise
   */
  private readonly isScrolledToBottom = () => {
    const scrollContainer = this.getScrollContainer();
    return scrollContainer
      ? // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
        scrollContainer.offsetHeight + scrollContainer.scrollTop === scrollContainer.scrollHeight
      : false;
  };

  /**
   * @returns true if the table is scrolled to the top of the current page; false otherwise
   */
  private readonly isScrolledToTop = () => {
    const scrollContainer = this.getScrollContainer();
    return scrollContainer.scrollTop === 0;
  };

  /**
   * @returns true if the table has scrolled to the latest, i.e. all the way
   * to the top or all the way to the bottom depending on the sorting; false otherwise
   */
  private readonly isScrolledToLatest = (): boolean => {
    const sort = this.getSortOrderForTime();
    // eslint-disable-next-line no-nested-ternary
    return sort ? (sort === 'asc' ? this.isScrolledToBottom() : this.isScrolledToTop()) : false;
  };

  /**
   * if there are messages, have we seen them all?
   */
  private readonly isNewMessageIndicatorVisible = () =>
    !this.props.isAutoScrollingEnabled &&
    SystemMessageTable.hasUnseenMessages &&
    this.state.systemMessages?.length > 0;

  /**
   * If the new message indicator is clicked, sort by time if not sorted by time,
   * and then scroll to the latest message and enable auto scrolling.
   */
  private readonly onNewMessageIndicatorClick = () => {
    if (this.getSortOrderForTime() !== 'asc') {
      this.sortByTime('asc');
      this.actionQueueAfterSort.push(() =>
        delay(() => {
          this.props.setIsAutoScrollingEnabled(true);
          SystemMessageTable.hasUnseenMessages = false;
        }, this.AUTO_SCROLL_AFTER_SORT_DELAY_MS)
      );
    } else {
      this.props.setIsAutoScrollingEnabled(true);
      SystemMessageTable.hasUnseenMessages = false;
    }
  };

  /**
   * Returns the sort order for the system message timestamp
   *
   * @returns 'asc', 'desc' or undefined
   */
  private readonly getSortOrderForTime = (): string | undefined => {
    const model = this.table?.getColumnApi()?.getColumnState();
    const timestampModel = model && model.length > 0 && model.find(m => m.colId === 'time');
    return timestampModel ? timestampModel.sort : undefined;
  };

  /**
   * The time value getter for a system message table cell.
   *
   * @param params the table cell parameters
   */
  private readonly timeValueGetter: StringValueGetter = (
    params: StringValueGetterParams
  ): string => {
    const time = this.state.systemMessages?.find(msg => params.data && msg?.id === params.data?.id)
      ?.time;
    return time
      ? secondsToString(time / 1000, DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
      : '';
  };

  /**
   * The category value getter for a system message table cell.
   *
   * @param params the table cell parameters
   */
  private readonly categoryValueGetter: StringValueGetter = (
    params: StringValueGetterParams
  ): string =>
    this.state.systemMessages?.find(msg => params.data && msg.id === params.data.id)?.category;

  /**
   * The subcategory value getter for a system message table cell.
   *
   * @param params the table cell parameters
   */
  private readonly subcategoryValueGetter: StringValueGetter = (
    params: StringValueGetterParams
  ): string =>
    this.state.systemMessages?.find(msg => params.data && msg.id === params.data.id)?.subCategory;

  /**
   * The severity value getter for a system message table cell.
   *
   * @param params the table cell parameters
   */
  private readonly severityValueGetter: StringValueGetter = (
    params: StringValueGetterParams
  ): string =>
    this.state.systemMessages?.find(msg => params.data && msg.id === params.data.id)?.severity;

  /**
   * The message value getter for a system message table cell.
   *
   * @param params the table cell parameters
   */
  private readonly messageValueGetter: StringValueGetter = (
    params: StringValueGetterParams
  ): string =>
    this.state.systemMessages?.find(msg => params.data && msg.id === params.data.id)?.message;

  /**
   * Table event handler for handling on cell click events
   *
   * @param params the table event parameters
   */
  private readonly onCellClicked = (
    params: CellClickedEvent<{ id: string; severity: string }, unknown, unknown>
  ): void => {
    if (params.node.isSelected()) {
      params.api.getSelectedNodes().forEach(node => node.selectThisNode(false));
      params.node.selectThisNode(false);
    } else {
      params.api.getSelectedNodes().forEach(node => node.selectThisNode(false));
      params.node.selectThisNode(true);
    }
  };

  /**
   * Table event handler for handling on cell context menu events
   *
   * @param params the table event parameters
   */
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  private readonly onCellContextMenu = (): void => {};
}
