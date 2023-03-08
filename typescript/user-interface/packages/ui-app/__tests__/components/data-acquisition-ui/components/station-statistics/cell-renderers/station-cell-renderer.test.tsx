/* eslint-disable react/jsx-no-constructed-context-values */
import { render } from '@testing-library/react';
import React from 'react';

import { StationNameCellRenderer } from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/cell-renderers/station-cell-renderer';
import { StationStatisticsTableDataContext } from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-table-context';
import { cellRendererProps, row } from './common-test-data';

describe('Station Name cell renderer', () => {
  const { container: stationNameCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [row] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationNameCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  const { container: nullStationNameCellRenderer } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationStatisticsTableDataContext.Provider value={{ data: [] }}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationNameCellRenderer {...cellRendererProps} />
    </StationStatisticsTableDataContext.Provider>
  );

  it('StationNameCellRenderer can be created', () => {
    expect(stationNameCellRenderer).toMatchSnapshot();
  });
  it('can be created handle no data', () => {
    expect(nullStationNameCellRenderer).toMatchSnapshot();
  });
});
