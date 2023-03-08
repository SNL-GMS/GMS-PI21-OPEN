import { StationTypes } from '@gms/common-model';
import { toEpochSeconds } from '@gms/common-util';
import { render } from '@testing-library/react';
import Immutable from 'immutable';
import * as React from 'react';

import { StationPropertiesTables } from '../../../../../src/ts/components/analyst-ui/components/station-properties/station-properties-tables';
import type {
  ChannelColumn,
  SiteColumn
} from '../../../../../src/ts/components/analyst-ui/components/station-properties/types';

describe('station-properties-tables', () => {
  const station: StationTypes.Station = {
    name: 'STA',
    effectiveAt: toEpochSeconds('2021-04-20T16:11:31.118870Z'),
    effectiveUntil: toEpochSeconds('2021-04-20T16:11:31.118870Z'),
    relativePositionsByChannel: {
      'Real Channel Name One': {
        northDisplacementKm: 50,
        eastDisplacementKm: 5,
        verticalDisplacementKm: 10
      },
      'Real Channel Name Two': {
        northDisplacementKm: 50,
        eastDisplacementKm: 5,
        verticalDisplacementKm: 10
      }
    },
    channelGroups: [{} as any],
    allRawChannels: [{} as any],
    description: 'This is a test station',
    location: {
      latitudeDegrees: 35.647,
      longitudeDegrees: 100.0,
      depthKm: 50.0,
      elevationKm: 10.0
    },
    type: StationTypes.StationType.SEISMIC_1_COMPONENT
  };

  const { container } = render(
    <StationPropertiesTables
      stationData={station}
      onChannelGroupRowSelection={jest.fn()}
      selectedSiteColumnsToDisplay={Immutable.Map<SiteColumn, boolean>()}
      selectedChannelGroup="TEST"
      channels={[]}
      selectedChannelColumnsToDisplay={Immutable.Map<ChannelColumn, boolean>()}
    />
  );

  const { container: nullStationData } = render(
    <StationPropertiesTables
      stationData={null}
      onChannelGroupRowSelection={jest.fn()}
      selectedSiteColumnsToDisplay={Immutable.Map<SiteColumn, boolean>()}
      selectedChannelGroup="TEST"
      channels={[]}
      selectedChannelColumnsToDisplay={Immutable.Map<ChannelColumn, boolean>()}
    />
  );

  const { container: emptyChannelGroup } = render(
    <StationPropertiesTables
      stationData={station}
      onChannelGroupRowSelection={jest.fn()}
      selectedSiteColumnsToDisplay={Immutable.Map<SiteColumn, boolean>()}
      selectedChannelGroup={null}
      channels={[]}
      selectedChannelColumnsToDisplay={Immutable.Map<ChannelColumn, boolean>()}
    />
  );

  test('can mount', () => {
    expect(container).toBeDefined();
    expect(StationPropertiesTables).toBeDefined();
  });

  test('ideal state', () => {
    expect(container).toMatchSnapshot();
  });

  test('non-ideal state', () => {
    expect(nullStationData).toMatchSnapshot();
  });

  test('non-ideal state missing channel group', () => {
    expect(emptyChannelGroup).toMatchSnapshot();
  });
});
