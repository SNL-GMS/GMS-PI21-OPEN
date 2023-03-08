import { SohTypes } from '@gms/common-model';
import { render } from '@testing-library/react';
import React from 'react';

// eslint-disable-next-line max-len
import { ChannelCellBaseRenderer } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/cell-renderers/channel-cell-base-renderer';
// eslint-disable-next-line max-len
import type { EnvironmentalSoh } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('base environment cell', () => {
  const envSOH: EnvironmentalSoh = {
    value: 10,
    status: SohTypes.SohStatusSummary.GOOD,
    monitorTypes: SohTypes.SohMonitorType.MISSING,
    channelName: 'channelName',
    hasUnacknowledgedChanges: true,
    quietTimingInfo: {
      quietUntilMs: 100000000,
      quietDurationMs: 100000
    },
    isSelected: false,
    isContributing: true
  };
  const renderComponentOne = render(<ChannelCellBaseRenderer environmentSoh={envSOH} />);
  const renderComponentTwo = render(<ChannelCellBaseRenderer environmentSoh={null} />);
  it('should be defined', () => {
    expect(ChannelCellBaseRenderer).toBeDefined();
  });
  it('should match snapshot', () => {
    expect(renderComponentOne).toMatchSnapshot();
  });
  it('should match snapshot for null data', () => {
    expect(renderComponentTwo).toMatchSnapshot();
  });
});
