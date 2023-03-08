import { render } from '@testing-library/react';
import React from 'react';

import type { ResizeHandleProps } from '../../../src/ts/components/resizer';
import { BottomResizeHandle } from '../../../src/ts/components/resizer';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('BottomResizeHandle', () => {
  const props: ResizeHandleProps = {
    onResizeEnd: jest.fn(),
    handleMouseMove: jest.fn()
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container } = render(<BottomResizeHandle {...props} />);
  it('is exported', () => {
    expect(BottomResizeHandle).toBeDefined();
  });
  // TODO redo this does not test anything
  it.skip('will match the snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
