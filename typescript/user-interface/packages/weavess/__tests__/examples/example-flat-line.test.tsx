import { render } from '@testing-library/react';
import React from 'react';

import { WeavessFlatLineExample } from '../../src/ts/examples/example-flat-line';

// set up window alert and open so we don't see errors
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).alert = jest.fn();
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).open = jest.fn();

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

it('renders a component', () => {
  const { container } = render(<WeavessFlatLineExample />);
  expect(container).toMatchSnapshot();
});
