/* eslint-disable react/jsx-no-constructed-context-values */
import { render } from '@testing-library/react';
import React from 'react';

import {
  ChannelEnvironmentCellRenderer,
  StationEnvironmentCellRenderer
} from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/cell-renderers/environment-cell-renderer';
import { StationStatisticsTableDataContext } from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-table-context';
import { cellRendererProps, row } from './common-test-data';

describe('Environment cell renderer', () => {
  const { container: channelEnvironmentCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <ChannelEnvironmentCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: stationEnvironmentCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationEnvironmentCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: nullStationEnvironmentCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationEnvironmentCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  it('ChannelEnvironmentCellRenderer can be created', () => {
    expect(channelEnvironmentCellRenderer).toMatchSnapshot();
  });

  it('StationEnvironmentCellRenderer can be created', () => {
    expect(stationEnvironmentCellRenderer).toMatchSnapshot();
  });

  it('can be created handle no data', () => {
    expect(nullStationEnvironmentCellRenderer).toMatchSnapshot();
  });
});
