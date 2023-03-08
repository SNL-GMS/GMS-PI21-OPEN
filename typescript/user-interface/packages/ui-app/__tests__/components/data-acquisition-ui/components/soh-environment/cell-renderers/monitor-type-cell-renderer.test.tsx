/* eslint-disable react/jsx-no-constructed-context-values */
import { SohTypes } from '@gms/common-model';
import { render } from '@testing-library/react';
import React from 'react';

import {
  MonitorTypeCellRenderer,
  MonitorTypeCellRendererBase
} from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/cell-renderers/monitor-type-cell-renderer';
// eslint-disable-next-line max-len
// import { EnvironmentalSoh } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/types';
import type {
  EnvironmentalSoh,
  EnvironmentTableRow
} from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/types';
import { EnvironmentTableDataContext } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Monitor Type Cell Renderer', () => {
  const valueAndStatusByChannelNameValue: EnvironmentalSoh = {
    value: 10,
    status: SohTypes.SohStatusSummary.GOOD,
    monitorTypes: SohTypes.SohMonitorType.MISSING,
    channelName: 'channelName',
    hasUnacknowledgedChanges: true,
    quietTimingInfo: {
      quietUntilMs: 100000000,
      quietDurationMs: 100000
    },
    isSelected: true,
    isContributing: true
  };

  const valueAndStatusByChannelName: Map<string, EnvironmentalSoh> = new Map();
  valueAndStatusByChannelName.set('monitorType', valueAndStatusByChannelNameValue);

  const envRow: EnvironmentTableRow = {
    id: '1234',
    monitorType: SohTypes.SohMonitorType.MISSING,
    monitorStatus: SohTypes.SohStatusSummary.GOOD,
    valueAndStatusByChannelName: new Map<string, EnvironmentalSoh>(),
    monitorIsSelected: true
  };

  const myProps: any = {
    data: envRow,
    colDef: {
      headerName: 'monitorType'
    }
  };

  it('should be defined', () => {
    expect(MonitorTypeCellRendererBase).toBeDefined();
    expect(MonitorTypeCellRenderer).toBeDefined();
  });

  it('should match snapshot when calling monitorTypeCellRenderer', () => {
    const { container } = render(
      <EnvironmentTableDataContext.Provider value={{ data: [myProps.data] }}>
        {MonitorTypeCellRenderer(myProps)}
      </EnvironmentTableDataContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('should match snapshot for MonitorTypeCellRendererBase', () => {
    const { container } = render(
      <EnvironmentTableDataContext.Provider value={{ data: [myProps.data] }}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <MonitorTypeCellRendererBase {...envRow} />
      </EnvironmentTableDataContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
