/* eslint-disable react/jsx-no-constructed-context-values */
import { uuid } from '@gms/common-util';
import { render } from '@testing-library/react';
import uniqueId from 'lodash/uniqueId';
import React from 'react';

import type {
  StationStatisticsDragCellProps
  // eslint-disable-next-line max-len
} from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/cell-renderers/station-statistics-drag-cell';
import {
  findClosestRow,
  StationStatisticsDragCell
  // eslint-disable-next-line max-len
} from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/cell-renderers/station-statistics-drag-cell';
import { StationStatisticsContext } from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-context';

uuid.asString = jest.fn().mockImplementation(uniqueId);

describe('Soh Drag Cell', () => {
  const dragCellProps: StationStatisticsDragCellProps = {
    stationId: 'AAA'
  };

  const { container } = render(
    <StationStatisticsContext.Provider
      value={{
        updateIntervalSecs: 1,
        quietTimerMs: 1,
        selectedStationIds: ['AAA'],
        setSelectedStationIds: jest.fn(),
        acknowledgeSohStatus: jest.fn(),
        sohStationStaleTimeMS: 30000
      }}
    >
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <StationStatisticsDragCell {...dragCellProps} />
    </StationStatisticsContext.Provider>
  );

  it('is defined', () => {
    expect(container).toBeDefined();
  });

  it('Matches snapshot', () => {
    expect(container).toMatchSnapshot();
  });

  it('Can find closest HTMLElement', () => {
    const closestElement = 'someClass';
    const element: Element = document.createElement('drag');
    const dragElement: any = {
      target: element
    };
    dragElement.target.closest = jest.fn(() => closestElement);
    const result = findClosestRow(dragElement);
    expect(result).toEqual(closestElement);
  });
  it('Find closest returns undefined', () => {
    const element: any = {
      className: 'someClass'
    };
    const result = findClosestRow(element);
    expect(result).toBeUndefined();
  });
});
