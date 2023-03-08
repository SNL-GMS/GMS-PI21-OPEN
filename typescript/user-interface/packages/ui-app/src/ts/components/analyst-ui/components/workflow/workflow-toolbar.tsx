import { IconNames } from '@blueprintjs/icons';
import {
  MILLISECONDS_IN_SECOND,
  secondsToString,
  splitMillisIntoTimeUnits,
  timeUnitsToString
} from '@gms/common-util';
import type { ToolbarTypes } from '@gms/ui-core-components';
import { ButtonToolbarItem, LabelValueToolbarItem, Toolbar } from '@gms/ui-core-components';
import { useAppSelector, useGetProcessingAnalystConfigurationQuery } from '@gms/ui-state';
import React, { useState } from 'react';

import { useBaseDisplaySize } from '~common-ui/components/base-display/base-display-hooks';

import { OpenAnythingDialog } from './open-anything-dialog';

export interface WorkflowToolbarProps {
  readonly onPan: (seconds: number) => void;
}

const marginForToolbarPx = 40;

const doubleLeftArrowButton = (
  keyPrefix: string | number,
  panDoubleArrow: number,
  onPan: (seconds: number) => void
): ToolbarTypes.ToolbarItemElement => (
  <ButtonToolbarItem
    key={`${keyPrefix}DoubleLeftArrowItem`}
    cyData="workflow-doubleLeftArrowItem"
    menuLabel="Back one week"
    onlyShowIcon
    icon={IconNames.DOUBLE_CHEVRON_LEFT}
    widthPx={20}
    tooltip={`Pan the workflow to the left by ${timeUnitsToString(
      splitMillisIntoTimeUnits(panDoubleArrow * MILLISECONDS_IN_SECOND)
    )} (Shift + \u2190)`}
    ianApp
    onButtonClick={() => onPan(-panDoubleArrow)}
  />
);

const singleLeftArrowButton = (
  keyPrefix: string | number,
  panSingleArrow: number,
  onPan: (seconds: number) => void
): ToolbarTypes.ToolbarItemElement => (
  <ButtonToolbarItem
    key={`${keyPrefix}SingleLeftArrowItem`}
    cyData="workflow-singleLeftArrowItem"
    menuLabel="Back one day"
    onlyShowIcon
    icon={IconNames.CHEVRON_LEFT}
    widthPx={20}
    tooltip={`Pan the workflow to the left by ${timeUnitsToString(
      splitMillisIntoTimeUnits(panSingleArrow * MILLISECONDS_IN_SECOND)
    )} (\u2190)`}
    ianApp
    onButtonClick={() => onPan(-panSingleArrow)}
  />
);

const doubleRightArrowButton = (
  keyPrefix: string | number,
  panDoubleArrow: number,
  onPan: (seconds: number) => void
): ToolbarTypes.ToolbarItemElement => (
  <ButtonToolbarItem
    key={`${keyPrefix}DoubleRightArrowItem`}
    cyData="workflow-doubleRightArrowItem"
    menuLabel="Forward one week"
    onlyShowIcon
    icon={IconNames.DOUBLE_CHEVRON_RIGHT}
    widthPx={20}
    tooltip={`Pan the workflow to the right by ${timeUnitsToString(
      splitMillisIntoTimeUnits(panDoubleArrow * MILLISECONDS_IN_SECOND)
    )} (Shift + \u2192)`}
    ianApp
    onButtonClick={() => onPan(panDoubleArrow)}
  />
);

const singleRightArrowButton = (
  keyPrefix: string | number,
  panSingleArrow: number,
  onPan: (seconds: number) => void
): ToolbarTypes.ToolbarItemElement => (
  <ButtonToolbarItem
    key={`${keyPrefix}SingleRightArrowItem`}
    cyData="workflow-singleRightArrowItem"
    menuLabel="Forward one day"
    onlyShowIcon
    icon={IconNames.CHEVRON_RIGHT}
    widthPx={20}
    tooltip={`Pan the workflow to the right by ${timeUnitsToString(
      splitMillisIntoTimeUnits(panSingleArrow * MILLISECONDS_IN_SECOND)
    )} (\u2192)`}
    ianApp
    onButtonClick={() => onPan(panSingleArrow)}
  />
);

const openAnythingButton = (
  keyPrefix: string | number,
  showOpenAnythingDialog: () => void
): ToolbarTypes.ToolbarItemElement => (
  <ButtonToolbarItem
    key={`${keyPrefix}OpenAnythingButtonItem`}
    labelRight="Open anything..."
    menuLabel="Open anything"
    cyData="workflow-openAnythingButtonItem"
    onlyShowIcon={false}
    icon={IconNames.SEARCH_TEMPLATE}
    widthPx={160}
    tooltip="Open anything"
    ianApp
    onButtonClick={showOpenAnythingDialog}
  />
);

const openTimeRangeItem = (timeRangeStr: string | JSX.Element): ToolbarTypes.ToolbarItemElement => (
  <LabelValueToolbarItem
    key="wftri"
    label="Open time range"
    tooltip="The opened time range"
    ianApp
    tooltipForIssue={undefined}
    hasIssue={false}
    widthPx={400}
    labelValue={timeRangeStr}
  />
);

const processingStageItem = (openIntervalNameStr: string): ToolbarTypes.ToolbarItemElement => (
  <LabelValueToolbarItem
    key="wfpsi"
    label="Processing stage"
    tooltip="The opened processing stage"
    ianApp
    tooltipForIssue={undefined}
    hasIssue={false}
    widthPx={400}
    style={{ marginLeft: '8em' }}
    labelValue={openIntervalNameStr}
  />
);

function WorkflowToolbarComponent(props: WorkflowToolbarProps) {
  const { onPan } = props;

  const [widthPx] = useBaseDisplaySize();

  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
  const startTimeSecs = useAppSelector(state => state.app.workflow.timeRange.startTimeSecs);
  const endTimeSecs = useAppSelector(state => state.app.workflow.timeRange.endTimeSecs);

  const processingAnalystConfigurationQuery = useGetProcessingAnalystConfigurationQuery();

  const [isOpenAnythingDialogVisible, setOpenAnythingDialogVisible] = useState(false);

  const showOpenAnythingDialog = () => {
    setOpenAnythingDialogVisible(true);
  };

  const onOpenAnythingDialog = () => {
    setOpenAnythingDialogVisible(false);
  };

  const onCancelAnythingDialog = () => {
    setOpenAnythingDialogVisible(false);
  };

  const hasTimeRangeAndProcessingStage =
    startTimeSecs !== undefined &&
    startTimeSecs !== null &&
    endTimeSecs !== undefined &&
    endTimeSecs !== null &&
    openIntervalName !== undefined &&
    openIntervalName !== null;

  const timeRange = React.useMemo(
    () =>
      hasTimeRangeAndProcessingStage ? (
        <>
          <span className="monospace">{secondsToString(startTimeSecs)}</span>
          &nbsp;&nbsp;to&nbsp;&nbsp;
          <span className="monospace">{secondsToString(endTimeSecs)}</span>
        </>
      ) : (
        'N/A'
      ),
    [endTimeSecs, hasTimeRangeAndProcessingStage, startTimeSecs]
  );

  const openIntervalNameStr = hasTimeRangeAndProcessingStage ? openIntervalName : 'N/A';

  const workflowControlsButtonKey = 'workflowButtonControls';
  const panSingleArrow = processingAnalystConfigurationQuery?.data?.workflow?.panSingleArrow;
  const panDoubleArrow = processingAnalystConfigurationQuery?.data?.workflow?.panDoubleArrow;

  /*
   * Right toolbar items
   */
  const rightToolbarItemDefs: ToolbarTypes.ToolbarItemElement[] = React.useMemo(() => {
    return [
      doubleLeftArrowButton(workflowControlsButtonKey, panDoubleArrow, onPan),
      singleLeftArrowButton(workflowControlsButtonKey, panSingleArrow, onPan),
      singleRightArrowButton(workflowControlsButtonKey, panSingleArrow, onPan),
      doubleRightArrowButton(workflowControlsButtonKey, panDoubleArrow, onPan),
      openAnythingButton(workflowControlsButtonKey, showOpenAnythingDialog)
    ];
  }, [onPan, panDoubleArrow, panSingleArrow]);

  /*
   * Left toolbar items
   */
  const leftToolbarItemDefs: ToolbarTypes.ToolbarItemElement[] = React.useMemo(() => {
    return [openTimeRangeItem(timeRange), processingStageItem(openIntervalNameStr)];
  }, [openIntervalNameStr, timeRange]);

  return (
    <>
      <Toolbar
        toolbarWidthPx={widthPx - marginForToolbarPx}
        itemsRight={rightToolbarItemDefs}
        minWhiteSpacePx={1}
        itemsLeft={leftToolbarItemDefs}
        parentContainerPaddingPx={0}
      />
      <OpenAnythingDialog
        isVisible={isOpenAnythingDialogVisible}
        onOpen={onOpenAnythingDialog}
        onCancel={onCancelAnythingDialog}
      />
    </>
  );
}

export const WorkflowToolbar: React.FunctionComponent<WorkflowToolbarProps> = React.memo(
  WorkflowToolbarComponent
);
