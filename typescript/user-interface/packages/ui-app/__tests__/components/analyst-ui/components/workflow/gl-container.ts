import type GoldenLayout from '@gms/golden-layout';

export const glContainer: GoldenLayout.Container = {
  title: 'workflow',
  width: 1900,
  height: 1200,
  isHidden: false,
  layoutManager: undefined,
  parent: undefined,
  tab: undefined,
  close: jest.fn(),
  emit: jest.fn(),
  extendState: jest.fn(),
  getElement: jest.fn(),
  getState: jest.fn(),
  hide: jest.fn(),
  off: jest.fn(),
  on: jest.fn(),
  setSize: jest.fn(),
  setState: jest.fn(),
  setTitle: jest.fn(),
  show: jest.fn(),
  trigger: jest.fn(),
  unbind: jest.fn()
};
