import { render } from '@testing-library/react';
import React from 'react';

import type { InfiniteTableProps } from '../../../src/ts/components/table/infinite-table';
import { InfiniteTable } from '../../../src/ts/components/table/infinite-table';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Infinite Table', () => {
  const infiniteTable: InfiniteTableProps<undefined, undefined> = {
    rowCount: 10,
    datasource: undefined
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container } = render(<InfiniteTable {...infiniteTable} />);
  it('is exported', () => {
    expect(InfiniteTable).toBeDefined();
  });
  it('Renders', () => {
    expect(container).toMatchSnapshot();
  });
});
