import includes from 'lodash/includes';
import React from 'react';
import { batch } from 'react-redux';
import { Checklist } from '../../components/Checklist';
import { AppSections, ChannelCapabilityErrorTypes } from '../../routes/types';
import { useAppDispatch, useAppSelector } from '../../state/react-redux-hooks';
import { useAppContext } from '../../state/state';
import { setChannelCapabilityRollup } from '../../state/station-controls-slice';
import {
  useSelectedMonitors,
  useUpdateErrorState,
} from '../../util/custom-hooks';
import {
  determineErrorsForChannelCapabilityThresholdsExceedsMax,
  getRollupByDefaultAndId,
  updateChannelCapabilityMonitors,
} from './util';

/**
 * The type of the props for the {@link CapabilityMonitorChecklist} component
 */
export interface MonitorChecklistProps {
  rollupId: string;
  groupName: string;
  channelName: string;
  rollupMonitorNames: string[];
}

export const CapabilityMonitorChecklist: React.FC<MonitorChecklistProps> = ({
  rollupMonitorNames,
  rollupId,
  groupName,
  channelName,
}) => {
  const dispatch = useAppDispatch();

  const [updateErrorState] = useUpdateErrorState();

  const { data: appData } = useAppContext();

  const stationName = useAppSelector(
    (store) => store.stationControls.stationName
  );

  const defaultRollup = useAppSelector(
    (store) =>
      store.stationControls.channelCapabilityRollup[stationName ?? ''][
        groupName
      ][channelName]
  );

  const monitorTypesRollups = useAppSelector(
    (store) => store.stationControls.monitorTypesForRollup[stationName ?? '']
  );

  const supportedMonitorTypes = appData?.supportedMonitorTypes;
  const selectedMonitors = useSelectedMonitors();
  const disabledMonitors = supportedMonitorTypes.filter(
    (monitor) => !includes(selectedMonitors, monitor)
  );
  const disabledMonitorsForChannel: string[] = [];
  monitorTypesRollups.forEach((rollup) => {
    if (rollup.channelOverrides) {
      const channel = rollup.channelOverrides.find(
        (channel) => channel.name === channelName
      );
      if (channel && !channel.isIncluded) {
        disabledMonitorsForChannel.push(rollup.name);
      }
    }
  });
  const handleToggle = React.useCallback(
    (newlySelected: string[], checkbox: string) => {
      dispatch(
        setChannelCapabilityRollup({
          stationName,
          groupName,
          channelName,
          rollup: updateChannelCapabilityMonitors(
            rollupId,
            defaultRollup,
            newlySelected
          ),
        })
      );
    },
    [dispatch, stationName, groupName, channelName, rollupId, defaultRollup]
  );

  const updateErrorStatus = React.useCallback(() => {
    batch(() => {
      if (rollupMonitorNames.length === 0) {
        updateErrorState(
          `${rollupId} ${ChannelCapabilityErrorTypes.NO_MONITORS}`,
          true,
          `Must have one monitor included, go to ${AppSections.GROUP} ${groupName} ${AppSections.CHANNEL_CAPABILITY} ${channelName} and enable a monitor`
        );
      } else {
        updateErrorState(
          `${rollupId} ${ChannelCapabilityErrorTypes.NO_MONITORS}`,
          false,
          ''
        );
      }
      const error = determineErrorsForChannelCapabilityThresholdsExceedsMax(
        getRollupByDefaultAndId(defaultRollup, rollupId),
        groupName,
        channelName
      );
      if (error.hasError) {
        updateErrorState(`${rollupId} ${error.type}`, true, error.reason);
      } else {
        updateErrorState(`${rollupId} ${error.type}`, false, '');
      }
    });
  }, [
    channelName,
    defaultRollup,
    groupName,
    rollupId,
    rollupMonitorNames.length,
    updateErrorState,
  ]);

  React.useEffect(() => {
    let isError = false;
    let badMonitors: string[] = [];
    disabledMonitors.forEach((monitor) => {
      if (includes(rollupMonitorNames, monitor)) {
        isError = true;
        badMonitors.push(monitor);
      }
    });
    if (isError) {
      updateErrorState(
        'channelCapability',
        true,
        `monitors: ${badMonitors} are not included at station monitor level, initial config state issue`
      );
    }
  }, [disabledMonitors, rollupMonitorNames, updateErrorState]);

  React.useEffect(() => {
    updateErrorStatus();
  }, [
    channelName,
    groupName,
    rollupId,
    rollupMonitorNames.length,
    updateErrorState,
    updateErrorStatus,
  ]);
  return (
    <Checklist
      checkboxes={supportedMonitorTypes}
      checkedBoxes={rollupMonitorNames}
      disabledCheckboxes={[...disabledMonitors, ...disabledMonitorsForChannel]}
      nonIdealState={'No monitors found'}
      handleToggle={handleToggle}
      helpText={(monitorName) =>
        `Check box results in monitor ${monitorName} being included in nested capability`
      }
      disabledText={(monitorName) => {
        if (includes(disabledMonitorsForChannel, monitorName)) {
          return `${monitorName} is disabled due to ${channelName} being unchecked at monitor channel level, enable there to include it`;
        }
        return `${monitorName} is disabled due to being unchecked at monitor checklist station level, enable there to include it`;
      }}
    />
  );
};
