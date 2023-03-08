/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { SpectrogramRenderer } from '../../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/spectrogram-renderer/spectrogram-renderer';
import type { SpectrogramRendererProps } from '../../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/spectrogram-renderer/types';

const props: SpectrogramRendererProps = {
  displayInterval: {
    startTimeSecs: 400,
    endTimeSecs: 700
  },
  startTimeSecs: 5,
  data: [
    [1, 2, 3, 4, 5],
    [6, 7, 8, 9, 10]
  ],
  frequencyStep: 4,
  timeStep: 2,
  setYAxisBounds: jest.fn(),
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  colorScale: jest.fn((value: number) => '#ff000')
};

const wrapper = Enzyme.mount(<SpectrogramRenderer {...props} />);
const instance: any = wrapper.find(SpectrogramRenderer).instance();

describe('Weavess Spectrogram Renderer', () => {
  it('to be defined', () => {
    expect(SpectrogramRenderer).toBeDefined();
  });

  it('update when various props change', () => {
    let prevProps = {
      ...props,
      timeStep: 6
    };
    expect(() => instance.componentDidUpdate(prevProps)).not.toThrow();

    prevProps = {
      ...props,
      frequencyStep: 5
    };
    expect(() => instance.componentDidUpdate(prevProps)).not.toThrow();

    prevProps = {
      ...props,
      data: [
        [1, 2, 3, 4, 5, 6],
        [6, 7, 8, 9, 10, 11]
      ]
    };
    expect(() => instance.componentDidUpdate(prevProps)).not.toThrow();

    prevProps = {
      ...props,
      displayInterval: {
        startTimeSecs: 450,
        endTimeSecs: 750
      }
    };
    expect(() => instance.componentDidUpdate(prevProps)).not.toThrow();
  });

  it('renders', () => {
    const { container } = render(<SpectrogramRenderer {...props} />);
    expect(container).toMatchSnapshot();
  });
});
