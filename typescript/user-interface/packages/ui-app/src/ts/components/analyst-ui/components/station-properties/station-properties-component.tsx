import type { CommonTypes } from '@gms/common-model';
import { IanDisplays } from '@gms/common-model/lib/displays/types';
import type GoldenLayout from '@gms/golden-layout';
import { WithNonIdealStates } from '@gms/ui-core-components';
import {
  useAppSelector,
  useGetStationsEffectiveAtTimesQuery,
  useOperationalTimePeriodConfiguration
} from '@gms/ui-state';
import React from 'react';

import { BaseDisplay } from '~common-ui/components/base-display';

import {
  nonIdealStateEmptyEffectiveAtsQuery,
  nonIdealStateLoadingEffectiveAtsQuery,
  nonIdealStateNoOperationalTimePeriod,
  nonIdealStateSelectAStation,
  nonIdealStateTooManyStationsSelected
} from './station-properties-non-ideal-states';
import { StationPropertiesPanel } from './station-properties-panel';
import type { StationPropertiesComponentProps, StationPropertiesPanelProps } from './types';

type StationPropertiesPanelOrNonIdealStateProps = StationPropertiesPanelProps & {
  readonly glContainer?: GoldenLayout.Container;
  operationalTimeRange: CommonTypes.TimeRange;
  selectedStations: string[];
};

export const StationPropertiesPanelOrNonIdealState = WithNonIdealStates<
  StationPropertiesPanelOrNonIdealStateProps
>(
  [
    {
      condition: (props: StationPropertiesPanelOrNonIdealStateProps): boolean => {
        return props.selectedStations.length < 1;
      },
      element: nonIdealStateSelectAStation
    },
    {
      condition: (props: StationPropertiesPanelOrNonIdealStateProps): boolean => {
        return props.selectedStations.length > 1;
      },
      element: nonIdealStateTooManyStationsSelected
    },
    {
      condition: (props: StationPropertiesPanelOrNonIdealStateProps): boolean => {
        return !props.operationalTimeRange.startTimeSecs || !props.operationalTimeRange.endTimeSecs;
      },
      element: nonIdealStateNoOperationalTimePeriod
    },
    {
      condition: (props: StationPropertiesPanelOrNonIdealStateProps): boolean => {
        return !props.effectiveAtTimes;
      },
      element: nonIdealStateLoadingEffectiveAtsQuery
    },
    {
      condition: (props: StationPropertiesPanelOrNonIdealStateProps): boolean => {
        return props.effectiveAtTimes.length <= 0;
      },
      element: nonIdealStateEmptyEffectiveAtsQuery
    }
  ],
  StationPropertiesPanel
);

// eslint-disable-next-line react/function-component-definition
export const StationPropertiesComponent: React.FunctionComponent<StationPropertiesComponentProps> = (
  props: StationPropertiesComponentProps
) => {
  const { glContainer } = props;

  const unfilteredSelectedStations = useAppSelector(state => state.app.common?.selectedStationIds);
  const { timeRange } = useOperationalTimePeriodConfiguration();

  const operationalTimeRange: CommonTypes.TimeRange = {
    startTimeSecs: timeRange?.startTimeSecs,
    endTimeSecs: timeRange?.endTimeSecs
  };

  // TODO Instead of filtering on a string we need to update the selected station IDs to be objects with a type field
  const selectedStations = unfilteredSelectedStations.filter((item: string) => !item.includes('.'));
  const effectiveAtTimes: string[] = useGetStationsEffectiveAtTimesQuery({
    stationName: selectedStations[0],
    startTime: operationalTimeRange.startTimeSecs,
    endTime: operationalTimeRange.endTimeSecs
  }).data;

  return (
    <BaseDisplay
      glContainer={glContainer}
      className="station-properties-display-window"
      data-cy="station-properties-display-window"
      tabName={IanDisplays.STATION_PROPERTIES}
    >
      <StationPropertiesPanelOrNonIdealState
        selectedStation={selectedStations[0]}
        effectiveAtTimes={effectiveAtTimes}
        operationalTimeRange={operationalTimeRange}
        selectedStations={selectedStations}
      />
    </BaseDisplay>
  );
};
