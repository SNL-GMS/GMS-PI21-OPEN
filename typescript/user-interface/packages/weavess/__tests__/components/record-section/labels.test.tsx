/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import * as React from 'react';

import type { RecordSectionLabelsProps } from '../../../src/ts/components/record-section-display/labels';
import { RecordSectionLabels } from '../../../src/ts/components/record-section-display/labels';

const props: RecordSectionLabelsProps = {
  bottomVal: 1000,
  topVal: 2,
  phases: [
    {
      percentX: 4,
      percentY: 6,
      phase: 'phase 1'
    }
  ]
};

describe('Weavess Empty Renderer', () => {
  it('to be defined', () => {
    expect(RecordSectionLabels).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<RecordSectionLabels {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('uses an interval of 5', () => {
    const p: RecordSectionLabelsProps = {
      ...props,
      bottomVal: 100,
      topVal: 80
    };
    const { container } = render(<RecordSectionLabels {...p} />);
    expect(container).toMatchSnapshot();
  });

  it('uses an interval of 10', () => {
    const p: RecordSectionLabelsProps = {
      ...props,
      bottomVal: 100,
      topVal: 40
    };
    const { container } = render(<RecordSectionLabels {...p} />);
    expect(container).toMatchSnapshot();
  });
});
