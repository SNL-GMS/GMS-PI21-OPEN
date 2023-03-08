import { render } from '@testing-library/react';
import React from 'react';

import { EnvironmentCellValue } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/cell-renderers/environment-cell-value';
// eslint-disable-next-line max-len
import type { EnvironmentCellValueProps } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/cell-renderers/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe("environment cell renderer's exports", () => {
  const myPropsNeedsAttention: EnvironmentCellValueProps = {
    hasUnacknowledgedChanges: true,
    value: 999
  };
  const myProps: EnvironmentCellValueProps = {
    hasUnacknowledgedChanges: false,
    value: 999
  };

  it('should be defined', () => {
    expect(EnvironmentCellValue).toBeDefined();
  });
  it('should match snapshot', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<EnvironmentCellValue {...myProps} />);
    expect(container).toMatchSnapshot();
  });
  it('should match snapshot with needs attention', () => {
    const { container } = render(
      // eslint-disable-next-line react/jsx-props-no-spreading
      <EnvironmentCellValue {...myPropsNeedsAttention} />
    );
    expect(container).toMatchSnapshot();
  });
});
