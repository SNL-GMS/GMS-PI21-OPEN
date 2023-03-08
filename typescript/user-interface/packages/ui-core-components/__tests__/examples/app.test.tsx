import { render } from '@testing-library/react';
import React from 'react';

import { App } from '../../src/ts/examples/app';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

it('renders a component', () => {
  const { container } = render(<App />);
  expect(container).toMatchSnapshot();
});
