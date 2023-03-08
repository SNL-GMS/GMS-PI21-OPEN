/* eslint-disable react/jsx-props-no-spreading */
import * as Enzyme from 'enzyme';
import Immutable from 'immutable';
import * as React from 'react';

import type { ClientSideTableProps } from '../../../src/ts/components/table/client-side-table';
import { ClientSideTable } from '../../../src/ts/components/table/client-side-table';
import type { ColumnDefinition, Row } from '../../../src/ts/ui-core-components';
import { waitForComponentToPaint } from '../../util/test-util';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

enum ColumnNames {
  test = 'test',
  test2 = 'test2'
}

interface RowType extends Row {
  versionId: number;
  color: string;
  category: string;
  type: string;
  startTime: number;
  endTime: number;
  Ids: number[];
  rationale: string;
}

const generateTableRows = () => {
  const rows: RowType[] = [
    {
      id: '1',
      versionId: 1,
      color: 'RED',
      category: 'normal',
      type: 'info',
      startTime: 1000,
      endTime: 2000,
      Ids: [1, 2],
      rationale: 'user'
    },
    {
      id: '2',
      versionId: 1,
      color: 'GREEN',
      category: 'normal',
      type: 'info',
      startTime: 2000,
      endTime: 3000,
      Ids: [1, 2],
      rationale: 'user'
    }
  ];
  rows[0]['first-in-table'] = true;
  return rows;
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const columns: any[] = [
  {
    headerName: 'test',
    field: 'test',
    headerTooltip: 'test',
    width: 10
  },
  {
    headerName: 'test2',
    field: 'test2',
    headerTooltip: 'test2',
    width: 10,
    sort: 'asc'
  }
];

const columnDefs: ColumnDefinition<RowType, unknown, unknown, unknown, unknown>[] = [...columns];

describe('Client Side Table', () => {
  const clientSideTableProps: ClientSideTableProps<RowType, any> = {
    defaultColDef: undefined,
    onCellClicked: jest.fn(),
    onCellContextMenu: jest.fn(),
    onRowSelected: jest.fn(),
    columnDefs: columnDefs as any,
    rowData: generateTableRows()
  };

  const defaultColumnWidthPx = 200;
  const headerCellBlockClass = 'table-header-cell';

  const compareCellValues = (a: number, b: number): number => {
    if (a === undefined && b === undefined) {
      return 0;
    }
    if (a === undefined) {
      return -1;
    }
    if (b === undefined) {
      return 1;
    }
    return a - b;
  };

  const defaultColumnDefinition = {
    headerClass: [`${headerCellBlockClass}`, `${headerCellBlockClass}--neutral`],
    width: defaultColumnWidthPx,
    sortable: true,
    filter: true,
    disableStaticMarkupForHeaderComponentFramework: true,
    disableStaticMarkupForCellRendererFramework: true,
    comparator: compareCellValues
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  const mockClientSideTable = Enzyme.shallow(<ClientSideTable {...clientSideTableProps} />);
  afterEach(() => {
    jest.clearAllMocks();
  });
  it('is exported', () => {
    expect(ClientSideTable).toBeDefined();
  });
  it('Renders', () => {
    expect(mockClientSideTable).toMatchSnapshot();
  });

  // TODO: fix when migrating off of Enzyme--passes locally but fails in pipeline
  it.skip('can update', async () => {
    const wrapper = Enzyme.mount(<ClientSideTable {...clientSideTableProps} />);
    const instance: ClientSideTable<any, any> = wrapper
      .find(ClientSideTable)
      .instance() as ClientSideTable<any, any>;
    const spy = jest.spyOn(instance, 'shouldComponentUpdate');
    const spy2 = jest.spyOn(instance, 'componentDidUpdate');
    const spy3 = jest.spyOn(instance, 'componentDidUpdate');
    const spy4 = jest.spyOn(instance, 'render');
    wrapper.setProps({ defaultColDef: defaultColumnDefinition });
    await waitForComponentToPaint(wrapper);

    expect(spy).toHaveBeenCalled();
    expect(spy2).toHaveBeenCalled();
    expect(spy3).toHaveBeenCalled();
    expect(spy4).toHaveBeenCalled();

    const tableAPI = instance.getTableApi();
    expect(tableAPI).toBeDefined();

    const columnAPI = instance.getColumnApi();
    expect(columnAPI).toBeDefined();

    const visibleColumns = instance.getNumberOfVisibleColumns();
    expect(visibleColumns).toBeDefined();

    let columnsMap = Immutable.Map(
      Object.values(ColumnNames)
        // all columns are visible by default
        .map(v => [v, true])
    );

    instance.updateVisibleColumns<ColumnNames>(columnsMap);
    expect(instance.getNumberOfVisibleColumns()).toEqual(2);

    columnsMap = columnsMap.set(ColumnNames.test, false);

    await waitForComponentToPaint(wrapper);

    instance.updateVisibleColumns<ColumnNames>(columnsMap);
    expect(instance.getNumberOfVisibleColumns()).toEqual(1);
  });
  it('will not clear refresh client side row model timeout if not needed', () => {
    const clearTimeoutSpy = jest.spyOn(global, 'clearTimeout');
    const setTimeoutSpy = jest.spyOn(global, 'setTimeout');
    const tableWrapper = Enzyme.mount(<ClientSideTable {...clientSideTableProps} />);
    const tableWrapperInstance: ClientSideTable<any, any> = tableWrapper
      .find(ClientSideTable)
      .instance() as ClientSideTable<any, any>;
    Reflect.set(
      tableWrapperInstance,
      'getTableApi',
      jest.fn(() => ({
        ensureIndexVisible: jest.fn(() => true),
        refreshInfiniteCache: jest.fn()
      }))
    );
    tableWrapper.update();
    tableWrapper.instance().forceUpdate();
    expect(setTimeoutSpy).toHaveBeenCalled();
    expect(clearTimeoutSpy).not.toHaveBeenCalled();
  });

  // TODO Unskip tests and fix
  it.skip('will clear refresh client side row model timeout if needed', () => {
    const clearTimeoutSpy = jest.spyOn(global, 'clearTimeout');
    const setTimeoutSpy = jest.spyOn(global, 'setTimeout');
    const tableWrapper = Enzyme.mount(<ClientSideTable {...clientSideTableProps} />);
    const tableWrapperInstance: ClientSideTable<any, any> = tableWrapper
      .find(ClientSideTable)
      .instance() as ClientSideTable<any, any>;
    Reflect.set(tableWrapperInstance, 'refreshClientSideRowModelTimeout', 1);
    Reflect.set(
      tableWrapperInstance,
      'getTableApi',
      jest.fn(() => ({
        ensureIndexVisible: jest.fn(() => true),
        refreshInfiniteCache: jest.fn()
      }))
    );
    tableWrapper.update();
    tableWrapper.instance().forceUpdate();
    expect(setTimeoutSpy).toHaveBeenCalled();
    expect(clearTimeoutSpy).toHaveBeenCalled();
  });
  it('will not refresh client side row model the table API is unavailable', () => {
    const clearTimeoutSpy = jest.spyOn(global, 'clearTimeout');
    const setTimeoutSpy = jest.spyOn(global, 'setTimeout');
    const tableWrapper = Enzyme.mount(<ClientSideTable {...clientSideTableProps} />);
    const tableWrapperInstance: ClientSideTable<any, any> = tableWrapper
      .find(ClientSideTable)
      .instance() as ClientSideTable<any, any>;
    Reflect.set(
      tableWrapperInstance,
      'getTableApi',
      jest.fn(() => false)
    );
    tableWrapper.update();
    tableWrapper.instance().forceUpdate();
    // calls attributed to component lifecycle up to componentDidUpdate
    expect(setTimeoutSpy).toHaveBeenCalledTimes(4);
    expect(clearTimeoutSpy).not.toHaveBeenCalled();
  });
});
