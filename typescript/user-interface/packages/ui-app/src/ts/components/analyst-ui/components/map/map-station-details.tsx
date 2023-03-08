import { ContextMenu } from '@blueprintjs/core';
import { Form, FormTypes } from '@gms/ui-core-components';
import React from 'react';

import { getTableCellStringValue } from '~common-ui/common/table-utils';

export interface IANStationDetailsProps {
  stationName: string;
  latitude: string;
  longitude: string;
  elevation: string;
  detailedType: string;
  entityType: string;
}

/**
 * MapStationDetails Component
 */
// eslint-disable-next-line react/function-component-definition
export const MapStationDetails: React.FunctionComponent<IANStationDetailsProps> = (
  props: IANStationDetailsProps
) => {
  const { stationName, latitude, longitude, elevation, detailedType, entityType } = props;

  const formItems: FormTypes.FormItem[] = [];
  formItems.push({
    itemKey: 'Name',
    labelText: 'Name',
    itemType: FormTypes.ItemType.Display,
    displayText: `${getTableCellStringValue(stationName)}`,
    displayTextFormat: FormTypes.TextFormats.Time
  });
  formItems.push({
    itemKey: 'Lat (°)',
    labelText: 'Lat (°)',
    itemType: FormTypes.ItemType.Display,
    displayText: `${getTableCellStringValue(latitude)}`,
    // Apply text format below to get monospace typeface per UX
    displayTextFormat: FormTypes.TextFormats.Time
  });
  formItems.push({
    itemKey: 'Lon (°)',
    labelText: 'Lon (°)',
    itemType: FormTypes.ItemType.Display,
    displayText: `${getTableCellStringValue(longitude)}`,
    // Apply text format below to get monospace typeface per UX
    displayTextFormat: FormTypes.TextFormats.Time
  });
  formItems.push({
    itemKey: 'Elevation (km)',
    labelText: 'Elevation (km)',
    itemType: FormTypes.ItemType.Display,
    displayText: `${getTableCellStringValue(elevation)}`,
    // Apply text format below to get monospace typeface per UX
    displayTextFormat: FormTypes.TextFormats.Time
  });
  if (entityType === 'Station') {
    formItems.push({
      itemKey: 'Type',
      labelText: 'Type',
      itemType: FormTypes.ItemType.Display,
      displayText: `${getTableCellStringValue(detailedType)}`
    });
  }

  const defaultPanel: FormTypes.FormPanel = {
    formItems,
    name: 'Additional Details'
  };

  const headerText = entityType === 'Station' ? 'Station Details' : 'Site Details';

  return (
    <div className="map-station-details__container">
      <Form
        header={headerText}
        defaultPanel={defaultPanel}
        disableSubmit
        onCancel={() => {
          ContextMenu.hide();
        }}
      />
    </div>
  );
};

export const showMapStationDetailsPopover = (
  station: any,
  clientX: number,
  clientY: number
): void => {
  ContextMenu.show(
    <MapStationDetails
      stationName={station.name}
      latitude={station.coordinates.latitude}
      longitude={station.coordinates.longitude}
      elevation={station.coordinates.elevation}
      entityType={station.type}
      detailedType={station.statype}
    />,
    {
      left: clientX,
      top: clientY
    },
    undefined,
    true
  );
};
