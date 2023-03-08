import { ContextMenu } from '@blueprintjs/core';
import { Tooltip2 } from '@blueprintjs/popover2';
import { EventTypes } from '@gms/common-model';
import { Form, FormTypes } from '@gms/ui-core-components';
import React from 'react';

import { messageConfig } from '~analyst-ui/config/message-config';
import { formatNumberForDisplayFixedThreeDecimalPlaces } from '~common-ui/common/table-utils';

import { EdgeTypes } from '../events/types';
import type { MapSDFormValues } from './types';

export interface MapSignalDetectionDetailsProps {
  sd: any;
}

/**
 * Given open/complete/other status and interval edge type, generates a string for popover swatch tooltip
 *
 * @param status
 * @param edgeSDType
 * @returns string
 */
export const getSwatchTooltipText = (status: string, edgeSDType: EdgeTypes): string => {
  let statusString;
  let edgeSDTypeString;
  switch (status) {
    case 'Associated to completed event':
      statusString = 'Completed event';
      break;
    case 'Associated to open event':
      statusString = 'Open event';
      break;
    case 'Associated to other event':
      statusString = 'Other event';
      break;
    default:
      statusString = 'Unassociated to event';
  }
  switch (edgeSDType) {
    case EdgeTypes.BEFORE:
      edgeSDTypeString = 'before interval';
      break;
    case EdgeTypes.AFTER:
      edgeSDTypeString = 'after interval';
      break;
    default:
      edgeSDTypeString = 'within interval';
  }
  return `${statusString}, ${edgeSDTypeString}`;
};

/**
 * Given properties from an SD entity, returns values to add to display in signal detection details popover form
 *
 * @param sd
 *
 */
export const getSdDetailDisplayValues = (sd: any): MapSDFormValues => {
  return {
    phaseValue: sd.phaseValue?.value ?? messageConfig.invalidCellText,
    stationName: sd.stationName ?? messageConfig.invalidCellText,
    detectionTimeValue: sd.detectionTime?.detectionTimeValue ?? messageConfig.invalidCellText,
    detectionTimeUncertainty: sd.detectionTime?.detectionTimeUncertainty
      ? `${sd.detectionTime?.detectionTimeUncertainty}s`
      : messageConfig.invalidCellText,
    azimuthValue:
      formatNumberForDisplayFixedThreeDecimalPlaces(sd.azimuth?.azimuthValue) ??
      messageConfig.invalidCellText,
    azimuthUncertainty:
      formatNumberForDisplayFixedThreeDecimalPlaces(sd.azimuth?.azimuthUncertainty) ??
      messageConfig.invalidCellText,
    slownessValue:
      formatNumberForDisplayFixedThreeDecimalPlaces(sd.slowness?.slownessValue) ??
      messageConfig.invalidCellText,
    slownessUncertainty:
      formatNumberForDisplayFixedThreeDecimalPlaces(sd.slowness?.slownessUncertainty) ??
      messageConfig.invalidCellText,
    status:
      sd.status === messageConfig.tooltipMessages.events.unassociated
        ? 'N/A'
        : sd.associatedEventTimeValue
  };
};

/**
 * Given properties from an SD entity, returns an array of form items for the details popover
 *
 * @param sd
 */
export const getSdDetailFormItems = (sd: any): FormTypes.FormItem[] => {
  const formItems: FormTypes.FormItem[] = [];
  const sdValues = getSdDetailDisplayValues(sd);
  formItems.push({
    itemKey: 'Phase',
    labelText: 'Phase',
    itemType: FormTypes.ItemType.Display,
    displayText: sdValues.phaseValue
  });
  formItems.push({
    itemKey: 'Station',
    labelText: 'Station',
    itemType: FormTypes.ItemType.Display,
    displayText: sd.stationName
  });
  formItems.push({
    itemKey: 'Detection time',
    labelText: 'Detection time',
    itemType: FormTypes.ItemType.Display,
    displayText: `${sdValues.detectionTimeValue} ± ${sdValues.detectionTimeUncertainty}`,
    displayTextFormat: FormTypes.TextFormats.Time
  });
  formItems.push({
    itemKey: 'Azimuth (°)',
    labelText: 'Azimuth (°)',
    itemType: FormTypes.ItemType.Display,
    displayText: `${sdValues.azimuthValue} ± ${sdValues.azimuthUncertainty}`
  });
  formItems.push({
    itemKey: 'Slowness (s/°)',
    labelText: 'Slowness (s/°)',
    itemType: FormTypes.ItemType.Display,
    displayText: `${sdValues.slownessValue} ± ${sdValues.slownessUncertainty}`
  });
  formItems.push({
    itemKey: 'Associated event time',
    labelText: 'Associated event time',
    itemType: FormTypes.ItemType.Display,
    displayText: sdValues.status,
    displayTextFormat:
      sdValues.status === EventTypes.AssociationStatus.UNASSOCIATED
        ? FormTypes.TextFormats.Standard
        : FormTypes.TextFormats.Time
  });

  return formItems;
};

// eslint-disable-next-line react/function-component-definition
export const MapSignalDetectionDetails: React.FunctionComponent<MapSignalDetectionDetailsProps> = (
  props: MapSignalDetectionDetailsProps
) => {
  const { sd } = props;
  const formItems = getSdDetailFormItems(sd);

  const defaultPanel: FormTypes.FormPanel = {
    formItems,
    name: 'Additional Details'
  };

  return (
    <div className="map-signal-detection-details__container">
      <Form
        header="Signal Detection Details"
        headerDecoration={
          <div>
            <Tooltip2
              content={
                <span className="sd-swatch-tooltip">
                  {sd.status && sd.edgeSDType
                    ? getSwatchTooltipText(sd.status, sd.edgeSDType)
                    : 'UNKNOWN'}
                </span>
              }
            >
              <div
                className="signal-detection-swatch"
                style={{ backgroundColor: sd.signalDetectionColor }}
              />
            </Tooltip2>
            <span className="signal-detection-swatch-label">{sd.status ?? ''}</span>
          </div>
        }
        defaultPanel={defaultPanel}
        disableSubmit
        onCancel={() => {
          ContextMenu.hide();
        }}
      />
    </div>
  );
};
