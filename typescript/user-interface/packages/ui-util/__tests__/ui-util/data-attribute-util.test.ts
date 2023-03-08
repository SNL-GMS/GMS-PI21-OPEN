import * as DataAttributeUtils from '../../src/ts/ui-util/data-attribute-util';

describe('Data attribute util', () => {
  it('extracts props prefixed with "data-"', () => {
    const dataProps = {
      'data-a': 'yep',
      'data-b': 'yep'
    };
    const props = {
      'not-data': 'nope',
      ...dataProps
    };
    const dataAttributes = DataAttributeUtils.getDataAttributesFromProps(props);
    expect(dataAttributes).toEqual(dataProps);
  });
});
