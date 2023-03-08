import type { ChannelTypes, StationTypes } from '@gms/common-model';
import { toEpochSeconds } from '@gms/common-util';
import type { AppDispatch } from '@gms/ui-state';
import {
  stationPropertiesConfigurationActions,
  useAppDispatch,
  useAppSelector,
  useGetStationsWithChannelsQuery,
  useOldQueryDataIfReloading
} from '@gms/ui-state';
import Immutable from 'immutable';
import React, { useCallback, useState } from 'react';

import { convertMapToObject } from '~common-ui/common/table-utils';

import { StationPropertiesTables } from './station-properties-tables';
import { StationPropertiesToolbar } from './station-properties-toolbar';
import type { SiteConfigurationRowClickedEvent, StationPropertiesPanelProps } from './types';
import { ChannelColumn, SiteColumn } from './types';

function convertObjectToChannelColumnMap(
  columnArguments: Record<string, boolean>
): Immutable.Map<ChannelColumn, boolean> {
  const notableValues = [...Object.keys(columnArguments)];
  return Immutable.Map<ChannelColumn, boolean>([
    ...Object.values(ChannelColumn)
      .filter(v => notableValues.includes(v))
      .map<[ChannelColumn, boolean]>(v => [v, columnArguments[v]])
  ]);
}

function convertObjectToSiteColumnMap(
  columnArguments: Record<string, boolean>
): Immutable.Map<SiteColumn, boolean> {
  const notableValues = [...Object.keys(columnArguments)];
  return Immutable.Map<SiteColumn, boolean>([
    ...Object.values(SiteColumn)
      .filter(v => notableValues.includes(v))
      .map<[SiteColumn, boolean]>(v => [v, columnArguments[v]])
  ]);
}

export const getOnEffectiveTimeChange = (
  dispatch: AppDispatch,
  newEffectiveAt: string,
  effectiveAtTimes: string[]
): void => {
  dispatch(
    stationPropertiesConfigurationActions.setSelectedEffectiveAt(
      effectiveAtTimes.indexOf(newEffectiveAt)
    )
  );
};

// eslint-disable-next-line react/function-component-definition
export const StationPropertiesPanel: React.FunctionComponent<StationPropertiesPanelProps> = ({
  selectedStation,
  effectiveAtTimes
}: StationPropertiesPanelProps) => {
  const dispatch = useAppDispatch();
  const [selectedChannelGroup, setSelectedChannelGroup] = useState(null);

  const selectedEffectiveAtIndex: number = useAppSelector(
    state => state.app.stationPropertiesConfiguration.selectedEffectiveAtIndex
  );

  // Site Columns
  const selectedSiteColumnsToDisplayObject = useAppSelector(
    state => state.app.stationPropertiesConfiguration.channelGroupConfigurationColumns
  );

  // Memoize
  const selectedSiteColumnsToDisplay = React.useMemo(
    () => convertObjectToSiteColumnMap(selectedSiteColumnsToDisplayObject),
    [selectedSiteColumnsToDisplayObject]
  );

  // Memoize
  const setSelectedSiteColumnsToDisplay = React.useCallback(
    (cols: Immutable.Map<SiteColumn, boolean>) =>
      dispatch(
        stationPropertiesConfigurationActions.updateChannelGroupConfigurationColumns(
          convertMapToObject(cols)
        )
      ),
    [dispatch]
  );

  // Channel Columns
  const selectedChannelColumnsToDisplayObject = useAppSelector(
    state => state.app.stationPropertiesConfiguration.channelConfigurationColumns
  );

  // Memoize
  const selectedChannelColumnsToDisplay = React.useMemo(
    () => convertObjectToChannelColumnMap(selectedChannelColumnsToDisplayObject),
    [selectedChannelColumnsToDisplayObject]
  );

  // Memoize
  const setSelectedChannelColumnsToDisplay = React.useCallback(
    (cols: Immutable.Map<ChannelColumn, boolean>) =>
      dispatch(
        stationPropertiesConfigurationActions.updateChannelConfigurationColumns(
          convertMapToObject(cols)
        )
      ),
    [dispatch]
  );

  const stationResult = useGetStationsWithChannelsQuery({
    stationNames: [selectedStation],
    effectiveTime: toEpochSeconds(effectiveAtTimes[selectedEffectiveAtIndex])
  });

  const stationDataArray = useOldQueryDataIfReloading<StationTypes.Station[]>(stationResult);

  let stationData: StationTypes.Station;

  if (stationResult && stationDataArray)
    stationData = stationDataArray.find(stn => stn.name === selectedStation);

  const chanGroup: ChannelTypes.ChannelGroup = stationData
    ? stationData.channelGroups.find(group => group.name === selectedChannelGroup)
    : undefined;
  // make sure we got a channelGroup
  const channels = chanGroup?.channels;

  const onChannelGroupRowSelection = useCallback((event: SiteConfigurationRowClickedEvent) => {
    setSelectedChannelGroup(event.node.data.name);
  }, []);

  const onEffectiveTimeChange = React.useCallback(
    (newEffectiveAt: string) =>
      getOnEffectiveTimeChange(dispatch, newEffectiveAt, effectiveAtTimes),
    [dispatch, effectiveAtTimes]
  );

  React.useEffect(() => {
    if (chanGroup === undefined) {
      setSelectedChannelGroup(null);
    }
  }, [chanGroup]);

  React.useEffect(() => {
    dispatch(stationPropertiesConfigurationActions.setSelectedEffectiveAt(0));
  }, [dispatch, selectedStation]);

  return (
    <div className="station-properties-panel">
      <StationPropertiesToolbar
        selectedStation={stationData}
        stationName={selectedStation}
        selectedEffectiveAt={effectiveAtTimes[selectedEffectiveAtIndex]}
        effectiveAtTimes={effectiveAtTimes}
        channelColumnsToDisplay={selectedChannelColumnsToDisplay}
        siteColumnsToDisplay={selectedSiteColumnsToDisplay}
        onEffectiveTimeChange={onEffectiveTimeChange}
        setSelectedSiteColumnsToDisplay={setSelectedSiteColumnsToDisplay}
        setSelectedChannelColumnsToDisplay={setSelectedChannelColumnsToDisplay}
      />
      <StationPropertiesTables
        stationData={stationData}
        onChannelGroupRowSelection={onChannelGroupRowSelection}
        selectedSiteColumnsToDisplay={selectedSiteColumnsToDisplay}
        selectedChannelGroup={selectedChannelGroup}
        channels={channels}
        selectedChannelColumnsToDisplay={selectedChannelColumnsToDisplay}
      />
    </div>
  );
};
