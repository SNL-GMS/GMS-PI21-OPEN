import { IconNames } from '@blueprintjs/icons';
import { formatTimeForDisplay } from '@gms/common-util';
import {
  CheckboxDropdownToolbarItem,
  DropdownToolbarItem,
  LabelValueToolbarItem,
  Toolbar
} from '@gms/ui-core-components';
import React from 'react';

import { stationTypeToFriendlyNameMap } from '~analyst-ui/components/map/ian-map-utils';
import { messageConfig } from '~analyst-ui/config/message-config';
import { formatNumberForDisplayFixedThreeDecimalPlaces } from '~common-ui/common/table-utils';
import { useBaseDisplaySize } from '~common-ui/components/base-display/base-display-hooks';

import type { StationPropertiesToolbarProps } from './types';
import {
  ChannelColumn,
  channelColumnDisplayStrings,
  SiteColumn,
  siteColumnDisplayStrings
} from './types';

const BASE_DISPLAY_PADDING = 40;

// eslint-disable-next-line react/function-component-definition
export const StationPropertiesToolbar: React.FunctionComponent<StationPropertiesToolbarProps> = ({
  stationName,
  selectedStation,
  effectiveAtTimes,
  selectedEffectiveAt,
  onEffectiveTimeChange,
  channelColumnsToDisplay,
  siteColumnsToDisplay,
  setSelectedSiteColumnsToDisplay,
  setSelectedChannelColumnsToDisplay
}: StationPropertiesToolbarProps) => {
  const [displayWidthPx] = useBaseDisplaySize();

  const dropdownText: string[] = React.useMemo(
    () => effectiveAtTimes.map(time => formatTimeForDisplay(time)),
    [effectiveAtTimes]
  );

  const leftToolbarItems: JSX.Element[] = React.useMemo(
    () => [
      <DropdownToolbarItem
        key="effectiveat"
        widthPx={220}
        tooltip="Select effective time to display"
        cyData="select-effective-at"
        itemSide="LEFT"
        label="Effective at"
        displayLabel
        dropdownOptions={effectiveAtTimes}
        dropdownText={dropdownText}
        value={selectedEffectiveAt ?? effectiveAtTimes[0]}
        onChange={onEffectiveTimeChange}
      />,
      <CheckboxDropdownToolbarItem
        key="cgcolumns"
        label="Channel group columns"
        menuLabel="Channel group columns"
        widthPx={220}
        tooltip="Select columns to be shown in the channel group table below"
        cyData="station-properties-channel-group-column-picker"
        itemSide="LEFT"
        onChange={setSelectedSiteColumnsToDisplay}
        values={siteColumnsToDisplay}
        enumOfKeys={SiteColumn}
        enumKeysToDisplayStrings={siteColumnDisplayStrings}
      />,
      <CheckboxDropdownToolbarItem
        key="channel"
        label="Channel columns"
        menuLabel="Channel columns"
        widthPx={220}
        tooltip="Select columns to be shown in the channel table below"
        cyData="station-properties-channel-column-picker"
        itemSide="LEFT"
        onChange={setSelectedChannelColumnsToDisplay}
        values={channelColumnsToDisplay}
        enumOfKeys={ChannelColumn}
        enumKeysToDisplayStrings={channelColumnDisplayStrings}
      />
    ],
    [
      channelColumnsToDisplay,
      dropdownText,
      effectiveAtTimes,
      onEffectiveTimeChange,
      selectedEffectiveAt,
      setSelectedChannelColumnsToDisplay,
      setSelectedSiteColumnsToDisplay,
      siteColumnsToDisplay
    ]
  );

  const rightToolbarItems: JSX.Element[] = React.useMemo(
    () => [
      <LabelValueToolbarItem
        key="selected"
        ianApp
        label="Station"
        tooltip="Currently selected station"
        widthPx={400}
        labelValue={selectedStation?.name ?? stationName}
      />,
      <LabelValueToolbarItem
        key="lat"
        ianApp
        label="Lat"
        tooltip="Station latitude"
        widthPx={400}
        labelValue={`${formatNumberForDisplayFixedThreeDecimalPlaces(
          selectedStation?.location?.latitudeDegrees
        )}°`}
      />,

      <LabelValueToolbarItem
        key="lon"
        ianApp
        label="Lon"
        tooltip="Station longitude"
        widthPx={400}
        labelValue={`${formatNumberForDisplayFixedThreeDecimalPlaces(
          selectedStation?.location?.longitudeDegrees
        )}°`}
      />,
      <LabelValueToolbarItem
        key="depth"
        ianApp
        label="Depth"
        tooltip="Depth (km)"
        widthPx={400}
        labelValue={
          `${formatNumberForDisplayFixedThreeDecimalPlaces(selectedStation?.location?.depthKm)}` ===
          messageConfig.invalidCellText
            ? messageConfig.invalidCellText
            : `${formatNumberForDisplayFixedThreeDecimalPlaces(
                selectedStation?.location?.depthKm
              )} km`
        }
      />,
      <LabelValueToolbarItem
        key="elev"
        ianApp
        label="Elev"
        tooltip="Station elevation"
        labelValue={
          `${formatNumberForDisplayFixedThreeDecimalPlaces(
            selectedStation?.location?.elevationKm
          )}` === messageConfig.invalidCellText
            ? messageConfig.invalidCellText
            : `${formatNumberForDisplayFixedThreeDecimalPlaces(
                selectedStation?.location?.elevationKm
              )} km`
        }
      />,
      <LabelValueToolbarItem
        key="type"
        ianApp
        label="Type"
        tooltip="Single station or array"
        widthPx={400}
        labelValue={
          stationTypeToFriendlyNameMap.get(selectedStation?.type) ?? messageConfig.invalidCellText
        }
      />,
      <LabelValueToolbarItem
        key="description"
        ianApp
        label="Description"
        tooltip="Station description"
        widthPx={400}
        labelValue={
          selectedStation?.description
            ? selectedStation.description.replace(/_/g, ' ')
            : messageConfig.invalidCellText
        }
      />
    ],
    [
      selectedStation?.description,
      selectedStation?.location?.depthKm,
      selectedStation?.location?.elevationKm,
      selectedStation?.location?.latitudeDegrees,
      selectedStation?.location?.longitudeDegrees,
      selectedStation?.name,
      selectedStation?.type,
      stationName
    ]
  );

  return (
    <>
      <Toolbar
        toolbarWidthPx={displayWidthPx}
        parentContainerPaddingPx={BASE_DISPLAY_PADDING}
        itemsLeft={leftToolbarItems}
      />
      <Toolbar
        toolbarWidthPx={displayWidthPx}
        parentContainerPaddingPx={BASE_DISPLAY_PADDING}
        overflowIcon={IconNames.INFO_SIGN}
        itemsLeft={rightToolbarItems}
      />
    </>
  );
};
