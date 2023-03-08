import { H1 } from '@blueprintjs/core';
import { render } from '@testing-library/react';
import React from 'react';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('Test should run', () => {
  describe('Test Environment is available', () => {
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
