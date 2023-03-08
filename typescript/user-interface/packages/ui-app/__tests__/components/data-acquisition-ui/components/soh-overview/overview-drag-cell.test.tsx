import { render } from '@testing-library/react';
import React from 'react';

import type { SohOverviewContextData } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-context';
import { SohOverviewContext } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-context';
import type { OverviewDragCellProps } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/station-groups/overview-drag-cell';
import {
  getSelectedIds,
  getSingleDragImage,
  OverviewDragCell
} from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/station-groups/overview-drag-cell';
import { testStationSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

const glContainer: any = { isHidden: false };
const contextValues: SohOverviewContextData = {
  glContainer,
  stationGroupSoh: [],
  stationSoh: [testStationSoh],
  selectedStationIds: ['test', 'test2'],
  sohStationStaleTimeMS: 100,
  updateIntervalSecs: 5,
  quietTimerMs: 5,
  setSelectedStationIds: jest.fn(),
  acknowledgeSohStatus: jest.fn()
};
describe('Overview Drag Cell', () => {
  const dragCellProps: OverviewDragCellProps = {
    stationId: 'AAA'
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container } = render(
    <SohOverviewContext.Provider value={contextValues}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <OverviewDragCell {...dragCellProps} />
    </SohOverviewContext.Provider>
  );

  it('is defined', () => {
    expect(OverviewDragCell).toBeDefined();
  });
  it('can render and match snapshot', () => {
    expect(container).toMatchSnapshot();
  });

  it('can handle getSingleDragImage', () => {
    const element: any = {
      target: document.createElement('div')
    };
    const result = getSingleDragImage(element);
    expect(result).toBeNull();
  });

  it('can handle getSingleDragImage and return undefined', () => {
    const element: any = {
      target: 'notAnElement'
    };
    const result = getSingleDragImage(element);
    expect(result).toBeUndefined();
  });

  it('can handle getSelectedIds', () => {
    const result = getSelectedIds(contextValues)();
    expect(result).toEqual(['test', 'test2']);
  });
});
