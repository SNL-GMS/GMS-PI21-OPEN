import { CheckboxDropdownToolbarItem, Toolbar } from '@gms/ui-core-components';
import type { AppDispatch } from '@gms/ui-state';
import {
  DisplayedSignalDetectionConfigurationEnum,
  SignalDetectionColumn,
  signalDetectionsActions,
  useAppDispatch,
  useAppSelector
} from '@gms/ui-state';
import Immutable from 'immutable';
import React from 'react';

import type { SignalDetectionsToolbarProps } from '~analyst-ui/components/signal-detections/types';
import {
  signalDetectionColumnDisplayStrings,
  signalDetectionSyncDisplayStrings,
  signalDetectionSyncLabelStrings,
  signalDetectionSyncRenderDividers
} from '~analyst-ui/components/signal-detections/types';
import { useBaseDisplaySize } from '~common-ui/components/base-display/base-display-hooks';

const BASE_DISPLAY_PADDING = 40;

export const onChangeHandler = (reduxDispatch: AppDispatch) => (event: any): void => {
  const eventObjectString = JSON.stringify(event);
  reduxDispatch(
    signalDetectionsActions.updateDisplayedSignalDetectionConfiguration(
      JSON.parse(eventObjectString)
    )
  );
};

// eslint-disable-next-line react/function-component-definition
export const SignalDetectionsToolbar: React.FunctionComponent<SignalDetectionsToolbarProps> = (
  props: SignalDetectionsToolbarProps
) => {
  const [displayWidthPx] = useBaseDisplaySize();
  const dispatch = useAppDispatch();
  const displayedSignalDetectionConfigurationObject = useAppSelector(
    state => state.app.signalDetections.displayedSignalDetectionConfiguration
  );

  // Map of values to pass to the checkbox dropdown toolbar item
  const displayedSignalDetectionConfigurationMap: Immutable.Map<
    DisplayedSignalDetectionConfigurationEnum,
    boolean
  > = React.useMemo(() => Immutable.fromJS(displayedSignalDetectionConfigurationObject), [
    displayedSignalDetectionConfigurationObject
  ]);

  const { selectedSDColumnsToDisplay, setSelectedSDColumnsToDisplay } = props;

  const itemsLeft: JSX.Element[] = React.useMemo(
    () => [
      <CheckboxDropdownToolbarItem
        key="SDdetections"
        label="Show detections"
        menuLabel="Show detections"
        tooltip="Display signal detections within the queried workflow time range."
        cyData="sd-table-detections-toggle"
        values={displayedSignalDetectionConfigurationMap}
        enumOfKeys={DisplayedSignalDetectionConfigurationEnum}
        enumKeysToDisplayStrings={signalDetectionSyncDisplayStrings}
        enumKeysToRenderDividers={signalDetectionSyncRenderDividers}
        enumKeysToLabelStrings={signalDetectionSyncLabelStrings}
        onChange={onChangeHandler(dispatch)}
      />,
      <CheckboxDropdownToolbarItem
        key="SDcolumns"
        label="Show columns"
        menuLabel="Show columns"
        tooltip="Select columns to be shown in the signal detection table below"
        onChange={setSelectedSDColumnsToDisplay}
        cyData="sd-table-column-picker"
        values={selectedSDColumnsToDisplay}
        enumOfKeys={SignalDetectionColumn}
        enumKeysToDisplayStrings={signalDetectionColumnDisplayStrings}
        itemSide="LEFT"
      />
    ],
    [
      dispatch,
      displayedSignalDetectionConfigurationMap,
      selectedSDColumnsToDisplay,
      setSelectedSDColumnsToDisplay
    ]
  );

  return (
    <Toolbar
      toolbarWidthPx={displayWidthPx}
      parentContainerPaddingPx={BASE_DISPLAY_PADDING}
      itemsLeft={itemsLeft}
    />
  );
};
