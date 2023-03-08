import React from 'react';
import renderer from 'react-test-renderer';

import {
  MapStationDetails,
  showMapStationDetailsPopover
} from '../../../../../src/ts/components/analyst-ui/components/map/map-station-details';

describe('MapStationDetails', () => {
  test('functions are defined', () => {
    expect(MapStationDetails).toBeDefined();
    expect(showMapStationDetailsPopover).toBeDefined();
  });

  it('matches snapshot', () => {
    const component = renderer
      .create(
        <MapStationDetails
          stationName="STA1"
          latitude="100"
          longitude="100"
          elevation="1000"
          detailedType="Single Station"
          entityType="Station"
        />
      )
      .toJSON();
    expect(component).toMatchSnapshot();
  });

  it('matches snapshot with a site type', () => {
    const component = renderer
      .create(
        <MapStationDetails
          stationName="STA1"
          latitude="100"
          longitude="100"
          elevation="1000"
          detailedType="Array"
          entityType="ChannelGroup"
        />
      )
      .toJSON();
    expect(component).toMatchSnapshot();
  });
});
