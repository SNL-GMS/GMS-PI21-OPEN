/* eslint-disable class-methods-use-this */
/* eslint-disable react/destructuring-assignment */
import { ContextMenu, NonIdealState } from '@blueprintjs/core';
import { SignalDetectionTypes } from '@gms/common-model';
import {
  DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  dateToString,
  toDate
} from '@gms/common-util';
import { Form, FormTypes, Table } from '@gms/ui-core-components';
import classNames from 'classnames';
import flatten from 'lodash/flatten';
import React from 'react';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import { parseBeamType } from '~analyst-ui/common/utils/signal-detection-util';

import { SIGNAL_DETECTION_HISTORY_COLUMN_DEFINITIONS } from './constants';
import type {
  SignalDetectionDetailsProps,
  SignalDetectionDetailsState,
  SignalDetectionHistoryRow
} from './types';
import { formatUncertainty } from './utils';

/**
 * SignalDetectionDetails Component
 */
export class SignalDetectionDetails extends React.Component<
  SignalDetectionDetailsProps,
  SignalDetectionDetailsState
> {
  /**
   * Constructor
   */
  public constructor(props: SignalDetectionDetailsProps) {
    super(props);
    this.state = {
      // eslint-disable-next-line react/no-unused-state
      showHistory: false
    };
  }

  /**
   * React component lifecycle
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    if (!this.props.detection) {
      return <NonIdealState />;
    }

    const formItems: FormTypes.FormItem[] = [];
    const { detection } = this.props;
    const arrivalTimeFM: SignalDetectionTypes.FeatureMeasurement = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(
      SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses)
        ?.featureMeasurements
    );
    const arrivalTimeFMValue = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses)
        ?.featureMeasurements
    );
    const fmPhase = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses)
        .featureMeasurements
    );
    // TODO: Add creation time when part of COI also uncomment the displayTextFormat
    formItems.push({
      itemKey: 'Creation time',
      labelText: 'Creation time',
      itemType: FormTypes.ItemType.Display,
      displayText: 'TBD'
      // displayTextFormat: FormTypes.TextFormats.Time
    });
    formItems.push({
      itemKey: 'Phase',
      labelText: 'Phase',
      displayText: fmPhase.value ?? 'Unknown',
      itemType: FormTypes.ItemType.Display
    });
    formItems.push({
      itemKey: 'Detection time',
      labelText: 'Detection time',
      itemType: FormTypes.ItemType.Display,
      className: 'monospace',
      displayText: dateToString(
        toDate(arrivalTimeFMValue?.arrivalTime.value),
        DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
      ),
      displayTextFormat: FormTypes.TextFormats.Time
    });
    formItems.push({
      itemKey: 'Time uncertainty',
      labelText: 'Time uncertainty',
      className: 'monospace',
      itemType: FormTypes.ItemType.Display,
      displayText: formatUncertainty(arrivalTimeFMValue.arrivalTime.standardDeviation)
    });
    formItems.push({
      itemKey: 'Channel segment type',
      labelText: 'Channel segment type',
      itemType: FormTypes.ItemType.Display,
      displayText: parseBeamType(arrivalTimeFM.channel.name)
    });
    formItems.push({
      itemKey: 'Author',
      labelText: 'Author',
      itemType: FormTypes.ItemType.Display,
      displayText: 'TBD'
    });
    formItems.push({
      itemKey: 'Rejected',
      labelText: 'Rejected',
      itemType: FormTypes.ItemType.Display,
      displayText: SignalDetectionTypes.Util.getCurrentHypothesis(
        detection.signalDetectionHypotheses
      )?.rejected
        ? 'True'
        : 'False'
    });

    const defaultPanel: FormTypes.FormPanel = {
      formItems,
      name: 'Current Version'
    };
    const extraPanels: FormTypes.FormPanel[] = [
      {
        name: 'All Versions',
        content: this.renderTable({
          rowData: this.generateDetectionHistoryTableRows(detection),
          overlayNoRowsTemplate: 'No Versions',
          rowClassRules: {
            'versions-table__row--first-in-table': params => {
              if (params.data['first-in-table']) {
                return true;
              }
              return false;
            }
          }
        })
      }
    ];

    return (
      <div className="signal-detection-details__container">
        <Form
          header="Signal Detection Details"
          headerDecoration={
            <div>
              <div
                className="signal-detection-swatch"
                style={{ backgroundColor: this.props.color }}
              />
              <span className="signal-detection-swatch-label">{this.props.assocStatus}</span>
            </div>
          }
          defaultPanel={defaultPanel}
          disableSubmit
          onCancel={() => {
            ContextMenu.hide();
          }}
          extraPanels={extraPanels}
        />
      </div>
    );
  }

  /**
   * Render the Detection table.
   */
  private readonly renderTable = (tableProps: any) => (
    <div className={classNames('ag-theme-dark', 'signal-detection-details-versions-table')}>
      <div className="max">
        <Table
          columnDefs={SIGNAL_DETECTION_HISTORY_COLUMN_DEFINITIONS}
          getRowId={params => params.data.id}
          rowSelection="single"
          // eslint-disable-next-line react/jsx-props-no-spreading
          {...tableProps}
        />
      </div>
    </div>
  );

  /**
   * Generate the table row data for the detection history.
   */
  private readonly generateDetectionHistoryTableRows = (
    detection: SignalDetectionTypes.SignalDetection
  ): SignalDetectionHistoryRow[] => {
    const rows = flatten(
      detection.signalDetectionHypotheses
        .map(signalDetectionHypo => {
          const phaseFMValue = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
            signalDetectionHypo.featureMeasurements
          );
          const arrivalTimeFM: SignalDetectionTypes.FeatureMeasurement = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(
            SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses)
              ?.featureMeasurements
          );
          const arrivalTimeFMValue = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
            SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses)
              ?.featureMeasurements
          );
          return {
            id: signalDetectionHypo.id.id,
            versionId: signalDetectionHypo.id.id, // TODO: what should the version be? (hypo number?)
            creationTimestamp: -1, // TODO: change when field is available for now -1 translates to 'TBD'
            phase: phaseFMValue.value,
            arrivalTimeMeasurementFeatureType:
              SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME,
            arrivalTimeMeasurementTimestamp: arrivalTimeFMValue.arrivalTime.value,
            arrivalTimeMeasurementUncertaintySec: arrivalTimeFMValue.arrivalTime.standardDeviation,
            channelSegmentType: parseBeamType(arrivalTimeFM.channel.name),
            authorName: 'TBD',
            rejected: signalDetectionHypo?.rejected ? 'True' : 'False'
          };
        })
        .sort((a, b) => b.arrivalTimeMeasurementTimestamp - a.arrivalTimeMeasurementTimestamp)
    );
    rows[0]['first-in-table'] = true;
    return rows;
  };
}
