import { render } from '@testing-library/react';
import React from 'react';

import * as DomUtils from '../../src/ts/ui-util/dom-util';

/* eslint-disable @typescript-eslint/consistent-type-assertions */
describe('Dom util', () => {
  const parent = {
    scrollHeight: 200,
    clientHeight: 100,
    getBoundingClientRect: () =>
      ({
        top: 0,
        bottom: 100
      } as DOMRect)
  } as Element;
  const element = {
    parentElement: parent,
    scrollHeight: 50,
    clientHeight: 50,
    getBoundingClientRect: () =>
      ({
        top: 25,
        bottom: 75
      } as DOMRect)
  } as Element;

  it('should filter non-DOM elements', () => {
    expect(DomUtils.isDomElement({ dummy: 'data' })).toBe(false);
  });

  it('should find scroll parents', () => {
    expect(DomUtils.getScrollParent(element)).toBe(parent);
    expect(DomUtils.getScrollParent(parent)).toBe(parent);
    expect(DomUtils.getScrollParent(null)).toBeUndefined();
  });

  it('should detect in- and out-of-view elements', () => {
    expect(DomUtils.isElementOutOfView(element)).toBe(false);
    expect(
      DomUtils.isElementOutOfView({
        ...element,
        getBoundingClientRect: () =>
          ({
            top: 0,
            bottom: 75
          } as DOMRect)
      })
    ).toBe(true);
    expect(DomUtils.isElementOutOfView(null)).toBeUndefined();
  });

  it(`should be able to render the text width`, () => {
    const { container: wrapperWithoutFont } = render(
      <div style={{ font: '8px' }}>{DomUtils.getTextWidth('this is my text')}</div>
    );
    expect(wrapperWithoutFont).toMatchSnapshot();

    const { container: wrapperWithFont } = render(
      <div>{DomUtils.getTextWidth('this is my text', '100px')}</div>
    );
    expect(wrapperWithFont).toMatchSnapshot();
  });
});
