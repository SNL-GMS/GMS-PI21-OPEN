import { H1 } from '@blueprintjs/core';
import * as React from 'react';

import type * as HorizontalDividerTypes from '../../../../src/ts/components/divider/horizontal-divider/types';

const topHeightPixValue = 100;
const minBottomHeightPixValue = 100;

const topElement = <H1>Hello</H1>;
const bottomElement = <H1>World</H1>;

const hdState: HorizontalDividerTypes.HorizontalDividerState = {
  topComponentHeightPx: topHeightPixValue
};

const horDivSizeRange: HorizontalDividerTypes.HorizontalDividerSizeRange = {
  minimumTopHeightPx: 100,
  minimumBottomHeightPx: 100
};

const props: HorizontalDividerTypes.HorizontalDividerProps = {
  topHeightPx: topHeightPixValue,
  sizeRange: horDivSizeRange,
  minimumBottomHeightPx: minBottomHeightPixValue,
  top: topElement,
  bottom: bottomElement,
  onResize: jest.fn(),
  onResizeEnd: jest.fn()
};

const props2: HorizontalDividerTypes.HorizontalDividerProps = {
  top: topElement,
  bottom: bottomElement
};

const dhProps: HorizontalDividerTypes.DragHandleDividerProps = {
  handleHeight: 10,
  onDrag: jest.fn()
};

describe('Horizontal Divider', () => {
  it('Horizontal Divider State to be defined', () => {
    expect(hdState).toBeDefined();
  });

  it('Horizontal Divider Size Range to be defined', () => {
    expect(horDivSizeRange).toBeDefined();
  });

  it('Horizontal Divider Props to be defined', () => {
    expect(props).toBeDefined();
  });

  it('Horizontal Divider Default Props to be defined', () => {
    expect(props2).toBeDefined();
  });

  it('Drag Handle Props to be defined', () => {
    expect(dhProps).toBeDefined();
  });
});
