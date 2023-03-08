/* eslint-disable import/first */
/* eslint-disable import/no-extraneous-dependencies */
import 'fake-indexeddb/auto';

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

import Adapter from '@cfaester/enzyme-adapter-react-18';
import type { PayloadAction } from '@reduxjs/toolkit';
import Enzyme, { mount, render, shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

// eslint-disable-next-line @typescript-eslint/no-require-imports
require('jest-canvas-mock');

const globalAny: any = global;

// eslint-disable-next-line @typescript-eslint/no-require-imports
globalAny.fetch = require('jest-fetch-mock');

globalAny.fetchMock = globalAny.fetch;

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports,  import/no-extraneous-dependencies
require('jest-fetch-mock').enableMocks();

// Make Enzyme functions available in all test files without importing
globalAny.shallow = shallow;
globalAny.render = render;
globalAny.mount = mount;
globalAny.toJson = toJson;
window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
window.MutationObserver = jest.fn(() => {
  return {
    observe: jest.fn(),
    disconnect: jest.fn(),
    unobserve: jest.fn(),
    takeRecords: jest.fn()
  };
});
globalAny.window = window;

// eslint-disable-next-line @typescript-eslint/no-require-imports
globalAny.$ = require('jquery');

globalAny.jQuery = globalAny.$;
globalAny.TextEncoder = util.TextEncoder;
// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
globalAny.fetch = require('jest-fetch-mock');
// create range was missing on documents mock
globalAny.document.createRange = () => ({
  setStart: jest.fn(),
  setEnd: jest.fn(),
  commonAncestorContainer: {
    nodeName: 'BODY',
    ownerDocument: document
  }
});
globalAny.window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

Enzyme.configure({ adapter: new Adapter() });

jest.mock('axios', () => {
  const success = 'success';
  const actualAxios = jest.requireActual('axios');
  return {
    ...actualAxios,
    request: jest.fn().mockReturnValue(Promise.resolve(success))
  };
});
jest.mock('cesium', () => {
  const realCesium = jest.requireActual('cesium');
  return {
    ...realCesium,
    BillboardGraphics: jest.fn(x => x),
    buildModuleUrl: jest.fn(),
    Camera: {
      DEFAULT_VIEW_FACTOR: 0
    },
    Cartesian2: jest.requireActual('cesium').Cartesian2,
    Cartesian3: jest.requireActual('cesium').Cartesian3,
    Color: {
      WHITE: '0000',
      GRAY: '5656',
      BLACK: { withAlpha: jest.fn(() => 'Test') },
      fromCssColorString: jest.fn(() => {
        return {
          alpha: 0
        };
      })
    },
    ColorMaterialProperty: jest.requireActual('cesium').ColorMaterialProperty,
    ConstantProperty: jest.fn(x => {
      return {
        _value: x,
        getValue: jest.fn(() => x)
      };
    }),
    ConstantPositionProperty: jest.fn(x => x),
    defined: jest.fn(x => x),
    DistanceDisplayCondition: jest.fn(),
    EllipsoidTerrainProvider: {},
    Entity: jest.requireActual('cesium').Entity,
    EntityCollection: jest.fn(x => x),
    GeoJsonDataSource: {},
    HorizontalOrigin: {
      CENTER: 0
    },
    JulianDate: {
      now: jest.fn(() => 0)
    },
    LabelGraphics: jest.fn(x => {
      return {
        _value: x,
        getValue: jest.fn(() => x)
      };
    }),
    LabelStyle: {
      FILL_AND_OUTLINE: 0
    },
    PolylineGraphics: jest.fn(x => x),
    ProviderViewModel: jest.fn(),
    SceneMode: {
      SCENE2D: true
    },
    ScreenSpaceEventType: {
      MOUSE_MOVE: 'MOUSE_MOVE'
    },
    TileMapServiceImageryProvider: jest.fn(),
    VerticalOrigin: {
      TOP: 0,
      CENTER: 0
    },
    Viewer: {
      scene: {
        pickPosition: jest.fn(x => x)
      }
    }
  };
});

jest.mock('resium', () => {
  const realResium = jest.requireActual('resium');
  return {
    ...realResium,
    Camera: {
      DEFAULT_VIEW_FACTOR: 0
    },
    CameraFlyTo: {},
    CesiumComponentRef: {},
    CesiumMovementEvent: {},
    ImageryLayer: {},
    cesiumElement: {},
    useCesium: () => {
      return {
        camera: {
          pickEllipsoid: jest.fn(() => {
            // Intentional
          })
        },
        globe: {
          ellipsoid: {}
        }
      };
    },
    Viewer: {
      scene: {
        pickPosition: jest.fn(x => x)
      }
    }
  };
});
jest.mock('three', () => {
  const THREE = jest.requireActual('three');
  return {
    ...THREE,
    WebGLRenderer: jest.fn().mockReturnValue({
      domElement: document.createElement('div'), // create a fake div
      setSize: jest.fn(),
      render: jest.fn(),
      setScissorTest: jest.fn(),
      setViewport: jest.fn()
    })
  };
});
jest.mock('worker-rpc', () => {
  const realWorkerRpc = jest.requireActual('worker-rpc');
  // We do this here to guarantee that it runs before the waveform panel generates its workers.
  // This works because jest.mock gets hoisted and run before even imports are figured out.
  Object.defineProperty(window.navigator, 'hardwareConcurrency', {
    writable: false,
    value: 4
  });

  const mockRpc = jest.fn(async () => {
    return new Promise(resolve => {
      resolve([]);
    });
  });

  const RpcProvider = jest.fn().mockImplementation(() => {
    return {
      rpc: mockRpc,
      _dispatch: jest.fn(),
      _nextTransactionId: 0,
      _pendingTransactions: {},
      _rpcHandlers: {},
      _rpcTimeout: 0,
      _signalHandlers: {},
      error: {
        _contexts: [],
        _handlers: [],
        dispatch: jest.fn(),
        hasHandlers: false
      }
    };
  });

  // We don't actually need to mock anything in the worker-rpc module... just to hijack the
  // window before it runs.
  return {
    ...realWorkerRpc,
    RpcProvider
  };
});

jest.mock('react-toastify', () => {
  const actualToastify = jest.requireActual('react-toastify');
  return {
    ...actualToastify,
    info: (message: string, options: any, ...args) => {
      actualToastify.info(message, { ...options, toastId: 'mock-toast-info' }, args);
    },
    warn: (message: string, options: any, ...args) => {
      actualToastify.warn(message, { ...options, toastId: 'mock-toast-warn' }, args);
    },
    error: (message: string, options: any, ...args) => {
      actualToastify.error(message, { ...options, toastId: 'mock-toast-error' }, args);
    }
  };
});

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
