import { UILogger } from '@gms/ui-util';
import orderBy from 'lodash/orderBy';
import { useRef } from 'react';

import type { Datasource, GetRowsParams } from './types/datasource';
import type { TableApi } from './types/table-api';

const logger = UILogger.create('GMS_LOG_TABLE', process.env.GMS_LOG_TABLE);

export function useDatasource<T>(rows: T[]): Datasource {
  const tableApiRef = useRef<TableApi>(undefined);
  const filterModelRef = useRef<any>(undefined);

  // store row data in a ref so it is not captured in a closure
  // make rowRef always have the current rows
  // "fixed" callbacks can refer to this object whenever
  // they need the current value.  Note: the callbacks will not
  // be reactive - they will not re-run the instant state changes,
  // but they *will* see the current value whenever they do run
  // For more info, see:
  // https://stackoverflow.com/questions/57847594/react-hooks-accessing-up-to-date-state-from-within-a-callback
  const rowsRef = useRef<T[]>([]);
  rowsRef.current = rows;

  return {
    // rowCount: rowsRef.current.length,
    getRows: (params: GetRowsParams) => {
      // eslint-disable-next-line @typescript-eslint/unbound-method
      const {
        startRow,
        endRow,
        successCallback,
        sortModel,
        filterModel,
        context // contains tableApi and columnApi
        // failCallback
      } = params;
      tableApiRef.current = context.tableApi;
      filterModelRef.current = filterModel;
      // required to use the ref.current in order to avoid getting captured values
      // !This is no longer using the any custom comparator that is specified in the column definitions
      // TODO: Do we need to address the top statement?
      const sortedRows = orderBy(
        rowsRef.current,
        sortModel.map(m => m.colId),
        sortModel.map(m => m.sort)
      );
      const visibleRows = sortedRows?.slice(
        startRow,
        Math.min(endRow, rowsRef.current?.length ?? 0)
      );
      successCallback(visibleRows, sortedRows.length);
    },
    destroy: () => {
      logger.info('destroy!');
    }
  };
}
