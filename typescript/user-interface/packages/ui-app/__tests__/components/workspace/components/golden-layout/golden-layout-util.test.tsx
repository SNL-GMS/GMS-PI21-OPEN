import {
  clearLayout,
  createCloseDisplayFunction,
  createOpenDisplayFunction,
  getAllDisplayKeys,
  getClosedDisplays,
  getDisplaysFromConfig,
  getDisplaysFromContent,
  getDisplayTitle,
  getOpenDisplays,
  removeDisplaysFromContent,
  showLogPopup,
  uniqueLayouts
} from '../../../../../src/ts/components/workspace/components/golden-layout/golden-layout-util';

(localStorage as any).removeItem = jest.fn();

describe('Golden layout util', () => {
  it('functions should be defined', () => {
    expect(getDisplaysFromConfig).toBeDefined();
    expect(getDisplaysFromContent).toBeDefined();
    expect(getOpenDisplays).toBeDefined();
    expect(getAllDisplayKeys).toBeDefined();
    expect(getDisplayTitle).toBeDefined();
    expect(getClosedDisplays).toBeDefined();
    expect(createOpenDisplayFunction).toBeDefined();
    expect(removeDisplaysFromContent).toBeDefined();
    expect(createCloseDisplayFunction).toBeDefined();
    expect(clearLayout).toBeDefined();
    expect(showLogPopup).toBeDefined();
    expect(uniqueLayouts).toBeDefined();
  });

  it('can handle getOpenDisplays if GL is not defined', () => {
    expect(getOpenDisplays(undefined)).toEqual([]);
  });

  it('can handle getOpenDisplays', () => {
    const glConfig: any = {
      content: undefined
    };
    const gl: any = {
      toConfig: jest.fn(() => glConfig)
    };
    expect(getOpenDisplays(gl)).toEqual([]);
  });

  it('can handle getAllDisplayKeys with no components', () => {
    const glMap: any = [];
    expect(getAllDisplayKeys(glMap)).toEqual([]);
  });
  it('can handle getAllDisplayKeys', () => {
    const map = new Map();
    map.set('test', 'test');
    const glMap: any = [map, { id: { component: 'test' } }];
    expect(getAllDisplayKeys(glMap)).toEqual(['test', 'test']);
  });
  it('can handle getDisplayTitle', () => {
    const map = new Map();
    map.set('test', { id: { title: 'test' } });
    const glMap: any = [map, { id: { component: 'test', title: 'test' } }];
    expect(getDisplayTitle(glMap, 'test')).toEqual('test');
  });

  it('can handle getClosedDisplays if GL is not defined', () => {
    expect(getClosedDisplays(undefined, undefined)).toEqual([]);
  });

  it('can handle getClosedDisplays', () => {
    const map = new Map();
    map.set('test', { id: { title: 'test' } });
    const glMap: any = [map, { id: { component: 'test', title: 'test' } }];
    expect(getDisplayTitle(glMap, 'test')).toEqual('test');
    const glConfig: any = {
      content: undefined
    };
    const gl: any = {
      toConfig: jest.fn(() => glConfig)
    };
    expect(getClosedDisplays(gl, map)).toEqual([undefined]);
  });
  it('can handle createOpenDisplayFunction', () => {
    const glConfig: any = {
      content: undefined
    };
    const gl: any = {
      registerComponent: jest.fn(),
      root: {}
    };
    createOpenDisplayFunction(gl, glConfig);
    expect(createOpenDisplayFunction).toBeDefined();
  });
  it('removeDisplaysFromContent can remove', () => {
    const content: any = {
      isComponent: true,
      config: {
        component: 'test'
      },
      contentItems: [{ id: { component: 'test', title: 'test' } }],
      remove: jest.fn()
    };
    removeDisplaysFromContent(content, 'test');
    expect(content.remove).toHaveBeenCalled();
  });

  it('can handle clearLayout', () => {
    clearLayout();
    expect(clearLayout).toBeDefined();
  });
  it('can handle showLogPopup', () => {
    showLogPopup();
    expect(showLogPopup).toBeDefined();
  });
  it('can handle createCloseDisplayFunction', () => {
    const gl: any = {
      registerComponent: jest.fn(),
      root: {
        contentItems: [{ id: { component: 'test', title: 'test' } }]
      }
    };
    createCloseDisplayFunction(gl);
    expect(createCloseDisplayFunction).toBeDefined();
  });
});
