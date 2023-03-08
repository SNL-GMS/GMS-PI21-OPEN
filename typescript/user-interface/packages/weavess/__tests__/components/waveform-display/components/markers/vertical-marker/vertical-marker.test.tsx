/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { WeavessTypes } from '@gms/weavess-core';
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import type { VerticalMarkerProps } from '../../../../../../src/ts/components/waveform-display/components/markers/vertical-marker/types';
import { VerticalMarker } from '../../../../../../src/ts/components/waveform-display/components/markers/vertical-marker/vertical-marker';

const props: VerticalMarkerProps = {
  name: 'my-marker',
  color: '#ff0000',
  lineStyle: WeavessTypes.LineStyle.DASHED,
  percentageLocation: 90
};

const wrapper = Enzyme.mount(<VerticalMarker {...props} />);
const instance: VerticalMarker = wrapper.find(VerticalMarker).instance() as VerticalMarker;

describe('Weavess Vertical Marker', () => {
  it('Weavess Vertical Marker to be defined', () => {
    expect(VerticalMarker).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<VerticalMarker {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('componentDidCatch', () => {
    const spy = jest.spyOn(instance, 'componentDidCatch');
    instance.componentDidCatch(new Error('error'), { componentStack: undefined });
    expect(spy).toBeCalled();
  });
});
