import { render } from '@testing-library/react';
import React from 'react';

import { VersionInfo } from '../../../../../src/ts/components/common-ui/components/version-info/version-info';

describe('Version info', () => {
  let container;
  beforeEach(() => {
    container = render(<VersionInfo />).container;
  });
  it('matches a snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
