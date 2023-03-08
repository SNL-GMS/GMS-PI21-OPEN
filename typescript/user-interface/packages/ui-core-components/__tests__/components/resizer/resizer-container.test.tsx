import { render } from '@testing-library/react';
import React from 'react';

import { ResizeContainer } from '../../../src/ts/components/resizer';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('ResizeContainer', () => {
  const { container } = render(
    <ResizeContainer>
      <div />
    </ResizeContainer>
  );
  it('is exported', () => {
    expect(ResizeContainer).toBeDefined();
  });
  // TODO Make new tests this tests nothing
  it.skip('will match its snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
