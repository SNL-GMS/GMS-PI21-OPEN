import {
  getElectron,
  getElectronEnhancer,
  isCypress,
  isElectron,
  isElectronMainProcess,
  isElectronRendererProcess,
  reload
} from '../../src/ts/ui-util/electron-util';

const obj = Object();

jest.mock('process', () => ({
  versions: () => obj
}));

describe('Electron utils', () => {
  it('to be defined', () => {
    expect(isElectronMainProcess).toBeDefined();
    expect(isElectronRendererProcess).toBeDefined();
    expect(getElectron).toBeDefined();
    expect(getElectronEnhancer).toBeDefined();
    expect(isElectron).toBeDefined();
    expect(isCypress).toBeDefined();
  });

  it('isElectronMainProcess', () => {
    expect(isElectronMainProcess()).toBeFalsy();

    const actual: any = process;
    actual.versions.electron = {};
    expect(isElectronMainProcess()).toBeTruthy();
    actual.versions.electron = undefined;
  });

  it('isElectronRendererProcess', () => {
    expect(isElectronRendererProcess()).toBeFalsy();

    const actual: any = process;
    actual.type = 'renderer';
    expect(isElectronRendererProcess()).toBeTruthy();
    actual.type = '';
  });

  it('isElectron', () => {
    expect(isElectron()).toBeFalsy();

    const actual: any = process;
    actual.versions.electron = {};
    expect(isElectron()).toBeTruthy();
    actual.versions.electron = undefined;

    actual.type = 'renderer';
    expect(isElectron()).toBeTruthy();
    actual.type = '';
  });

  // TODO Unskip tests and fix
  it.skip('getElectron', () => {
    expect(getElectron()).toBeUndefined();

    const actual: any = process;
    actual.versions.electron = {};
    expect(getElectron()).toBeDefined();
    actual.versions.electron = undefined;

    actual.type = 'renderer';
    expect(getElectron()).toBeDefined();
    actual.type = '';
  });

  // TODO Unskip tests and fix
  it.skip('getElectronEnhancer', () => {
    expect(getElectronEnhancer()).toBeUndefined();

    const actual: any = process;
    actual.versions.electron = {};
    expect(getElectronEnhancer()).toBeDefined();
    actual.versions.electron = undefined;

    actual.type = 'renderer';
    expect(getElectronEnhancer()).toBeDefined();
    actual.type = '';
  });

  it('reload', () => {
    const spy = jest.fn();
    Object.defineProperty(window, 'location', {
      writable: true,
      value: { reload: spy }
    });
    reload();
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('isCypress returns false', () => {
    expect(isCypress()).toBeFalsy();
  });

  it('isCypress returns true', () => {
    (window as any).Cypress = jest.fn(() => true);
    expect(isCypress()).toBeTruthy();
  });
});
