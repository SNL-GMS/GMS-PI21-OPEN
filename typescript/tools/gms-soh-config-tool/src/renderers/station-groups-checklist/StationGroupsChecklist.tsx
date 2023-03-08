import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogTitle,
  Tooltip,
} from '@mui/material';
import makeStyles from '@mui/styles/makeStyles';
import produce from 'immer';
import { includes } from 'lodash';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import ErrorIcon from '@mui/icons-material/Error';
import { DataNames } from '../../coi-types/data-names';
import { Checklist } from '../../components/Checklist';
import { windowAPI } from '../../electron-util';
import { AppSections } from '../../routes/types';
import {
  useResolveChannelCapabilityRollup,
  useResolveStationGroupCapability,
  useResolveStationGroups,
} from '../../state/api-slice';
import { useAppDispatch, useAppSelector } from '../../state/react-redux-hooks';
import { StationGroupsDefinition } from '../../state/retrieve-station-groups';
import { useAppContext } from '../../state/state';
import {
  setStationGroups,
  StationGroup,
} from '../../state/station-controls-slice';
import {
  useCheckChannelCapabilityForErrors,
  useSelectedMonitors,
  useUpdateChannelCapabilityRollup,
  useUpdateDataLoaded,
  useUpdateStationGroupCapability,
} from '../../util/custom-hooks';
import { determineSectionContainsErrorState } from '../../util/util';
import { getChannels } from '../channel-checklist/ChannelChecklist';
import { StationCapabilityRollup } from '../station-capability-rollup/StationCapabilityRollup';
import { ALL_STATION_GROUP_NAME, determineGroupsForStation } from './util';
import remarkGfm from 'remark-gfm';
import { batch } from 'react-redux';

/**
 * The type of the props for the {@link StationGroupsChecklist} component
 */
export interface StationGroupsChecklistProps {
  formData: any;
  label: string;
  description: string;
}

const useStyles = makeStyles({
  accordionLabel: {
    fontWeight: 'bold',
    color: '#666666',
  },
  errorIcon: {
    color: 'tomato',
  },
  errorBorder: {
    border: 'solid',
    borderWidth: '1px',
    borderColor: 'tomato',
  },
});

/**
 * Creates a checklist of Station Groups that can be selected and deselected.
 */
export const StationGroupsChecklist: React.FC<StationGroupsChecklistProps> = ({
  label,
  description,
}: StationGroupsChecklistProps) => {
  const { data: appData } = useAppContext();
  const dispatch = useAppDispatch();
  const classes = useStyles();
  const [updateDataLoaded] = useUpdateDataLoaded();
  const [checkChannelCapabilityForErrors] =
    useCheckChannelCapabilityForErrors();
  const [confirmErrorState, setConfirmErrorStateOpen] = React.useState(false);
  const selectedMonitors = useSelectedMonitors();
  const hasStationCapabilityRollupLoaded = useAppSelector(
    (state) =>
      state.stationControls.loadedData[DataNames.STATION_CAPABILITY_ROLLUP]
  );

  const hasStationGroupsLoaded = useAppSelector(
    (state) => state.stationControls.loadedData[DataNames.STATION_GROUPS]
  );

  const hasChannelCapabilityRollupLoaded = useAppSelector(
    (state) =>
      state.stationControls.loadedData[DataNames.CHANNEL_CAPABILITY_ROLLUP]
  );

  const hasSelectedMonitorsAndThresholdsLoaded = useAppSelector(
    (state) => state.stationControls.loadedData[DataNames.MONITOR_THRESHOLDS]
  );

  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );

  const errors = useAppSelector((state) => state.stationControls.error);
  const selectedChannels = useAppSelector(
    (state) => state.stationControls.selectedChannels[stationName ?? '']
  );
  const allChannels = React.useMemo(
    () => getChannels(stationName, appData.processingStationGroups),
    [appData.processingStationGroups, stationName]
  );
  const allChannelNames = allChannels.map((channel) => channel.name);
  const stationGroupsForStation = useAppSelector(
    (state) => state.stationControls.stationGroups[stationName ?? '']
  );

  const resolvedStationGroups = useResolveStationGroups(
    {},
    { skip: !stationName }
  );

  const stationGroupNames = stationGroupsForStation?.map((group) => group.name);
  const resolvedStationGroupsCapability = useResolveStationGroupCapability(
    {
      stationName: stationName ?? '',
      groupNames: stationGroupNames,
      channelNames: selectedChannels,
    },
    {
      skip:
        !stationName ||
        !stationGroupNames ||
        stationGroupNames.length === 0 ||
        resolvedStationGroups.isLoading,
    }
  );

  const resolvedChannelCapabilityRollups = useResolveChannelCapabilityRollup(
    {
      stationName: stationName ?? '',
      groupNames: stationGroupNames,
      channelNames: allChannelNames,
      allMonitorNames: selectedMonitors ?? [],
    },
    {
      skip:
        !stationName ||
        !stationGroupNames ||
        stationGroupNames.length === 0 ||
        resolvedStationGroups.isLoading ||
        !hasStationCapabilityRollupLoaded ||
        !hasSelectedMonitorsAndThresholdsLoaded,
    }
  );

  const [updateStationGroupCapabilityRollup] =
    useUpdateStationGroupCapability();

  const [updateChannelCapabilityRollup] = useUpdateChannelCapabilityRollup();

  const updateStationGroups = React.useCallback(
    (newStationGroups: StationGroup[]) => {
      dispatch(
        setStationGroups({
          stationName,
          stationGroups: newStationGroups,
        })
      );
    },
    [dispatch, stationName]
  );

  const handleToggle = React.useCallback(
    (newlySelected: string[], checkbox: string) => {
      const updatedGroups = produce(stationGroupsForStation, (draft) => {
        if (stationGroupsForStation) {
          const group = draft.find((g) => g.name === checkbox);
          if (group) {
            group.included = !group.included;
          }
        }
      });
      updateStationGroups(updatedGroups);
    },
    [stationGroupsForStation, updateStationGroups]
  );

  const determineGroupsAndLoadIntoState = React.useCallback(async () => {
    const processingStationGroupDefinitionsFromDisk =
      await windowAPI.electronAPI.loadConfigFromDir(
        '../station-reference/definitions'
      );
    updateStationGroups(
      determineGroupsForStation(
        stationName,
        resolvedStationGroups?.data?.stationGroupNames,
        processingStationGroupDefinitionsFromDisk as StationGroupsDefinition[]
      )
    );
  }, [
    resolvedStationGroups?.data?.stationGroupNames,
    stationName,
    updateStationGroups,
  ]);

  const addStationGroupCapabilityIntoState = React.useCallback(() => {
    batch(() => {
      resolvedStationGroupsCapability.data?.forEach((rollup) => {
        updateStationGroupCapabilityRollup(
          stationName ?? '',
          rollup.groupName,
          rollup.defaultRollup
        );
      });
    });
  }, [
    resolvedStationGroupsCapability.data,
    stationName,
    updateStationGroupCapabilityRollup,
  ]);

  const addChannelCapabilityRollupIntoState = React.useCallback(() => {
    batch(() => {
      resolvedChannelCapabilityRollups.data?.forEach((rollup) => {
        updateChannelCapabilityRollup(
          stationName ?? '',
          rollup.groupName,
          rollup.channelName,
          rollup.defaultRollup
        );
      });
    });
    checkChannelCapabilityForErrors(
      resolvedChannelCapabilityRollups.data ?? []
    );
  }, [
    checkChannelCapabilityForErrors,
    resolvedChannelCapabilityRollups.data,
    stationName,
    updateChannelCapabilityRollup,
  ]);

  React.useEffect(() => {
    if (
      resolvedStationGroups.isSuccess &&
      resolvedStationGroups.data &&
      resolvedStationGroups.data.stationGroupNames &&
      !hasStationGroupsLoaded
    ) {
      determineGroupsAndLoadIntoState();
      updateDataLoaded(DataNames.STATION_GROUPS, true);
    }
  }, [
    determineGroupsAndLoadIntoState,
    hasStationGroupsLoaded,
    resolvedStationGroups.data,
    resolvedStationGroups.isSuccess,
    updateDataLoaded,
  ]);

  React.useEffect(() => {
    if (
      resolvedStationGroupsCapability.isSuccess &&
      resolvedStationGroupsCapability.data &&
      resolvedStationGroupsCapability.data.length > 0 &&
      resolvedStationGroupsCapability.data[0] &&
      resolvedStationGroupsCapability.data[0].stationName === stationName &&
      !hasStationCapabilityRollupLoaded
    ) {
      addStationGroupCapabilityIntoState();
      updateDataLoaded(DataNames.STATION_CAPABILITY_ROLLUP, true);
    }
  }, [
    addStationGroupCapabilityIntoState,
    hasStationCapabilityRollupLoaded,
    resolvedStationGroupsCapability.currentData,
    resolvedStationGroupsCapability.data,
    resolvedStationGroupsCapability.isSuccess,
    stationName,
    updateDataLoaded,
    updateStationGroupCapabilityRollup,
  ]);

  React.useEffect(() => {
    if (
      resolvedChannelCapabilityRollups.isSuccess &&
      resolvedChannelCapabilityRollups.data &&
      resolvedChannelCapabilityRollups.data.length > 0 &&
      resolvedChannelCapabilityRollups.data[0] &&
      resolvedChannelCapabilityRollups.data[0].stationName === stationName &&
      !hasChannelCapabilityRollupLoaded
    ) {
      addChannelCapabilityRollupIntoState();
      updateDataLoaded(DataNames.CHANNEL_CAPABILITY_ROLLUP, true);
    }
  }, [
    addChannelCapabilityRollupIntoState,
    hasChannelCapabilityRollupLoaded,
    resolvedChannelCapabilityRollups.data,
    resolvedChannelCapabilityRollups.isSuccess,
    stationName,
    updateDataLoaded,
  ]);

  const checkedBoxesNames = React.useMemo(
    () =>
      stationGroupsForStation
        ?.filter((group) => group.included)
        ?.map((group) => group.name) ?? [],
    [stationGroupsForStation]
  );

  const hasErrors = React.useMemo(
    () =>
      determineSectionContainsErrorState(errors, AppSections.GROUP).length > 0,
    [errors]
  );
  return (
    <>
      <Dialog
        open={confirmErrorState}
        onClose={() => setConfirmErrorStateOpen(false)}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        {' '}
        <DialogTitle id='alert-dialog-title'>
          {
            'Conflict with monitor types for rollup station and channel capability rollup, hover save button error icon for more info'
          }
        </DialogTitle>
        <DialogActions>
          <Button onClick={() => setConfirmErrorStateOpen(false)}>ok</Button>
        </DialogActions>
      </Dialog>
      <Accordion
        className={`${hasErrors ? classes.errorBorder : ''}`}
        disabled={stationName === null || !hasStationGroupsLoaded}
      >
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <strong className={classes.accordionLabel}>{label}</strong>
        </AccordionSummary>
        <AccordionDetails>
          {description && (
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {description}
            </ReactMarkdown>
          )}
          {resolvedStationGroups.isLoading ? (
            <CircularProgress size={'4rem'} />
          ) : (
            <Checklist
              checkboxes={stationGroupNames ?? []}
              checkedBoxes={checkedBoxesNames}
              disabledCheckboxes={[ALL_STATION_GROUP_NAME]}
              disabledText={(stationGroup) =>
                `${stationGroup} Station Group cannot be unchecked, contains all Stations`
              }
              nonIdealState={'No station groups found'}
              handleToggle={handleToggle}
              helpText={(stationGroup) =>
                `Check box results in station ${stationName} being included/excluded from ${stationGroup}`
              }
              renderRightElement={(checkbox) =>
                includes(checkedBoxesNames, checkbox) ? (
                  hasStationCapabilityRollupLoaded === true ? (
                    <StationCapabilityRollup
                      groupName={checkbox}
                      allChannelNames={allChannelNames}
                    />
                  ) : (
                    <CircularProgress size={'1rem'} />
                  )
                ) : determineSectionContainsErrorState(errors, checkbox)
                    .length > 0 ? (
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
          )}
        </AccordionDetails>
      </Accordion>
    </>
  );
};
