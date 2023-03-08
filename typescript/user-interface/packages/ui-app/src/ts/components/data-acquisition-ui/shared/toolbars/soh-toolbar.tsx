/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { SohTypes } from '@gms/common-model';
import {
  DATE_TIME_FORMAT_WITH_SECOND_PRECISION,
  dateToString,
  MILLISECONDS_IN_SECOND
} from '@gms/common-util';
import { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import { useAppSelector, useGetSohConfigurationQuery } from '@gms/ui-state';
import Immutable from 'immutable';
import React from 'react';

import { useBaseDisplaySize } from '~components/common-ui/components/base-display/base-display-hooks';
import { dataAcquisitionUserPreferences } from '~data-acquisition-ui/config';
import { messageConfig } from '~data-acquisition-ui/config/message-config';

import { BaseToolbar } from './base-toolbar';

const statusFilterWidthPx = 200;

export enum FilterableSOHTypes {
  GOOD = SohTypes.SohStatusSummary.GOOD,
  MARGINAL = SohTypes.SohStatusSummary.MARGINAL,
  BAD = SohTypes.SohStatusSummary.BAD,
  NONE = SohTypes.SohStatusSummary.NONE
}

export enum FilterableSOHTypesDrillDown {
  GOOD = SohTypes.SohStatusSummary.GOOD,
  MARGINAL = SohTypes.SohStatusSummary.MARGINAL,
  BAD = SohTypes.SohStatusSummary.BAD
}

export const FilterableSohTypesDisplayStrings = Immutable.Map<string, string>([
  ['GOOD', 'Good'],
  ['MARGINAL', 'Marginal'],
  ['BAD', 'Bad'],
  ['NONE', 'None']
]);

export interface SohToolbarProps {
  statusesToDisplay: Record<FilterableSOHTypes | FilterableSOHTypesDrillDown, boolean>;
  widthPx?: number;
  leftItems: DeprecatedToolbarTypes.ToolbarItem[];
  rightItems: DeprecatedToolbarTypes.ToolbarItem[];
  statusFilterText: string;
  isDrillDown: boolean;
  statusFilterTooltip?: string;
  setStatusesToDisplay(statuses: Record<FilterableSOHTypes, boolean>): void;
  toggleHighlight(ref?: HTMLDivElement): void;
}

/**
 * Toolbar used in SOH components
 */
export function SohToolbar(props: SohToolbarProps) {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { setStatusesToDisplay } = props;
  const [widthPx] = useBaseDisplaySize();

  const configuration = useGetSohConfigurationQuery();
  const reprocessingPeriodSecs = configuration.data?.reprocessingPeriodSecs ?? 0;
  const sohStationStaleMs = configuration.data?.sohStationStaleMs ?? 0;

  const lastUpdated =
    useAppSelector(state => state.app.dataAcquisition?.data?.sohStatus?.lastUpdated) ?? 0;

  const isStale = useAppSelector(state => state.app.dataAcquisition?.data?.sohStatus?.isStale);

  const statusesToDisplayMap = Immutable.Map(props.statusesToDisplay);

  const handleChange = (statuses: Immutable.Map<FilterableSOHTypes, boolean>): void => {
    setStatusesToDisplay(statuses.toObject());
  };

  const statusToDisplayCheckBoxDropdown: DeprecatedToolbarTypes.CheckboxDropdownItem = {
    enumOfKeys: props.isDrillDown ? FilterableSOHTypesDrillDown : FilterableSOHTypes,
    label: props.statusFilterText,
    menuLabel: props.statusFilterText,
    rank: 0,
    widthPx: statusFilterWidthPx,
    type: DeprecatedToolbarTypes.ToolbarItemType.CheckboxList,
    tooltip: props.statusFilterTooltip ? props.statusFilterTooltip : props.statusFilterText,
    values: statusesToDisplayMap,
    enumKeysToDisplayStrings: FilterableSohTypesDisplayStrings,
    onChange: handleChange,
    cyData: 'filter-soh',
    onPopUp: ref => {
      props.toggleHighlight(ref);
    },
    onPopoverDismissed: () => {
      props.toggleHighlight();
    },
    colors: Immutable.Map([
      [FilterableSOHTypes.GOOD, dataAcquisitionUserPreferences.colors.ok],
      [FilterableSOHTypes.MARGINAL, dataAcquisitionUserPreferences.colors.warning],
      [FilterableSOHTypes.BAD, dataAcquisitionUserPreferences.colors.strongWarning],
      [FilterableSOHTypes.NONE, 'NULL_CHECKBOX_COLOR_SWATCH']
    ])
  };

  const leftToolbarItemDefs: DeprecatedToolbarTypes.ToolbarItem[] = [
    statusToDisplayCheckBoxDropdown,
    ...props.leftItems
  ];

  const updateIntervalDisplay: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    label: messageConfig.labels.sohToolbar.interval,
    tooltip: messageConfig.tooltipMessages.sohToolbar.interval,
    widthPx: 400,
    rank: 0,
    value: `${reprocessingPeriodSecs} second${reprocessingPeriodSecs !== 1 ? 's' : ''}`
  };

  const lastUpdatedStr =
    lastUpdated !== undefined
      ? dateToString(
          new Date(lastUpdated * MILLISECONDS_IN_SECOND),
          DATE_TIME_FORMAT_WITH_SECOND_PRECISION
        )
      : '-';
  const updateTimeDisplay: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    label: !isStale ? messageConfig.labels.sohToolbar.updateTimeDisplay : undefined,
    tooltip: messageConfig.tooltipMessages.sohToolbar.lastUpdateTime,
    tooltipForIssue: `${messageConfig.tooltipMessages.sohToolbar.lastUpdateTime}${
      lastUpdated !== undefined ? ` at ${lastUpdatedStr}` : ''
    }`,
    hasIssue: isStale,
    widthPx: 400,
    rank: 0,
    style: { marginLeft: '1em' },
    value: !isStale
      ? lastUpdatedStr
      : messageConfig.labels.sohToolbar.updateTimeDisplayIssue(sohStationStaleMs)
  };

  const rightToolbarItemDefs: DeprecatedToolbarTypes.ToolbarItem[] = [
    ...props.rightItems,
    updateTimeDisplay,
    updateIntervalDisplay
  ];

  return (
    <BaseToolbar
      widthPx={props.widthPx ?? widthPx}
      itemsRight={rightToolbarItemDefs}
      itemsLeft={leftToolbarItemDefs}
    />
  );
}
