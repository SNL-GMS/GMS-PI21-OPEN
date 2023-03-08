/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import React from 'react';

import { EmptyRenderer } from '../../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/empty-renderer/empty-renderer';
import type { EmptyRendererProps } from '../../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/empty-renderer/types';

const props: EmptyRendererProps = {
  displayInterval: {
    startTimeSecs: 0,
    endTimeSecs: 500
  }
};

describe('Weavess Empty Renderer', () => {
  it('to be defined', () => {
    expect(EmptyRenderer).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<EmptyRenderer {...props} />);
    expect(container).toMatchSnapshot();
  });
});
