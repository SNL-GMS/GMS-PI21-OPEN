/* eslint-disable react/jsx-no-constructed-context-values */
import { render } from '@testing-library/react';
import React from 'react';

import {
  ChannelMissingCellRenderer,
  StationMissingCellRenderer
} from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/cell-renderers/missing-cell-renderer';
import { StationStatisticsTableDataContext } from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-table-context';
import { cellRendererProps, row } from './common-test-data';

describe('Missing cell renderer', () => {
  const { container: channelMissingCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <ChannelMissingCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: stationMissingCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationMissingCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: nullStationMissingCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationMissingCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  it('ChannelMissingCellRenderer can be created', () => {
    expect(channelMissingCellRenderer).toMatchSnapshot();
  });

  it('StationMissingCellRenderer can be created', () => {
    expect(stationMissingCellRenderer).toMatchSnapshot();
  });

  it('can be created handle no data', () => {
    expect(nullStationMissingCellRenderer).toMatchSnapshot();
  });
});
