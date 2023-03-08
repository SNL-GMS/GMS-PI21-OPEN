import { H1 } from '@blueprintjs/core';
import * as React from 'react';
// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
/* eslint-disable import/first */
/* eslint-disable import/no-extraneous-dependencies */
import * as util from 'util';

Object.defineProperty(window, 'TextEncoder', {
  writable: true,
  value: util.TextEncoder
});
Object.defineProperty(window, 'TextDecoder', {
  writable: true,
  value: util.TextDecoder
});
Object.defineProperty(global, 'TextEncoder', {
  writable: true,
  value: util.TextEncoder
});
Object.defineProperty(global, 'TextDecoder', {
  writable: true,
  value: util.TextDecoder
});

import { render } from '@testing-library/react';

describe('Test should run', () => {
  describe('Test Environment is available', () => {
    describe('enzyme should be available', () => {
      test('render a label', () => {
        // eslint-disable-next-line jsx-a11y/label-has-associated-control
        const { container } = render(<label>Hello Jest!</label>);
        expect(container).toMatchSnapshot();
      });
      test('render a div', () => {
        const { container } = render(<div>Hello Jest!</div>);
        expect(container).toMatchSnapshot();
      });
      test('render an h1', () => {
        const { container } = render(<H1>Hello Jest!</H1>);
        expect(container).toMatchSnapshot();
      });
    });
  });
});
