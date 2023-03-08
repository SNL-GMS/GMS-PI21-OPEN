/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import * as React from 'react';

import { LabelValue } from '../../../../../src/ts/components/waveform-display/components/ruler/label-value';
import type { LabelValueProps } from '../../../../../src/ts/components/waveform-display/components/ruler/types';

const props: LabelValueProps = {
  value: '10',
  label: 'test',
  tooltip: 'test-tip',
  valueColor: '#000000'
};

const props2: LabelValueProps = {
  value: '',
  label: 'test',
  tooltip: 'test-tip'
};

describe('Weavess LabelValue Renderer', () => {
  it('to be defined', () => {
    expect(LabelValue).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<LabelValue {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('renders with label with 0 length', () => {
    const { container } = render(<LabelValue {...props2} />);
    expect(container).toMatchSnapshot();
  });
});
