/* eslint-disable react/destructuring-assignment */
import { ContextMenu } from '@blueprintjs/core';
import type {
  NumberValueGetter,
  NumberValueGetterParams,
  StringValueGetter,
  StringValueGetterParams
} from '@gms/ui-core-components';
import { Table } from '@gms/ui-core-components';
import type Immutable from 'immutable';
import includes from 'lodash/includes';
import isEqual from 'lodash/isEqual';
import memoizeOne from 'memoize-one';
import * as React from 'react';

import { messageConfig } from '~components/data-acquisition-ui/config/message-config';
import {
  getHeaderHeight,
  getRowHeightWithBorder
} from '~components/data-acquisition-ui/shared/table/utils';
import type { Offset } from '~components/data-acquisition-ui/shared/types';

import type {
  Columns,
  StationStatisticsCellClickedEvent,
  StationStatisticsColumnDefinition,
  StationStatisticsRowClickedEvent
} from './column-definitions';
import { buildColumnDefs, defaultColumnDefinition } from './column-definitions';
import type { StationStatisticsContextData } from './station-statistics-context';
import { StationStatisticsContext } from './station-statistics-context';
import type { StationStatisticsRow } from './station-statistics-table-context';
import { StationStatisticsTableDataContext } from './station-statistics-table-context';

/**
 * Station Statistics table component props
 */
export interface StationStatisticsTableProps {
  id: string;
  tableData: StationStatisticsRow[];
  selectedIds?: string[];
  suppressContextMenu?: boolean;
  onRowClicked?(event: StationStatisticsRowClickedEvent);
  acknowledgeContextMenu?(selectedIds: string[], comment?: string): JSX.Element;
  highlightDropZone?(): void;
}

/**
 * Station statistics table, provides a Station level SOH focusing on:
 * channelMissing, channelLag, channelEnvironment, and channelTimeliness,
 * stationMissing, stationLag, stationEnvironment, and stationTimeliness
 */
export class StationStatisticsTable extends React.Component<StationStatisticsTableProps> {
  /** the context type */
  // eslint-disable-next-line react/static-property-placement
  public static readonly contextType: React.Context<
    StationStatisticsContextData
  > = StationStatisticsContext;

  /** the station statistics context contains station statistics scoped globals */
  // eslint-disable-next-line react/static-property-placement
  public declare context: React.ContextType<typeof StationStatisticsContext>;

  /** the table column definitions */
  private readonly columnDefinitions: StationStatisticsColumnDefinition[];

  /**
   * A reference to the table.
   * Usage:
   * this.tableRef.getTableApi() return the table API.
   * this.tableRef.getColumnApi() returns the column API
   */
  private tableRef: Table<{ id: string }, unknown>;

  /**
   * The value getter for the channel lag cells.
   * Used to sort and filter cells, so AG Grid can get the value in the cell
   * from any data object
   *
   * @param params the value getter params
   */
  private readonly channelLagValueGetter: NumberValueGetter = this.buildNumberValueGetter(
    data => data?.channelLag.value
  );

  /**
   * The value getter for the channel missing cells.
   * Used to sort and filter cells, so AG Grid can get the value in the cell
   * from any data object
   *
   * @param params the value getter params
   */
  private readonly channelMissingValueGetter: NumberValueGetter = this.buildNumberValueGetter(
    data => data?.channelMissing.value
  );

  /**
   * The value getter for the channel environment cells.
   * Used to sort and filter cells, so AG Grid can get the value in the cell
   * from any data object
   *
   * @param params the value getter params
   */
  private readonly channelEnvironmentValueGetter: NumberValueGetter = this.buildNumberValueGetter(
    data => data?.channelEnvironment.value
  );

  /**
   * The value getter for the channel timeliness cells.
   * Used to sort and filter cells, so AG Grid can get the value in the cell
   * from any data object
   *
   * @param params the value getter params
   */
  private readonly channelTimelinessValueGetter: NumberValueGetter = this.buildNumberValueGetter(
    data => data?.channelTimeliness.value
  );

  /**
   * The value getter for the station lag cells.
   * Used to sort and filter cells, so AG Grid can get the value in the cell
   * from any data object
   *
   * @param params the value getter params
   */
  private readonly stationLagValueGetter: NumberValueGetter = this.buildNumberValueGetter(
    data => data?.stationLag
  );

  /**
   * The value getter for the station missing cells.
   * Used to sort and filter cells, so AG Grid can get the value in the cell
   * from any data object
   *
   * @param params the value getter params
   */
  private readonly stationMissingValueGetter: NumberValueGetter = this.buildNumberValueGetter(
    data => data?.stationMissing
  );

  /**
   * The value getter for the channel timeliness cells.
   * Used to sort and filter cells, so AG Grid can get the value in the cell
   * from any data object
   *
   * @param params the value getter params
   */
  private readonly stationEnvironmentValueGetter: NumberValueGetter = this.buildNumberValueGetter(
    data => data?.stationEnvironment
  );

  /**
   * The value getter for the station missing cells.
   * Used to sort and filter cells, so AG Grid can get the value in the cell
   * from any data object
   *
   * @param params the value getter params
   */
  private readonly stationTimelinessValueGetter: NumberValueGetter = this.buildNumberValueGetter(
    data => data?.stationTimeliness
  );

  /**
   * The value getter for the station name cells.
   *
   * @param params the value getter params
   */
  private readonly stationNameValueGetter: StringValueGetter = this.buildStringValueGetter(
    data => data.stationData.stationName
  );

  /**
   * Returns a table data object, passed to memoizeOne
   */
  private memoizedGetTableData: () => { data: StationStatisticsRow[] };

  /**
   * Component class constructor
   * Define the column definitions for the table
   */
  public constructor(props: StationStatisticsTableProps) {
    super(props);
    this.columnDefinitions = buildColumnDefs(
      this.stationNameValueGetter,
      this.stationMissingValueGetter,
      this.stationTimelinessValueGetter,
      this.stationLagValueGetter,
      this.stationEnvironmentValueGetter,
      this.channelMissingValueGetter,
      this.channelTimelinessValueGetter,
      this.channelLagValueGetter,
      this.channelEnvironmentValueGetter
    );
  }

  /**
   * React lifecycle `componentDidUpdate`.
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: StationStatisticsTableProps): void {
    // if selections changed or the list of stations size has changed added/deleted an entry
    if (
      !isEqual(prevProps.selectedIds, this.props.selectedIds) ||
      prevProps.tableData?.length !== this.props.tableData?.length
    ) {
      this.updateRowSelection(this.props?.selectedIds);
    }
  }

  /**
   * Updates the row selection with the provided ids - synced selection
   *
   * @param ids the ids to select
   */
  private readonly updateRowSelection = (ids: string[]): void => {
    if (this.tableRef && this.tableRef.getTableApi()) {
      const selectedNodes = this.tableRef.getTableApi().getSelectedNodes();
      selectedNodes.forEach(rowNode => {
        if (!includes(ids, rowNode.id)) {
          rowNode.setSelected(false);
        }
      });

      this.tableRef.getTableApi().forEachNode(rowNode => {
        if (includes(ids, rowNode.id)) {
          rowNode.setSelected(true);
        }
      });
    }
  };

  /**
   * Updates the column visibility based on the state provided.
   *
   * @param state the mapping of unique column id and the visible state
   */
  public readonly updateColumnVisibility = (state: Immutable.Map<Columns, boolean>): void => {
    if (this.tableRef && this.tableRef.getColumnApi()) {
      state.forEach((v, k) => {
        this.tableRef.getColumnApi().setColumnVisible(k, v);
      });
    }
  };

  /**
   * Shows a contest menu used for acknowledging
   *
   * @param event StationStatisticsCellClickedEvent
   */
  private readonly onCellContextMenu = (event: StationStatisticsCellClickedEvent): void => {
    const stationNames = this.getSelectedIdsOnRightClick(event);
    const offset: Offset = { left: event.event.x, top: event.event.y };
    // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
    ContextMenu.show(this.props.acknowledgeContextMenu(stationNames), offset);
    this.context.setSelectedStationIds(stationNames);
  };

  /**
   * Gets the station Ids based on the tables selection
   *
   * @param event StationStatisticsCellClickedEvent
   * @returns selectedIds as string[]
   */
  private readonly getSelectedIdsOnRightClick = (
    event: StationStatisticsCellClickedEvent
  ): string[] => {
    let selectedIds: string[] = this.tableRef
      .getTableApi()
      .getSelectedRows()
      .map(row => row.id);
    const clickedRowId = event.node.id;
    if (!selectedIds.includes(clickedRowId)) {
      selectedIds = [event.node.id];
    }
    return selectedIds;
  };

  /**
   * Creates a value getter function that finds the corresponding row and then
   * uses the provided getter function to return the value.
   * Value getters are used by AG Grid to sort/filter rows.
   * They are passed in to the column definition for each column.
   *
   * @param getter a function that accesses the value from a row data object
   */
  private buildNumberValueGetter(getter: (d: StationStatisticsRow) => number): NumberValueGetter {
    return (params: NumberValueGetterParams): number => {
      const data = this.props.tableData.find(d => d.id === params.data.id);
      return data && getter(data);
    };
  }

  private buildStringValueGetter(getter: (d: StationStatisticsRow) => string) {
    return (params: StringValueGetterParams): string => {
      const data = this.props.tableData.find(d => d.id === params.data.id);
      return data && getter(data);
    };
  }

  /**
   * React lifecycle `render`.
   * Renders the component.
   */
  public render(): JSX.Element {
    this.memoizedGetTableData = memoizeOne(() => {
      return { data: this.props.tableData };
    }, isEqual);
    return (
      <div
        className="station-statistics-table__wrapper"
        data-cy="station-statistics-table__wrapper"
      >
        <StationStatisticsTableDataContext.Provider value={this.memoizedGetTableData()}>
          <Table<{ id: string }, unknown>
            ref={ref => {
              this.tableRef = ref;
            }}
            // provide just the row ids to the table;
            // use the react context to update the cells for performance (and memory) benefits
            rowData={this.props.tableData.map(r => ({ id: r.id }))}
            defaultColDef={defaultColumnDefinition}
            onGridReady={() => {
              if (this.tableRef) {
                this.updateRowSelection(this.props.selectedIds);
              }
            }}
            columnDefs={this.columnDefinitions}
            headerHeight={getHeaderHeight()}
            overlayNoRowsTemplate={messageConfig.table.noDataMessage}
            rowHeight={getRowHeightWithBorder()}
            rowSelection="multiple"
            rowDeselection
            suppressCellFocus
            onRowClicked={this.props.onRowClicked}
            onCellContextMenu={this.onCellContextMenu}
            suppressContextMenu={this.props.suppressContextMenu ?? false}
          />
        </StationStatisticsTableDataContext.Provider>
      </div>
    );
  }
}
