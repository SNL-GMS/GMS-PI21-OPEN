import { render } from '@testing-library/react';
import React from 'react';

import { Home } from '../../src/ts/examples/home';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// TODO Unskip tests and fix
it.skip('renders a component', () => {
  const { container } = render(<Home />);
  expect(container).toMatchSnapshot();
});
