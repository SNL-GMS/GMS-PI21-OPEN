import React from 'react';
import renderer from 'react-test-renderer';

import {
  MapEventDetails,
  showMapEventDetailsPopover
} from '~analyst-ui/components/map/map-event-details';

import {} from '../../../../../src/ts/components/analyst-ui/components/map/map-station-details';

describe('MapEventDetails', () => {
  test('functions are defined', () => {
    expect(MapEventDetails).toBeDefined();
    expect(showMapEventDetailsPopover).toBeDefined();
  });

  it('matches snapshot', () => {
    const component = renderer
      .create(
        <MapEventDetails
          eventTime={0}
          latitude={45}
          longitude={45}
          depth={10}
          workflowStatus="Not Started"
        />
      )
      .toJSON();
    expect(component).toMatchSnapshot();
  });
});
