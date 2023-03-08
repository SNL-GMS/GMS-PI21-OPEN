import { SohTypes } from '@gms/common-model';
import { uuid } from '@gms/common-util';
import { render } from '@testing-library/react';
import React from 'react';

import type { SohOverviewContextData } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-context';
import { SohOverviewContext } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-context';
import {
  generateSohStationGroupMap,
  StationGroupsLayout
} from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/station-groups/station-groups-layout';
import { stationAndStationGroupSohStatus } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

const contextValues: SohOverviewContextData = {
  stationSoh: stationAndStationGroupSohStatus.stationSoh,
  stationGroupSoh: stationAndStationGroupSohStatus.stationGroups,
  acknowledgeSohStatus: jest.fn(),
  glContainer: undefined,
  quietTimerMs: 5000,
  updateIntervalSecs: 5,
  selectedStationIds: [],
  setSelectedStationIds: jest.fn(),
  sohStationStaleTimeMS: 30000
};

const statusesToDisplay: SohTypes.SohStatusSummary[] = [
  SohTypes.SohStatusSummary.GOOD,
  SohTypes.SohStatusSummary.MARGINAL,
  SohTypes.SohStatusSummary.BAD
];

describe('Soh station groups layout', () => {
  let idCount = 0;
  // eslint-disable-next-line no-plusplus
  uuid.asString = jest.fn().mockImplementation(() => ++idCount);
  it('should be defined', () => {
    expect(StationGroupsLayout).toBeDefined();
  });

  it('matches the snapshot when provided with a context', () => {
    const { container } = render(
      <SohOverviewContext.Provider value={contextValues}>
        <StationGroupsLayout
          statusesToDisplay={statusesToDisplay}
          isHighlighted
          stationGroupsToDisplay={contextValues.stationGroupSoh.map(
            stationGroup => stationGroup.stationGroupName
          )}
        />
      </SohOverviewContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('can generate the SohStationGroupMap', () => {
    const stationGroupMap = generateSohStationGroupMap(
      true,
      contextValues.stationGroupSoh,
      contextValues.stationSoh,
      statusesToDisplay
    );
    expect(stationGroupMap).toBeDefined();
    expect(stationGroupMap).toMatchSnapshot();
  });

  it('matches the snapshot when provided with no station groups to display', () => {
    const { container } = render(
      <SohOverviewContext.Provider value={contextValues}>
        <StationGroupsLayout
          statusesToDisplay={statusesToDisplay}
          isHighlighted
          stationGroupsToDisplay={[]}
        />
      </SohOverviewContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
