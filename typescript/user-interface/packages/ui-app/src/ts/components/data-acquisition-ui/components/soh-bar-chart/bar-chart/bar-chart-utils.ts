import { SohTypes } from '@gms/common-model';
import type { ValueType } from '@gms/common-util';
import { determinePrecisionByType } from '@gms/common-util';
import type { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import React from 'react';

import { getDataType } from '~components/data-acquisition-ui/components/historical-trends/utils';
import { showQuietingContextMenu } from '~components/data-acquisition-ui/shared/context-menus/quieting-menu';
import {
  makeLagSortingDropdown,
  makeLagTimelinessDropdown,
  makeMissingSortingDropdown,
  SOHLagOptions,
  SOHMissingOptions,
  SOHTimelinessOptions
} from '~components/data-acquisition-ui/shared/toolbars/soh-toolbar-items';
import { gmsColors } from '~scss-config/color-preferences';

import type { ChannelSohForMonitorType, Type } from './types';

interface ChartData {
  barData: {
    value: {
      y: number;
      x: string;
      quietUntilMs?: number;
      quietDurationMs?: number;
      channelStatus?: SohTypes.SohStatusSummary;
      onContextMenus?: {
        onContextMenuBar(e: React.MouseEvent<any, MouseEvent>, data: any): void;
        onContextMenuBarLabel(e: React.MouseEvent<any, MouseEvent>, index: any): void;
      };
    };
    id: string;
    color: string;
  }[];
  barCategories: {
    x: string[];
    y: any[];
  };
  thresholdsBad: number[];
  thresholdsMarginal: number[];
}

/**
 * Extracts matching monitor/value for given monitor type from all channels
 */
export const convertChannelSohToValueWithStatus = (
  channelSohs: SohTypes.ChannelSoh[],
  sohMonitorType: SohTypes.SohMonitorType
): ChannelSohForMonitorType[] =>
  channelSohs
    .map(channel => {
      // find the monitor value and status on the channel that matches soh type
      const maybeMatchingMonitorValue = channel.allSohMonitorValueAndStatuses.find(
        mvs => mvs.monitorType === sohMonitorType
      );
      if (maybeMatchingMonitorValue) {
        return {
          value: maybeMatchingMonitorValue.valuePresent ? maybeMatchingMonitorValue.value : null,
          status: maybeMatchingMonitorValue.status
            ? maybeMatchingMonitorValue.status
            : SohTypes.SohStatusSummary.NONE,
          quietExpiresAt: maybeMatchingMonitorValue.quietUntilMs,
          quietDurationMs: maybeMatchingMonitorValue.quietDurationMs,
          name: channel.channelName,
          hasUnacknowledgedChanges: maybeMatchingMonitorValue.hasUnacknowledgedChanges,
          thresholdBad: maybeMatchingMonitorValue.thresholdBad,
          thresholdMarginal: maybeMatchingMonitorValue.thresholdMarginal,
          isNullData: !maybeMatchingMonitorValue.valuePresent
        };
      }
      return {
        value: 0,
        status: SohTypes.SohStatusSummary.NONE,
        quietExpiresAt: 0,
        name: channel.channelName,
        hasUnacknowledgedChanges: false,
        thresholdBad: 0,
        thresholdMarginal: 0,
        isNullData: true
      };
    })
    .filter(a => a !== undefined);

/** Returns the channel SOH for the MISSING monitor type */
export const getChannelSoh = (
  type: Type,
  station: SohTypes.UiStationSoh
): ChannelSohForMonitorType[] => convertChannelSohToValueWithStatus(station.channelSohs, type);

// TODO: getSortFunctionForDropdownMissing getSortFunctionForDropdownLag getSortFunctionForDropdownTimeliness
// can be one function
/**
 * Given a specific sortOrder returns a function that
 * will sort based on that sort order for missing
 *
 * @param sortOrder the sort order
 */
export const getSortFunctionForDropdownMissing = (
  sortOrder: SOHMissingOptions
): ((a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => number) => {
  switch (sortOrder) {
    case SOHMissingOptions.CHANNEL_FIRST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) =>
        a.name.localeCompare(b.name);
    case SOHMissingOptions.CHANNEL_LAST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) =>
        b.name.localeCompare(a.name);
    case SOHMissingOptions.MISSING_HIGHEST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => b.value - a.value;
    default:
      // SOHMissingOptions.MISSING_LOWEST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => a.value - b.value;
  }
};

/**
 * Given a specific sortOrder returns a function that
 * will sort based on that sort order for lag
 *
 * @param sortOrder the sort order
 */
export const getSortFunctionForDropdownLag = (
  sortOrder: SOHLagOptions
): ((a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => number) => {
  switch (sortOrder) {
    case SOHLagOptions.CHANNEL_FIRST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) =>
        a.name.localeCompare(b.name);
    case SOHLagOptions.CHANNEL_LAST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) =>
        b.name.localeCompare(a.name);
    case SOHLagOptions.LAG_HIGHEST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => b.value - a.value;
    default:
      // SOHLagOptions.LAG_LOWEST
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => a.value - b.value;
  }
};

/**
 * Given a specific sortOrder returns a function that
 * will sort based on that sort order for Timeliness
 *
 * @param sortOrder the sort order
 */
export const getSortFunctionForDropdownTimeliness = (
  sortOrder: SOHTimelinessOptions
): ((a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => number) => {
  switch (sortOrder) {
    case SOHTimelinessOptions.CHANNEL_FIRST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) =>
        a.name.localeCompare(b.name);
    case SOHTimelinessOptions.CHANNEL_LAST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) =>
        b.name.localeCompare(a.name);
    case SOHTimelinessOptions.TIMELINESS_HIGHEST:
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => b.value - a.value;
    default:
      // SOHTimelinessOptions.TIMELINESS_LOWEST
      return (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => a.value - b.value;
  }
};

// TODO: use toSentenceCase function potentially
/** Returns based on the type provided */
export const getName = (type: SohTypes.SohMonitorType): string => {
  // !update this function when adding in new types
  switch (type) {
    case SohTypes.SohMonitorType.LAG:
      return 'Lag';
    case SohTypes.SohMonitorType.MISSING:
      return 'Missing';
    default:
      return 'Timeliness';
  }
};

/** Returns the bar chart Y axis label based on the type provided */
export const toolbarBarChartYAxisLabel = (type: SohTypes.SohMonitorType): string =>
  `${getName(type)} (${getDataType(type)})`;

/**
 * Get gms color based on status passed in
 */
export const getColorForStatus = (status: SohTypes.SohStatusSummary): string => {
  switch (status) {
    case SohTypes.SohStatusSummary.GOOD:
      return gmsColors.gmsOk;
    case SohTypes.SohStatusSummary.BAD:
      return gmsColors.gmsStrongWarning;
    case SohTypes.SohStatusSummary.MARGINAL:
      return gmsColors.gmsWarning;
    default:
      return '';
  }
};

/**
 * helper function to build the data that will be passed to the
 * bar chart
 *
 * @param channelSoh an array on ChannelSohMonitorTypes
 * @param onContextMenus both the label and bar context menu, passed so the html foreign object has access to it
 */
export const buildData = (
  channelSoh: ChannelSohForMonitorType[],
  onContextMenus: {
    onContextMenuBar(e: React.MouseEvent<any, MouseEvent>, data: any): void;
    onContextMenuBarLabel(e: React.MouseEvent<any, MouseEvent>, index: any): void;
  },
  valueType: ValueType
): ChartData => {
  const barData = channelSoh
    .map(channel => ({
      value: {
        y: !channel.isNullData
          ? (determinePrecisionByType(channel.value, valueType, false) as number)
          : null,
        x: channel.name,
        // need to pass this through to datum so we can use this when creating
        // the quiet context menu
        quietUntilMs: channel.quietExpiresAt ? channel.quietExpiresAt : undefined,
        quietDurationMs: channel.quietDurationMs ? channel.quietDurationMs : undefined,
        channelStatus: channel.status,
        onContextMenus
      },
      id: channel.name,
      color: getColorForStatus(channel.status)
    }))
    .filter(a => a !== undefined);
  // need bar categories just so the bar chart know how many bars it is going to make
  // or else the spacing will be off
  const barCategories = {
    x: barData.map(bar => bar.value.x),
    y: []
  };
  // used to build the bad threshold line on the chart, will choose the worse one
  const thresholdsBad = channelSoh.map(channel => channel.thresholdBad);
  // used to build the marginal threshold line on the chart, will choose the worse one
  const thresholdsMarginal = channelSoh.map(channel => channel.thresholdMarginal);
  return { barData, barCategories, thresholdsBad, thresholdsMarginal };
};

/** React hook for constructing the sort drop down control */
export const useSortDropdown = (
  type: SohTypes.SohMonitorType
): [
  DeprecatedToolbarTypes.DropdownItem,
  () => (a: ChannelSohForMonitorType, b: ChannelSohForMonitorType) => number
] => {
  switch (type) {
    case SohTypes.SohMonitorType.MISSING:
      // !FIX ESLINT DO NOT USE REACT HOOK HOOKS IN CONDITIONAL
      // eslint-disable-next-line react-hooks/rules-of-hooks, no-case-declarations
      const [sortOrderMissing, setSortOrderMissing] = React.useState(
        SOHMissingOptions.CHANNEL_FIRST
      );
      // eslint-disable-next-line no-case-declarations
      const sortDropdownMissing = makeMissingSortingDropdown(sortOrderMissing, setSortOrderMissing);
      return [sortDropdownMissing, () => getSortFunctionForDropdownMissing(sortOrderMissing)];
    case SohTypes.SohMonitorType.LAG:
      // TODO HANDLE TIMELINESS - try to avoid these specific functions for lag, missing, etc...
      // !FIX ESLINT DO NOT USE REACT HOOK HOOKS IN CONDITIONAL
      // eslint-disable-next-line react-hooks/rules-of-hooks, no-case-declarations
      const [sortOrderLag, setSortOrderLag] = React.useState(SOHLagOptions.CHANNEL_FIRST);
      // eslint-disable-next-line no-case-declarations
      const sortDropdownLag = makeLagSortingDropdown(sortOrderLag, setSortOrderLag);
      return [sortDropdownLag, () => getSortFunctionForDropdownLag(sortOrderLag)];
    case SohTypes.SohMonitorType.TIMELINESS:
      // TODO HANDLE TIMELINESS - try to avoid these specific functions for lag, missing, etc...
      // !FIX ESLINT DO NOT USE REACT HOOK HOOKS IN CONDITIONAL
      // eslint-disable-next-line react-hooks/rules-of-hooks, no-case-declarations
      const [sortOrderTimeliness, setSortOrderTimeliness] = React.useState(
        SOHTimelinessOptions.CHANNEL_FIRST
      );
      // eslint-disable-next-line no-case-declarations
      const sortDropdownTimeliness = makeLagTimelinessDropdown(
        sortOrderTimeliness,
        setSortOrderTimeliness
      );
      return [
        sortDropdownTimeliness,
        () => getSortFunctionForDropdownTimeliness(sortOrderTimeliness)
      ];
    default:
      return undefined;
  }
};

/**
 * builds a quiet event that can be passed do to victory for on context calls
 *
 * @param channelName name of the channel
 * @param quietUntilMs channels quietExpiresAt time
 * @param e react mouse event from on context
 */
export const getOnContextMenus = (
  type: SohTypes.SohMonitorType,
  availableQuietDurations: number[],
  stationName: string,
  isStale: boolean,
  channelSoh: ChannelSohForMonitorType[]
): {
  onContextMenuBar(e: React.MouseEvent<any, MouseEvent>, data: any): void;
  onContextMenuBarLabel(e: React.MouseEvent<any, MouseEvent>, index: any): void;
} => {
  // little helper function for building the quiet event
  const buildQuietEvent = (
    channelName: string,
    quietUntilMs: number,
    e: React.MouseEvent<any, MouseEvent>
  ) => ({
    channelMonitorPairs: [{ channelName, monitorType: type }],
    position: { left: e.clientX, top: e.clientY },
    quietUntilMs,
    quietingDurationSelections: availableQuietDurations,
    stationName,
    isStale
  });
  // this context gets called when right clicking the bar itself
  const onContextMenuBar = (e: React.MouseEvent<any, MouseEvent>, data) => {
    e.preventDefault();
    showQuietingContextMenu(buildQuietEvent(data.datum.x, data.datum.quietUntilMs, e));
  };
  // we need a separate on context menu for the labels because the labels do not pass
  // a datum object since the labels do now have all the info on the selected channel
  // just the cleaned channel name
  const onContextMenuBarLabel = (e: React.MouseEvent<any, MouseEvent>, index: any) => {
    e.preventDefault();
    showQuietingContextMenu(
      buildQuietEvent(
        channelSoh[index].name,
        channelSoh[index].quietExpiresAt ? channelSoh[index].quietExpiresAt : undefined,
        e
      )
    );
  };
  return { onContextMenuBar, onContextMenuBarLabel };
};

/** The toolbar bar chart X axis formatter */
export const toolbarBarChartXAxisTicFormat = (
  stationName: string,
  channelSoh: ChannelSohForMonitorType[]
) => (name: string): string => {
  const channelHasUnackChanges: boolean = channelSoh.filter(chan => chan.name === name)[0]
    .hasUnacknowledgedChanges;
  const nameToReturn: string = name && name.replace ? name.replace(`${stationName}.`, '') : name;
  return channelHasUnackChanges ? `\u25cf ${nameToReturn}` : nameToReturn;
};
