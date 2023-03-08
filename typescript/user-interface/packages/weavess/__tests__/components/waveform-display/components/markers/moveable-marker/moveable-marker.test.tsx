/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { WeavessTypes } from '@gms/weavess-core';
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { MoveableMarker } from '../../../../../../src/ts/components/waveform-display/components/markers/moveable-marker/moveable-marker';
import type { MoveableMarkerProps } from '../../../../../../src/ts/components/waveform-display/components/markers/moveable-marker/types';

const props: MoveableMarkerProps = {
  marker: {
    id: 'my-marker',
    color: '#ff0000',
    lineStyle: WeavessTypes.LineStyle.DASHED,
    timeSecs: 200,
    minTimeSecsConstraint: 15,
    maxTimeSecsConstraint: 600
  },
  percentageLocation: 80,
  labelWidthPx: 10,
  timeRange: () => ({ startTimeSecs: 0, endTimeSecs: 1000 }),
  getZoomRatio: jest.fn(() => 0.5),
  containerClientWidth: () => 900,
  viewportClientWidth: () => 900,
  onUpdateMarker: undefined,
  updateTimeWindowSelection: undefined,
  associatedStartMarker: {
    id: 'assoc-start-marker',
    color: 'ffff00',
    lineStyle: WeavessTypes.LineStyle.DASHED,
    timeSecs: 8,
    minTimeSecsConstraint: 5,
    maxTimeSecsConstraint: 100
  },
  associatedEndMarker: {
    id: 'assoc-end-marker',
    color: 'ffffff',
    lineStyle: WeavessTypes.LineStyle.DASHED,
    timeSecs: 650,
    minTimeSecsConstraint: 400,
    maxTimeSecsConstraint: 700
  }
};

const wrapper = Enzyme.mount(<MoveableMarker {...props} />);
const instance: MoveableMarker = wrapper.find(MoveableMarker).instance() as MoveableMarker;

describe('Weavess Moveable Marker', () => {
  it('Weavess Moveable Marker to be defined', () => {
    expect(MoveableMarker).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<MoveableMarker {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('componentDidCatch', () => {
    const spy = jest.spyOn(instance, 'componentDidCatch');
    instance.componentDidCatch(new Error('error'), { componentStack: undefined });
    expect(spy).toBeCalled();
  });

  it('getMinConstraint', () => {
    const spy = jest.spyOn(instance, 'getMinConstraint');
    instance.getMinConstraint();
    expect(spy).toBeCalled();
    expect(instance.getMinConstraint()).toEqual(props.marker.minTimeSecsConstraint);

    const wrapper2 = Enzyme.mount(
      <MoveableMarker
        {...props}
        associatedStartMarker={{
          id: 'assoc-start-marker',
          color: 'ffff00',
          lineStyle: WeavessTypes.LineStyle.DASHED,
          timeSecs: 35,
          minTimeSecsConstraint: 5,
          maxTimeSecsConstraint: 100
        }}
      />
    );
    expect((wrapper2.find(MoveableMarker).instance() as MoveableMarker).getMinConstraint()).toEqual(
      35
    );

    const wrapper3 = Enzyme.mount(
      <MoveableMarker
        {...props}
        marker={{
          ...props.marker,
          minTimeSecsConstraint: undefined
        }}
      />
    );
    expect((wrapper3.find(MoveableMarker).instance() as MoveableMarker).getMinConstraint()).toEqual(
      props.associatedStartMarker?.timeSecs
    );
  });

  it('getMinConstraintPercentage', () => {
    const spy = jest.spyOn(instance, 'getMinConstraintPercentage');
    instance.getMinConstraintPercentage();
    expect(spy).toBeCalled();
    expect(instance.getMinConstraintPercentage()).toEqual(1.55);
  });

  it('getMaxConstraint', () => {
    const spy = jest.spyOn(instance, 'getMaxConstraint');
    instance.getMaxConstraint();
    expect(spy).toBeCalled();
    expect(instance.getMaxConstraint()).toEqual(props.marker.maxTimeSecsConstraint);

    const wrapper2 = Enzyme.mount(
      <MoveableMarker
        {...props}
        associatedEndMarker={{
          id: 'assoc-start-marker',
          color: 'ffff00',
          lineStyle: WeavessTypes.LineStyle.DASHED,
          timeSecs: 99,
          minTimeSecsConstraint: 5,
          maxTimeSecsConstraint: 600
        }}
      />
    );
    expect((wrapper2.find(MoveableMarker).instance() as MoveableMarker).getMaxConstraint()).toEqual(
      99
    );

    const wrapper3 = Enzyme.mount(
      <MoveableMarker
        {...props}
        marker={{
          ...props.marker,
          maxTimeSecsConstraint: undefined
        }}
      />
    );
    expect((wrapper3.find(MoveableMarker).instance() as MoveableMarker).getMaxConstraint()).toEqual(
      props.associatedEndMarker?.timeSecs
    );
  });

  it('getMaxConstraintPercentage', () => {
    const spy = jest.spyOn(instance, 'getMaxConstraintPercentage');
    instance.getMaxConstraintPercentage();
    expect(spy).toBeCalled();
    expect(instance.getMaxConstraintPercentage()).toEqual(59.95);
  });

  it('onMoveableMarkerClick', () => {
    const event: any = {
      stopPropagation: jest.fn(),
      target: {
        offsetLeft: 5
      },
      nativeEvent: {
        offsetX: 200,
        offsetY: 180
      }
    };

    const mm: any = new MoveableMarker(props);
    const spy = jest.spyOn(mm, 'onMoveableMarkerClick');
    mm.onMoveableMarkerClick(event);
    expect(spy).toHaveBeenCalled();

    // const mockCallBack = jest.fn();
    // const wrapper2 = Enzyme.mount(<MoveableMarker {...props} onUpdateMarker={mockCallBack} />);
    // wrapper2.find('.moveable-marker').invoke('onMouseDown')(event);
  });
});
