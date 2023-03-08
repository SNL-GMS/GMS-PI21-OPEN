/* eslint-disable @typescript-eslint/unbound-method */
import type GoldenLayout from '@gms/golden-layout';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import {
  addGlForceUpdateOnHide,
  addGlForceUpdateOnResize,
  addGlForceUpdateOnShow,
  addGlUpdateOnHide,
  addGlUpdateOnResize,
  addGlUpdateOnShow,
  useForceGlUpdateOnResizeAndShow
} from '../../src/ts/ui-util/gl-util';
import { renderHook } from './utils/render-hook-util';

// Tell Jest to mock all timeout functions
jest.useFakeTimers();

const gl: GoldenLayout.Container = {
  close: jest.fn(),
  emit: jest.fn(),
  extendState: jest.fn(),
  getElement: jest.fn(),
  getState: jest.fn(),
  height: 100,
  layoutManager: undefined,
  off: jest.fn(),
  parent: undefined,
  setState: jest.fn(),
  setTitle: jest.fn(),
  tab: undefined,
  title: undefined,
  trigger: jest.fn(),
  unbind: jest.fn(),
  width: 400,
  isHidden: false,
  on: jest.fn((action: string, callback: () => void) => {
    document.addEventListener(action, callback);
  }),
  show: jest.fn(() => {
    const event = new Event('show');
    document.dispatchEvent(event);
    return true;
  }),
  hide: jest.fn(() => {
    const event = new Event('hide');
    document.dispatchEvent(event);
    return true;
  }),
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  setSize: jest.fn((height: number, width: number) => {
    const event = new Event('resize');
    document.dispatchEvent(event);
    return true;
  })
};

describe('Electron utils', () => {
  it('to be defined', () => {
    expect(addGlForceUpdateOnHide).toBeDefined();
    expect(addGlForceUpdateOnResize).toBeDefined();
    expect(addGlForceUpdateOnShow).toBeDefined();
    expect(addGlUpdateOnHide).toBeDefined();
    expect(addGlUpdateOnResize).toBeDefined();
    expect(addGlUpdateOnShow).toBeDefined();
    expect(useForceGlUpdateOnResizeAndShow).toBeDefined();
  });

  it('addGlUpdateOnShow', () => {
    const spy = jest.fn();

    addGlUpdateOnShow(gl, spy);

    gl.show();

    // Fast-forward time
    jest.runAllTimers();

    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('addGlUpdateOnHide', () => {
    const spy = jest.fn();

    addGlUpdateOnHide(gl, spy);

    gl.hide();

    // Fast-forward time
    jest.runAllTimers();

    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('addGlForceUpdateOnShow', () => {
    const spy = jest.fn();

    const wrapper = Enzyme.mount(<div />);
    wrapper.instance().forceUpdate = spy;

    addGlForceUpdateOnShow(undefined, wrapper.instance());
    addGlForceUpdateOnShow(gl, undefined);
    addGlForceUpdateOnShow(gl, wrapper.instance());

    gl.show();

    // Fast-forward time
    jest.runAllTimers();

    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('addGlForceUpdateOnHide', () => {
    const spy = jest.fn();

    const wrapper = Enzyme.mount(<div />);
    wrapper.instance().forceUpdate = spy;

    addGlForceUpdateOnHide(undefined, wrapper.instance());
    addGlForceUpdateOnHide(gl, undefined);
    addGlForceUpdateOnHide(gl, wrapper.instance());

    gl.hide();

    // Fast-forward time
    jest.runAllTimers();

    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('addGlUpdateOnResize', () => {
    const spy = jest.fn();

    addGlUpdateOnResize(gl, spy);

    gl.setSize(100, 100);

    // Fast-forward time
    jest.runAllTimers();

    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('addGlForceUpdateOnResize', () => {
    const spy = jest.fn();

    const wrapper = Enzyme.mount(<div />);
    wrapper.instance().forceUpdate = spy;

    addGlForceUpdateOnResize(undefined, wrapper.instance());
    addGlForceUpdateOnResize(gl, undefined);
    addGlForceUpdateOnResize(gl, wrapper.instance());

    gl.setSize(100, 100);

    // Fast-forward time
    jest.runAllTimers();

    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('useForceGlUpdateOnResizeAndShow', () => {
    const container: GoldenLayout.Container = {
      ...gl,
      on: jest.fn((action: string, callback: () => void) => {
        document.addEventListener(action, callback);
      }),
      show: jest.fn(() => {
        const event = new Event('show');
        document.dispatchEvent(event);
        return true;
      }),
      hide: jest.fn(() => {
        const event = new Event('hide');
        document.dispatchEvent(event);
        return true;
      }),
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      setSize: jest.fn((height: number, width: number) => {
        const event = new Event('resize');
        document.dispatchEvent(event);
        return true;
      })
    };

    renderHook(() => {
      useForceGlUpdateOnResizeAndShow(undefined);
      return [];
    });

    renderHook(() => {
      useForceGlUpdateOnResizeAndShow(container);
      return [];
    });

    container.hide();
    container.show();
    container.setSize(100, 100);

    // Fast-forward time
    jest.runAllTimers();

    expect(container.hide).toHaveBeenCalledTimes(1);
    expect(container.show).toHaveBeenCalledTimes(1);
    expect(container.setSize).toHaveBeenCalledTimes(1);
  });
});
