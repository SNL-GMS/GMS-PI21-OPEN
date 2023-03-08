import { DeprecatedToolbarTypes } from '@gms/ui-core-components';

export enum SOHLagOptions {
  LAG_HIGHEST = 'Lag: highest to lowest',
  LAG_LOWEST = 'Lag: lowest to highest',
  CHANNEL_FIRST = 'Channel: A - Z',
  CHANNEL_LAST = 'Channel: Z - A'
}

export enum SOHTimelinessOptions {
  TIMELINESS_HIGHEST = 'Timeliness: highest to lowest',
  TIMELINESS_LOWEST = 'Timeliness: lowest to highest',
  CHANNEL_FIRST = 'Channel: A - Z',
  CHANNEL_LAST = 'Channel: Z - A'
}

export enum SOHMissingOptions {
  MISSING_HIGHEST = 'Missing %: highest to lowest',
  MISSING_LOWEST = 'Missing %: lowest to highest',
  CHANNEL_FIRST = 'Channel: A - Z',
  CHANNEL_LAST = 'Channel: Z - A'
}

export const makeLagSortingDropdown = (
  currentSorting: SOHLagOptions,
  setSorting: (value: any) => void
): DeprecatedToolbarTypes.DropdownItem => ({
  label: 'Sort Channels',
  rank: undefined,
  tooltip: 'Sort',
  type: DeprecatedToolbarTypes.ToolbarItemType.Dropdown,
  dropdownOptions: SOHLagOptions,
  value: currentSorting,
  onChange: setSorting,
  widthPx: 220
});

export const makeLagTimelinessDropdown = (
  currentSorting: SOHTimelinessOptions,
  setSorting: (value: any) => void
): DeprecatedToolbarTypes.DropdownItem => ({
  label: 'Sort Channels',
  rank: undefined,
  tooltip: 'Sort',
  type: DeprecatedToolbarTypes.ToolbarItemType.Dropdown,
  dropdownOptions: SOHTimelinessOptions,
  value: currentSorting,
  onChange: setSorting,
  widthPx: 220
});

export const makeMissingSortingDropdown = (
  currentSorting: SOHMissingOptions,
  setSorting: (value: any) => void
): DeprecatedToolbarTypes.DropdownItem => ({
  label: 'Sort Channels',
  rank: undefined,
  tooltip: 'Sort',
  type: DeprecatedToolbarTypes.ToolbarItemType.Dropdown,
  dropdownOptions: SOHMissingOptions,
  value: currentSorting,
  onChange: setSorting,
  widthPx: 220
});
