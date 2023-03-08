import { useDatasource } from '../../../src/ts/components/table/use-datasource';
import { renderHook } from '../../util/render-hook-util';

const rows: any[] = [
  {
    hasNotificationStatusError: false,
    entry: {
      availableEntry: {
        first: 'first'
      },
      selectedEntry: 'string',
      onSelect: jest.fn()
    },
    category: 'color',
    subcategory: 'primary',
    severity: 'normal',
    message: 'message 1'
  },
  {
    hasNotificationStatusError: false,
    entry: {
      availableEntry: {
        second: 'second'
      },
      selectedEntry: 'string',
      onSelect: jest.fn()
    },
    category: 'color',
    subcategory: 'primary',
    severity: 'normal',
    message: 'message 2'
  }
];

describe('Use Data Source', () => {
  it('is exported', () => {
    expect(useDatasource).toBeDefined();
  });
  it('Returns the data source', () => {
    const props = renderHook(() => useDatasource(rows));
    expect(props).toBeDefined();
    expect(props).toMatchSnapshot();
  });
});
