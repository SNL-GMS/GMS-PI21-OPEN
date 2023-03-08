/* eslint-disable react/jsx-no-constructed-context-values */
import { render } from '@testing-library/react';
import React from 'react';

import {
  ChannelLagCellRenderer,
  StationLagCellRenderer
} from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/cell-renderers/lag-cell-renderer';
import { StationStatisticsTableDataContext } from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-table-context';
import { cellRendererProps, row } from './common-test-data';

describe('Lag cell renderer', () => {
  const { container: channelLagCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <ChannelLagCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: stationLagCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationLagCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: nullStationLagCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationLagCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  it('ChannelLagCellRenderer can be created', () => {
    expect(channelLagCellRenderer).toMatchSnapshot();
  });

  it('StationLagCellRenderer can be created', () => {
    expect(stationLagCellRenderer).toMatchSnapshot();
  });

  it('can be created handle no data', () => {
    expect(nullStationLagCellRenderer).toMatchSnapshot();
  });
});
