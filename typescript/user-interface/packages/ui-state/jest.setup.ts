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
import 'fake-indexeddb/auto';

import Adapter from '@cfaester/enzyme-adapter-react-18';
import type { PayloadAction } from '@reduxjs/toolkit';
import Enzyme, { mount, render, shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import type { GlobalWithFetchMock } from 'jest-fetch-mock';

const customGlobal: GlobalWithFetchMock = (global as unknown) as GlobalWithFetchMock;
// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
customGlobal.fetch = require('jest-fetch-mock');

customGlobal.fetchMock = customGlobal.fetch;

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
require('jest-canvas-mock');

Enzyme.configure({ adapter: new Adapter() });

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const globalAny: any = global;

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
globalAny.fetch = require('jest-fetch-mock');

// Make Enzyme functions available in all test files without importing
globalAny.shallow = shallow;
globalAny.render = render;
globalAny.mount = mount;
globalAny.toJson = toJson;
globalAny.TextEncoder = util.TextEncoder;

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports,  import/no-extraneous-dependencies
require('jest-fetch-mock').enableMocks();

jest.mock('@gms/ui-wasm', () => {
  const idcHeapFilter = async (
    samples: Float32Array,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    frequency = 40,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    filterDesign = '1 2 1 BP 0'
  ): Promise<Float32Array> => Promise.resolve(samples);
  return {
    idcHeapFilter
  };
});

jest.mock('redux-state-sync', () => ({
  createStateSyncMiddleware: () => () => (next: (action: PayloadAction) => void) => (
    action: PayloadAction
  ) => next(action),
  initMessageListener: () => jest.fn(),
  initStateWithPrevTab: () => jest.fn(),
  withReduxStateSync: (obj: any) => obj
}));
