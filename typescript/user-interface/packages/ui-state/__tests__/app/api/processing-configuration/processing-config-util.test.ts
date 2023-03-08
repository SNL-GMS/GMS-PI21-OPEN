import { getStore } from '../../../../src/ts/app';
import { getUiTheme } from '../../../../src/ts/app/api/processing-configuration';

const store = getStore();
describe('Processing Configuration Util', () => {
  it('can retreive ui theme configuration', () => {
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(getUiTheme(store.getState)).toMatchSnapshot();
  });
});
