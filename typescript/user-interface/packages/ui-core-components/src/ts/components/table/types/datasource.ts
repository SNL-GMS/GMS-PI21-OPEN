// Params for the above IDatasource.getRows()
export interface GetRowsParams {
  // The first row index to get.
  startRow: number;

  // The first row index to NOT get.
  endRow: number;

  // If doing server-side sorting, contains the sort model
  sortModel: any;

  // If doing server-side filtering, contains the filter model
  filterModel: any;

  // The grid context object
  context: any;

  // Callback to call when the request is successful.
  successCallback(rowsThisBlock: any[], lastRow?: number): void;

  // Callback to call when the request fails.
  failCallback(): void;
}

// Infinite Scrolling Datasource
export interface Datasource {
  /** If you know up front how many rows are in the dataset, set it here. Otherwise leave blank. */
  rowCount?: number;

  /** Callback the grid calls that you implement to fetch rows from the server. See below for params. */
  getRows(params: GetRowsParams): void;

  /** optional destroy method, if your datasource has state it needs to clean up. */
  destroy?(): void;
}
