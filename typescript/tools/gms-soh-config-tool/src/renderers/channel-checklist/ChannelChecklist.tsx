import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Button,
  Dialog,
  DialogActions,
  DialogTitle,
} from '@mui/material';
import makeStyles from '@mui/styles/makeStyles';
import produce from 'immer';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import { batch } from 'react-redux';
import remarkGfm from 'remark-gfm';
import { DataNames } from '../../coi-types/data-names';
import {
  Channel,
  ProcessingStationGroups,
} from '../../coi-types/processing-types';
import { Checklist } from '../../components/Checklist';
import { useResolveChannelsByMonitorType } from '../../state/api-slice';
import { useAppDispatch, useAppSelector } from '../../state/react-redux-hooks';
import { useAppContext } from '../../state/state';
import {
  ErrorRecord,
  setChannelNames,
} from '../../state/station-controls-slice';
import {
  useChannelListHasError,
  useUpdateErrorState,
  useUpdateStationGroupCapability,
} from '../../util/custom-hooks';
import { useMonitorTypesForRollup } from '../monitor-types-rollup/MonitorTypesRollup';
import { updateAllStationGroupCapabilityChannels } from '../station-capability-rollup/util';
import { determineSelectedChannels } from './util';

/**
 * The type of the props for the {@link ChannelChecklist} component
 */
export interface ChannelChecklistProps {
  updateChannelChecklist: (selectedChannels: string[]) => void;
  formData: any;
  label: string;
  description: string;
}

const useStyles = makeStyles({
  accordionLabel: {
    fontWeight: 'bold',
    color: '#666666',
  },
  errorBorder: {
    border: 'solid',
    borderWidth: '1px',
    borderColor: 'tomato',
  },
});

/**
 * @param stationName the station from which to get the channels
 * @param processingStationGroups the processing station groups loaded from the file that is chosen in the app settings
 *
 * @returns the list of all channels for this station.
 */
export const getChannels = (
  stationName: string | null,
  processingStationGroups: ProcessingStationGroups
): Channel[] =>
  stationName && processingStationGroups
    ? processingStationGroups
        .flatMap((group) => group.stations)
        .find((s) => s.name === stationName)?.channels ?? []
    : [];

/**
 * Creates util functions for the redux state for the station configuration. Automatically
 * updates the redux state when the station is changed.
 *
 * @returns an object containing
 * * the selected channels for the current station (or undefined if not found)
 * * all channels for the current station
 * * a setter to update which channels are selected
 */
export const useSelectedChannels = () => {
  const { data: appData } = useAppContext();
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const selectedChannels = useAppSelector(
    (state) => state.stationControls.selectedChannels
  );
  const dispatch = useAppDispatch();
  const setSelectedChannels = React.useCallback(
    (newChannels: string[]) =>
      dispatch(setChannelNames({ stationName, channelNames: newChannels })),
    [dispatch, stationName]
  );
  const allChannels = React.useMemo(
    () => getChannels(stationName, appData.processingStationGroups),
    [appData.processingStationGroups, stationName]
  );

  const resolvedChannelsByMonitorType = useResolveChannelsByMonitorType(
    {
      stationName: stationName ?? '',
    },
    { skip: !stationName }
  );

  const hasLoadedDataIntoState = React.useRef(false);
  React.useEffect(() => {
    if (
      stationName != null &&
      resolvedChannelsByMonitorType.data &&
      !hasLoadedDataIntoState.current
    ) {
      hasLoadedDataIntoState.current = true;
      setSelectedChannels(
        determineSelectedChannels(
          resolvedChannelsByMonitorType.data,
          allChannels.map((chan) => chan.name)
        )
      );
    }
  }, [
    allChannels,
    resolvedChannelsByMonitorType.data,
    selectedChannels,
    setSelectedChannels,
    stationName,
  ]);
  return {
    selectedChannels: stationName ? selectedChannels[stationName] : undefined,
    allChannels,
    setSelectedChannels,
  };
};

/**
 * Creates a checklist of channels that can be selected and deselected.
 */
export const ChannelChecklist: React.FC<ChannelChecklistProps> = ({
  label,
  description,
}: ChannelChecklistProps) => {
  const classes = useStyles();
  const [updateErrorState] = useUpdateErrorState();
  const [determineChannelListHasError] = useChannelListHasError();
  const [confirmToggle, setConfirmToggle] = React.useState({
    open: false,
    newlySelected: [''],
    checkbox: '',
  });
  const [updateStationGroupCapabilityRollup] =
    useUpdateStationGroupCapability();
  const { selectedChannels, allChannels, setSelectedChannels } =
    useSelectedChannels();
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const { setMonitorTypesForRollup } = useMonitorTypesForRollup(stationName);
  const monitorTypeConfigs = useAppSelector(
    (state) => state.stationControls.monitorTypesForRollup[stationName ?? '']
  );
  const defaultRollup = useAppSelector(
    (store) => store.stationControls.stationGroupCapabilityRollup
  );
  const stationGroupsForStation = useAppSelector(
    (state) => state.stationControls.stationGroups[stationName ?? '']
  );
  const hasMonitorThresholdsLoaded = useAppSelector(
    (state) => state.stationControls.loadedData[DataNames.MONITOR_THRESHOLDS]
  );
  const hasStationCapabilityRollupLoaded = useAppSelector(
    (state) =>
      state.stationControls.loadedData[DataNames.STATION_CAPABILITY_ROLLUP]
  );
  const stationGroupNames = stationGroupsForStation?.map((group) => group.name);
  const errors: ErrorRecord[] = [];
  // Ref of errors passed down to recursive update which checks for errors and updates ref as needed
  const errorsRef = React.useRef(errors);

  const updateData = (newlySelected: string[], checkbox: string) => {
    if (newlySelected.length === 0) {
      updateErrorState(
        label.replace(/ /g, ''),
        true,
        `Must have one channel included, goto ${label}`
      );
    } else {
      updateErrorState(label.replace(/ /g, ''), false, '');
    }
    const updatedConfig = produce(monitorTypeConfigs, (draft) => {
      draft.forEach((config) =>
        config.channelOverrides?.forEach((channelOverride) => {
          if (channelOverride.name === checkbox)
            channelOverride.isIncluded = newlySelected.includes(checkbox);
        })
      );
    });

    batch(() => {
      stationGroupNames.forEach((groupName) => {
        updateStationGroupCapabilityRollup(
          stationName ?? '',
          groupName,
          updateAllStationGroupCapabilityChannels(
            defaultRollup[stationName ?? ''][groupName],
            checkbox,
            newlySelected,
            groupName,
            errorsRef
          )
        );
        errorsRef.current.forEach((error) => {
          updateErrorState(
            `${error.id} ${error.type}`,
            error.hasError,
            error.reason
          );
        });
        errorsRef.current = [];
      });
      setMonitorTypesForRollup(updatedConfig);
      setSelectedChannels(newlySelected);
    });
  };
  const handleToggle = React.useCallback(
    (newlySelected: string[], checkbox: string) => {
      setConfirmToggle({ open: true, newlySelected, checkbox });
    },
    []
  );
  return (
    <>
      <Dialog
        open={confirmToggle.open}
        onClose={() =>
          setConfirmToggle({ open: false, newlySelected: [''], checkbox: '' })
        }
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        {' '}
        <DialogTitle id='alert-dialog-title'>
          {`Warning: Modifying the Channel will cause the Channel to be modified for rollup under each Monitor Type
            and modified for the Station Capability rollup for each Station Group. This action cannot be undone.`}
        </DialogTitle>
        <DialogActions>
          <Button
            onClick={() =>
              setConfirmToggle({
                open: false,
                newlySelected: [''],
                checkbox: '',
              })
            }
          >
            Cancel
          </Button>
          <Button
            onClick={() => {
              updateData(confirmToggle.newlySelected, confirmToggle.checkbox);
              setConfirmToggle({
                open: false,
                newlySelected: [''],
                checkbox: '',
              });
            }}
            autoFocus
          >
            Continue
          </Button>
        </DialogActions>
      </Dialog>
      <Accordion
        disabled={
          !allChannels ||
          allChannels.length === 0 ||
          !selectedChannels ||
          !hasMonitorThresholdsLoaded ||
          !hasStationCapabilityRollupLoaded
        }
      >
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          className={`${
            determineChannelListHasError(label.replace(/ /g, ''))
              ? classes.errorBorder
              : ''
          }`}
        >
          <strong className={classes.accordionLabel}>{label}</strong>
        </AccordionSummary>
        <AccordionDetails>
          {description && (
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {description}
            </ReactMarkdown>
          )}
          <Checklist
            checkboxes={allChannels.map((channel) => channel.name)}
            checkedBoxes={selectedChannels}
            nonIdealState={'No channels found'}
            handleToggle={handleToggle}
            helpText={(channelName) =>
              `Check box results in channel ${channelName} being excluded from the Worst of Rollup and Capability Rollup`
            }
          />
        </AccordionDetails>
      </Accordion>
    </>
  );
};
