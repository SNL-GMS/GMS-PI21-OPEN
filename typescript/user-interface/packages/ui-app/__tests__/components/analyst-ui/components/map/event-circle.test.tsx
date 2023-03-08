import { buildEventCircle } from '../../../../../src/ts/components/analyst-ui/components/map/img/event-circle';

describe('buildEventCircle', () => {
  test('is defined', () => {
    expect(buildEventCircle).toBeDefined();
  });

  test('builds an event circle', () => {
    expect(buildEventCircle(1, 1)).toMatchSnapshot();
  });
});
