import { SohTypes } from '@gms/common-model';

// import React from 'react';
import {
  getEnvironmentTableRows,
  getPerChannelEnvRollup
} from '../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/soh-environment-utils';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

/**
 * Tests the ability to check if the peak trough is in warning
 */
describe('Environment Utils', () => {
  const channel: SohTypes.ChannelSoh = {
    allSohMonitorValueAndStatuses: [
      {
        status: SohTypes.SohStatusSummary.GOOD,
        value: 1,
        valuePresent: true,
        monitorType: SohTypes.SohMonitorType.ENV_ZEROED_DATA,
        hasUnacknowledgedChanges: false,
        contributing: true,
        thresholdMarginal: 1,
        thresholdBad: 10,
        quietUntilMs: 1
      }
    ],
    channelName: 'channelName',
    channelSohStatus: SohTypes.SohStatusSummary.GOOD
  };
  const channels: SohTypes.ChannelSoh[] = [channel];

  test('functions should be defined', () => {
    expect(getPerChannelEnvRollup).toBeDefined();
    expect(getEnvironmentTableRows).toBeDefined();
  });

  test('getPerChannelEnvRollup function get env rollup', () => {
    expect(getPerChannelEnvRollup(channel)).toMatchSnapshot();
  });

  test('getEnvironmentTableRows function get the table rows', () => {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(
      getEnvironmentTableRows(channels, [], SohTypes.AceiType.BEGINNING_TIME_OUTAGE)
    ).toMatchSnapshot();
  });
});
