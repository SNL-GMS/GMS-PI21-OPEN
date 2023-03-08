import { buildSelectedEventCircle } from '../../../../../src/ts/components/analyst-ui/components/map/img/selected-event-circle';

describe('buildSelectedEventCircle', () => {
  test('is defined', () => {
    expect(buildSelectedEventCircle).toBeDefined();
  });

  test('builds an event circle', () => {
    expect(buildSelectedEventCircle(0, 0)).toBeDefined();
  });
});
