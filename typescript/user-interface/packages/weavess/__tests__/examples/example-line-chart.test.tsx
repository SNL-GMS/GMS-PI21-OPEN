import { render } from '@testing-library/react';
import React from 'react';

import { WeavessLineChartExample } from '../../src/ts/examples/example-line-chart';

// set up window alert and open so we don't see errors
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).alert = jest.fn();
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).open = jest.fn();

it('renders a component', () => {
  const { container } = render(<WeavessLineChartExample />);
  expect(container).toMatchSnapshot();
});
