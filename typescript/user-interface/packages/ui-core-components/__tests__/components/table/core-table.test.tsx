/* eslint-disable react/jsx-props-no-spreading */
import Enzyme from 'enzyme';
import noop from 'lodash/noop';
import * as React from 'react';

import type { CoreTableProps } from '../../../src/ts/components/table/core-table';
import { CoreTable } from '../../../src/ts/components/table/core-table';
import type { ColumnDefinition, Row } from '../../../src/ts/ui-core-components';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

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

describe('Core Table', () => {
  const tableProps: CoreTableProps<RowType, any> = {
    defaultColDef: undefined,
    onCellClicked: jest.fn(),
    onCellContextMenu: jest.fn(),
    onRowSelected: jest.fn(),
    columnDefs: columnDefs as any,
    rowData: generateTableRows()
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  const table = Enzyme.shallow(<CoreTable {...tableProps} />);

  it('is exported', () => {
    expect(CoreTable).toBeDefined();
  });
  it('Renders', () => {
    expect(table).toMatchSnapshot();
  });

  it('can handle row clicked', () => {
    const wrapper3 = Enzyme.mount(<CoreTable {...tableProps} />);
    const event = {
      api: {
        getSelectedRows: jest.fn(() => [{ id: 1 }, { id: 2 }])
      }
    };
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const wrapper = wrapper3.find(CoreTable).instance() as any;
    wrapper.onRowClicked(event);
    expect(CoreTable).toBeDefined();
  });
  it('can handle select', () => {
    const wrapper2 = Enzyme.mount(<CoreTable {...tableProps} />);
    const instance: CoreTable<any, any> = wrapper2.find(CoreTable).instance() as CoreTable<
      any,
      any
    >;
    const ids = ['1', '2'];
    instance.tableApi = {
      getSelectedNodes: jest.fn(
        () =>
          [
            { id: '1', setSelected: jest.fn() },
            { id: '2', setSelected: jest.fn() }
          ] as any
      ),
      forEachNode: jest.fn(
        () =>
          [
            { id: '1', setSelected: jest.fn() },
            { id: '2', setSelected: jest.fn() }
          ] as any
      )
    } as any;
    instance.tableApi.getSelectedNodes = jest.fn(
      () =>
        [
          { id: '1', setSelected: jest.fn() },
          { id: '2', setSelected: jest.fn() }
        ] as any
    );
    const selectedNodes = instance.tableApi.getSelectedNodes();
    instance.updateRowSelection(ids);
    expect(selectedNodes.length).toBeGreaterThan(0);
  });

  it('can create an empty datasource', () => {
    const wrapper = Enzyme.mount(<CoreTable {...tableProps} />);
    const instance: CoreTable<any, any> = wrapper.find(CoreTable).instance() as CoreTable<any, any>;
    const emptyDataSource = instance.getEmptyDataSource();
    expect(emptyDataSource).toBeDefined();
  });

  it('can clear the datasource if the row model type is infinite', () => {
    const wrapper = Enzyme.mount(<CoreTable {...tableProps} />);
    const instance: CoreTable<any, any> = wrapper.find(CoreTable).instance() as CoreTable<any, any>;
    instance.tableApi = {
      purgeInfiniteCache: jest.fn(),
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      setDatasource: jest.fn(dataSource => noop),
      getSelectedNodes: jest.fn(
        () =>
          [
            { id: '1', setSelected: jest.fn() },
            { id: '2', setSelected: jest.fn() }
          ] as any
      ),
      forEachNode: jest.fn(
        () =>
          [
            { id: '1', setSelected: jest.fn() },
            { id: '2', setSelected: jest.fn() }
          ] as any
      ),
      getModel: jest.fn(() => {
        return { getType: jest.fn(() => 'infinite') };
      }),
      refreshCells: jest.fn(),
      destroy: jest.fn()
    } as any;
    instance.destroy();
    expect(instance.tableApi).toBeUndefined();
  });
});
