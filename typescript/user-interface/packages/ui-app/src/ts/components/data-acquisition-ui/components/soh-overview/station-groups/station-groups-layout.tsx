/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { SohTypes } from '@gms/common-model';
import { ResizeContainer } from '@gms/ui-core-components';
import includes from 'lodash/includes';
import sortBy from 'lodash/sortBy';
import React from 'react';

import { SohOverviewContext } from '../soh-overview-context';
import { StationGroup } from './station-group';
import type { StatusCounts } from './station-group-header';

/**
 * For each station, calculate the number of entries in each category
 * of good, marginal and bad.
 *
 * @param stationGroupName station group to get counts for
 * @param stationSoh the stations included in each stationGroup table
 * (includes both top and bottom tables in the display)
 */
export const calculateStatusCounts = (
  stationGroupName: string,
  stationSoh: SohTypes.UiStationSoh[]
): StatusCounts => {
  const notNoneCapabilityStatuses = stationSoh
    .map(
      soh =>
        soh.stationGroups.find(group => group.groupName === stationGroupName)?.sohStationCapability
    )
    .filter(status => status !== SohTypes.SohStatusSummary.NONE);
  const counts = notNoneCapabilityStatuses.reduce(
    (accum, val) => ({
      badCount:
        (val as string) === SohTypes.SohStatusSummary.BAD ? accum.badCount + 1 : accum.badCount,
      marginalCount:
        val === SohTypes.SohStatusSummary.MARGINAL ? accum.marginalCount + 1 : accum.marginalCount,
      okCount: val === SohTypes.SohStatusSummary.GOOD ? accum.okCount + 1 : accum.okCount
    }),
    {
      badCount: 0,
      marginalCount: 0,
      okCount: 0
    }
  );
  return {
    hasCapabilityRollup: notNoneCapabilityStatuses.length > 0,
    badCount: counts.badCount,
    marginalCount: counts.marginalCount,
    okCount: counts.okCount
  };
};

/**
 * * generateSohStationGroupMap
 * Generates a sohStationGroupStatus map filtered capability soh status and requires acknowledgement
 *
 * @param requiresAcknowledgement used to determined what to filter on
 * @returns map key StationGroup Name and value soh statues
 */
export const generateSohStationGroupMap = (
  requiresAcknowledgement: boolean,
  stationGroups: SohTypes.StationGroupSohStatus[],
  stationSohs: SohTypes.UiStationSoh[],
  statusesToDisplay: SohTypes.SohStatusSummary[]
): Map<string, SohTypes.UiStationSoh[]> => {
  const sohByStationGroupRowsMap = new Map<string, SohTypes.UiStationSoh[]>();

  stationGroups.forEach(stationGroup => {
    const { stationGroupName } = stationGroup;
    sohByStationGroupRowsMap.set(
      stationGroupName,
      stationSohs
        .filter(stationSoh =>
          stationSoh.stationGroups.find(sg => sg.groupName === stationGroupName)
        )
        .filter(sohStatus => sohStatus.needsAttention === requiresAcknowledgement)
        .filter(
          sohStatus =>
            requiresAcknowledgement ||
            statusesToDisplay.find(
              status =>
                status ===
                sohStatus.stationGroups.find(group => group.groupName === stationGroupName)
                  ?.sohStationCapability
            ) !== undefined
        )
        .sort((stationA, stationB) => stationA.id.localeCompare(stationB.id))
    );
  });

  return sohByStationGroupRowsMap;
};

/**
 * * orderSohStationGroupsByImportance
 * Using the config returning a new StationGroup Soh Status array ordered by importance
 *
 * @param sohStationGroups StationGroupSohStatus to order
 * @returns an ordered StationGroupSohStatus[]
 */
export const orderSohStationGroupsByImportance = (
  sohStationGroups: SohTypes.StationGroupSohStatus[]
): SohTypes.StationGroupSohStatus[] =>
  sortBy<SohTypes.StationGroupSohStatus>(sohStationGroups, 'priority');

/**
 * Props for station groups layout
 */
export interface StationGroupsLayoutProps {
  statusesToDisplay: SohTypes.SohStatusSummary[];
  stationGroupsToDisplay: string[];
  isHighlighted: boolean;
}

/**
 * * StationGroupsLayout
 * Creates the list of station groups
 */
export function StationGroupsLayout(props: StationGroupsLayoutProps) {
  const context = React.useContext(SohOverviewContext);
  const [groupHeight, setGroupHeight] = React.useState(0);
  const [topContainerHeight, setTopContainerHeight] = React.useState(0);
  const requiresAcknowledgement = true;
  const sohNeedsAttention = generateSohStationGroupMap(
    requiresAcknowledgement,
    context.stationGroupSoh,
    context.stationSoh,
    props.statusesToDisplay
  );

  const sohStations = generateSohStationGroupMap(
    !requiresAcknowledgement,
    context.stationGroupSoh,
    context.stationSoh,
    props.statusesToDisplay
  );

  const stationGroupSohStatusesByImportance = orderSohStationGroupsByImportance(
    context.stationGroupSoh
  );

  const tables = stationGroupSohStatusesByImportance
    .filter(stationGroup => includes(props.stationGroupsToDisplay, stationGroup.stationGroupName))
    .map((stationGroupSohStatus: SohTypes.StationGroupSohStatus) => {
      const stationSohForGroup = context.stationSoh.filter(st =>
        st.stationGroups.find(sg => sg.groupName === stationGroupSohStatus.stationGroupName)
      );
      return (
        <StationGroup
          key={stationGroupSohStatus.id}
          stationGroupName={stationGroupSohStatus.stationGroupName}
          statusCounts={calculateStatusCounts(
            stationGroupSohStatus.stationGroupName,
            stationSohForGroup
          )}
          totalStationCount={stationSohForGroup.length}
          sohStatuses={sohStations.get(stationGroupSohStatus.stationGroupName)}
          needsAttentionStatuses={sohNeedsAttention.get(stationGroupSohStatus.stationGroupName)}
          isHighlighted={props.isHighlighted}
          topContainerHeight={topContainerHeight}
          setTopContainerHeight={setTopContainerHeight}
          // groupHeight is used to trigger an update. Even if it is unused within the component,
          // it is necessary for a responsive update of sibling groups.
          groupHeight={groupHeight}
          setGroupHeight={setGroupHeight}
          selectedStationIds={context.selectedStationIds}
          setSelectedStationIds={ids => context.setSelectedStationIds(ids)}
        />
      );
    });
  return (
    <ResizeContainer
      className={`
        quadra-grid-container
        display__scroll-container--vertical
        drop-zone__wrapper`}
      data-cy="soh-overview-good-or-acknowledged"
    >
      {tables}
    </ResizeContainer>
  );
}
