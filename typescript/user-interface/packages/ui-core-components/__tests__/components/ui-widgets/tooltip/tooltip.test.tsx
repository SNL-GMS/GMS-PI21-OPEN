import React from 'react';
import { create } from 'react-test-renderer';

import { Tooltip2Wrapper } from '../../../../src/ts/components/ui-widgets/tooltip';

describe('Tooltip2Wrapper', () => {
  const wrapper = create(
    <Tooltip2Wrapper content="contents rendered here">target rendered here</Tooltip2Wrapper>
  );
  it('matches a snapshot', () => {
    expect(wrapper.toJSON()).toMatchSnapshot();
  });
});
