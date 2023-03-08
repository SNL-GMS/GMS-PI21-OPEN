import * as ClassListUtil from '../../src/ts/ui-util/class-list-util';

describe('Class list util', () => {
  const cl = { a: true, b: false, e: true };
  it('filters to keys with value of true', () => {
    expect(ClassListUtil.classList(cl)).toEqual('a e');
  });

  it('includes additional classes', () => {
    expect(ClassListUtil.classList(cl, 'i o u')).toEqual('a e i o u');
    expect(ClassListUtil.classList(cl, ' i o u')).toEqual('a e  i o u');
  });
});
