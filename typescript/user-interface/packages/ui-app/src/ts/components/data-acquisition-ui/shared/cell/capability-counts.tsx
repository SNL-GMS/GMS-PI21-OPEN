import { SohTypes } from '@gms/common-model';
import React from 'react';

import { messageConfig } from '~components/data-acquisition-ui/config/message-config';

const nbsp = '\u00a0';

const badTooltipMsg = messageConfig.tooltipMessages.stationStatistics.topCount;
const marginalTooltipMsg = messageConfig.tooltipMessages.stationStatistics.bottomCount;

export interface CapabilityTagsProps {
  parentCapability: SohTypes.SohStatusSummary;
  stationGroups: SohTypes.StationSohCapabilityStatus[];
}

/**
 * @param stationGroups the list of visible station groups
 * @param capabilityType Are we counting bad or marginal capabilities
 * @returns the number of capabilities fount in the station group of a given capability status.
 */
const countCapabilities = (
  stationGroups: SohTypes.StationSohCapabilityStatus[],
  capabilityType: SohTypes.SohStatusSummary
) => stationGroups.filter(group => group.sohStationCapability === capabilityType).length;

/**
 * Returns the counts of how many capabilities are bad and marginal for the groups that are
 * not shown by the status color of the cell
 *
 * @param stationGroups the list of visible station groups
 * @returns [badCount, marginalCount]
 */
const getBadMarginalCapabilityCounts = (
  stationGroups: SohTypes.StationSohCapabilityStatus[]
): [number, number] => {
  const badCount = countCapabilities(stationGroups, SohTypes.SohStatusSummary.BAD);
  const marginalCount = countCapabilities(stationGroups, SohTypes.SohStatusSummary.MARGINAL);
  return [badCount, marginalCount];
};

export function CapabilityCounts({ parentCapability, stationGroups }: CapabilityTagsProps) {
  const [badCount, marginalCount] = getBadMarginalCapabilityCounts(stationGroups);
  const badThreshold = parentCapability === SohTypes.SohStatusSummary.BAD ? 1 : 0;
  const marginalThreshold = parentCapability === SohTypes.SohStatusSummary.MARGINAL ? 1 : 0;

  return (
    <div className="stacked-counts">
      <div
        className="stacked-counts__entry"
        data-capability-status={SohTypes.SohStatusSummary.BAD.toLowerCase()}
        title={badCount > badThreshold ? badTooltipMsg : undefined}
      >
        {badCount > badThreshold ? badCount : nbsp}
      </div>
      <div
        className="stacked-counts__entry"
        data-capability-status={SohTypes.SohStatusSummary.MARGINAL.toLowerCase()}
        title={marginalCount > marginalThreshold ? marginalTooltipMsg : undefined}
      >
        {marginalCount > marginalThreshold ? marginalCount : nbsp}
      </div>
    </div>
  );
}
