import { render } from '@testing-library/react';
import React from 'react';
import { BrowserRouter } from 'react-router-dom';

import { Home } from '../../src/ts/examples/home';

// set up window alert and open so we don't see errors
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).alert = jest.fn();
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).open = jest.fn();

it('renders a component', () => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const { container } = render(
    <BrowserRouter>
      <Home />
    </BrowserRouter>
  );
  expect(container).toMatchSnapshot();
});
