/* eslint-disable react/destructuring-assignment */
import { SohTypes } from '@gms/common-model';
import { setDecimalPrecisionAsNumber } from '@gms/common-util';
import type { CellClickedEvent, CellContextMenuEvent, CellEvent } from '@gms/ui-core-components';
import { Table } from '@gms/ui-core-components';
import type Immutable from 'immutable';
import includes from 'lodash/includes';
import isEqual from 'lodash/isEqual';
import uniqWith from 'lodash/uniqWith';
import memoizeOne from 'memoize-one';
import React from 'react';

import { useBaseDisplaySize } from '~components/common-ui/components/base-display/base-display-hooks';
import { dataAcquisitionUIConfig } from '~components/data-acquisition-ui/config';
import { messageConfig } from '~components/data-acquisition-ui/config/message-config';
import type { QuietAction } from '~components/data-acquisition-ui/shared/context-menus/quieting-menu';
import { showQuietingContextMenu } from '~components/data-acquisition-ui/shared/context-menus/quieting-menu';
import type { SohContextData } from '~components/data-acquisition-ui/shared/soh-context';
import { SohContext } from '~components/data-acquisition-ui/shared/soh-context';
import type { CellData } from '~components/data-acquisition-ui/shared/table/types';
import {
  getDataReceivedStatusRollup,
  getHeaderHeight,
  getRowHeightWithBorder,
  sharedSohTableClasses
} from '~components/data-acquisition-ui/shared/table/utils';
import { FilterableSOHTypes } from '~components/data-acquisition-ui/shared/toolbars/soh-toolbar';
import type { Offset } from '~components/data-acquisition-ui/shared/types';
import { convertSohMonitorTypeToAceiMonitorType } from '~components/data-acquisition-ui/shared/utils';
import { gmsLayout } from '~scss-config/layout-preferences';

import type {
  ChannelValueGetterParams,
  EnvironmentColumnDefinition,
  MonitorTypeValueGetterParams
} from './soh-column-definitions';
import {
  defaultColumnDefinition,
  getEnvironmentColumnDefinitions,
  headerNameMonitorType
} from './soh-column-definitions';
import {
  getChannelSohToDisplay,
  getEnvironmentTableRows,
  getPerChannelEnvRollup
} from './soh-environment-utils';
import type { EnvironmentTableContext, EnvironmentTableRow } from './types';
import { EnvironmentTableDataContext } from './types';

export interface EnvironmentPanelProps {
  channelSohs: SohTypes.ChannelSoh[];
  monitorStatusesToDisplay: Record<FilterableSOHTypes, boolean>;
  channelStatusesToDisplay: Record<FilterableSOHTypes, boolean>;
  defaultQuietDurationMs: number;
  quietingDurationSelections: number[];
  isStale: boolean;
  stationName: string;
}

export interface EnvironmentPanelState {
  selectedChannelMonitorPairs: SohTypes.ChannelMonitorPair[];
}

/**
 * Soh environment panel used to process data and props coming and pass it to core table
 */
export class EnvironmentPanel extends React.PureComponent<
  EnvironmentPanelProps,
  EnvironmentPanelState
> {
  /** The SOH context type */
  // eslint-disable-next-line react/static-property-placement
  public static readonly contextType: React.Context<SohContextData> = SohContext;

  /** The SOH Context */
  // eslint-disable-next-line react/static-property-placement
  public declare context: React.ContextType<typeof SohContext>;

  /** Some size constants */
  private readonly TOP_HEADER_HEIGHT_PX: number = 112;

  private readonly PADDING_PX: number = gmsLayout.displayPaddingPx * 2;

  /** A reference to the table component. */
  public table: Table<{ id: string }, EnvironmentTableContext>;

  /** memoize get row data */
  private readonly memoizeGetRows: (
    monitorStatusesToDisplay: Record<FilterableSOHTypes, boolean>,
    channelSohs: SohTypes.ChannelSoh[]
  ) => { data: EnvironmentTableRow[] };

  /** A memoized function for building the environment table column definitions. */
  private readonly memoizedGetEnvironmentColumnDefinitions: (
    channelNames: string[],
    monitorTypeValueGetter: (params: MonitorTypeValueGetterParams) => string,
    channelValueGetter: (params: ChannelValueGetterParams) => number,
    namesOfChannelsToHide: string[]
  ) => EnvironmentColumnDefinition[];

  /** A memoized function for building the environment table rows. */
  private readonly memoizeGetEnvironmentTableRows: (
    channels: SohTypes.ChannelSoh[],
    selectedChannelMonitorPairs: SohTypes.ChannelMonitorPair[],
    aceiType: SohTypes.AceiType
  ) => EnvironmentTableRow[];

  /** Constructor */
  public constructor(props: EnvironmentPanelProps) {
    super(props);
    this.memoizedGetEnvironmentColumnDefinitions = memoizeOne(
      getEnvironmentColumnDefinitions,
      isEqual
    );
    this.memoizeGetRows = memoizeOne(
      (
        monitorStatusesToDisplay: Immutable.Map<FilterableSOHTypes, boolean>,
        channelSohs: SohTypes.ChannelSoh[]
      ) => {
        return { data: this.getRows(monitorStatusesToDisplay, channelSohs) };
      }
    );
    this.memoizeGetEnvironmentTableRows = memoizeOne(getEnvironmentTableRows, isEqual);
    this.state = {
      selectedChannelMonitorPairs: []
    };
  }

  /**
   * React lifecycle `componentDidUpdate`.
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(): void {
    this.maybeRemoveHiddenChannelColumns();
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * React lifecycle `render`. Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const currentRows = this.memoizeGetRows(
      this.props.monitorStatusesToDisplay,
      this.props.channelSohs
    );
    return (
      <this.TableDisplayWrapper>
        <EnvironmentTableDataContext.Provider value={currentRows}>
          <Table<{ id: string }, EnvironmentTableContext>
            ref={ref => {
              this.table = ref;
            }}
            debug={false}
            headerHeight={getHeaderHeight()}
            rowHeight={getRowHeightWithBorder()}
            suppressRowClickSelection
            suppressCellFocus
            context={{
              selectedChannelMonitorPairs: this.state.selectedChannelMonitorPairs,
              rollupStatusByChannelName: new Map(
                this.props.channelSohs.map(channel => [
                  channel.channelName,
                  getPerChannelEnvRollup(channel)
                ])
              ),
              dataReceivedByChannelName: this.getDataReceivedForChannelName()
            }}
            defaultColDef={defaultColumnDefinition}
            columnDefs={this.getColumnDefinitions()}
            // provide just the row ids to the table;
            // use the react context to update the cells for performance (and memory) benefits
            rowData={currentRows.data.map(r => ({ id: r.id }))}
            onCellContextMenu={this.onCellContextMenu}
            onCellClicked={this.onCellClicked}
            overlayNoRowsTemplate={messageConfig.table.noDataMessage}
          />
        </EnvironmentTableDataContext.Provider>
      </this.TableDisplayWrapper>
    );
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  private readonly TableDisplayWrapper: React.FunctionComponent<
    React.PropsWithChildren<unknown>
  > = props => {
    // !FIX ESLINT DO NOT USE REACT HOOK HOOKS IN CONDITIONAL
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const [, heightPx] = useBaseDisplaySize();
    const minHeightPx = Math.min(
      heightPx - (this.TOP_HEADER_HEIGHT_PX + this.PADDING_PX),
      dataAcquisitionUIConfig.dataAcquisitionUserPreferences.minChartHeightPx
    );
    return (
      <div
        className={`soh-environment-table ${sharedSohTableClasses} table--cell-selection-only`}
        style={{ minHeight: minHeightPx }}
      >
        {props.children}
      </div>
    );
  };

  private readonly maybeRemoveHiddenChannelColumns = () => {
    if (this.table && this.table.getColumnApi()) {
      // update the column visibility based on the filter
      const channelToDisplay = getChannelSohToDisplay(
        this.props.channelSohs,
        this.props.channelStatusesToDisplay
      );
      this.table
        .getColumnApi()
        .getAllColumns()
        .forEach(col => {
          const visible =
            channelToDisplay.find(
              c => col.getColId() === headerNameMonitorType || c.channelName === col.getColId()
            ) !== undefined;
          this.table.setColumnVisible(col.getColId(), visible);
        });
    }
  };

  /**
   * Returns the column definitions for the environment table.
   */
  private readonly getColumnDefinitions = (): EnvironmentColumnDefinition[] =>
    this.memoizedGetEnvironmentColumnDefinitions(
      this.props.channelSohs.map(soh => soh.channelName).sort(),
      this.monitorTypeValueGetter,
      this.channelValueGetter,
      this.props.channelSohs
        .filter(ch => !this.props.channelStatusesToDisplay[ch.channelSohStatus])
        .map(soh => soh.channelName)
        .sort()
    );

  /**
   * Returns the rows for the environment table
   */
  private readonly getRows = (
    monitorStatusesToDisplay: Record<FilterableSOHTypes, boolean>,
    channelSohs: SohTypes.ChannelSoh[]
  ): EnvironmentTableRow[] => {
    const rowsUnfiltered = this.memoizeGetEnvironmentTableRows(
      channelSohs,
      this.state.selectedChannelMonitorPairs,
      this.context.selectedAceiType
    );

    const isMonitorStatusVisible = (row: EnvironmentTableRow): boolean =>
      monitorStatusesToDisplay[FilterableSOHTypes[row.monitorStatus]];

    return rowsUnfiltered.filter(isMonitorStatusVisible).filter(
      r =>
        // !CR25474 always filter out the following Monitor Types from the Environment Table
        !includes(
          [
            SohTypes.SohMonitorType.ENV_CLOCK_DIFFERENTIAL_IN_MICROSECONDS,
            SohTypes.AceiType.ENV_LAST_GPS_SYNC_TIME,
            SohTypes.SohMonitorType.ENV_STATION_POWER_VOLTAGE
          ],
          r.monitorType
        )
    );
  };

  /**
   * Selects the table cells based on the channel monitor pair and the event.
   *
   * @param event the table event that caused the action
   * @param channelMonitorPair the channel monitor pair to select
   */
  private readonly selectCells = (
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    params: CellEvent<{ id: string }, EnvironmentTableContext, any>,
    shouldRemoveIfExisting: boolean,
    callback?: () => void
  ): SohTypes.ChannelMonitorPair[] => {
    const data = this.getRows(this.props.monitorStatusesToDisplay, this.props.channelSohs).find(
      d => d.id === params.data.id
    );

    const channelMonitorPair: SohTypes.ChannelMonitorPair = {
      channelName: params.colDef.colId,
      monitorType: data.monitorType
    };

    // determine if the channel monitor pair is already selected
    const isAlreadySelected: boolean =
      channelMonitorPair &&
      this.state.selectedChannelMonitorPairs.find(
        cm =>
          cm.channelName === channelMonitorPair.channelName &&
          cm.monitorType === channelMonitorPair.monitorType
      ) !== undefined;

    const { event } = params;
    let selectedChannelMonitorPairs =
      // eslint-disable-next-line no-nested-ternary
      event.metaKey || event.ctrlKey || (isAlreadySelected && !shouldRemoveIfExisting)
        ? (event.metaKey || event.ctrlKey) && isAlreadySelected && shouldRemoveIfExisting
          ? // eslint-disable-next-line react/no-access-state-in-setstate
            [...this.state.selectedChannelMonitorPairs].filter(
              cm =>
                !(
                  cm.channelName === channelMonitorPair.channelName &&
                  cm.monitorType === channelMonitorPair.monitorType
                )
            )
          : // eslint-disable-next-line react/no-access-state-in-setstate
            [...this.state.selectedChannelMonitorPairs]
        : [];

    if (!isAlreadySelected) {
      selectedChannelMonitorPairs.push(channelMonitorPair);
    }

    // ensure that channel monitor pairs are unique
    selectedChannelMonitorPairs = uniqWith(
      selectedChannelMonitorPairs,
      (a: SohTypes.ChannelMonitorPair, b: SohTypes.ChannelMonitorPair) =>
        a.channelName === b.channelName && a.monitorType === b.monitorType
    );

    this.setState({ selectedChannelMonitorPairs }, callback);
    return selectedChannelMonitorPairs;
  };

  /**
   * ! Getter is used for ag-grid to be able to sort, it can seem like they it used but used internally
   * The value getter for the monitor type cells.
   *
   * @param params the value getter params
   */
  private readonly monitorTypeValueGetter = (params: MonitorTypeValueGetterParams): string => {
    const data = this.getRows(this.props.monitorStatusesToDisplay, this.props.channelSohs).find(
      d => d.id === params.data.id
    );
    return data?.monitorType;
  };

  /**
   * ! Getter is used for ag-grid to be able to sort, it can seem like it not used but used internally
   * The value getter for the chanel cells.
   *
   * @param params the value getter params
   */
  private readonly channelValueGetter = (params: ChannelValueGetterParams): number => {
    const data = this.getRows(this.props.monitorStatusesToDisplay, this.props.channelSohs).find(
      d => d.id === params.data.id
    );
    const environmentalSoh = data?.valueAndStatusByChannelName.get(params.colDef.colId);
    return environmentalSoh?.value;
  };

  /**
   * Returns true if the event ocurred on the Monitor Type column; false otherwise
   *
   * @param params the table event parameters
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly isMonitorTypeColumn = (
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    params: CellEvent<{ id: string }, EnvironmentTableContext, any>
  ): boolean => params.colDef.colId === headerNameMonitorType;

  /**
   * Table event handler for handling on cell click events
   *
   * @param params the table event parameters
   */
  private readonly onCellClicked = (
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    params: CellClickedEvent<{ id: string }, EnvironmentTableContext, any>
  ): void => {
    // ignore events on the monitor type column
    if (this.isMonitorTypeColumn(params)) {
      const data = this.getRows(this.props.monitorStatusesToDisplay, this.props.channelSohs).find(
        d => d.id === params.data.id
      );
      this.context.setSelectedAceiType(convertSohMonitorTypeToAceiMonitorType(data.monitorType));
      return;
    }

    this.selectCells(params, true);
  };

  /**
   * Call quiet channel monitor statuses context menu
   *
   * @param params the table event parameters
   */
  private readonly onCellContextMenu = (
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    params: CellContextMenuEvent<{ id: string }, EnvironmentTableContext, any>
  ): void => {
    const data = this.getRows(this.props.monitorStatusesToDisplay, this.props.channelSohs).find(
      d => d.id === params.data.id
    );

    // ignore events on the monitor type column
    if (this.isMonitorTypeColumn(params)) {
      return;
    }

    const newSelection = this.selectCells(params, false);
    const envSoh = data.valueAndStatusByChannelName.get(params.colDef.colId);
    const quietAction = this.generateQuietAction(
      newSelection,
      { left: params.event.x, top: params.event.y },
      envSoh?.quietTimingInfo?.quietUntilMs
    );
    showQuietingContextMenu(quietAction);
  };

  private readonly generateQuietAction = (
    channelMonitorPairs: SohTypes.ChannelMonitorPair[],
    position: Offset,
    quietUntilMs: number
  ): QuietAction => ({
    stationName: this.props.stationName,
    channelMonitorPairs,
    position,
    quietingDurationSelections: this.props.quietingDurationSelections,
    quietUntilMs,
    isStale: this.props.isStale
  });

  private readonly getDataReceivedForChannelName = () =>
    new Map(
      this.props.channelSohs.map(channel => {
        const cellDataForChannel: CellData[] = channel.allSohMonitorValueAndStatuses
          .filter(mvs => SohTypes.isEnvironmentalIssue(mvs.monitorType))
          .map(mvs => ({
            status: mvs.status,
            value: mvs && mvs.valuePresent ? setDecimalPrecisionAsNumber(mvs.value, 1) : undefined,
            isContributing: true // for the sake of getting the dataReceived rollup
          }));
        const channelDataReceivedStatus = getDataReceivedStatusRollup(cellDataForChannel);
        return [channel.channelName, channelDataReceivedStatus];
      })
    );
}
