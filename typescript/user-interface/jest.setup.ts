/* eslint-disable import/no-extraneous-dependencies */
import { mount, render, shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import type { GlobalWithFetchMock } from 'jest-fetch-mock';

const customGlobal: GlobalWithFetchMock = global as GlobalWithFetchMock;
// eslint-disable-next-line @typescript-eslint/no-require-imports, import/no-extraneous-dependencies
customGlobal.fetch = require('jest-fetch-mock');

customGlobal.fetchMock = customGlobal.fetch;

// eslint-disable-next-line @typescript-eslint/no-require-imports
require('jest-canvas-mock');
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const globalAny: any = global;

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Adapter = require('enzyme-adapter-react-16');

// React Enzyme adapter
Enzyme.configure({ adapter: new Adapter() });

// Make Enzyme functions available in all test files without importing
globalAny.shallow = shallow;
globalAny.render = render;
globalAny.mount = mount;
globalAny.toJson = toJson;
globalAny.window = window;
// eslint-disable-next-line @typescript-eslint/no-require-imports
globalAny.$ = require('jquery');

globalAny.jQuery = globalAny.$;
// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
globalAny.fetch = require('jest-fetch-mock');

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
