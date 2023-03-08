import { render } from '@testing-library/react';
import React from 'react';

import type { SohContextData } from '../../../../src/ts/components/data-acquisition-ui/shared/soh-context';
import { SohContext } from '../../../../src/ts/components/data-acquisition-ui/shared/soh-context';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Soh context', () => {
  // eslint-disable-next-line react/jsx-no-constructed-context-values
  const sohChartContext: SohContextData = {
    glContainer: {} as any
  };
  // eslint-disable-next-line react/function-component-definition
  const DumbComp: React.FunctionComponent = () => <div />;
  const { container } = render(
    <SohContext.Provider value={sohChartContext}>
      <DumbComp />
    </SohContext.Provider>
  );
  it('should be defined', () => {
    expect(SohContext).toBeDefined();
  });
  it('should match snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
