import { H1 } from '@blueprintjs/core';
import { render } from '@testing-library/react';
import * as React from 'react';

describe('Test should run', () => {
  describe('Test Environment is available', () => {
    describe('RTL should be available', () => {
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
