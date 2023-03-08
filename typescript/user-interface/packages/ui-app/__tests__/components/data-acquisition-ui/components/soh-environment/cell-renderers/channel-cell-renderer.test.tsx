/* eslint-disable react/jsx-no-constructed-context-values */
import { SohTypes } from '@gms/common-model';
import { render } from '@testing-library/react';
import React from 'react';

import { ChannelCellRenderer } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/cell-renderers/channel-cell-renderer';
// eslint-disable-next-line max-len
import type { EnvironmentalSoh } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/types';
import { EnvironmentTableDataContext } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe("channel cell renderer's", () => {
  const valueAndStatusByChannelNameVal: EnvironmentalSoh = {
    value: 10,
    status: SohTypes.SohStatusSummary.GOOD,
    monitorTypes: SohTypes.SohMonitorType.MISSING,
    channelName: 'channelName',
    hasUnacknowledgedChanges: true,
    quietTimingInfo: {
      quietUntilMs: null,
      quietDurationMs: 100000
    },
    isSelected: false,
    isContributing: true
  };
  const valueAndStatusByChannelName: Map<string, EnvironmentalSoh> = new Map();
  valueAndStatusByChannelName.set('channelName', valueAndStatusByChannelNameVal);

  const myProps: any = {
    colDef: {
      headerName: 'channelName',
      colId: 'channelName'
    },
    context: {
      selectedChannelMonitorPairs: [
        {
          channelName: 'channelName',
          monitorType: 'monitorType'
        }
      ]
    },
    setTooltipProps: jest.fn(),
    rowIndex: 1,
    columnId: 'channelName',
    parentCellHeight: 1,
    parentCellWidth: 1,
    data: {
      id: 'id',
      monitorType: SohTypes.SohMonitorType.LAG,
      monitorStatus: SohTypes.SohStatusSummary.GOOD,
      valueAndStatusByChannelName
    }
  };

  // eslint-disable-next-line prefer-const
  let badProps = JSON.parse(JSON.stringify(myProps));
  badProps.colDef.headerName = 'badChannel';
  badProps.data.valueAndStatusByChannelName = valueAndStatusByChannelName;

  it('should be defined', () => {
    expect(ChannelCellRenderer).toBeDefined();
  });
  it('should match snapshot', () => {
    const { container } = render(
      <EnvironmentTableDataContext.Provider value={{ data: [myProps.data] }}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <ChannelCellRenderer {...myProps} />
      </EnvironmentTableDataContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });
  it('should match snapshot with bad data', () => {
    const { container } = render(
      <EnvironmentTableDataContext.Provider value={{ data: [badProps.data] }}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <ChannelCellRenderer {...badProps} />
      </EnvironmentTableDataContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
