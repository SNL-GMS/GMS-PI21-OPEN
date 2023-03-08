import React from 'react';

import { DragCell } from '../../../../../src/ts/components/data-acquisition-ui/shared/table/drag-cell';
import type { DragCellProps } from '../../../../../src/ts/components/data-acquisition-ui/shared/types';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

describe('Drag Cell', () => {
  const dragCellProps: DragCellProps = {
    stationId: 'AAA',
    getSelectedStationIds: jest.fn(() => ['AAA', 'BBB']),
    getSingleDragImage: jest.fn(),
    setSelectedStationIds: jest.fn()
  };

  const dragCellProps2: DragCellProps = {
    stationId: 'CCC',
    getSelectedStationIds: jest.fn(() => undefined),
    getSingleDragImage: jest.fn(),
    setSelectedStationIds: jest.fn()
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  const wrapper = Enzyme.mount(<DragCell {...dragCellProps} />);
  // eslint-disable-next-line react/jsx-props-no-spreading
  const wrapper2 = Enzyme.mount(<DragCell {...dragCellProps2} />);
  it('is defined', () => {
    expect(wrapper).toBeDefined();
  });
  it('can get updated selection', () => {
    const updatedSelection = wrapper2.find(DragCell).instance().getUpdatedSelection();
    const expectedResult = ['CCC'];
    expect(updatedSelection).toEqual(expectedResult);
  });
  it('can handle drag start', () => {
    const dragEvent = {};
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'handleDragStart');
    wrapper.find(DragCell).instance().handleDragStart(dragEvent);
    expect(spy).toHaveBeenCalled();
  });

  it('can handle drag end', () => {
    const dragEvent = {};
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'handleDragEnd');
    wrapper.find(DragCell).instance().handleDragEnd(dragEvent);
    expect(spy).toHaveBeenCalled();
  });

  it('can handle isDraggingMultipleCells', () => {
    const dragEvent = {};
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'isDraggingMultipleCells');
    wrapper.find(DragCell).instance().isDraggingMultipleCells(dragEvent);
    expect(spy).toHaveBeenCalled();
  });

  it('can getDragImage when isDraggingMultipleCells is true', () => {
    const dragEvent = {};
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'getDragImage');
    wrapper.find(DragCell).instance().getDragImage(dragEvent);
    expect(spy).toHaveBeenCalled();
  });
  it('can getDragImage when isDraggingMultipleCells is false', () => {
    const dragEvent = {};
    const spy = jest.spyOn(wrapper2.find(DragCell).instance(), 'getDragImage');
    wrapper2.find(DragCell).instance().getDragImage(dragEvent);
    expect(spy).toHaveBeenCalled();
  });

  it('can getDropZones', () => {
    const closestElement: any = {
      querySelector: jest.fn(() => 'test'),
      querySelectorAll: jest.fn(() => 'test')
    };
    const element: Element = document.createElement('drag');
    const dragEvent = {
      target: element
    };
    dragEvent.target.closest = jest.fn(() => closestElement);
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'getDropZones');
    wrapper.find(DragCell).instance().getDropZones(dragEvent);
    expect(spy).toHaveBeenCalled();
  });

  it('can highlightDropZone', () => {
    const closestElement: any = {
      querySelector: jest.fn(() => 'test'),
      querySelectorAll: jest.fn(() => [document.createElement('div')])
    };
    const element: Element = document.createElement('drag');
    const dragEvent = {
      target: element
    };
    dragEvent.target.closest = jest.fn(() => closestElement);
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'highlightDropZone');
    wrapper.find(DragCell).instance().highlightDropZone(dragEvent);
    expect(spy).toHaveBeenCalled();
  });

  it('can removeParentDropZoneHighlight', () => {
    const closestElement: any = {
      querySelector: jest.fn(() => 'test'),
      querySelectorAll: jest.fn(() => [document.createElement('div')])
    };
    const element: Element = document.createElement('drag');
    const dragEvent = {
      target: element
    };
    dragEvent.target.closest = jest.fn(() => closestElement);
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'removeParentDropZoneHighlight');
    wrapper.find(DragCell).instance().removeParentDropZoneHighlight(dragEvent);
    expect(spy).toHaveBeenCalled();
  });

  it('can cleanUpDragImages', () => {
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'cleanUpDragImages');
    wrapper.find(DragCell).instance().cleanUpDragImages();
    expect(spy).toHaveBeenCalled();
  });

  it('can componentWillUnmount clean up draw images', () => {
    const spy = jest.spyOn(wrapper.find(DragCell).instance(), 'cleanUpDragImages');
    wrapper.find(DragCell).instance().componentWillUnmount();
    expect(spy).toHaveBeenCalled();
  });

  it('can getDropZones handle error', () => {
    expect(() => {
      wrapper.find(DragCell).instance().getDropZones({});
    }).toThrowError();
  });

  it('can validateDropZoneAndWrapper handle no dropZoneWrapper error', () => {
    const element: Element = document.createElement('drag');
    const dragEvent = {
      target: element
    };
    dragEvent.target.closest = jest.fn(() => {
      throw Error();
    });
    expect(() => {
      wrapper.find(DragCell).instance().validateDropZoneAndWrapper(dragEvent);
    }).toThrowError();
  });

  it('can validateDropZoneAndWrapper handle no dropZone error', () => {
    const closestElement: any = {
      querySelector: jest.fn(() => undefined),
      querySelectorAll: jest.fn(() => 'test')
    };
    const element: Element = document.createElement('drag');
    const dragEvent = {
      target: element
    };
    dragEvent.target.closest = jest.fn(() => closestElement);
    expect(() => {
      wrapper.find(DragCell).instance().validateDropZoneAndWrapper(dragEvent);
    }).toThrowError();
  });
});
