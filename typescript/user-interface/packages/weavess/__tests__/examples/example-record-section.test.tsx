import { render } from '@testing-library/react';
import React from 'react';

import { RecordSectionExample } from '../../src/ts/examples/example-record-section';

// set up window alert and open so we don't see errors
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).alert = jest.fn();
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(window as any).open = jest.fn();

it('renders a component', () => {
  const { container } = render(<RecordSectionExample />);
  expect(container).toMatchSnapshot();
});
