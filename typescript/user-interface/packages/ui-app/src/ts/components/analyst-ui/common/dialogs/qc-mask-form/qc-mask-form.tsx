/* eslint-disable react/destructuring-assignment */
import { ContextMenu } from '@blueprintjs/core';
import type { QcMaskTypes } from '@gms/common-model';
import {
  dateToString,
  ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION,
  MILLISECONDS_IN_SECOND
} from '@gms/common-util';
import { Form, FormTypes, Table, WidgetTypes } from '@gms/ui-core-components';
import classNames from 'classnames';
import flatten from 'lodash/flatten';
import React from 'react';
import { toast } from 'react-toastify';

import { getKeyForEnumValue } from '~analyst-ui/common/utils/enum-util';
import type { QcMaskDisplayFilters } from '~analyst-ui/config/';
import { userPreferences } from '~analyst-ui/config/';
import { QcMaskCategory, QcMaskType } from '~analyst-ui/config/system-config';

import type { QcMaskHistoryRow } from '../types';
import { QcMaskDialogBoxType } from '../types';
import { MASK_HISTORY_COLUMN_DEFINITIONS } from './constants';
import type {
  QcMaskDialogBoxProps as QcMaskFormProps,
  QcMaskDialogBoxState as QcMaskFormState
} from './types';

/**
 * QcMaskDialogBox component.
 */
export class QcMaskForm extends React.Component<QcMaskFormProps, QcMaskFormState> {
  private constructor(props) {
    super(props);
    // Creating a mask
    if (this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.Create) {
      this.state = {
        startDate: new Date(this.props.startTimeSecs * MILLISECONDS_IN_SECOND),
        endDate: new Date(this.props.endTimeSecs * MILLISECONDS_IN_SECOND),
        type: 'SPIKE',
        rationale: '',
        // eslint-disable-next-line react/no-unused-state
        showHistory: false,
        mask: undefined,
        // eslint-disable-next-line react/no-unused-state
        startTimeOnHold: false,
        // eslint-disable-next-line react/no-unused-state
        endTimeOnHold: false,
        category: undefined
      };
    } else if (this.props.mask) {
      this.state = {
        startDate: new Date(this.props.mask.currentVersion.startTime * MILLISECONDS_IN_SECOND),
        endDate: new Date(this.props.mask.currentVersion.endTime * MILLISECONDS_IN_SECOND),
        rationale: this.props.mask.currentVersion.rationale,
        type: this.props.mask.currentVersion.type,
        mask: this.props.mask,
        // eslint-disable-next-line react/no-unused-state
        showHistory: false,
        // eslint-disable-next-line react/no-unused-state
        startTimeOnHold: false,
        // eslint-disable-next-line react/no-unused-state
        endTimeOnHold: false,
        category: this.props.mask.currentVersion.category
      };
    }
  }

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line complexity
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const formItems = this.generateFormItems();
    const currentVersionPanel: FormTypes.FormPanel = {
      formItems,
      name: 'Current Version'
    };
    const extraPanels: FormTypes.FormPanel[] = [];

    if (this.props.qcMaskDialogBoxType !== QcMaskDialogBoxType.Create) {
      const allVersionsPanel: FormTypes.FormPanel = {
        content: this.renderMaskHistoryTable({
          rowData: this.generateMaskHistoryTableRows(this.state.mask),
          overlayNoRowsTemplate: 'No versions',
          rowClassRules: {
            'versions-table__row--first-in-table': params => {
              if (params.data['first-in-table']) {
                return true;
              }
              return false;
            }
          }
        }),
        name: 'All Versions'
      };
      extraPanels.push(allVersionsPanel);
    }
    const qcMaskSwatch = this.renderMaskSwatch(
      this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.Create
        ? QcMaskCategory.ANALYST_DEFINED
        : QcMaskCategory[this.state.category]
    );
    return (
      <div className="qc-dialog">
        <Form
          header="QC Mask"
          headerDecoration={qcMaskSwatch}
          extraPanels={extraPanels}
          defaultPanel={currentVersionPanel}
          onSubmit={this.onAcceptReject}
          onCancel={ContextMenu.hide}
          submitButtonText={
            this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.Reject ? 'Reject' : 'Save'
          }
          disableSubmit={this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.View}
          requiresModificationForSubmit={
            this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.Modify
          }
        />
      </div>
    );
  }

  private readonly generateFormItems = (): FormTypes.FormItem[] => {
    const items: FormTypes.FormItem[] = [];
    const startTimeItem: FormTypes.FormItem =
      this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.Reject ||
      this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.View
        ? {
            itemKey: 'startTime',
            labelText: 'Start Time',
            itemType: FormTypes.ItemType.Display,
            displayText: dateToString(
              this.state.startDate,
              ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION
            ),
            displayTextFormat: FormTypes.TextFormats.Time
          }
        : {
            itemKey: 'startTime',
            labelText: 'Start Time',
            itemType: FormTypes.ItemType.Input,

            value: {
              type: WidgetTypes.WidgetInputType.TimePicker,
              defaultValue: this.state.startDate
            }
          };
    items.push(startTimeItem);
    const endTimeItem: FormTypes.FormItem =
      this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.Reject ||
      this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.View
        ? {
            itemKey: 'endTime',
            labelText: 'End Time',
            itemType: FormTypes.ItemType.Display,
            displayText: dateToString(
              this.state.endDate,
              ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION
            ),
            displayTextFormat: FormTypes.TextFormats.Time
          }
        : {
            itemKey: 'endTime',
            labelText: 'End Time',
            itemType: FormTypes.ItemType.Input,

            value: {
              type: WidgetTypes.WidgetInputType.TimePicker,
              defaultValue: this.state.endDate
            }
          };
    items.push(endTimeItem);
    const categoryItem: FormTypes.FormItem = {
      itemKey: 'category',
      labelText: 'Category',
      itemType: FormTypes.ItemType.Display,
      displayText:
        this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.Create
          ? QcMaskCategory.ANALYST_DEFINED
          : QcMaskCategory[this.state.category]
    };
    items.push(categoryItem);

    if (this.state.type !== null && this.state.type !== undefined) {
      const typeItem: FormTypes.FormItem =
        this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.Reject ||
        this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.View
          ? {
              itemKey: 'type',
              labelText: 'Type',
              itemType: FormTypes.ItemType.Display,
              displayText: QcMaskType[this.state.type]
            }
          : {
              itemKey: 'type',
              labelText: 'Type',
              itemType: FormTypes.ItemType.Input,
              value: {
                type: WidgetTypes.WidgetInputType.DropDown,
                defaultValue: QcMaskType[this.state.type],
                params: {
                  dropDownItems: QcMaskType
                }
              }
            };
      items.push(typeItem);
    }

    const rationaleItem: FormTypes.FormItem =
      this.props.qcMaskDialogBoxType === QcMaskDialogBoxType.View
        ? {
            itemKey: 'rationale',
            labelText: 'Rationale',
            itemType: FormTypes.ItemType.Display,
            displayText: this.state.rationale
          }
        : {
            itemKey: 'rationale',
            labelText: 'Rationale',
            itemType: FormTypes.ItemType.Input,

            value: {
              defaultValue: this.state.rationale,
              type: WidgetTypes.WidgetInputType.TextArea
            }
          };
    items.push(rationaleItem);
    return items;
  };

  /**
   * Generate the table row data for the mask history.
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly generateMaskHistoryTableRows = (
    mask: QcMaskTypes.QcMask
  ): QcMaskHistoryRow[] => {
    const rows = flatten(
      mask.qcMaskVersions
        .map(m => ({
          id: mask.id,
          versionId: m.version,
          color: userPreferences.colors.maskDisplayFilters[m.category].color,
          category: userPreferences.colors.maskDisplayFilters[m.category].name,
          type: QcMaskType[m.type],
          startTime: m.startTime,
          endTime: m.endTime,
          channelSegmentIds: m.channelSegmentIds.join(', '),
          rationale: m.rationale
        }))
        .sort((a, b) => b.startTime - a.startTime)
    );
    rows[0]['first-in-table'] = true;
    return rows;
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onAcceptReject = (data: any) => {
    const startTimeSecs = data.startTime
      ? data.startTime.valueOf() / MILLISECONDS_IN_SECOND
      : this.state.startDate.getTime() / MILLISECONDS_IN_SECOND;
    const endTimeSecs = data.endTime
      ? data.endTime.valueOf() / MILLISECONDS_IN_SECOND
      : this.state.endDate.getTime() / MILLISECONDS_IN_SECOND;
    const category = 'ANALYST_DEFINED';

    if (endTimeSecs < startTimeSecs) {
      toast.warn('Start time cannot be after end time');
      return;
    }

    const type = getKeyForEnumValue(data.type, QcMaskType)
      ? getKeyForEnumValue(data.type, QcMaskType)
      : data.type;
    const rationale = data.rationale ? data.rationale : this.state.rationale;
    const maskId = this.state.mask ? this.state.mask.id : undefined;

    const qcInput: QcMaskTypes.QcMaskInput = {
      timeRange: {
        startTimeSecs,
        endTimeSecs
      },
      category,
      rationale,
      type
    };
    ContextMenu.hide();
    this.props.applyChanges(this.props.qcMaskDialogBoxType, maskId, qcInput);
  };

  // eslint-disable-next-line class-methods-use-this
  private readonly renderMaskSwatch = (category: QcMaskCategory): JSX.Element => {
    const maskFilters: QcMaskDisplayFilters = userPreferences.colors.maskDisplayFilters;
    const filter =
      maskFilters[Object.keys(maskFilters).find(key => maskFilters[key].name === category)];
    return <div className="qc-mask-swatch" style={{ backgroundColor: filter.color }} />;
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any, class-methods-use-this
  private readonly renderMaskHistoryTable = (tableProps: any) => (
    <div className={classNames('ag-theme-dark', 'qc-mask-history-table')}>
      <div className="max">
        <Table
          columnDefs={MASK_HISTORY_COLUMN_DEFINITIONS}
          getRowId={node => node.data.id}
          rowSelection="single"
          // eslint-disable-next-line react/jsx-props-no-spreading
          {...tableProps}
        />
      </div>
    </div>
  );
}
