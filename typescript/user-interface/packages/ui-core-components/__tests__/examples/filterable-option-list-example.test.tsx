import { render } from '@testing-library/react';
import React from 'react';

import { FilterableOptionListExample } from '../../src/ts/examples/filterable-option-list-example';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

it('renders a component', () => {
  const { container } = render(<FilterableOptionListExample />);
  expect(container).toMatchSnapshot();
});
