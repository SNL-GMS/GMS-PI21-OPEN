import type { StationTypes } from '@gms/common-model';
import { formatTimeForDisplay } from '@gms/common-util';
import { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import type Immutable from 'immutable';

import { stationTypeToFriendlyNameMap } from '~analyst-ui/components/map/ian-map-utils';
import { messageConfig } from '~analyst-ui/config/message-config';
import { formatNumberForDisplayFixedThreeDecimalPlaces } from '~common-ui/common/table-utils';

import type { PropertiesToolbarItemDefs } from './types';
import {
  ChannelColumn,
  channelColumnDisplayStrings,
  SiteColumn,
  siteColumnDisplayStrings
} from './types';

export const getToolbarItemDefs = (
  effectiveAtTimes: string[],
  selectedStation: StationTypes.Station,
  stationName: string,
  selectedEffectiveAt: string,
  siteColumnsToDisplay: Immutable.Map<SiteColumn, boolean>,
  channelColumnsToDisplay: Immutable.Map<ChannelColumn, boolean>,
  onEffectiveTimeChange: (args: string) => void,
  setSelectedSiteColumnsToDisplay: (args: Immutable.Map<SiteColumn, boolean>) => void,
  setSelectedChannelColumnsToDisplay: (args: Immutable.Map<ChannelColumn, boolean>) => void
): PropertiesToolbarItemDefs => {
  const stationNameToolbarItem: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    ianApp: true,
    label: 'Station',
    tooltip: 'Currently selected station',
    widthPx: 400,
    rank: 0,
    value: selectedStation?.name ?? stationName
  };

  const typeToolbarItem: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    ianApp: true,
    label: 'Type',
    tooltip: 'Single station or array',
    widthPx: 400,
    rank: 1,
    value: stationTypeToFriendlyNameMap.get(selectedStation?.type) ?? messageConfig.invalidCellText
  };

  const latitudeToolbarItem: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    ianApp: true,
    label: 'Lat',
    tooltip: 'Station latitude',
    widthPx: 400,
    rank: 2,
    value: `${formatNumberForDisplayFixedThreeDecimalPlaces(
      selectedStation?.location?.latitudeDegrees
    )}°`
  };

  const longitudeToolbarItem: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    ianApp: true,
    label: 'Lon',
    tooltip: 'Station longitude',
    widthPx: 400,
    rank: 3,
    value: `${formatNumberForDisplayFixedThreeDecimalPlaces(
      selectedStation?.location?.longitudeDegrees
    )}°`
  };

  const depthToolbarItem: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    ianApp: true,
    label: 'Depth',
    tooltip: 'Depth (km)',
    widthPx: 400,
    rank: 4,
    value:
      `${formatNumberForDisplayFixedThreeDecimalPlaces(selectedStation?.location?.depthKm)}` ===
      messageConfig.invalidCellText
        ? messageConfig.invalidCellText
        : `${formatNumberForDisplayFixedThreeDecimalPlaces(selectedStation?.location?.depthKm)} km`
  };

  const descriptionToolbarItem: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    ianApp: true,
    label: 'Description',
    tooltip: 'Station description',
    widthPx: 400,
    rank: 5,
    value: selectedStation?.description
      ? selectedStation.description.replace(/_/g, ' ')
      : messageConfig.invalidCellText
  };

  const elevationToolbarItem: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    ianApp: true,
    label: 'Elev',
    tooltip: 'Station elevation',
    rank: 6,
    value:
      `${formatNumberForDisplayFixedThreeDecimalPlaces(selectedStation?.location?.elevationKm)}` ===
      messageConfig.invalidCellText
        ? messageConfig.invalidCellText
        : `${formatNumberForDisplayFixedThreeDecimalPlaces(
            selectedStation?.location?.elevationKm
          )} km`
  };

  const dropdownText: string[] = effectiveAtTimes.map(time => formatTimeForDisplay(time));
  const effectiveTimeDropdown: DeprecatedToolbarTypes.DropdownItem = {
    label: 'Effective at',
    displayLabel: true,
    dropdownOptions: effectiveAtTimes,
    dropdownText,
    value: selectedEffectiveAt ?? effectiveAtTimes[0],
    onChange: onEffectiveTimeChange,
    widthPx: 220,
    tooltip: 'Select effective time to display',
    type: DeprecatedToolbarTypes.ToolbarItemType.Dropdown,
    rank: 7,
    cyData: 'select-effective-at'
  };

  /**
   *
   * @param columnsToDisplay is passed to this function by the checkbox dropdown, we don't control the parameters
   */
  function setSiteColumnsToDisplay(columnsToDisplay: Immutable.Map<SiteColumn, boolean>) {
    setSelectedSiteColumnsToDisplay(columnsToDisplay);
  }

  /**
   *
   * @param columnsToDisplay is passed to this function by the checkbox dropdown, we don't control the parameters
   */
  function setChannelColumnsToDisplay(columnsToDisplay: Immutable.Map<ChannelColumn, boolean>) {
    setSelectedChannelColumnsToDisplay(columnsToDisplay);
  }

  const siteColumnPickerCheckboxDropdown: DeprecatedToolbarTypes.CheckboxDropdownItem = {
    label: 'Channel group columns',
    menuLabel: 'Channel group columns',
    widthPx: 220,
    tooltip: 'Select columns to be shown in the channel group table below',
    type: DeprecatedToolbarTypes.ToolbarItemType.CheckboxList,
    onChange: setSiteColumnsToDisplay,
    cyData: 'station-properties-channel-group-column-picker',
    values: siteColumnsToDisplay,
    enumOfKeys: SiteColumn,
    enumKeysToDisplayStrings: siteColumnDisplayStrings,
    rank: 8
  };

  const channelColumnPickerCheckboxDropdown: DeprecatedToolbarTypes.CheckboxDropdownItem = {
    label: 'Channel columns',
    menuLabel: 'Channel columns',
    widthPx: 220,
    tooltip: 'Select columns to be shown in the channel table below',
    type: DeprecatedToolbarTypes.ToolbarItemType.CheckboxList,
    onChange: setChannelColumnsToDisplay,
    cyData: 'station-properties-channel-column-picker',
    values: channelColumnsToDisplay,
    enumOfKeys: ChannelColumn,
    enumKeysToDisplayStrings: channelColumnDisplayStrings,
    rank: 9
  };

  const rightToolbarItemDefs: DeprecatedToolbarTypes.ToolbarItem[] = [
    stationNameToolbarItem,
    latitudeToolbarItem,
    longitudeToolbarItem,
    depthToolbarItem,
    elevationToolbarItem,
    typeToolbarItem,
    descriptionToolbarItem
  ];
  const leftToolbarItemDefs: DeprecatedToolbarTypes.ToolbarItem[] = [
    effectiveTimeDropdown,
    siteColumnPickerCheckboxDropdown,
    channelColumnPickerCheckboxDropdown
  ];
  return { rightToolbarItemDefs, leftToolbarItemDefs };
};
