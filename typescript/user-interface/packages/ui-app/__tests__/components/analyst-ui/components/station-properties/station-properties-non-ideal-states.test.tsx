import { render } from '@testing-library/react';

import {
  nonIdealStateSelectAStation,
  nonIdealStateSelectChannelGroupRow,
  nonIdealStateTooManyStationsSelected
} from '../../../../../src/ts/components/analyst-ui/components/station-properties/station-properties-non-ideal-states';

describe('station-properties non ideal states', () => {
  test('can mount', () => {
    expect(nonIdealStateSelectAStation).toBeDefined();
    expect(nonIdealStateTooManyStationsSelected).toBeDefined();
    expect(nonIdealStateSelectChannelGroupRow).toBeDefined();
  });
  test('match snapshots for nonIdealStateSelectAStation', () => {
    const { container } = render(nonIdealStateSelectAStation);
    expect(container).toMatchSnapshot();
  });
  test('match snapshots for nonIdealStateTooManyStationsSelected', () => {
    const { container } = render(nonIdealStateTooManyStationsSelected);
    expect(container).toMatchSnapshot();
  });
  test('match snapshots for nonIdealStateSelectChannelGroupRow', () => {
    const { container } = render(nonIdealStateSelectChannelGroupRow);
    expect(container).toMatchSnapshot();
  });
});
