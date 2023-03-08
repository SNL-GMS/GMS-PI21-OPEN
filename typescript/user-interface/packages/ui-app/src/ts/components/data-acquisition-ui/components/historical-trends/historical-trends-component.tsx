import { SohTypes } from '@gms/common-model';
import type { ValueType } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import { WithNonIdealStates } from '@gms/ui-core-components';
import type { SohConfigurationQueryProps, SohStatus } from '@gms/ui-state';
import { useAppSelector, useGetSohConfigurationQuery } from '@gms/ui-state';
import React from 'react';

import { BaseDisplay } from '~components/common-ui/components/base-display';
import { CommonNonIdealStateDefs } from '~components/common-ui/components/non-ideal-states';
import { DataAcquisitionNonIdealStateDefs } from '~components/data-acquisition-ui/shared/non-ideal-states';

import { HistoricalTrendsHistoryPanel } from './historical-trends-panel';

type HistoricalTrendsComponentProps = SohConfigurationQueryProps & {
  glContainer: GoldenLayout.Container;
  valueType: ValueType;
  monitorType: SohTypes.SohMonitorType;
  displaySubtitle: string;
  selectedStationIds: string[];
  sohStatus: SohStatus;
};

export function HistoricalTrendsComponent(props: HistoricalTrendsComponentProps) {
  const {
    glContainer,
    monitorType,
    sohStatus,
    sohConfigurationQuery,
    valueType,
    displaySubtitle,
    selectedStationIds
  } = props;

  /**
   * Returns the selected station
   */
  const getStation = (): SohTypes.UiStationSoh =>
    sohStatus?.stationAndStationGroupSoh?.stationSoh?.find(
      s => s.stationName === selectedStationIds[0]
    );

  return (
    <BaseDisplay
      glContainer={glContainer}
      className="history-display top-level-container scroll-box scroll-box--y"
    >
      <HistoricalTrendsHistoryPanel
        monitorType={SohTypes.SohMonitorType[monitorType]}
        station={getStation()}
        sohStatus={sohStatus}
        sohHistoricalDurations={sohConfigurationQuery.data.sohHistoricalTimesMs}
        valueType={valueType}
        displaySubtitle={displaySubtitle}
        glContainer={glContainer}
      />
    </BaseDisplay>
  );
}

/**
 * Renders the Timeliness History display, or a non-ideal state from the provided list of
 * non ideal state definitions
 */
const HistoricalTrendsComponentOrNonIdealState = WithNonIdealStates<HistoricalTrendsComponentProps>(
  [
    ...CommonNonIdealStateDefs.baseNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.generalSohNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.stationSelectedSohNonIdealStateDefinitions
  ],
  HistoricalTrendsComponent
);

/**
 * State of health historical trends component display.
 * Composes together the various charts into the a display.
 */
export const buildHistoricalTrendsComponent = (
  type: SohTypes.SohMonitorType,
  valueType: ValueType,
  displaySubtitle: string
  // eslint-disable-next-line react/display-name
): React.FunctionComponent<{ glContainer: GoldenLayout.Container }> =>
  // eslint-disable-next-line func-names, react/display-name
  function (props: { glContainer: GoldenLayout.Container }) {
    const { glContainer } = props;
    const sohStatus = useAppSelector(state => state.app.dataAcquisition.data.sohStatus);
    const selectedStationIds = useAppSelector(state => state.app.common.selectedStationIds);
    const sohConfigurationQuery = useGetSohConfigurationQuery();

    return (
      <HistoricalTrendsComponentOrNonIdealState
        glContainer={glContainer}
        valueType={valueType}
        monitorType={type}
        displaySubtitle={displaySubtitle}
        selectedStationIds={selectedStationIds}
        sohStatus={sohStatus}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
  };
