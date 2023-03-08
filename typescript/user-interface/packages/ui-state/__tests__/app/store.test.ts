import { getStore } from '../../src/ts/ui-state';

describe('store', () => {
  it('exists', () => {
    expect(getStore).toBeDefined();
  });

  it('create store', () => {
    const store = getStore();
    expect(store).toBeDefined();
    expect(store).toMatchSnapshot();
  });
});
