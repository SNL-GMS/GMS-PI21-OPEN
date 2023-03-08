import {
  Button,
  Dialog,
  DialogActions,
  DialogTitle,
  Grid,
} from '@mui/material';
import makeStyles from '@mui/styles/makeStyles';
import React from 'react';
import { AddIconButton } from '../../components/AddIconButton';
import { DeleteIconButton } from '../../components/DeleteIconButton';
import { DeselectAllIconButton } from '../../components/DeselectAllIconButton';
import { ErrorIconInfo } from '../../components/ErrorIcon';
import { IconControls } from '../../components/IconControls';
import { SelectAllIconButton } from '../../components/SelectAllIconButton';
import { useAppSelector } from '../../state/react-redux-hooks';
import {
  ErrorRecord,
  OperatorType,
  RollupEntry,
  RollupType,
} from '../../state/station-controls-slice';
import { indentedContainer, layoutStyles } from '../../styles/layout';
import {
  useSelectedMonitors,
  useUpdateChannelCapabilityRollup,
  useUpdateErrorState,
  useUpdateStationGroupCapability,
} from '../../util/custom-hooks';
import { CapabilityHeaderEntry } from './CapabilityHeaderEntry';
import { CapabilityRollupEntry } from './CapabilityRollupEntry';
import {
  addChannelCapabilityRollupEntry,
  addStationGroupCapabilityRollupEntry,
  deleteCapabilityRollupEntry,
  updateChannelCapabilityMonitors,
  updateStationGroupCapabilityChannels,
} from './util';

/**
 * The type of the props for the {@link CapabilityRollup} component
 */
export interface CapabilityRollupProps {
  defaultRollup: RollupEntry;
  groupName: string;
  channelName?: string;
  rollupId: string;
  rollupType: RollupType;
  rollupTypeOptions: string[];
  rollups: RollupEntry[] | undefined;
  operatorType: OperatorType;
  operatorTypeOptions: string[];
  goodThreshold?: string | number;
  marginalThreshold?: string | number;
  children?: React.ReactNode;
}

const useStyles = makeStyles({
  ...layoutStyles,
  form: {
    height: '10px',
    width: '300px',
  },
  addIcon: {
    fontSize: '1em',
    marginLeft: '.25em',
    backgroundColor: 'green',
    borderRadius: '1em',
    color: 'white',
    opacity: '.5',
    '&:hover': {
      opacity: '1',
    },
  },
  selectAllIcon: {
    fontSize: '1em',
    marginLeft: '.25em',
    backgroundColor: 'blue',
    borderRadius: '1em',
    color: 'white',
    opacity: '.5',
    '&:hover': {
      opacity: '1',
    },
  },
  deselectAllIcon: {
    fontSize: '1em',
    marginLeft: '.25em',
    backgroundColor: 'blue',
    borderRadius: '1em',
    color: 'white',
    opacity: '.5',
    '&:hover': {
      opacity: '1',
    },
  },
  deleteIcon: {
    fontSize: '1em',
    backgroundColor: 'red',
    borderRadius: '1em',
    color: 'white',
    opacity: '.5',
    '&:hover': {
      opacity: '1',
    },
  },
  errorIcon: {
    fontSize: '1em',
    marginLeft: '.25em',
    backgroundColor: 'red',
    borderRadius: '1em',
    color: 'white',
    opacity: '.5',
    '&:hover': {
      opacity: '1',
    },
  },
  threshold: {
    marginTop: '25px',
  },
  container: { ...indentedContainer },
});

/**
 * Creates capability rollup
 */
export const CapabilityRollup: React.FC<CapabilityRollupProps> = ({
  children,
  defaultRollup,
  goodThreshold,
  groupName,
  channelName,
  marginalThreshold,
  operatorType,
  operatorTypeOptions,
  rollupId,
  rollups,
  rollupType,
  rollupTypeOptions,
}: CapabilityRollupProps) => {
  const classes = useStyles();

  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );

  const selectedMonitors = useSelectedMonitors();

  const selectedChannels = useAppSelector(
    (store) => store.stationControls.selectedChannels[stationName ?? '']
  );
  const [updateErrorState] = useUpdateErrorState();
  const errors: ErrorRecord[] = [];
  // Ref of errors passed down to recursive update which checks for errors and updates ref as needed
  const errorsRef = React.useRef(errors);

  const isChannelCapability = channelName !== undefined;

  const [updateStationGroupCapabilityRollup] =
    useUpdateStationGroupCapability();

  const [updateChannelCapabilityRollup] = useUpdateChannelCapabilityRollup();

  const updateErrors = () => {
    errorsRef.current.forEach((error) => {
      updateErrorState(
        `${error.id} ${error.type}`,
        error.hasError,
        error.reason
      );
    });
    errorsRef.current = [];
  };

  const onDeleteIconClick = (containerId: string) => {
    if (isChannelCapability) {
      const newRollup = deleteCapabilityRollupEntry(
        containerId,
        defaultRollup,
        errorsRef,
        groupName,
        channelName
      );
      updateChannelCapabilityRollup(
        stationName ?? '',
        groupName,
        channelName,
        newRollup
      );
    } else {
      const newRollup = deleteCapabilityRollupEntry(
        containerId,
        defaultRollup,
        errorsRef,
        groupName
      );
      updateStationGroupCapabilityRollup(
        stationName ?? '',
        groupName,
        newRollup
      );
    }
    updateErrors();
  };

  const onAddIconClick = (containerId: string) => {
    if (isChannelCapability) {
      const newRollup = addChannelCapabilityRollupEntry(
        containerId,
        defaultRollup,
        selectedMonitors ?? [],
        errorsRef,
        groupName,
        channelName
      );
      updateChannelCapabilityRollup(
        stationName ?? '',
        groupName,
        channelName,
        newRollup
      );
    } else {
      const newRollup = addStationGroupCapabilityRollupEntry(
        containerId,
        defaultRollup,
        selectedChannels,
        errorsRef,
        groupName
      );
      updateStationGroupCapabilityRollup(
        stationName ?? '',
        groupName,
        newRollup
      );
    }
    updateErrors();
  };

  const onSelectAllIconClick = (containerId: string) => {
    if (isChannelCapability) {
      updateChannelCapabilityRollup(
        stationName ?? '',
        groupName,
        channelName,
        updateChannelCapabilityMonitors(
          containerId,
          defaultRollup,
          selectedMonitors
        )
      );
    } else {
      updateStationGroupCapabilityRollup(
        stationName ?? '',
        groupName,
        updateStationGroupCapabilityChannels(
          containerId,
          defaultRollup,
          selectedChannels
        )
      );
    }
  };

  const onDeselectAllIconClick = (containerId: string) => {
    if (isChannelCapability) {
      updateChannelCapabilityRollup(
        stationName ?? '',
        groupName,
        channelName,
        updateChannelCapabilityMonitors(containerId, defaultRollup, [])
      );
    } else {
      updateStationGroupCapabilityRollup(
        stationName ?? '',
        groupName,
        updateStationGroupCapabilityChannels(containerId, defaultRollup, [])
      );
    }
  };

  const [confirmDeleteEntry, setIsConfirmPromptOpen] = React.useState({
    open: false,
    containerId: '',
  });

  return (
    <Grid
      container
      justifyContent={'space-between'}
      direction={'row'}
      spacing={1}
      className={classes.container}
    >
      <Dialog
        open={confirmDeleteEntry.open}
        onClose={() => setIsConfirmPromptOpen({ open: false, containerId: '' })}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        {' '}
        <DialogTitle id='alert-dialog-title'>
          {'Are you sure you want to delete this entry?'}
        </DialogTitle>
        <DialogActions>
          <Button
            onClick={() =>
              setIsConfirmPromptOpen({ open: false, containerId: '' })
            }
          >
            No
          </Button>
          <Button
            onClick={() => {
              onDeleteIconClick(confirmDeleteEntry.containerId);
              setIsConfirmPromptOpen({ open: false, containerId: '' });
            }}
            autoFocus
          >
            Yes
          </Button>
        </DialogActions>
      </Dialog>
      <IconControls
        key={`icons-${groupName}-${rollupId}`}
        controls={
          rollupId.includes('default')
            ? rollupType === RollupType.ROLLUP_OF_ROLLUPS
              ? [
                  <AddIconButton
                    key={`add-button-${groupName}-${rollupId}`}
                    className={classes.addIcon}
                    containerId={rollupId}
                    helpText={'Add Entry'}
                    onIconClick={(containerId: string) =>
                      onAddIconClick(containerId)
                    }
                  />,
                  <ErrorIconInfo
                    key={`error-icon-info-${groupName}-${rollupId}`}
                    rollupId={rollupId}
                    className={classes.errorIcon}
                  />,
                ]
              : [
                  <SelectAllIconButton
                    key={`select-all-button-${groupName}-${rollupId}`}
                    className={classes.selectAllIcon}
                    containerId={rollupId}
                    helpText={'Check all'}
                    onIconClick={(containerId: string) =>
                      onSelectAllIconClick(containerId)
                    }
                  />,
                  <DeselectAllIconButton
                    key={`deselect-all-button-${groupName}-${rollupId}`}
                    className={classes.deselectAllIcon}
                    containerId={rollupId}
                    helpText={'Uncheck all'}
                    onIconClick={() => onDeselectAllIconClick(rollupId)}
                  />,
                  <ErrorIconInfo
                    key={`error-icon-info-${groupName}-${rollupId}`}
                    rollupId={rollupId}
                    className={classes.errorIcon}
                  />,
                ]
            : [
                <DeleteIconButton
                  key={`delete-button-${groupName}-${rollupId}`}
                  className={classes.deleteIcon}
                  containerId={rollupId}
                  helpText={'Delete entry'}
                  onIconClick={(containerId: string) =>
                    setIsConfirmPromptOpen({ open: true, containerId })
                  }
                />,
                <AddIconButton
                  key={`add-button-${groupName}-${rollupId}`}
                  className={classes.addIcon}
                  containerId={rollupId}
                  helpText={'Add Entry'}
                  onIconClick={(containerId: string) =>
                    onAddIconClick(containerId)
                  }
                />,
                <ErrorIconInfo
                  key={`error-icon-info-${groupName}-${rollupId}`}
                  rollupId={rollupId}
                  className={classes.errorIcon}
                />,
              ]
        }
      >
        <CapabilityHeaderEntry
          groupName={groupName}
          channelName={channelName}
          operatorType={operatorType}
          operatorTypeOptions={operatorTypeOptions}
          rollupId={rollupId}
          rollupType={rollupType}
          rollupTypeOptions={rollupTypeOptions}
          goodThreshold={goodThreshold}
          marginalThreshold={marginalThreshold}
        />
        {defaultRollup.rollupType !== RollupType.ROLLUP_OF_ROLLUPS ? (
          <CapabilityRollupEntry
            key={`${groupName}-${rollupId}`}
            groupName={groupName}
            channelName={channelName}
            rollup={defaultRollup}
          />
        ) : undefined}
      </IconControls>
      {rollups && rollups.length > 0
        ? rollups.map((rollup) => {
            if (
              rollup.rollupType === RollupType.ROLLUP_OF_ROLLUPS &&
              rollup.rollups &&
              rollup.rollups.length > 0
            ) {
              return (
                <CapabilityRollup
                  key={`${groupName}-${rollup.id}`}
                  defaultRollup={defaultRollup}
                  groupName={groupName}
                  channelName={channelName}
                  rollupId={rollup.id}
                  rollups={rollup.rollups}
                  rollupType={rollup.rollupType}
                  rollupTypeOptions={rollupTypeOptions}
                  operatorType={rollup.operatorType}
                  operatorTypeOptions={operatorTypeOptions}
                  goodThreshold={rollup.threshold?.goodThreshold}
                  marginalThreshold={rollup.threshold?.marginalThreshold}
                />
              );
            }
            return (
              <div
                key={`rollup-entry-${groupName}-${rollup.id}`}
                className={classes.container}
              >
                <IconControls
                  key={`delete-entry-${groupName}-${rollup.id}`}
                  controls={
                    rollup.rollupType === RollupType.ROLLUP_OF_ROLLUPS
                      ? [
                          <DeleteIconButton
                            key={`delete-button-${groupName}-${rollup.id}`}
                            className={classes.deleteIcon}
                            containerId={rollup.id}
                            helpText={'Delete entry'}
                            onIconClick={(containerId: string) =>
                              setIsConfirmPromptOpen({
                                open: true,
                                containerId,
                              })
                            }
                          />,
                          <AddIconButton
                            key={`add-button-${groupName}-${rollup.id}`}
                            className={classes.addIcon}
                            containerId={rollup.id}
                            helpText={'Add entry'}
                            onIconClick={(containerId: string) =>
                              onAddIconClick(containerId)
                            }
                          />,
                          <ErrorIconInfo
                            key={`error-icon-info-${groupName}-${rollupId}`}
                            rollupId={rollup.id}
                            className={classes.errorIcon}
                          />,
                        ]
                      : [
                          <DeleteIconButton
                            key={`delete-button-${groupName}-${rollup.id}`}
                            className={classes.deleteIcon}
                            containerId={rollup.id}
                            helpText={'Delete entry'}
                            onIconClick={(containerId: string) =>
                              setIsConfirmPromptOpen({
                                open: true,
                                containerId,
                              })
                            }
                          />,
                          <SelectAllIconButton
                            key={`select-all-button-${groupName}-${rollupId}`}
                            className={classes.selectAllIcon}
                            containerId={rollup.id}
                            helpText={'Check all'}
                            onIconClick={(containerId: string) =>
                              onSelectAllIconClick(containerId)
                            }
                          />,
                          <DeselectAllIconButton
                            key={`deselect-all-button-${groupName}-${rollupId}`}
                            className={classes.deselectAllIcon}
                            containerId={rollup.id}
                            helpText={'Uncheck all'}
                            onIconClick={(containerId: string) =>
                              onDeselectAllIconClick(containerId)
                            }
                          />,
                          <ErrorIconInfo
                            key={`error-icon-info-${groupName}-${rollupId}`}
                            rollupId={rollup.id}
                            className={classes.errorIcon}
                          />,
                        ]
                  }
                >
                  <CapabilityHeaderEntry
                    key={`header-${groupName}-${rollup.id}`}
                    groupName={groupName}
                    channelName={channelName}
                    operatorType={rollup.operatorType}
                    operatorTypeOptions={operatorTypeOptions}
                    rollupId={rollup.id}
                    rollupType={rollup.rollupType}
                    rollupTypeOptions={rollupTypeOptions}
                    goodThreshold={rollup.threshold?.goodThreshold}
                    marginalThreshold={rollup.threshold?.marginalThreshold}
                  />
                </IconControls>
                <CapabilityRollupEntry
                  key={`${groupName}-${rollup.id}`}
                  groupName={groupName}
                  channelName={channelName}
                  rollup={rollup}
                />
              </div>
            );
          })
        : null}
      {children}
    </Grid>
  );
};
