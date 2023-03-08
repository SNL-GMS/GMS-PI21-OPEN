/* eslint-disable react/jsx-props-no-spreading */

import { H1 } from '@blueprintjs/core';
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { HorizontalDivider } from '../../../../src/ts/components/divider/horizontal-divider/horizontal-divider';
// import { HorizontalDividerProps, HorizontalDividerSizeRange, HorizontalDividerState } from '../../../../src/ts/components/divider/horizontal-divider/types';
import type * as HorizontalDividerTypes from '../../../../src/ts/components/divider/horizontal-divider/types';

const topHeightPixValue = 100;
const minBottomHeightPixValue = 100;
const horDivSizeRange: HorizontalDividerTypes.HorizontalDividerSizeRange = {
  minimumTopHeightPx: 100,
  minimumBottomHeightPx: 100
};
const topElement = <H1>Hello</H1>;
const bottomElement = <H1>World</H1>;

const expectedHandleHeight = 7;

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

const wrapper = Enzyme.mount(<HorizontalDivider {...props} />);
const instance: HorizontalDivider = wrapper.find(HorizontalDivider).instance() as HorizontalDivider;

describe('Horizontal Divider', () => {
  it('to be defined', () => {
    expect(HorizontalDivider).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<HorizontalDivider {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('TopContainerHeight', () => {
    const tch: any = new HorizontalDivider(props);
    const spy = jest.spyOn(tch, 'setTopContainerHeight');
    tch.setTopContainerHeight(100);
    expect(spy).toHaveBeenCalled();

    const spy2 = jest.spyOn(tch, 'getTopContainerHeight');
    const topContHeight = tch.getTopContainerHeight();
    expect(topContHeight).toEqual(100);
    expect(spy2).toHaveBeenCalled();
  });

  it('BottomContainerHeight', () => {
    const bch: any = new HorizontalDivider(props2);
    const spy = jest.spyOn(bch, 'getBottomContainerHeight');
    const bottomContHeight = bch.getBottomContainerHeight();
    expect(bottomContHeight).toBe(NaN);
    expect(spy).toHaveBeenCalled();
  });

  it('Total Height', () => {
    const th: any = new HorizontalDivider(props2);
    const spy = jest.spyOn(th, 'getTotalHeight');
    const totalHeight = th.getTotalHeight();
    expect(totalHeight).toBe(undefined);
    expect(spy).toHaveBeenCalled();
  });

  it('Handle Height', () => {
    const hh: any = new HorizontalDivider(props2);
    const spy = jest.spyOn(hh, 'getHandleHeight');
    const handleHeight = hh.getHandleHeight();
    expect(handleHeight).toBe(expectedHandleHeight);
    expect(spy).toHaveBeenCalled();
  });

  it('Min Top Height', () => {
    const mth: any = new HorizontalDivider(props2);
    const spy = jest.spyOn(mth, 'getMinTopHeightPx');
    const minTopHeight = mth.getMinTopHeightPx();
    expect(minTopHeight).toBe(100);
    expect(spy).toHaveBeenCalled();
  });

  it('Verify', () => {
    const v: any = new HorizontalDivider(props2);
    const spy = jest.spyOn(v, 'verifyConfiguration');
    const spy2 = jest.spyOn(v, 'verifyNonZeroHeight');
    const spy3 = jest.spyOn(v, 'verifyMinDefinedHeights');
    v.verifyConfiguration();
    v.verifyNonZeroHeight();
    v.verifyMinDefinedHeights();
    expect(spy).toHaveBeenCalled();
    expect(spy2).toHaveBeenCalled();
    expect(spy3).toHaveBeenCalled();
  });

  it('onMouseUp', () => {
    const event: any = {
      stopPropagation: jest.fn(),
      target: {
        offsetLeft: 5
      },
      nativeEvent: {
        offsetX: 200,
        offsetY: 180
      }
    };

    const mu: any = new HorizontalDivider(props2);
    const spy = jest.spyOn(mu, 'onMouseUp');
    mu.onMouseUp(event);
    expect(spy).toHaveBeenCalled();
  });

  it('Clean Up Event Listeners', () => {
    const cuel: any = new HorizontalDivider(props2);
    const spy = jest.spyOn(cuel, 'cleanUpEventListeners');
    cuel.cleanUpEventListeners();
    expect(spy).toHaveBeenCalled();
  });

  it('instance is defined', () => {
    expect(instance).toBeDefined();
  });

  /**
   * need to fix
   */

  // xit('Max Top Height', () => {
  //   const spy = jest.spyOn(instance, 'getMaxTopHeightPx');
  //   const maxTopHeight = instance.getMaxTopHeightPx();
  //   expect(maxTopHeight).toBe(100);
  //   expect(spy).toHaveBeenCalled();
  // });

  // xit('onMouseMove', () => {
  //   const event: any = {
  //     stopPropagation: jest.fn(),
  //     target: {
  //       offsetLeft: 5
  //     },
  //     nativeEvent: {
  //       offsetX: 200,
  //       offsetY: 180
  //     }
  //   };

  //   const spy = jest.spyOn(instance, 'onMouseMove');
  //   instance.onMouseMove(event);
  //   expect(spy).toHaveBeenCalled();
  // });

  // xit('THumbnail Horizontal Divider Drag', () => {

  //   const event: any = {
  //     stopPropagation: jest.fn(),
  //     preventDefault: jest.fn(),
  //     target: {
  //       offsetLeft: 5
  //     },
  //     nativeEvent: {
  //       offsetX: 200,
  //       offsetY: 180
  //     }
  //   };

  //   const thdd: any= new HorizontalDivider(props2);
  //   const spy = jest.spyOn(thdd, 'onThumbnailHorizontalDividerDrag');
  //   thdd.onThumbnailHorizontalDividerDrag(event);
  //   expect(spy).toHaveBeenCalled();
  // });
});
