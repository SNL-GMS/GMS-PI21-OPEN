import { render } from '@testing-library/react';
import React from 'react';

import { FilterableOptionList } from '../../src/ts/components/ui-widgets/filterable-option-list';
import type { FilterableOptionListProps } from '../../src/ts/components/ui-widgets/filterable-option-list/types';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

const props: FilterableOptionListProps = {
  options: ['alpha', 'beta', 'delta', 'gamma'],
  priorityOptions: ['aa', 'bb', 'cc', 'dd'],
  defaultSelection: 'uno',
  defaultFilter: 'o',
  disabled: false,
  widthPx: 120,
  onSelection: jest.fn(),
  onEnter: jest.fn(),
  onClick: jest.fn(),
  onDoubleClick: jest.fn()
};

// eslint-disable-next-line react/jsx-props-no-spreading
const wrapper = Enzyme.shallow(<FilterableOptionList {...props} />);
// eslint-disable-next-line react/jsx-props-no-spreading
const { container } = render(<FilterableOptionList {...props} />);

describe('Core drop down', () => {
  it('Renders', () => {
    expect(container).toMatchSnapshot();
  });
  const instance = wrapper.instance();

  it('Can empty filter', () => {
    instance.onFilterInput({ currentTarget: { value: '' } });
  });
  it('selects', () => {
    instance.selectOption('alpha', false);
    expect(instance.state.currentlySelected).toBe('alpha');
  });
  it('double clicks', () => {
    instance.selectOption('beta', true);
    expect(instance.state.currentlySelected).toBe('beta');
  });
  it('key press up', () => {
    instance.onKeyPress({ key: 'ArrowUp', preventDefault: jest.fn() });
    expect(instance.state.currentlySelected).toBe('alpha');
  });
  it('key press down', () => {
    instance.onKeyPress({ key: 'ArrowDown', preventDefault: jest.fn() });
    expect(instance.state.currentlySelected).toBe('beta');
  });

  it('can move down in priority list', () => {
    instance.selectOption('aa', false);
    instance.selectOption('aa', false);
    instance.onKeyPress({ key: 'ArrowDown', preventDefault: jest.fn() });
    expect(instance.state.currentlySelected).toBe('bb');
  });
  it('can move up in priority list', () => {
    instance.onKeyPress({ key: 'ArrowUp', preventDefault: jest.fn() });
    expect(instance.state.currentlySelected).toBe('aa');
  });
  it('can move up in priority list back to bottom of list', () => {
    instance.onKeyPress({ key: 'ArrowUp', preventDefault: jest.fn() });
    expect(instance.state.currentlySelected).toBe('gamma');
  });
  it('can move down in  list back to top of list', () => {
    instance.onKeyPress({ key: 'ArrowDown', preventDefault: jest.fn() });
    expect(instance.state.currentlySelected).toBe('aa');
  });

  it('can move up into priority list', () => {
    instance.selectOption('alpha', false);
    instance.onKeyPress({ key: 'ArrowUp', preventDefault: jest.fn() });
    expect(instance.state.currentlySelected).toBe('dd');
  });
  it('Selects item after filter', () => {
    instance.onFilterInput({ currentTarget: { value: 'bb' } });
    expect(instance.state.currentlySelected).toBe('bb');
    instance.onFilterInput({ currentTarget: { value: 'delta' } });
    expect(instance.state.currentlySelected).toBe('delta');
  });

  it('accepts enter key', () => {
    instance.onKeyPress({ key: 'Enter', preventDefault: jest.fn() });
    expect(instance.state).toMatchSnapshot();
  });
});
