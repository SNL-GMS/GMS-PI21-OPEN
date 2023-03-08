import { render } from '@testing-library/react';
import React from 'react';

import { ResizeContainer, Resizer } from '../../../src/ts/components/resizer';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Resizer', () => {
  const { container } = render(
    <ResizeContainer>
      <Resizer />
    </ResizeContainer>
  );
  it('is exported', () => {
    expect(Resizer).toBeDefined();
  });
  // TODO redo this does not test anything
  it.skip('should match snap', () => {
    expect(container).toMatchSnapshot();
  });
});
