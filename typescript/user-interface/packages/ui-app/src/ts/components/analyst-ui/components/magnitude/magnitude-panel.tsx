/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import type { LegacyEventTypes } from '@gms/common-model';
import { HorizontalDivider } from '@gms/ui-core-components';
import React from 'react';

import { getLatestLocationSolutionSetLegacy } from '~analyst-ui/common/utils/event-util';

import { MagnitudeToolbar } from './components/magnitude-toolbar';
import { NetworkMagnitude } from './components/network-magnitude/network-magnitude-component';
import { StationMagnitude } from './components/station-magnitude/station-magnitude-component';
import { getAmplitudesByStation, getSignalDetectionMagnitudeData } from './magnitude-panel-utils';
import type { MagnitudePanelProps } from './types';

// eslint-disable-next-line react/function-component-definition
export const MagnitudePanel: React.FunctionComponent<MagnitudePanelProps> = props => {
  const topOptions = { alignedGrids: [], rowClass: 'network-magnitude-row' };
  const bottomOptions = { alignedGrids: [], rowClass: 'station-magnitude-row' };
  topOptions.alignedGrids.push(bottomOptions);
  bottomOptions.alignedGrids.push(topOptions);

  const topTableHeight = 200;
  const latestLss = getLatestLocationSolutionSetLegacy(props.currentlyOpenEvent);
  const isLatestLssSelected =
    latestLss &&
    (!props.location.selectedLocationSolutionSetId ||
      latestLss.id === props.location.selectedLocationSolutionSetId);

  const selectedLocationSolutionSet =
    props.currentlyOpenEvent && props.currentlyOpenEvent.currentEventHypothesis.eventHypothesis
      ? props.currentlyOpenEvent.currentEventHypothesis.eventHypothesis.locationSolutionSets.find(
          lss => lss.id === props.location.selectedLocationSolutionSetId
        )
      : undefined;

  const selectedLocationSolution = selectedLocationSolutionSet
    ? selectedLocationSolutionSet.locationSolutions.find(
        ls => ls.id === props.location.selectedLocationSolutionId
      )
    : undefined;

  const stationDefiningCallback = async (
    magnitudeType: LegacyEventTypes.MagnitudeType,
    stationNames: string[],
    defining: boolean
  ): Promise<[{ stationName: string; rational: string }]> => {
    if (stationNames.length >= 1) {
      const networkMagInput: LegacyEventTypes.ComputeNetworkMagnitudeInput = {
        defining,
        stationNames,
        magnitudeType,
        eventHypothesisId: props.currentlyOpenEvent.currentEventHypothesis.eventHypothesis.id,
        locationSolutionSetId: props.location.selectedLocationSolutionSetId
      };
      const variables = {
        computeNetworkMagnitudeSolutionInput: networkMagInput
      };
      await props.computeNetworkMagnitudeSolution({
        variables
      });
    }
    return undefined;
  };

  return (
    <div
      className="location-wrapper"
      data-cy="magnitude"
      tabIndex={-1}
      onMouseEnter={e => {
        e.currentTarget.focus();
      }}
    >
      <MagnitudeToolbar
        widthPx={props.widthPx}
        displayedMagnitudeTypes={props.displayedMagnitudeTypes}
        setDisplayedMagnitudeTypes={displayedMagnitudeTypes => {
          props.setDisplayedMagnitudeTypes(displayedMagnitudeTypes);
        }}
      />
      <HorizontalDivider
        topHeightPx={topTableHeight}
        top={
          <NetworkMagnitude
            options={topOptions}
            locationSolutionSet={selectedLocationSolutionSet}
            computeNetworkMagnitudeSolution={props.computeNetworkMagnitudeSolution}
            preferredSolutionId={props.location.selectedPreferredLocationSolutionId}
            selectedSolutionId={props.location.selectedLocationSolutionId}
            displayedMagnitudeTypes={props.displayedMagnitudeTypes}
            setSelectedLocationSolution={(lssId, locId) => {
              props.setSelectedLocationSolution(lssId, locId);
            }}
          />
        }
        bottom={
          <StationMagnitude
            options={bottomOptions}
            amplitudesByStation={getAmplitudesByStation(
              props.stations,
              getSignalDetectionMagnitudeData(
                props.currentlyOpenEvent,
                props.associatedSignalDetections,
                props.location.selectedLocationSolutionSetId,
                isLatestLssSelected
              ),
              props.magnitudeTypesForPhase
            )}
            checkBoxCallback={stationDefiningCallback}
            locationSolution={selectedLocationSolution}
            historicalMode={!isLatestLssSelected}
            selectedSdIds={props.selectedSdIds}
            setSelectedSdIds={sdIds => props.setSelectedSdIds(sdIds)}
            displayedMagnitudeTypes={props.displayedMagnitudeTypes}
            computeNetworkMagnitudeSolution={props.computeNetworkMagnitudeSolution}
            openEventId={props.openEventId}
          />
        }
      />
    </div>
  );
};
