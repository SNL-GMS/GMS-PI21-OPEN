/* eslint-disable react/jsx-no-constructed-context-values */
import { render } from '@testing-library/react';
import React from 'react';

import {
  ChannelTimelinessCellRenderer,
  StationTimelinessCellRenderer
} from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/cell-renderers/timeliness-cell-renderer';
import { StationStatisticsTableDataContext } from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-table-context';
import { cellRendererProps, row } from './common-test-data';

describe('Timeliness cell renderer', () => {
  const { container: channelTimelinessCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <ChannelTimelinessCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: stationMissingCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationTimelinessCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: nullStationTimelinessCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationTimelinessCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  it('ChannelTimelinessCellRenderer can be created', () => {
    expect(channelTimelinessCellRenderer).toMatchSnapshot();
  });

  it('StationTimelinessCellRenderer can be created', () => {
    expect(stationMissingCellRenderer).toMatchSnapshot();
  });
  it('can be created handle no data', () => {
    expect(nullStationTimelinessCellRenderer).toMatchSnapshot();
  });
});
