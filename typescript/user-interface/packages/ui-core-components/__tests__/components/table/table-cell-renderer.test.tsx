import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import {
  getColumnPosition,
  TableCellRenderer,
  TableCellRendererFramework
} from '../../../src/ts/components/table/table-cell-renderer';

const mockFindIndex = jest.fn();
const mockProps: any = {
  columnApi: {
    getAllDisplayedColumns: jest.fn(() => {
      return { findIndex: mockFindIndex, length: 20 };
    })
  }
};

describe('table cell renderer', () => {
  it('is defined', () => {
    expect(TableCellRenderer).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(
      <TableCellRenderer
        className="test-cell"
        heightCSS="100px"
        isNumeric={false}
        shouldCenterText
        tooltipMsg="tooltip message"
        value="cell value"
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('renders with leftChild', () => {
    const { container } = render(
      <TableCellRenderer
        className="test-cell"
        heightCSS="100px"
        isNumeric={false}
        shouldCenterText
        tooltipMsg="tooltip message"
        value="cell value"
        leftChild={<div> this is a child div</div>}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('renders using the TableCellRendererFramework method', () => {
    mockFindIndex.mockReset();
    mockFindIndex.mockImplementationOnce(() => 5);
    mockProps.value = '1';
    const frameworkRender = Enzyme.mount(TableCellRendererFramework<unknown>(mockProps));

    expect(frameworkRender).toMatchSnapshot();

    mockProps.valueFormatted = 'Mock Value';

    const formattedRender = Enzyme.mount(TableCellRendererFramework<unknown>(mockProps));
    expect(formattedRender).toMatchSnapshot();
  });

  it('calculates column location with getColumnPosition', () => {
    mockFindIndex.mockReset();
    mockFindIndex.mockImplementationOnce(() => 5);

    expect(getColumnPosition(mockProps)).toEqual(5);

    mockFindIndex.mockImplementationOnce(() => 0);

    expect(getColumnPosition(mockProps)).toEqual('first');

    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    mockFindIndex.mockImplementationOnce(() => 19);

    expect(getColumnPosition(mockProps)).toEqual('last');

    expect(mockProps).toBeDefined();
  });
});
