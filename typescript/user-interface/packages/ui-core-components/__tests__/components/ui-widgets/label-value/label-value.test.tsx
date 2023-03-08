/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import * as React from 'react';

import { LabelValue } from '../../../../src/ts/components/ui-widgets/label-value';
import type { LabelValueProps } from '../../../../src/ts/components/ui-widgets/label-value/types';

describe('label-value', () => {
  const props: LabelValueProps = {
    value: 'value',
    label: 'label',
    tooltip: 'tooltip',
    valueColor: 'blue',
    ianApp: false
  };
  it('to be defined', () => {
    expect(LabelValue).toBeDefined();
  });

  it('label-value renders with ian false', () => {
    const { container } = render(<LabelValue {...props} />);
    expect(container).toMatchSnapshot();
  });
  it('label-value renders with ian true', () => {
    props.ianApp = true;
    const { container } = render(<LabelValue {...props} />);
    expect(container).toMatchSnapshot();
  });
});
