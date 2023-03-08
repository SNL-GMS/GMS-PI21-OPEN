/* eslint-disable react/destructuring-assignment */
import { UILogger } from '@gms/ui-util';
import type { RowDataTransaction } from 'ag-grid-community';
import type Immutable from 'immutable';
import includes from 'lodash/includes';
import isEqual from 'lodash/isEqual';
import * as React from 'react';

import type { CoreTableProps } from './core-table';
import { CoreTable } from './core-table';
import type {
  ColumnApi,
  ColumnDefinition,
  ColumnGroupDefinition,
  GridReadyEvent,
  Row,
  TableApi
} from './types';

const logger = UILogger.create('GMS_LOG_TABLE', process.env.GMS_LOG_TABLE);

/** The Table Props
 * The context object is passed to most of the callbacks used in the grid.
 * The purpose of the context object is to allow the client application to
 * pass details to custom callbacks such as the Cell Renderers and Cell
 * Editors.
 */
export interface ClientSideTableProps<RowDataType, ContextDataType>
  extends CoreTableProps<RowDataType, ContextDataType> {
  rowDataNeverUpdates?: boolean;
}

/**
 * Table component that wraps AgGrid React.
 */
export class ClientSideTable<RowDataType extends Row, ContextDataType> extends React.Component<
  ClientSideTableProps<RowDataType, ContextDataType>,
  unknown
> {
  private refreshClientSideRowModelTimeout;

  /** Default row buffer */
  private readonly rowBuffer: number = 10;

  private coreTableRef: CoreTable<RowDataType, ContextDataType>;

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * React lifecycle `shouldComponentUpdate`.
   * Determines if the component should update based on the next props passed in.
   *
   * @param nextProps props for the axis of type YAxisProps
   *
   * @returns boolean
   */
  public shouldComponentUpdate(
    nextProps: ClientSideTableProps<RowDataType, ContextDataType>
  ): boolean {
    // the component should only update (render) if the props have changed
    const propsHaveChanged = !isEqual(nextProps, this.props);
    return propsHaveChanged;
  }

  /**
   * React lifecycle `componentDidUpdate`.
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: ClientSideTableProps<RowDataType, ContextDataType>): void {
    this.updateRowData(prevProps.rowData, this.props.rowData);

    if (this.getTableApi()) {
      if (this.refreshClientSideRowModelTimeout) {
        clearTimeout(this.refreshClientSideRowModelTimeout);
        this.refreshClientSideRowModelTimeout = undefined;
      }
      this.refreshClientSideRowModelTimeout = setTimeout(() => {
        this.getTableApi().refreshClientSideRowModel();
      });
    }
  }

  /**
   * React lifecycle `render`.
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <CoreTable
        ref={ref => {
          this.coreTableRef = ref;
        }}
        rowBuffer={this.rowBuffer}
        // user passed in props
        // eslint-disable-next-line react/jsx-props-no-spreading
        {...this.props}
        /*
         * override user passed in props
         */
        onGridReady={this.onGridReady}
      />
    );
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Destroys the component instance.
   */
  public readonly destroy = (): void => {
    if (this.getTableApi()) {
      this.getTableApi().setRowData([]);
    }
    if (this.coreTableRef) {
      this.coreTableRef.destroy();
    }
    this.coreTableRef = undefined;
  };

  /**
   * Returns the Table Api instance.
   */
  public readonly getTableApi = (): TableApi => this.coreTableRef?.getTableApi();

  /**
   * Returns the Column Api instance.
   */
  public readonly getColumnApi = (): ColumnApi => this.coreTableRef?.getColumnApi();

  /**
   * Returns the number of visible columns.
   */
  public readonly getNumberOfVisibleColumns = (): number =>
    this.coreTableRef?.getNumberOfVisibleColumns();

  /**
   * Sets a column as visible or not visible (show/hide).
   *
   * @param key the unique key for the column
   * @param visible true to show; false to hide
   */
  public readonly setColumnVisible = (key: string, visible: boolean): void => {
    if (this.coreTableRef) {
      this.coreTableRef.setColumnVisible(key, visible);
    }
  };

  /**
   * Sets the column definitions for the table.
   *
   * @param columnDefinitions the column definitions
   */
  public readonly setColumnDefinitions = (
    columnDefinitions: (
      | ColumnDefinition<RowDataType, ContextDataType, unknown, unknown, unknown>
      | ColumnGroupDefinition
    )[]
  ): void => this.coreTableRef?.setColumnDefinitions(columnDefinitions);

  // ******************************************
  // START PRIVATE METHODS
  // ******************************************

  /**
   * Event handler for the `onGridReady` event. This is fired once when the ag-grid is initially created.
   * Invokes the users' `onGridReady` callback when specified.
   *
   * @param event The grid ready even
   */
  private readonly onGridReady = (event: GridReadyEvent): void => {
    this.updateRowData([], this.props.rowData);

    if (this.props.onGridReady) {
      this.props.onGridReady(event);
    }
  };

  /**
   * Determine the `changes` between row data (creates a RowDataTransaction).
   *
   * @param previousRowData the previous or current row data
   * @param rowData the new row data
   */
  private readonly determineRowDataChanges = (
    previousRowData: RowDataType[],
    rowData: RowDataType[]
  ): RowDataTransaction => {
    const prevIds = previousRowData.map(previousRow => previousRow.id);
    const newIds = rowData.map(newRow => newRow.id);
    const addData = rowData.filter(newRow => !includes(prevIds, newRow.id));
    const removeData = previousRowData.filter(previousRow => !includes(newIds, previousRow.id));
    const updateData = this.props.rowDataNeverUpdates
      ? []
      : rowData
          .filter(newRow => includes(prevIds, newRow.id))
          .filter(
            newRow =>
              !isEqual(
                newRow,
                previousRowData.find(previousRow => newRow.id === previousRow.id)
              )
          );
    return {
      add: addData,
      remove: removeData,
      update: updateData
    };
  };

  /**
   * Update the row data for the table.
   *
   * @param previousRowData the previous or current row data
   * @param rowData the new row data
   */
  private readonly updateRowData = (
    previousRowData: RowDataType[],
    rowData: RowDataType[]
  ): void => {
    if (this.getTableApi()) {
      // update the row data if it has changed
      if ((rowData && rowData.length === 0) || !isEqual(rowData, previousRowData)) {
        if (this.props.debug) {
          logger.debug(`table applyTransactionAsync`);
        }

        const changes: RowDataTransaction = this.determineRowDataChanges(previousRowData, rowData);

        // batch add, remove, and update the rows that changed
        if (this.getTableApi()) {
          this.getTableApi().applyTransaction(changes);
        }

        // refresh the updated row nodes
        if (this.coreTableRef) {
          this.coreTableRef.refreshRowNodesByIds(
            changes.update.map((change: RowDataType) => change.id)
          );
        }
      }
    }
  };

  /**
   * Toggles visible columns
   *
   * @param columnsToDisplay enum of columnNames to display map
   */
  public updateVisibleColumns<T extends string>(columnsToDisplay: Immutable.Map<T, boolean>): void {
    if (this.coreTableRef) {
      this.coreTableRef.updateVisibleColumns<T>(columnsToDisplay);
    }
  }

  // ******************************************
  // END PRIVATE METHODS
  // ******************************************
}
