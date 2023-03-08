import React from 'react';
import { Checklist } from '../../components/Checklist';
import { useAppDispatch, useAppSelector } from '../../state/react-redux-hooks';
import { setStationGroupCapabilityRollup } from '../../state/station-controls-slice';
import {
  determineErrorsForStationCapabilityThresholdsExceedsMax,
  getRollupByDefaultAndId,
  updateStationGroupCapabilityChannels,
} from './util';
import { getChannels } from '../channel-checklist/ChannelChecklist';
import includes from 'lodash/includes';
import { ChannelCapabilityRollup } from './ChannelCapabilityRollup';
import { useAppContext } from '../../state/state';
import { useUpdateErrorState } from '../../util/custom-hooks';
import { AppSections, StationCapabilityErrorTypes } from '../../routes/types';
import { batch } from 'react-redux';
import { determineSectionContainsErrorState } from '../../util/util';
import remarkGfm from 'remark-gfm';
import makeStyles from '@mui/styles/makeStyles';
import Tooltip from '@mui/material/Tooltip';
import ReactMarkdown from 'react-markdown';
import ErrorIcon from '@mui/icons-material/Error';

const useStyles = makeStyles({
  errorIcon: {
    color: 'tomato',
  },
});

/**
 * The type of the props for the {@link CapabilityChannelChecklist} component
 */
export interface ChannelChecklistProps {
  groupName: string;
  rollupId: string;
  rollupChannels: string[];
}

export const CapabilityChannelChecklist: React.FC<ChannelChecklistProps> = ({
  groupName,
  rollupId,
  rollupChannels,
}) => {
  const dispatch = useAppDispatch();
  const { data: appData } = useAppContext();
  const [updateErrorState] = useUpdateErrorState();
  const classes = useStyles();

  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );

  const selectedChannels = useAppSelector(
    (state) => state.stationControls.selectedChannels
  );

  const errors = useAppSelector((state) => state.stationControls.error);

  const defaultRollup = useAppSelector(
    (store) =>
      store.stationControls.stationGroupCapabilityRollup[stationName ?? ''][
        groupName
      ]
  );

  const allChannels = React.useMemo(
    () => getChannels(stationName, appData.processingStationGroups),
    [appData.processingStationGroups, stationName]
  );

  const handleToggle = React.useCallback(
    (newlySelected: string[], checkbox: string) => {
      dispatch(
        setStationGroupCapabilityRollup({
          stationName,
          groupName,
          rollup: updateStationGroupCapabilityChannels(
            rollupId,
            defaultRollup,
            newlySelected
          ),
        })
      );
    },
    [defaultRollup, dispatch, groupName, rollupId, stationName]
  );
  const allChannelNames = allChannels.map((channel) => channel.name);
  const checkedChannels = rollupChannels.filter((c) =>
    includes(selectedChannels[stationName ?? ''], c)
  );

  const updateErrorStatus = React.useCallback(() => {
    batch(() => {
      if (checkedChannels.length === 0) {
        updateErrorState(
          `${rollupId} ${StationCapabilityErrorTypes.NO_CHANNELS}`,
          true,
          `Must have one channel included, go to ${AppSections.GROUP} ${groupName} ${AppSections.STATION_CAPABILITY} and enable a channel`
        );
      } else {
        updateErrorState(
          `${rollupId} ${StationCapabilityErrorTypes.NO_CHANNELS}`,
          false,
          ''
        );
      }
      const error = determineErrorsForStationCapabilityThresholdsExceedsMax(
        getRollupByDefaultAndId(defaultRollup, rollupId),
        groupName
      );
      if (error.hasError) {
        updateErrorState(`${rollupId} ${error.type}`, true, error.reason);
      } else {
        updateErrorState(`${rollupId} ${error.type}`, false, '');
      }
    });
  }, [
    checkedChannels.length,
    defaultRollup,
    groupName,
    rollupId,
    updateErrorState,
  ]);

  React.useEffect(() => {
    updateErrorStatus();
  }, [
    checkedChannels.length,
    groupName,
    rollupId,
    updateErrorState,
    updateErrorStatus,
  ]);

  return (
    <Checklist
      checkboxes={allChannels.map((channel) => channel.name)}
      checkedBoxes={checkedChannels}
      disabledCheckboxes={allChannelNames.filter(
        (c) => !includes(selectedChannels[stationName ?? ''], c)
      )}
      nonIdealState={'No channels found'}
      handleToggle={handleToggle}
      helpText={(channelName) =>
        `Check box results in channel ${channelName} being included in nested capability`
      }
      disabledText={(channelName) =>
        `${channelName} is disabled due to being unchecked at 'Enable/Disable channel section, enable it there to include it`
      }
      renderRightElement={(checkbox) =>
        includes(checkedChannels, checkbox) ? (
          <ChannelCapabilityRollup
            channelName={checkbox}
            groupName={groupName}
          />
        ) : determineSectionContainsErrorState(
            errors,
            `${AppSections.CHANNEL_CAPABILITY} ${checkbox}`
          ).length > 0 ? (
          <Tooltip
            title={
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {`Capability Error, enable ${checkbox} to edit/correct errors`}
              </ReactMarkdown>
            }
          >
            <ErrorIcon className={classes.errorIcon} />
          </Tooltip>
        ) : (
          <div />
        )
      }
    />
  );
};
