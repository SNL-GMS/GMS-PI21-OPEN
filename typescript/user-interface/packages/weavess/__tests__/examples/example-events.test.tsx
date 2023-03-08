import { render } from '@testing-library/react';
import React from 'react';

import { EventsExample } from '../../src/ts/examples/example-events';

jest.mock('lodash/uniqueId', () => {
  const id = 1;
  // eslint-disable-next-line no-plusplus
  return () => id;
});

// set up window alert and open so we don't see errors
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).alert = jest.fn();
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).open = jest.fn();

it('renders a component', () => {
  const { container } = render(<EventsExample />);
  expect(container).toMatchSnapshot();
});
