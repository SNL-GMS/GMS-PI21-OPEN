import type {
  ChannelTypes,
  EventTypes,
  SignalDetectionTypes,
  StationTypes
} from '@gms/common-model';
import type { PhaseType } from '@gms/common-model/lib/common/types';
import { findPreferredEventHypothesis } from '@gms/common-model/lib/event';
import type { CheckboxSearchListTypes } from '@gms/ui-core-components';
import type {
  AnalystWaveformTypes,
  EventsFetchResult,
  PredictFeaturesForEventLocationQuery,
  ReceiverCollection,
  SignalDetectionFetchResult,
  StationQuery
} from '@gms/ui-state';
import {
  AnalystWaveformUtil,
  useAppDispatch,
  useAppSelector,
  useEffectiveTime,
  useGetAllStationsQuery,
  useGetChannelsByNamesQuery,
  useGetProcessingAnalystConfigurationQuery,
  usePredictFeaturesForEventLocationQuery,
  useStationsVisibility,
  useVisibleStations,
  useWorkflowQuery,
  useZoomInterval,
  waveformActions
} from '@gms/ui-state';
import type { WeavessTypes } from '@gms/weavess-core';
import produce from 'immer';
import merge from 'lodash/merge';
import memoize from 'nano-memoize';
import * as React from 'react';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';

import { useStationData } from '../map/ian-map-hooks';
import { getStationNameFromChannel } from './utils';

export const MAX_FEATURE_PREDICTION_REQUEST = 500;

/**
 * Takes checkbox items and a station visibility map and returns a function that can update redux when a checkbox is clicked
 *
 * @param checkboxItemsList list of check boxed items
 * @param stationsVisibility station visibility map from redux
 * @returns a function for changing stationsVisibility for on clicking a checkbox on station dropdown
 */
export const useStationsVisibilityFromCheckboxState = (
  checkboxItemsList: CheckboxSearchListTypes.CheckboxItem[]
): ((
  getUpdatedCheckboxItemsList: (
    previousList: CheckboxSearchListTypes.CheckboxItem[]
  ) => CheckboxSearchListTypes.CheckboxItem[]
) => void) => {
  const { stationsVisibility } = useStationsVisibility();
  const dispatch = useAppDispatch();

  return React.useCallback(
    (
      getUpdatedCheckboxItemsList: (
        previousList: CheckboxSearchListTypes.CheckboxItem[]
      ) => CheckboxSearchListTypes.CheckboxItem[]
    ) => {
      const updatedCheckboxItemsList = getUpdatedCheckboxItemsList(checkboxItemsList);
      const newStationsVisibility = produce(stationsVisibility, draft =>
        updatedCheckboxItemsList
          // filter to the checkbox items that we changed
          .filter(checkBoxItem => {
            const previousVersionCheckBoxItem = checkboxItemsList.find(
              item => item.name === checkBoxItem.name
            );
            if (previousVersionCheckBoxItem.checked !== checkBoxItem.checked) {
              return true;
            }
            return false;
          })
          .forEach(checkBoxItem => {
            const stationVisibilityObject: AnalystWaveformTypes.StationVisibilityChanges =
              draft[checkBoxItem.name] ??
              AnalystWaveformUtil.newStationVisibilityChangesObject(
                checkBoxItem.name,
                checkBoxItem.checked
              );
            stationVisibilityObject.visibility = checkBoxItem.checked;
            draft[checkBoxItem.name] = stationVisibilityObject;
          })
      );
      dispatch(waveformActions.setStationsVisibility(newStationsVisibility));
    },
    [checkboxItemsList, dispatch, stationsVisibility]
  );
};

/**
 * If a current interval is not open, then this will query for 'nowish.' Otherwise, query for the
 * effective time from the current interval.
 *
 * @returns the list of all station definitions from the query for all stations
 */
export const useWaveformStations = (): StationQuery =>
  // We should hit a non-ideal state because there is no current interval if we fetch 'nowish'.
  // This prevents us from caching stations effectiveAt a time of `null` (1970),
  // which would just use memory for no reason. Querying for the same effective time as other
  // displays, however, will result in a cache hit.
  useGetAllStationsQuery(useEffectiveTime());

/**
 * Gets raw channel names from signal detection query result
 *
 * @param signalDetectionResult
 */
export const useRawChannelNamesForFeaturePredictions = (
  signalDetectionResult: SignalDetectionFetchResult
): string[] => {
  const sdStationNames: string[] = React.useMemo(() => {
    if (signalDetectionResult.fulfilled && signalDetectionResult.data != null) {
      const featureMeasurements: SignalDetectionTypes.FeatureMeasurement[][] = [];
      signalDetectionResult.data.forEach(sd => {
        featureMeasurements.push(sd.signalDetectionHypotheses[0]?.featureMeasurements);
      });
      const derivedChannelNames = Array.from(
        new Set(
          featureMeasurements.map(fm => SignalDetectionUtils.findFeatureMeasurementChannelName(fm))
        )
      );
      return derivedChannelNames.map(channelName =>
        channelName.substring(0, channelName.indexOf('.'))
      );
    }
    return [];
  }, [signalDetectionResult.data, signalDetectionResult.fulfilled]);

  const stations = useVisibleStations();
  return React.useMemo(() => {
    if (stations && sdStationNames != null) {
      const tempChannels = stations.flatMap(station => {
        return station.allRawChannels;
      });
      const tempChannelNames = [];
      if (tempChannels.length !== 0 && tempChannels != null) {
        tempChannels.forEach(tempChannel => {
          sdStationNames.forEach(stationName => {
            if (getStationNameFromChannel(tempChannel) === stationName) {
              tempChannelNames.push(tempChannel.name);
            }
          });
        });
      }
      return tempChannelNames;
    }
    return [];
  }, [sdStationNames, stations]);
};
/**
 * Build the receiver param for the usePredictFeaturesForEventLocationQuery call
 *
 * @param channels channels to build receivers for
 * @param stations stations to build receivers for
 * @returns ReceiverCollection[] an array of ReceiverCollections each one has a max size of 500 receivers
 */
export const buildReceiversForFeaturePredictionQuery = (
  channels: ChannelTypes.Channel[],
  stations: StationTypes.Station[]
): ReceiverCollection[] => {
  const receivers: ReceiverCollection[] = [];
  if (channels) {
    const channelBandTypes = channels.map(channel => channel.channelBandType);
    const receiverMap = new Map<string, ChannelTypes.Channel[]>(
      channelBandTypes.map(channelBandType => [channelBandType, []])
    );
    channels.forEach(channel => receiverMap.get(channel.channelBandType).push(channel));

    receiverMap.forEach((channelArray, channelBandType) => {
      if (channelArray.length < MAX_FEATURE_PREDICTION_REQUEST) {
        receivers.push({
          receiverBandType: channelBandType,
          receiverDataType: null,
          receiverLocationsByName: Object.fromEntries(
            channelArray.map(channel => [channel.name, channel.location] || [])
          )
        });
      } else {
        for (let i = 0; i < channelArray.length; i += MAX_FEATURE_PREDICTION_REQUEST) {
          receivers.push({
            receiverBandType: channelBandType,
            receiverDataType: null,
            receiverLocationsByName: Object.fromEntries(
              channelArray
                .slice(i, i + MAX_FEATURE_PREDICTION_REQUEST)
                .map(channel => [channel.name, channel.location] || [])
            )
          });
        }
      }
    });
  }

  if (stations) {
    receivers.push({
      receiverBandType: null,
      receiverDataType: null,
      receiverLocationsByName: Object.fromEntries(
        stations.map(station => [station.name, station.location] || [])
      )
    });
  }

  return receivers;
};

/**
 * Given two feature prediction queries, merges their data objects and returns the result
 *
 * @param priorityQuery
 * @param defaultQuery
 */
export const combineFeaturePredictionQueries = (
  priorityQuery: PredictFeaturesForEventLocationQuery,
  defaultQuery: PredictFeaturesForEventLocationQuery
): PredictFeaturesForEventLocationQuery => {
  if (priorityQuery.isFetching || priorityQuery.isLoading || priorityQuery.isError) {
    return produce(priorityQuery, draft => {
      // if the query is loading or fetching or errored return an undefined data object to prevent loading old data
      draft.data = undefined;
    });
  }
  if (defaultQuery.data && defaultQuery.isSuccess) {
    return produce(priorityQuery, draft => {
      merge(draft.data.receiverLocationsByName, defaultQuery.data.receiverLocationsByName);
      draft.data.isRequestingDefault = true;
    });
  }
  if (priorityQuery.data && (defaultQuery.isFetching || defaultQuery.isLoading)) {
    // if we are requesting default data but it is still loading still set the flag
    return produce(priorityQuery, draft => {
      draft.data.isRequestingDefault = true;
    });
  }
  return priorityQuery;
};

const memoizedCombineFeaturePredictionQueries = memoize(combineFeaturePredictionQueries);

/**
 * Subscribe to the feature prediction for location solution query, for use in the waveform component.
 *
 * @param eventResult The result of the latest event query
 * @param currentOpenEventId The currently open event ID for use in creating the feature prediction query
 * @returns the result of the predict features for location solution query, from redux
 */
export const useFeaturePredictionQueryByLocationForWaveformDisplay = (
  eventResult: EventsFetchResult,
  currentOpenEventId: string,
  phaseToAlignOn?: PhaseType
): PredictFeaturesForEventLocationQuery => {
  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
  const workflowQuery = useWorkflowQuery();
  const processingAnalystConfiguration = useGetProcessingAnalystConfigurationQuery();
  const effectiveTime = useEffectiveTime();
  const stageNames = React.useMemo(
    () => (workflowQuery.isSuccess ? workflowQuery.data?.stages.map(stage => stage.name) : []),
    [workflowQuery.isSuccess, workflowQuery.data?.stages]
  );

  const stations = useStationData();
  const rawChannelNames =
    stations?.flatMap(station => station.allRawChannels).map(channel => channel.name) || [];

  const locationSolutionForOpenEvent = React.useMemo(() => {
    let solution: EventTypes.LocationSolution;

    if (eventResult.data) {
      const openEvent = eventResult.data.find(event => event.id === currentOpenEventId);
      if (openEvent) {
        const eventHypothesis = findPreferredEventHypothesis(
          openEvent,
          openIntervalName,
          stageNames
        );
        const locationSolution = eventHypothesis.locationSolutions.find(
          ls => ls.id === eventHypothesis.preferredLocationSolution.id
        );
        solution = locationSolution;
      }
    }
    return solution;
  }, [currentOpenEventId, eventResult.data, openIntervalName, stageNames]);

  const channelResult = useGetChannelsByNamesQuery({
    channelNames: rawChannelNames || [],
    effectiveTime
  });

  const receivers = React.useMemo(
    () => buildReceiversForFeaturePredictionQuery(channelResult.data, stations),
    [channelResult.data, stations]
  );

  const priorityPhases = processingAnalystConfiguration.data?.priorityPhases;

  const defaultQuery = usePredictFeaturesForEventLocationQuery({
    sourceLocation: locationSolutionForOpenEvent?.location ?? undefined,
    receivers,
    phases: priorityPhases && !priorityPhases.includes(phaseToAlignOn) ? [phaseToAlignOn] : null
  });

  const priorityQuery = usePredictFeaturesForEventLocationQuery({
    sourceLocation: locationSolutionForOpenEvent?.location ?? undefined,
    receivers,
    phases: processingAnalystConfiguration.data?.priorityPhases
  });

  return memoizedCombineFeaturePredictionQueries(priorityQuery, defaultQuery);
};

/**
 * Creates a function to be called when weavess mounts
 * Note, this will cause update when the weavess zoom interval renders.
 *
 * @returns a function to be called when weavess mounts.
 */
export const useOnWeavessMount = (): ((weavess: WeavessTypes.WeavessInstance) => void) => {
  const [zoomInterval] = useZoomInterval();
  // To avoid capturing the value, we store it in a ref so we
  // can pass down a referentially stable function
  // that does not cause renders when zoomInterval changes
  // which, in this case, we don't want, since we're just
  // trying to provide a function that is called once, when
  // WEAVESS mounts
  const zoomIntervalRef = React.useRef(zoomInterval);
  zoomIntervalRef.current = zoomInterval;
  return React.useCallback((weavess: WeavessTypes.WeavessInstance) => {
    if (weavess?.waveformPanelRef) {
      weavess?.waveformPanelRef.zoomToTimeWindow(zoomIntervalRef.current);
    }
  }, []);
};
