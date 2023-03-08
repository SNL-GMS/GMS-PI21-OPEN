import produce, { Draft } from 'immer';
import React from 'react';
import { LoadingState, useAppContext } from '../state/state';
import { windowAPI } from '../electron-util';
import { useAppDispatch, useAppSelector } from '../state/react-redux-hooks';
import {
  ErrorRecord,
  RollupEntry,
  setChannelCapabilityRollup,
  setHasError,
  setLoadedData,
  setStationGroupCapabilityRollup,
} from '../state/station-controls-slice';
import { getChannels } from '../renderers/channel-checklist/ChannelChecklist';
import {
  checkIfRollupHasConflictedMonitors,
  updateAllChannelCapabilityMonitor,
  updateAllChannelCapabilityMonitors,
} from '../renderers/station-capability-rollup/util';
import { batch } from 'react-redux';
import { ChannelCapabilityRollup } from '../state/retrieve-channel-capability-rollups';
import { getErrorRecordKeyForChannelCapabilityMonitors } from '../state/error-state-utils';

/**
 * Returns the previous value that was passed in, and stores the current
 * value for future reference. On the next run, returns the previous value,
 * and then stores the passed in value for future reference. Etc...
 * On the first run, it returns initialValue
 *
 * @param value a value to assign for future retrieval.
 * @param initialValue a starting value
 * @returns the previous value, or the initial value on the first run
 */
export function usePrevious<T = unknown>(value: T, initialValue: T): T {
  const ref = React.useRef(initialValue);
  React.useEffect(() => {
    ref.current = value;
  }, [value]);
  return ref.current;
}

/**
 * Prints out which of the dependencies have changed, helping to debug useEffect code
 *
 * @param effectHook a function that should be executed when useEffect is called
 * @param dependencies an array of dependencies, checked for referential equality
 * @param dependencyNames an optional list of names corresponding to the dependencies with the same indices
 */
export const useEffectDebugger = (
  effectHook: () => void,
  dependencies: unknown[],
  dependencyNames: string[] = []
): void => {
  const previousDeps = usePrevious(dependencies, []);
  const { current: effectHookCallback } = React.useRef(effectHook);
  const changedDeps: any = dependencies.reduce(
    (accum: { [key: string]: unknown }, dependency, index) => {
      if (dependency !== previousDeps[index]) {
        const keyName = dependencyNames[index] || index;
        return {
          ...accum,
          [keyName]: {
            before: previousDeps[index],
            after: dependency,
          },
        };
      }
      return accum;
    },
    {}
  );

  if (Object.keys(changedDeps).length) {
    console.log(changedDeps);
  }

  React.useEffect(effectHookCallback, [effectHookCallback, ...dependencies]);
};

export const useOnce = (callback: () => void) => {
  const isFirstTimeRef = React.useRef(true);
  if (isFirstTimeRef.current) {
    callback();
    isFirstTimeRef.current = false;
  }
};

export const useLoadData = (
  loadData: () => Promise<void>,
  setLoadingState: React.Dispatch<React.SetStateAction<LoadingState>>
) => {
  useOnce(async () => {
    setLoadingState(
      (prev: LoadingState): LoadingState => ({
        numRequested: prev.numRequested + 1,
        numComplete: prev.numComplete,
      })
    );
    await loadData().catch((e) => console.error(e));
    setLoadingState(
      (prev: LoadingState): LoadingState => ({
        numRequested: prev.numRequested,
        numComplete: prev.numComplete + 1,
      })
    );
  });
};

export const useConfigFromDirectory = (
  dirName: string,
  setData: React.Dispatch<any>,
  setLoadingState: React.Dispatch<React.SetStateAction<LoadingState>>
) => {
  const loadConfig = async () => {
    const configName = dirName;
    const result = await windowAPI.electronAPI.loadConfigFromDir(configName);
    setData((data: any) => {
      return produce(data, (draft: Draft<any>) => {
        draft[configName] = result;
      });
    });
  };
  useLoadData(loadConfig, setLoadingState);
};

export function useDebouncedSetState<T>(
  setState: React.Dispatch<React.SetStateAction<T>> | ((val: any) => void),
  delay = 250
): (newState: T) => void {
  const tempStateRef = React.useRef<T>();
  const timeoutRef = React.useRef<NodeJS.Timeout>();
  return React.useCallback(
    (newState: T) => {
      tempStateRef.current = newState;
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      timeoutRef.current = setTimeout(() => {
        if (!!tempStateRef.current) {
          setState(tempStateRef.current);
          tempStateRef.current = undefined;
        }
      }, delay);
    },
    [delay, setState]
  );
}

/**
 * @returns whether a station has been selected
 */
export const useIsStationSelected = (): boolean => {
  return !!useAppSelector((state) => state.stationControls.stationName);
};

/**
 * Hook using error state to determine if error
 * @returns function to determine if monitor has error
 */
export const useMonitorHaveError = () => {
  const userInputErrorsMap = useAppSelector(
    (state) => state.stationControls.error
  );

  const determineMonitorHasError = (monitorName: string): boolean => {
    let isError = false;
    Object.keys(userInputErrorsMap).forEach((entryName) => {
      if (
        userInputErrorsMap[entryName].hasError &&
        entryName.includes(monitorName)
      ) {
        isError = true;
      }
    });
    return isError;
  };
  return [determineMonitorHasError];
};

/**
 * Hook using error state to determine if error
 * @returns function to determine if channel list has error
 */
export const useChannelListHasError = () => {
  const userInputErrorsMap = useAppSelector(
    (state) => state.stationControls.error
  );

  const determineChannelListHasError = (key: string): boolean => {
    let isError = false;
    Object.keys(userInputErrorsMap).forEach((entryName) => {
      if (userInputErrorsMap[entryName].hasError && entryName.includes(key)) {
        isError = true;
      }
    });
    return isError;
  };
  return [determineChannelListHasError];
};

/**
 * Hook to wrap redux update to error state
 * @returns updateErrorState function
 */
export const useUpdateErrorState = (): [
  (
    attributeName: string,
    hasError: boolean,
    reason: string
  ) => {
    payload: any;
    type: string;
  }
] => {
  const dispatch = useAppDispatch();
  const updateErrorState = React.useCallback(
    (attributeName: string, hasError: boolean, reason: string) =>
      dispatch(setHasError({ attributeName, errorInfo: { hasError, reason } })),
    [dispatch]
  );
  return [updateErrorState];
};

/**
 * Hook to wrap redux update to which data has loaded
 * @returns updateErrorState function
 */
export const useUpdateDataLoaded = (): [
  (
    dataName: string,
    hasLoaded: boolean
  ) => {
    payload: any;
    type: string;
  }
] => {
  const dispatch = useAppDispatch();
  const updateDataLoaded = React.useCallback(
    (dataName: string, hasLoaded: boolean) =>
      dispatch(setLoadedData({ dataName, hasLoaded })),
    [dispatch]
  );
  return [updateDataLoaded];
};

/**
 * Hook wrap redux update for station group capability rollup
 * @returns updateStationGroupCapabilityRollup
 */
export const useUpdateStationGroupCapability = (): [
  (
    stationName: string,
    groupName: string,
    newRollup: RollupEntry
  ) => {
    payload: any;
    type: string;
  }
] => {
  const dispatch = useAppDispatch();
  const updateStationGroupCapabilityRollup = React.useCallback(
    (stationName: string, groupName: string, newRollup: RollupEntry) =>
      dispatch(
        setStationGroupCapabilityRollup({
          stationName,
          groupName,
          rollup: newRollup,
        })
      ),
    [dispatch]
  );
  return [updateStationGroupCapabilityRollup];
};

/**
 * Hook wrap redux update for channel capability rollup
 * @returns updateChannelCapabilityRollup
 */
export const useUpdateChannelCapabilityRollup = (): [
  (
    stationName: string,
    groupName: string,
    channelName: string,
    newRollup: RollupEntry
  ) => {
    payload: any;
    type: string;
  }
] => {
  const dispatch = useAppDispatch();
  const updateChannelCapabilityRollup = React.useCallback(
    (
      stationName: string,
      groupName: string,
      channelName: string,
      newRollup: RollupEntry
    ) =>
      dispatch(
        setChannelCapabilityRollup({
          stationName,
          groupName,
          channelName,
          rollup: newRollup,
        })
      ),
    [dispatch]
  );
  return [updateChannelCapabilityRollup];
};

/**
 * Loops through MonitorTypesRollups to determine selected monitors
 * @returns array of selected monitor names
 */
export const useSelectedMonitors = (): string[] => {
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );

  const monitorTypesForRollup = useAppSelector(
    (state) => state.stationControls.monitorTypesForRollup
  );

  const selectedMonitors = React.useMemo(
    () =>
      monitorTypesForRollup[stationName ?? '']
        ?.filter((mt) => mt.isIncluded)
        .map((monitorType) => monitorType.name),
    [stationName, monitorTypesForRollup]
  );
  return selectedMonitors;
};

/**
 * Hook wrap redux update for monitors for all groups for all capability rollups
 * @returns updateAllMonitorsForAllCapabilityRollups
 */
export const useUpdateAllMonitorsForAllCapabilityRollups = (): [
  (monitorName: string, selectedMonitors: string[]) => void
] => {
  const { data: appData } = useAppContext();
  const dispatch = useAppDispatch();
  const [updateErrorState] = useUpdateErrorState();
  const errors: ErrorRecord[] = [];
  // Ref of errors passed down to recursive update which checks for errors and updates ref as needed
  const errorsRef = React.useRef(errors);
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const stationGroupsForStation = useAppSelector(
    (state) => state.stationControls.stationGroups[stationName ?? '']
  );
  const stationGroupNames = stationGroupsForStation?.map((group) => group.name);

  const allChannelsNames = React.useMemo(
    () => getChannels(stationName, appData.processingStationGroups),
    [appData.processingStationGroups, stationName]
  ).map((channel) => channel.name);

  const channelCapabilityRollupRecord = useAppSelector(
    (store) => store.stationControls.channelCapabilityRollup[stationName ?? '']
  );
  const updateAllMonitorsForAllCapabilityRollups = React.useCallback(
    (monitorName: string, selectedMonitors: string[]) =>
      batch(() => {
        stationGroupNames.forEach((groupName) => {
          let doesGroupHaveConflicts = false;
          allChannelsNames.forEach((channelName) => {
            const channelCapabilityGroup =
              channelCapabilityRollupRecord[groupName];
            if (channelCapabilityGroup) {
              const defaultChannelCapabilityRollup =
                channelCapabilityGroup[channelName];
              if (defaultChannelCapabilityRollup) {
                dispatch(
                  setChannelCapabilityRollup({
                    stationName,
                    groupName,
                    channelName,
                    rollup: updateAllChannelCapabilityMonitors(
                      defaultChannelCapabilityRollup,
                      monitorName,
                      selectedMonitors,
                      groupName,
                      channelName,
                      errorsRef
                    ),
                  })
                );

                errorsRef.current.forEach((error) => {
                  dispatch(
                    setHasError({
                      attributeName: `${error.id} ${error.type}`,
                      errorInfo: {
                        hasError: error.hasError,
                        reason: error.reason,
                      },
                    })
                  );
                });

                const monitorConflicts = checkIfRollupHasConflictedMonitors(
                  defaultChannelCapabilityRollup,
                  selectedMonitors
                );
                if (monitorConflicts.hasConflicts) {
                  doesGroupHaveConflicts = true;
                }
              }
            }
            errorsRef.current = [];
          });
          // When data is initially query it is possible for there to be an preexisting issue in config
          // Where the monitors for rollup station conflict with channel capability rollup monitors
          // Once a user resolved that conflict by toggling the monitor on, this will resolve the error state
          // A user cannot create a conflict, thus only have to remove and not create an error state for this case
          if (!doesGroupHaveConflicts) {
            updateErrorState(
              getErrorRecordKeyForChannelCapabilityMonitors(groupName),
              false,
              ''
            );
          }
        });
      }),
    [
      allChannelsNames,
      channelCapabilityRollupRecord,
      dispatch,
      stationGroupNames,
      stationName,
      updateErrorState,
    ]
  );
  return [updateAllMonitorsForAllCapabilityRollups];
};

/**
 * Hook wrap redux update for monitors for all groups for all capability rollups
 * @returns updateAllMonitorsForAllCapabilityRollups
 */
export const useUpdateAllMonitorsForChannelForAllCapabilityRollups = (): [
  (monitorName: string, channelName: string, isChannelSelected: boolean) => void
] => {
  const dispatch = useAppDispatch();
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const stationGroupsForStation = useAppSelector(
    (state) => state.stationControls.stationGroups[stationName ?? '']
  );
  const stationGroupNames = stationGroupsForStation?.map((group) => group.name);

  const channelCapabilityRollupRecord = useAppSelector(
    (store) => store.stationControls.channelCapabilityRollup
  );

  const errors: ErrorRecord[] = [];
  // Ref of errors passed down to recursive update which checks for errors and updates ref as needed
  const errorsRef = React.useRef(errors);

  const updateAllMonitorsForChannelForAllCapabilityRollups = React.useCallback(
    (monitorName: string, channelName: string, isChannelSelected: boolean) =>
      batch(() => {
        stationGroupNames.forEach((groupName) => {
          const channelCapabilityGroup =
            channelCapabilityRollupRecord[stationName ?? ''][groupName];
          if (channelCapabilityGroup) {
            const defaultChannelCapabilityRollup =
              channelCapabilityGroup[channelName];
            if (defaultChannelCapabilityRollup) {
              dispatch(
                setChannelCapabilityRollup({
                  stationName,
                  groupName,
                  channelName,
                  rollup: updateAllChannelCapabilityMonitor(
                    defaultChannelCapabilityRollup,
                    monitorName,
                    isChannelSelected,
                    groupName,
                    channelName,
                    errorsRef
                  ),
                })
              );
              errorsRef.current.forEach((error) => {
                dispatch(
                  setHasError({
                    attributeName: `${error.id} ${error.type}`,
                    errorInfo: {
                      hasError: error.hasError,
                      reason: error.reason,
                    },
                  })
                );
              });
            }
          }
          errorsRef.current = [];
        });
      }),
    [channelCapabilityRollupRecord, dispatch, stationGroupNames, stationName]
  );
  return [updateAllMonitorsForChannelForAllCapabilityRollups];
};

/**
 * Hook wrap redux that checks the returned query data for conflicts and updates error status
 * @returns checkChannelCapabilityForErrors function which returns a boolean
 */
export const useCheckChannelCapabilityForErrors = (): [
  (channelCapabilityRollups: ChannelCapabilityRollup[]) => boolean
] => {
  const [updateErrorState] = useUpdateErrorState();
  const selectedMonitors = useSelectedMonitors();
  const checkChannelCapabilityForErrors = React.useCallback(
    (channelCapabilityRollups: ChannelCapabilityRollup[]) => {
      let conflictedMonitors: { groupName: string; monitors: string[] }[] = [];
      channelCapabilityRollups.forEach((rollup) => {
        const result = checkIfRollupHasConflictedMonitors(
          rollup.defaultRollup,
          selectedMonitors
        );
        if (result.hasConflicts) {
          conflictedMonitors.push({
            groupName: rollup.groupName,
            monitors: result.conflictedMonitors,
          });
        }
      });
      if (conflictedMonitors.length > 0) {
        batch(() => {
          conflictedMonitors.forEach((conflict) => {
            updateErrorState(
              getErrorRecordKeyForChannelCapabilityMonitors(conflict.groupName),
              true,
              `Monitor(s): ${conflict.monitors.join(
                ', '
              )} are included and should not be for group ${
                conflict.groupName
              } channel capability, toggle monitor to be selected to resolve`
            );
          });
        });
        return true;
      }
      return false;
    },
    [selectedMonitors, updateErrorState]
  );
  return [checkChannelCapabilityForErrors];
};
