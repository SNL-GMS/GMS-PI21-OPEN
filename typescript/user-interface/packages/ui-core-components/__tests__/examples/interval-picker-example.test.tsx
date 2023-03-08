import { render } from '@testing-library/react';
import React from 'react';

import { IntervalPickerExample } from '../../src/ts/examples/interval-picker-example';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

it('renders a component', () => {
  const { container } = render(<IntervalPickerExample />);
  expect(container).toMatchSnapshot();
});
