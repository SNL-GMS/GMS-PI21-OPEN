import { render } from '@testing-library/react';
import React from 'react';

import { ToolbarPopover } from '../../src/ts/examples/toolbar-popover';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

it('renders a component', () => {
  const { container } = render(<ToolbarPopover defaultValue={undefined} onChange={jest.fn()} />);
  expect(container).toMatchSnapshot();
});
