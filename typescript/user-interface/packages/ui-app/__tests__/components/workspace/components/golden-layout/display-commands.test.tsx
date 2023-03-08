import {
  createCloseDisplayCommands,
  createOpenDisplayCommands
} from '../../../../../src/ts/components/workspace/components/golden-layout/display-commands';

jest.mock(
  '../../../../../src/ts/components/workspace/components/golden-layout/golden-layout-util',
  () => ({
    createOpenDisplayFunction: jest.fn(),
    getClosedDisplays: jest.fn(() => ['key', 'key2']),
    getDisplayTitle: jest.fn(() => 'testTitle'),
    createCloseDisplayFunction: jest.fn(),
    getOpenDisplays: jest.fn(() => ['key', 'key2'])
  })
);

describe('Display Commands', () => {
  it('functions should be defined', () => {
    expect(createCloseDisplayCommands).toBeDefined();
    expect(createOpenDisplayCommands).toBeDefined();
  });

  it('createCloseDisplayCommands can handle undefined parameters', () => {
    const result = createCloseDisplayCommands(undefined, undefined, undefined);
    expect(result).toEqual([]);
  });
  it('createOpenDisplayCommands can handle undefined parameters', () => {
    const result = createOpenDisplayCommands(undefined, undefined, undefined);
    expect(result).toEqual([]);
  });
  it('can createOpenDisplayCommands', () => {
    const anyObject: any = {};
    const result = createOpenDisplayCommands(anyObject, anyObject, anyObject);
    expect(result).toBeDefined();
  });
  it('can createCloseDisplayCommands', () => {
    const anyObject: any = {};
    const result = createCloseDisplayCommands(anyObject, anyObject, anyObject);
    expect(result).toBeDefined();
  });
});
