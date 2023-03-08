import { ContextMenu } from '@blueprintjs/core';
import { humanReadable, secondsToString, toSentenceCase } from '@gms/common-util';
import { Form, FormTypes } from '@gms/ui-core-components';
import React from 'react';

import { formatNumberForDisplayFixedThreeDecimalPlaces } from '~common-ui/common/table-utils';

export interface IANEventDetailsProps {
  eventTime: number;
  latitude: number;
  longitude: number;
  depth: number;
  workflowStatus: string;
}

/**
 * Returns a form item object given location data
 *
 * @param key item and label text
 * @param value data to be displayed
 * @returns a {@link FormTypes.FormItem} object
 */
function getLocationFormItem(key: string, value: number) {
  return {
    itemKey: key,
    labelText: key,
    itemType: FormTypes.ItemType.Display,
    displayText: `${formatNumberForDisplayFixedThreeDecimalPlaces(value)}`,
    displayTextFormat: FormTypes.TextFormats.Time
  };
}

/**
 * MapEventDetails Component
 */
export function MapEventDetails(props: IANEventDetailsProps) {
  const { eventTime, latitude, longitude, depth, workflowStatus } = props;

  // FormTypes.TextFormats.Time allows us to apply monospace typeface per UX
  const formItems: FormTypes.FormItem[] = [];
  formItems.push({
    itemKey: 'Event Time',
    labelText: 'Event Time',
    itemType: FormTypes.ItemType.Display,
    displayText: `${secondsToString(eventTime)}`,
    displayTextFormat: FormTypes.TextFormats.Time
  });
  formItems.push(getLocationFormItem('Lat (°)', latitude));
  formItems.push(getLocationFormItem('Lon (°)', longitude));
  formItems.push(getLocationFormItem('Depth (km)', depth));
  formItems.push({
    itemKey: 'Workflow Status',
    labelText: 'Workflow Status',
    itemType: FormTypes.ItemType.Display,
    displayText: `${toSentenceCase(humanReadable(workflowStatus ?? 'Not started'))}`
  });

  const defaultPanel: FormTypes.FormPanel = {
    formItems,
    name: 'Additional Details'
  };

  return (
    <div className="map-station-details__container">
      <Form
        header="Event Details"
        defaultPanel={defaultPanel}
        disableSubmit
        onCancel={() => {
          ContextMenu.hide();
        }}
      />
    </div>
  );
}

export const showMapEventDetailsPopover = (event: any, clientX: number, clientY: number): void => {
  ContextMenu.show(
    <MapEventDetails
      eventTime={event.time}
      latitude={event.latitudeDegrees}
      longitude={event.longitudeDegrees}
      depth={event.depthKm}
      workflowStatus={event.status}
    />,
    {
      left: clientX,
      top: clientY
    },
    undefined,
    true
  );
};
