import { render } from '@testing-library/react';
import React from 'react';

import { TableExample } from '../../src/ts/examples/table-example';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

it('renders a component', () => {
  const { container } = render(<TableExample />);
  expect(container).toMatchSnapshot();
});
