import React from 'react';

import type { DragInitiatorProps } from '../../../../../src/ts/components/data-acquisition-ui/shared/table/drag-initiator';
import { DragInitiator } from '../../../../../src/ts/components/data-acquisition-ui/shared/table/drag-initiator';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

describe('Drag Initiator', () => {
  const dragInitiatorProps: DragInitiatorProps<string[]> = {
    getDragPayload: jest.fn(() => ['test']),
    onDragStart: jest.fn(),
    onDragEnd: jest.fn(),
    getDragImage: jest.fn(() => document.createElement('div'))
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  const wrapper = Enzyme.mount(<DragInitiator<string[]> {...dragInitiatorProps} />);
  it('is defined', () => {
    expect(wrapper).toBeDefined();
  });

  it('can handle onDragStart', () => {
    const dragEvent: any = {
      dataTransfer: {
        setData: jest.fn(),
        setDragImage: jest.fn()
      }
    };
    const spy = jest.spyOn(wrapper.find(DragInitiator).instance(), 'onDragStart');
    wrapper.find(DragInitiator).instance().onDragStart(dragEvent);
    expect(spy).toHaveBeenCalled();
  });
});
