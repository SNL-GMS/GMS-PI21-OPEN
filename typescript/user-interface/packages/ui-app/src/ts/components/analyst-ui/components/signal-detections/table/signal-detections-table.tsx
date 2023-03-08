import type { RowNode } from '@gms/ui-core-components';
import { AgGridReact } from '@gms/ui-core-components';
import { analystActions, useAppDispatch, useAppSelector, useGetSelectedSdIds } from '@gms/ui-state';
import classNames from 'classnames';
import isEqual from 'lodash/isEqual';
import React from 'react';

import { signalDetectionsColumnDefs } from '~analyst-ui/components/signal-detections/table/signal-detections-column-definitions';
import {
  agGridDoesExternalFilterPass,
  agGridIsExternalFilterPresent,
  edgeSDRowClassRules,
  handleNonIdealState,
  updateColumns,
  updateRowSelection
} from '~analyst-ui/components/signal-detections/table/signal-detections-table-utils';
import { defaultColumnDefinition } from '~common-ui/common/table-types';
import { getHeaderHeight, getRowHeightWithBorder } from '~common-ui/common/table-utils';

import type { SignalDetectionRow, SignalDetectionsTableProps } from '../types';

/**
 * Update the signal detection selection. If the table API is not defined due to a race condition on startup,
 * set a timeout and try again. And again. And again, up to ten times, or the provided max number of tries to attempt.
 */
export const useUpdatedSignalDetectionSelection = (
  tableRef: React.MutableRefObject<AgGridReact>,
  selectedSdIds: string[],
  maxTries = 10,
  backOffIncrement = 16
): void => {
  const timeoutRef = React.useRef<NodeJS.Timeout | number>();
  const numTriesRef = React.useRef<number>(0);
  const maybeUpdateRowSelection = React.useCallback(() => {
    numTriesRef.current += 1;
    if (tableRef.current.api != null) {
      updateRowSelection(tableRef, selectedSdIds);
    } else if (numTriesRef.current < maxTries) {
      timeoutRef.current = setTimeout(
        maybeUpdateRowSelection,
        backOffIncrement * numTriesRef.current
      );
    }
  }, [backOffIncrement, maxTries, selectedSdIds, tableRef]);

  React.useEffect(() => {
    if (tableRef.current != null) {
      maybeUpdateRowSelection();
    }
    return () => {
      clearTimeout(timeoutRef.current as any);
    };
  }, [maybeUpdateRowSelection, selectedSdIds, tableRef]);
};

// eslint-disable-next-line react/function-component-definition
export const SignalDetectionsTableComponent: React.FunctionComponent<SignalDetectionsTableProps> = (
  props: SignalDetectionsTableProps
) => {
  const { isSynced, signalDetectionsQuery, data, columnsToDisplay } = props;
  const dispatch = useAppDispatch();
  const tableRef = React.useRef<AgGridReact>(null);
  const selectedSdIds = useGetSelectedSdIds();

  const displayedSignalDetectionConfigurationObject = useAppSelector(
    state => state.app.signalDetections.displayedSignalDetectionConfiguration
  );

  /**
   * Required by {@link isExternalFilterPresent} and {@link doesExternalFilterPass} due to
   * the way ag-grid creates closures for those respective functions.
   */
  const filterStateRef = React.useRef(displayedSignalDetectionConfigurationObject);

  React.useEffect(() => {
    updateColumns(tableRef, columnsToDisplay);
  }, [columnsToDisplay]);

  React.useEffect(() => {
    filterStateRef.current = displayedSignalDetectionConfigurationObject;
    // Notifies the grid that the filter conditions have changed.
    tableRef.current?.api?.onFilterChanged();
  }, [displayedSignalDetectionConfigurationObject]);

  /**
   * Called by ag-grid to determine if an external filter is present/active.
   */
  const isExternalFilterPresent = React.useCallback(
    () => agGridIsExternalFilterPresent(filterStateRef.current),
    []
  );

  /**
   * Called by ag-grid once for each {@link RowNode}. Should return true if external filter
   * passes, otherwise false.
   */
  const doesExternalFilterPass = React.useCallback(
    (node: RowNode): boolean => agGridDoesExternalFilterPass(node, filterStateRef.current),
    []
  );

  const onRowClickedCallback = React.useCallback(
    event => {
      if (event.node.isSelected()) {
        dispatch(analystActions.setSelectedSdIds(selectedSdIds.filter(sd => sd === event.id)));
      }
    },
    [dispatch, selectedSdIds]
  );

  const onSelectionChanged = React.useCallback(() => {
    const selectedRows = tableRef?.current?.api.getSelectedRows();
    const updatedSelectedSdIds = selectedRows.map(sd => sd.id);
    if (!isEqual(updatedSelectedSdIds, selectedSdIds)) {
      dispatch(analystActions.setSelectedSdIds(updatedSelectedSdIds));
    }
  }, [dispatch, selectedSdIds]);

  /** Handler function for {@link AgGridReact} `onGridReady` prop. */
  const onGridReady = () => {
    if (!tableRef?.current) return;
    tableRef.current?.api?.addGlobalListener((event: string) => {
      if (event === 'rowContainerHeightChanged' || event === 'bodyHeightChanged') {
        tableRef?.current?.api?.redrawRows();
      }
    });
    const newState = Object.entries(columnsToDisplay.toObject()).map(item => {
      return {
        colId: item[0], // Column key
        hide: !item[1] // Column bool
      };
    });
    tableRef.current?.columnApi.applyColumnState({
      state: newState
    });
  };

  useUpdatedSignalDetectionSelection(tableRef, selectedSdIds);

  return (
    handleNonIdealState(signalDetectionsQuery, isSynced) ?? (
      <div
        className={classNames([
          'signal-detection-table-wrapper',
          'ag-theme-dark',
          'with-separated-rows-color'
        ])}
      >
        <AgGridReact
          ref={tableRef}
          context={{}}
          onGridReady={onGridReady}
          isExternalFilterPresent={isExternalFilterPresent}
          doesExternalFilterPass={doesExternalFilterPass}
          defaultColDef={defaultColumnDefinition<SignalDetectionRow>()}
          columnDefs={signalDetectionsColumnDefs}
          rowData={data}
          rowHeight={getRowHeightWithBorder()}
          rowClassRules={edgeSDRowClassRules}
          headerHeight={getHeaderHeight()}
          getRowId={node => node.data.id}
          onRowClicked={onRowClickedCallback}
          onSelectionChanged={onSelectionChanged}
          rowDeselection
          rowSelection="multiple"
          suppressCellFocus
          suppressDragLeaveHidesColumns
          overlayNoRowsTemplate="No Signal Detections to display"
          enableBrowserTooltips
          enableCellChangeFlash={false}
        />
      </div>
    )
  );
};

export const sdPanelMemoCheck = (
  prev: SignalDetectionsTableProps,
  next: SignalDetectionsTableProps
): boolean => {
  // if false, reload
  // If anything in the query changes except for pending/isLoading/isError/data.length
  if (prev.signalDetectionsQuery.isError !== next.signalDetectionsQuery.isError) return false;
  if (prev.signalDetectionsQuery.isLoading !== next.signalDetectionsQuery.isLoading) return false;
  if (prev.signalDetectionsQuery.pending !== next.signalDetectionsQuery.pending) return false;
  if (
    (prev.signalDetectionsQuery.data?.length === 0) !==
    (next.signalDetectionsQuery.data?.length === 0)
  )
    return false;

  if (!isEqual(prev.data, next.data)) return false;

  if (prev.isSynced !== next.isSynced) return false;

  if (!isEqual(prev.columnsToDisplay, next.columnsToDisplay)) return false;

  // Default, do not reload
  return true;
};

export const SignalDetectionsTable = React.memo(SignalDetectionsTableComponent, sdPanelMemoCheck);
