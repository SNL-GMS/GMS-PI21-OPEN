import { SohTypes } from '@gms/common-model';

import {
  buildData,
  convertChannelSohToValueWithStatus,
  getChannelSoh,
  getColorForStatus,
  getName,
  getOnContextMenus,
  getSortFunctionForDropdownLag,
  getSortFunctionForDropdownMissing,
  getSortFunctionForDropdownTimeliness,
  toolbarBarChartXAxisTicFormat,
  toolbarBarChartYAxisLabel,
  useSortDropdown
} from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/bar-chart/bar-chart-utils';
import type { ChannelSohForMonitorType } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/bar-chart/types';
import {
  SOHLagOptions,
  SOHMissingOptions,
  SOHTimelinessOptions
} from '../../../../../../src/ts/components/data-acquisition-ui/shared/toolbars/soh-toolbar-items';
// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Bar chart utils', () => {
  it('functions should be defined', () => {
    expect(convertChannelSohToValueWithStatus).toBeDefined();
    expect(getChannelSoh).toBeDefined();
    expect(getSortFunctionForDropdownMissing).toBeDefined();
    expect(getSortFunctionForDropdownLag).toBeDefined();
    expect(getSortFunctionForDropdownTimeliness).toBeDefined();
    expect(getName).toBeDefined();
    expect(toolbarBarChartYAxisLabel).toBeDefined();
    expect(getColorForStatus).toBeDefined();
    expect(buildData).toBeDefined();
    expect(useSortDropdown).toBeDefined();
    expect(getOnContextMenus).toBeDefined();
    expect(toolbarBarChartXAxisTicFormat).toBeDefined();
  });

  const channelSohsNoStatusOrValue: SohTypes.ChannelSoh[] = [
    {
      channelName: 'test',
      channelSohStatus: SohTypes.SohStatusSummary.GOOD,
      allSohMonitorValueAndStatuses: [
        {
          monitorType: SohTypes.SohMonitorType.LAG,
          hasUnacknowledgedChanges: true,
          contributing: false,
          status: undefined,
          thresholdBad: 10,
          value: undefined,
          valuePresent: false,
          thresholdMarginal: 10,
          quietUntilMs: 1000
        }
      ]
    }
  ];

  const channelSohs: SohTypes.ChannelSoh[] = [
    {
      channelName: 'TestSt.adsf',
      channelSohStatus: undefined,
      allSohMonitorValueAndStatuses: [
        {
          monitorType: SohTypes.SohMonitorType.LAG,
          value: 8,
          valuePresent: true,
          status: SohTypes.SohStatusSummary.GOOD,
          hasUnacknowledgedChanges: false,
          contributing: false,
          quietUntilMs: 1,
          thresholdBad: 3,
          thresholdMarginal: 3
        },
        {
          monitorType: SohTypes.SohMonitorType.TIMELINESS,
          value: 11,
          valuePresent: true,
          status: SohTypes.SohStatusSummary.GOOD,
          hasUnacknowledgedChanges: true,
          contributing: false,
          quietUntilMs: 1,
          thresholdBad: 3,
          thresholdMarginal: 3
        }
      ]
    },
    {
      channelName: 'TestSt.adsf2',
      channelSohStatus: undefined,
      allSohMonitorValueAndStatuses: [
        {
          monitorType: SohTypes.SohMonitorType.LAG,
          value: 10,
          valuePresent: true,
          status: SohTypes.SohStatusSummary.GOOD,
          hasUnacknowledgedChanges: true,
          contributing: false,
          quietUntilMs: 1,
          thresholdBad: 3,
          thresholdMarginal: 3
        },
        {
          monitorType: SohTypes.SohMonitorType.TIMELINESS,
          value: 12,
          valuePresent: true,
          status: SohTypes.SohStatusSummary.GOOD,
          hasUnacknowledgedChanges: true,
          contributing: false,
          quietUntilMs: 1,
          thresholdBad: 3,
          thresholdMarginal: 3
        }
      ]
    }
  ];

  const uiStationSoh: SohTypes.UiStationSoh = {
    id: '1',
    uuid: '1',
    needsAcknowledgement: true,
    needsAttention: true,
    sohStatusSummary: undefined,
    stationGroups: [],
    statusContributors: [],
    time: undefined,
    stationName: '1',
    allStationAggregates: [],
    channelSohs
  };

  const chSohForMonType: ChannelSohForMonitorType[] = [
    {
      hasUnacknowledgedChanges: false,
      isNullData: false,
      name: 'TestSt.adsf',
      quietDurationMs: undefined,
      quietExpiresAt: 1,
      status: SohTypes.SohStatusSummary.GOOD,
      thresholdBad: 3,
      thresholdMarginal: 3,
      value: 8
    },
    {
      hasUnacknowledgedChanges: true,
      isNullData: false,
      name: 'TestSt.adsf2',
      quietDurationMs: undefined,
      quietExpiresAt: 1,
      status: SohTypes.SohStatusSummary.GOOD,
      thresholdBad: 3,
      thresholdMarginal: 3,
      value: 10
    }
  ];

  it('should have convertChannelSohToValueWithStatus function return for maybeMatchingMonitorValue true', () => {
    const sohMonitorType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.LAG;
    expect(convertChannelSohToValueWithStatus(channelSohs, sohMonitorType)).toEqual(
      chSohForMonType
    );
  });

  it('should have convertChannelSohToValueWithStatus function return for maybeMatchingMonitorValue false', () => {
    const sohMonitorType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.MISSING;
    expect(convertChannelSohToValueWithStatus(channelSohs, sohMonitorType)).toEqual([
      {
        hasUnacknowledgedChanges: false,
        isNullData: true,
        name: 'TestSt.adsf',
        quietDurationMs: undefined,
        quietExpiresAt: 0,
        status: 'NONE',
        thresholdBad: 0,
        thresholdMarginal: 0,
        value: 0
      },
      {
        hasUnacknowledgedChanges: false,
        isNullData: true,
        name: 'TestSt.adsf2',
        quietDurationMs: undefined,
        quietExpiresAt: 0,
        status: 'NONE',
        thresholdBad: 0,
        thresholdMarginal: 0,
        value: 0
      }
    ]);
  });

  it('should have convertChannelSohToValueWithStatus function return 0 branching for false maybeMatchingMonitorValue.valuePresent', () => {
    const sohMonitorType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.LAG;
    expect(convertChannelSohToValueWithStatus(channelSohsNoStatusOrValue, sohMonitorType)).toEqual([
      {
        hasUnacknowledgedChanges: true,
        isNullData: true,
        name: 'test',
        quietDurationMs: undefined,
        quietExpiresAt: 1000,
        status: SohTypes.SohStatusSummary.NONE,
        thresholdBad: 10,
        thresholdMarginal: 10,
        value: null
      }
    ]);
  });

  it('should getChannelSohs for LAG monitor type', () => {
    const sohMonitorType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.LAG;
    expect(getChannelSoh(sohMonitorType, uiStationSoh)).toEqual(chSohForMonType);
  });

  it('should getChannelSohs for MISSING monitor type', () => {
    const sohMonitorType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.MISSING;
    expect(getChannelSoh(sohMonitorType, uiStationSoh)).toEqual([
      {
        hasUnacknowledgedChanges: false,
        isNullData: true,
        name: 'TestSt.adsf',
        quietDurationMs: undefined,
        quietExpiresAt: 0,
        status: 'NONE',
        thresholdBad: 0,
        thresholdMarginal: 0,
        value: 0
      },
      {
        hasUnacknowledgedChanges: false,
        isNullData: true,
        name: 'TestSt.adsf2',
        quietDurationMs: undefined,
        quietExpiresAt: 0,
        status: 'NONE',
        thresholdBad: 0,
        thresholdMarginal: 0,
        value: 0
      }
    ]);
  });

  it('should sort for missing/lag/timeliness', () => {
    let sortingFn = getSortFunctionForDropdownMissing(SOHMissingOptions.MISSING_HIGHEST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(2); // finds diff in value

    sortingFn = getSortFunctionForDropdownMissing(SOHMissingOptions.MISSING_LOWEST);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(-2);

    sortingFn = getSortFunctionForDropdownMissing(SOHMissingOptions.CHANNEL_FIRST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(-1); // locale compare results in number

    sortingFn = getSortFunctionForDropdownMissing(SOHMissingOptions.CHANNEL_LAST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(1);

    sortingFn = getSortFunctionForDropdownLag(SOHLagOptions.LAG_HIGHEST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(2);

    sortingFn = getSortFunctionForDropdownLag(SOHLagOptions.LAG_LOWEST);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(-2);

    sortingFn = getSortFunctionForDropdownLag(SOHLagOptions.CHANNEL_FIRST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(-1);

    sortingFn = getSortFunctionForDropdownLag(SOHLagOptions.CHANNEL_LAST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(1);

    sortingFn = getSortFunctionForDropdownTimeliness(SOHTimelinessOptions.TIMELINESS_HIGHEST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(2);

    sortingFn = getSortFunctionForDropdownTimeliness(SOHTimelinessOptions.TIMELINESS_LOWEST);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(-2);

    sortingFn = getSortFunctionForDropdownTimeliness(SOHTimelinessOptions.CHANNEL_FIRST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(-1);

    sortingFn = getSortFunctionForDropdownTimeliness(SOHTimelinessOptions.CHANNEL_LAST);
    expect(sortingFn(chSohForMonType[0], chSohForMonType[1])).toEqual(1);
  });

  it('should getName of monitor types', () => {
    let sohMonitorType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.MISSING;
    expect(getName(sohMonitorType)).toEqual('Missing');

    sohMonitorType = SohTypes.SohMonitorType.LAG;
    expect(getName(sohMonitorType)).toEqual('Lag');

    sohMonitorType = SohTypes.SohMonitorType.TIMELINESS;
    expect(getName(sohMonitorType)).toEqual('Timeliness');
  });

  it('should return the bar chart Y axis label based on the type provided', () => {
    let sohMonitorType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.MISSING;
    expect(toolbarBarChartYAxisLabel(sohMonitorType)).toEqual('Missing (%)');

    sohMonitorType = SohTypes.SohMonitorType.LAG;
    expect(toolbarBarChartYAxisLabel(sohMonitorType)).toEqual('Lag (s)');

    sohMonitorType = SohTypes.SohMonitorType.TIMELINESS;
    expect(toolbarBarChartYAxisLabel(sohMonitorType)).toEqual('Timeliness (s)');
  });

  it.skip('should get gms color based on status passed in', () => {
    // let sohStatus: SohTypes.SohStatusSummary = SohTypes.SohStatusSummary.GOOD;
    // expect(getColorForStatus(sohStatus)).toEqual(gmsColors.gmsOk);
    // sohStatus = SohTypes.SohStatusSummary.BAD;
    // expect(getColorForStatus(sohStatus)).toEqual(gmsColors.gmsStrongWarning);
    // sohStatus = SohTypes.SohStatusSummary.MARGINAL;
    // expect(getColorForStatus(sohStatus)).toEqual(gmsColors.gmsWarning);
    // sohStatus = SohTypes.SohStatusSummary.NONE;
    // expect(getColorForStatus(sohStatus)).toEqual('');
  });

  it('should return a formatter for X Axis', () => {
    const formatterFn = toolbarBarChartXAxisTicFormat('TestSt', chSohForMonType);
    expect(formatterFn('TestSt.adsf')).toEqual('adsf');
    expect(formatterFn('TestSt.adsf2')).toEqual('\u25cf adsf2');
  });
});
