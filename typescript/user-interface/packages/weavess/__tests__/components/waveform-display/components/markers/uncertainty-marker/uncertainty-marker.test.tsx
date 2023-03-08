/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import type { UncertaintyMarkerProps } from '../../../../../../src/ts/components/waveform-display/components/markers/uncertainty-marker/types';
import { UncertaintyMarker } from '../../../../../../src/ts/components/waveform-display/components/markers/uncertainty-marker/uncertainty-marker';

const props: UncertaintyMarkerProps = {
  id: 'my-uncertainty-marker',
  color: '#ff0000',
  position: 90,
  pickMarkerPosition: 92,
  isLeftUncertaintyBar: true
};

const wrapper = Enzyme.mount(<UncertaintyMarker {...props} />);
const instance: UncertaintyMarker = wrapper.find(UncertaintyMarker).instance() as UncertaintyMarker;

describe('Weavess Vertical Marker', () => {
  it('Weavess Vertical Marker to be defined', () => {
    expect(UncertaintyMarker).toBeDefined();
  });

  it('shallow renders as left marker', () => {
    const { container } = render(<UncertaintyMarker {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('shallow renders as right marker', () => {
    const rightProps = {
      ...props,
      isLeftUncertaintyBar: false
    };
    const { container } = render(<UncertaintyMarker {...rightProps} />);
    expect(container).toMatchSnapshot();
  });

  it('componentDidCatch', () => {
    const spy = jest.spyOn(instance, 'componentDidCatch');
    instance.componentDidCatch(new Error('error'), { componentStack: undefined });
    expect(spy).toBeCalled();
  });
});
