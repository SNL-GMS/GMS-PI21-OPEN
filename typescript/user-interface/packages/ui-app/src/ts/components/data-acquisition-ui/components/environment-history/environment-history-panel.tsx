/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { SohTypes } from '@gms/common-model';
import { prettifyAllCapsEnumType } from '@gms/common-util';
import { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import { useGetHistoricalAceiDataQuery } from '@gms/ui-state';
import * as React from 'react';

import { useBaseDisplaySize } from '~components/common-ui/components/base-display/base-display-hooks';
import { messageConfig } from '~components/data-acquisition-ui/config/message-config';
import { DrillDownTitle } from '~components/data-acquisition-ui/shared/drill-down-components';
import { BaseToolbar } from '~components/data-acquisition-ui/shared/toolbars/base-toolbar';
import { useTrendTimeIntervalSelector } from '~components/data-acquisition-ui/shared/toolbars/trend-time-interval-hook';
import {
  convertSohMonitorTypeToAceiMonitorType,
  isAnalogAceiMonitorType
} from '~components/data-acquisition-ui/shared/utils';

import type { AceiMonitorTypeOption } from './acei-context';
import { AceiContext } from './acei-context';
import { validateNonIdealState } from './non-ideal-states';
import { WeavessDisplay } from './weavess-display';

export interface EnvironmentHistoryPanelProps {
  station: SohTypes.UiStationSoh;
  channelSohs: SohTypes.ChannelSoh[];
  sohHistoricalDurations: number[];
}

/**
 * @returns a list of all environmental issues from the SohMonitorType enum
 */
const getEnvSohMonitorTypeNames = () =>
  Object.keys(SohTypes.SohMonitorType).filter(SohTypes.isEnvironmentalIssue);

/**
 * TODO: add support for a  default in the toolbar dropdown
 *
 * @returns undefined if the option matches the default. Otherwise, return the enum value.
 */
const convertAceiMonitorTypeOptionToEnum = (option: AceiMonitorTypeOption): SohTypes.AceiType =>
  option && option !== 'CHOOSE_A_MONITOR_TYPE' ? SohTypes.AceiType[option] : undefined;

/**
 * Creates and manages state for the selector dropdown list for Env Monitor Types.
 * The dropdown  will contain all monitor types from the SohMonitorType enum that are ACEI monitors.
 * Modifies the AceiContext to update which monitor type has been selected.
 *
 * @returns an array containing exactly two items. The first is a dropdown,
 * the second is the currently selected option from the dropdown, or undefined
 * if no ACEI monitor type is selected.
 */
const useMonitorTypeDropdown = (): [DeprecatedToolbarTypes.DropdownItem, SohTypes.AceiType] => {
  const envMonitorTypes: AceiMonitorTypeOption[] = [
    'CHOOSE_A_MONITOR_TYPE', // default
    ...getEnvSohMonitorTypeNames()
      .map(convertSohMonitorTypeToAceiMonitorType)
      // TODO: remove filtering once analog types are supported
      .filter(monitorType => !isAnalogAceiMonitorType(monitorType))
  ];

  const context = React.useContext(AceiContext);

  const dropdown: DeprecatedToolbarTypes.DropdownItem = {
    label: 'Select Monitor Type',
    rank: undefined,
    tooltip: 'Select Monitor Type',
    type: DeprecatedToolbarTypes.ToolbarItemType.Dropdown,
    dropdownOptions: envMonitorTypes,
    dropdownText: envMonitorTypes.map(monitorType => prettifyAllCapsEnumType(monitorType, false)),
    value: context.selectedAceiType ? context.selectedAceiType : undefined,
    onChange: value => context.setSelectedAceiType(value),
    widthPx: 220
  };

  const aceiMonitorType = convertAceiMonitorTypeOptionToEnum(context.selectedAceiType);

  return [dropdown, aceiMonitorType];
};

/**
 * The Environment History Panel
 * Composes together the toolbar and the env history charts.
 * Performs a query when a monitor type is selected (or if it is passed down). Then renders the
 * line charts based on the result.
 * Renders a non-ideal state if the query doesn't get the required data in the result.
 */
// eslint-disable-next-line react/function-component-definition
export const EnvironmentHistoryPanel: React.FunctionComponent<EnvironmentHistoryPanelProps> = props => {
  const [startTimeMS, endTimeMs, timeIntervalSelector] = useTrendTimeIntervalSelector(
    'ACEI',
    props.sohHistoricalDurations
  );

  const [monitorTypeDropdown, selectedMonitorType] = useMonitorTypeDropdown();
  const [widthPx] = useBaseDisplaySize();
  const ONE_HUNDRED = 100;
  const queryInput: SohTypes.UiHistoricalAceiInput = {
    endTime: endTimeMs / 1000,
    startTime: startTimeMS / 1000,
    stationName: props.station.stationName,
    type: selectedMonitorType
  };

  const historicalAceiQuery = useGetHistoricalAceiDataQuery(queryInput);
  // update loading state for the button that shows this panel
  const nonIdealState = validateNonIdealState(
    selectedMonitorType,
    historicalAceiQuery.isLoading,
    startTimeMS,
    endTimeMs,
    historicalAceiQuery.data
  );
  return (
    <>
      <BaseToolbar
        widthPx={widthPx}
        itemsLeft={[monitorTypeDropdown]}
        itemsRight={[timeIntervalSelector]}
      />
      {selectedMonitorType ? (
        <DrillDownTitle
          title={props.station.stationName}
          subtitle={prettifyAllCapsEnumType(selectedMonitorType)}
          description={messageConfig.labels.decimationDescription(ONE_HUNDRED)}
        />
      ) : undefined}

      {nonIdealState || (
        <WeavessDisplay
          // eslint-disable-next-line react/jsx-props-no-spreading
          {...props}
          startTimeMs={startTimeMS}
          endTimeMs={endTimeMs}
          aceiData={historicalAceiQuery.data}
        />
      )}
    </>
  );
};
