import { render } from '@testing-library/react';
import * as React from 'react';

import { DragHandleDivider } from '../../../../src/ts/components/divider/horizontal-divider/drag-handle-divider';
import type * as DragHandleDividerTypes from '../../../../src/ts/components/divider/horizontal-divider/types';

const props: DragHandleDividerTypes.DragHandleDividerProps = {
  handleHeight: 10,
  onDrag: jest.fn()
};
const { handleHeight } = props;

describe('Drag Handle Divider', () => {
  it('to be defined', () => {
    expect(DragHandleDivider).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(
      <DragHandleDivider handleHeight={handleHeight} onDrag={jest.fn()} />
    );
    expect(container).toMatchSnapshot();
  });
});
