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
  Typography,
} from '@mui/material';
import { makeStyles } from '@mui/styles';
import React from 'react';
import {
  ChannelOverrides,
  MonitorTypeConfig,
} from '../../coi-types/monitor-types';
import { Checklist } from '../../components/Checklist';
import {
  useResolveChannelsByMonitorType,
  useResolveStationConfig,
  useResolveThresholdsForChannelsForMonitorInStation,
  useResolveThresholdsForMonitorsInStation,
} from '../../state/api-slice';
import { useAppDispatch, useAppSelector } from '../../state/react-redux-hooks';
import { useAppContext } from '../../state/state';
import { setMonitorTypeForRollup } from '../../state/station-controls-slice';
import { useSelectedChannels } from '../channel-checklist/ChannelChecklist';
import {
  addChannelsWithThresholdsForMonitorToConfig,
  convertChannelOverridesWithThresholdsToThresholdsMap,
  convertMonitorTypeConfigToThresholdMap,
  convertThresholdConfigToThresHoldMap,
  getUpdatedMonitorTypes,
  isChannelIncludedForMonitorType,
  updateGoodThresholdForChannel,
  updateGoodThresholdForMonitor,
  updateMarginalThresholdForChannel,
  updateMarginalThresholdForMonitor,
  updateMonitorChannelOverrides,
} from './util';
import { layoutStyles } from '../../styles/layout';
import { isSelectedChannelIncludedInAnyMonitor } from '../channel-checklist/util';
import { Thresholds } from './Thresholds';
import { isDurationMonitor } from '../../util/util';
import {
  useMonitorHaveError,
  useSelectedMonitors,
  useUpdateAllMonitorsForAllCapabilityRollups,
  useUpdateAllMonitorsForChannelForAllCapabilityRollups,
  useUpdateDataLoaded,
  useUpdateErrorState,
} from '../../util/custom-hooks';
import { DataNames } from '../../coi-types/data-names';
import includes from 'lodash/includes';

const helpTextGoodForStationThreshold =
  'Values less than or equal to the Good Threshold will result in a Good status. The threshold defined per Station are defaults used for individual Channels in which thresholds are not specifically defined they are not used in calculations.';

const helpTextMarginalForStationThreshold =
  'Values less than or equal to the Marginal Threshold will result in a Marginal status. The threshold defined per Station are defaults used for individual Channels in which thresholds are not specifically defined they are not used in calculations.';

const useStyles = makeStyles({
  ...layoutStyles,
  listItem: {
    alignSelf: 'flex-start',
    marginTop: '0.8em',
  },
  listButton: {
    alignSelf: 'flex-start',
    margin: '0',
    paddingTop: '0',
    paddingBottom: '0',
  },
  thresholdInputs: {
    '&:focus-within input': {
      background: 'white',
    },
  },
  accordion: {
    width: '100%',
  },
  accordionLabel: {
    fontWeight: 'bold',
    color: '#666666',
  },
  accordionSummary: {
    flexDirection: 'column',
    width: '100%',
    '& .MuiAccordionSummary-contentGutters': {
      width: '100%',
    },
  },
  expandButton: {
    width: '100%',
  },
  spacer: {
    width: '1em',
    height: '100%',
  },
  errorBorder: {
    border: 'solid',
    borderWidth: '1px',
    borderColor: 'tomato',
  },
});

/**
 * The type of the props for the {@link MonitorTypesRollup} component
 */
export interface MonitorTypesRollupProps {
  label: string;
  description: string;
}

/**
 * Returns data in (roughly) the same format as {@link React.useState}. It returns the
 * monitor types from redux and a setter for them for the provided station.
 *
 * @param stationName the name of the station for which to get the configured monitor types from redux
 * @returns a touple containing: [
 * * the list of monitor type configuration objects, or undefined if no station is provided
 * * a referentially stable setter for updating the redux state for the list of monitor types for this station.
 * ]
 */
const useMonitorTypesForRollupForStation = (
  stationName: string | null
): [
  MonitorTypeConfig[] | undefined,
  (mt: MonitorTypeConfig[]) => {
    payload: any;
    type: string;
  }
] => {
  const dispatch = useAppDispatch();
  const allMonitorTypesForRollup: Record<string, MonitorTypeConfig[]> =
    useAppSelector((state) => state.stationControls.monitorTypesForRollup);
  const setMonitorTypesForRollup = React.useCallback(
    (mt: MonitorTypeConfig[]) =>
      dispatch(
        setMonitorTypeForRollup({ stationName, monitorTypesForRollup: mt })
      ),
    [dispatch, stationName]
  );
  return [
    stationName ? allMonitorTypesForRollup[stationName] : undefined,
    setMonitorTypesForRollup,
  ];
};

/**
 * @returns initial configuration for all supported monitor types.
 * This includes the name and isIncluded, which is set if it is found in @param includedMonitorTypes
 */
const useInitialMonitorTypes = (includedMonitorTypes: string[]) => {
  const { data: appData } = useAppContext();
  const supportedMonitorTypes = appData?.supportedMonitorTypes;

  return React.useMemo(
    () =>
      includedMonitorTypes != null && supportedMonitorTypes != null
        ? supportedMonitorTypes.map((smt) => ({
            name: smt,
            isIncluded: includedMonitorTypes.includes(smt),
          }))
        : undefined,
    [includedMonitorTypes, supportedMonitorTypes]
  );
};

/**
 * Queries for the resolved monitor types for a station, and updates the redux store for the station config to
 * match that monitor type list.
 *
 * @param stationName the name of the station for which to resolve monitor types
 * @returns the station's resolved monitor types, and a setter for updating the redux store with these types.
 */
const useResolvedMonitorTypesForRollup = (
  stationName: string | null
): [
  MonitorTypeConfig[] | undefined,
  (mt: MonitorTypeConfig[]) => {
    payload: any;
    type: string;
  }
] => {
  const resolvedMonitorTypesForRollupStationQuery = useResolveStationConfig(
    {
      stationName: stationName ?? '',
      configName: 'soh-control.soh-monitor-types-for-rollup-station',
    },
    {
      skip: !stationName,
    }
  );
  const includedMonitorTypesForStation: string[] =
    resolvedMonitorTypesForRollupStationQuery.data?.sohMonitorTypesForRollup;

  const monitorTypeConfigs = useInitialMonitorTypes(
    includedMonitorTypesForStation
  );

  const [monitorTypesForRollup, setMonitorTypesForRollup] =
    useMonitorTypesForRollupForStation(stationName);

  // update the redux store with the query result for this station
  React.useEffect(() => {
    if (monitorTypeConfigs != null) {
      setMonitorTypesForRollup(monitorTypeConfigs);
    }
  }, [
    includedMonitorTypesForStation,
    monitorTypeConfigs,
    setMonitorTypesForRollup,
  ]);

  return [monitorTypesForRollup, setMonitorTypesForRollup];
};

/**
 * @param stationName the name of the station for which to return the monitor types
 * @returns an object containing:
 * @param selectedMonitorTypes the names of the monitor types that are selected in the checklist
 * @param supportedMonitorTypes the names of all monitor types that are supported (all checkboxes)
 * @param setMonitorTypesForRollup Sets the redux state to use the provided list of selected monitor types for this station.
 */
export const useMonitorTypesForRollup = (
  stationName: string | null
): {
  selectedMonitorTypes: MonitorTypeConfig[] | undefined;
  supportedMonitorTypes: string[];
  setMonitorTypesForRollup: (mt: MonitorTypeConfig[]) => {
    payload: any;
    type: string;
  };
} => {
  const { data: appData } = useAppContext();
  const [resolvedMonitorTypesForRollup, setMonitorTypesForRollup] =
    useResolvedMonitorTypesForRollup(stationName);
  const reduxMonitorTypesForRollup = useAppSelector(
    (state) => state.stationControls.monitorTypesForRollup
  );
  const monitorTypesForRollup =
    reduxMonitorTypesForRollup ?? resolvedMonitorTypesForRollup;
  return {
    selectedMonitorTypes: stationName
      ? monitorTypesForRollup[stationName]?.filter((mt) => mt.isIncluded)
      : undefined,
    supportedMonitorTypes: appData?.supportedMonitorTypes,
    setMonitorTypesForRollup,
  };
};

const useMonitorTypeNames = (mtc: MonitorTypeConfig[] | undefined) => {
  return React.useMemo<string[] | undefined>(
    () => mtc?.filter((mt) => mt.isIncluded)?.map((mt) => mt.name) ?? undefined,
    [mtc]
  );
};

const AccordionWrapper: React.FC<
  React.PropsWithChildren<{
    checkboxName: string;
  }>
> = (props) => {
  const classes = useStyles();
  const [updateErrorState] = useUpdateErrorState();
  const [updateDataLoaded] = useUpdateDataLoaded();
  const [determineMonitorHasError] = useMonitorHaveError();
  const [confirmToggle, setConfirmToggle] = React.useState({
    open: false,
    newlySelected: [''],
    checkbox: '',
  });
  const { allChannels, setSelectedChannels } = useSelectedChannels();
  const allChannelNames = allChannels.map((channel) => channel.name);
  const dispatch = useAppDispatch();
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const [updateAllMonitorsForChannelForAllCapabilityRollups] =
    useUpdateAllMonitorsForChannelForAllCapabilityRollups();
  const selectedMonitors = useSelectedMonitors();
  const allMonitorTypesForRollup: Record<string, MonitorTypeConfig[]> =
    useAppSelector((state) => state.stationControls.monitorTypesForRollup);
  const stationSelectedChannels: string[] = useAppSelector(
    (state) => state.stationControls.selectedChannels
  )[stationName ?? ''];

  const [getChannelsForMonitorThresholds, { isLoading }] =
    useResolveThresholdsForChannelsForMonitorInStation();

  const hasChannelsWithThresholdsLoadedIntoState = React.useRef(false);

  let selectedChannels: string[] = [];
  if (stationName && allMonitorTypesForRollup[stationName]) {
    const monitorTypeConfig: MonitorTypeConfig | undefined =
      allMonitorTypesForRollup[stationName].find(
        (monitorTypeRollup) => monitorTypeRollup.name === props.checkboxName
      );
    if (monitorTypeConfig && monitorTypeConfig.channelOverrides) {
      const channelOverrides: ChannelOverrides[] =
        monitorTypeConfig.channelOverrides;
      if (channelOverrides) {
        const channels = channelOverrides
          .map((channelOverride) => {
            if (
              channelOverride.isIncluded &&
              stationSelectedChannels.includes(channelOverride.name)
            ) {
              return channelOverride.name;
            }
            return undefined;
          })
          .filter((s) => s !== undefined);
        selectedChannels = channels as string[];
      }
    }
  }

  const updateMonitorTypeForRollup = React.useCallback(
    (monitorTypeConfigs: MonitorTypeConfig[]) => {
      updateDataLoaded(DataNames.CHANNEL_MONITOR_THRESHOLDS, true);
      dispatch(
        setMonitorTypeForRollup({
          stationName,
          monitorTypesForRollup: monitorTypeConfigs,
        })
      );
    },
    [dispatch, stationName, updateDataLoaded]
  );

  const updateData = (newlyChecked: string[], checkbox: string) => {
    const allMonitorTypesRollupConfig =
      allMonitorTypesForRollup[stationName ?? ''];
    if (allMonitorTypesRollupConfig) {
      const newConfig = updateMonitorChannelOverrides(
        allMonitorTypesRollupConfig,
        props.checkboxName,
        newlyChecked,
        checkbox
      );
      if (newlyChecked.length === 0) {
        updateErrorState(
          `checkbox_${props.checkboxName}`,
          true,
          `${props.checkboxName} must have one channel included`
        );
      } else {
        updateErrorState(`checkbox_${props.checkboxName}`, false, '');
      }
      if (!isSelectedChannelIncludedInAnyMonitor(newConfig, checkbox)) {
        setSelectedChannels(
          allChannelNames.filter((channel) => channel !== checkbox)
        );
      }
      // edit channel capability and update state to remove monitor for toggled channel for channel capability
      if (includes(selectedMonitors, props.checkboxName)) {
        updateAllMonitorsForChannelForAllCapabilityRollups(
          props.checkboxName,
          checkbox,
          includes(newlyChecked, checkbox)
        );
      }
      updateMonitorTypeForRollup(newConfig);
    }
  };
  const handleToggle = React.useCallback(
    (newlySelected: string[], checkbox: string) => {
      setConfirmToggle({ open: true, newlySelected, checkbox });
    },
    []
  );
  let disabledCheckboxes: string[] = [];
  allChannelNames.forEach((channelName) => {
    if (
      stationSelectedChannels &&
      !stationSelectedChannels.includes(channelName)
    ) {
      disabledCheckboxes.push(channelName);
    }
  });

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
          {`Warning: Modifying a Channel Monitor will cause the Monitor Type to be
          modified within the Channel Capability Rollup for the specific Channel under each Station Group. This action cannot be undone.`}
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
      <Accordion className={classes.accordion} disabled={false}>
        <AccordionSummary
          className={`${classes.accordionSummary} ${
            determineMonitorHasError(props.checkboxName)
              ? classes.errorBorder
              : ''
          }`}
          expandIcon={
            <div role='button' className={classes.expandButton}>
              <ExpandMoreIcon />
            </div>
          }
          onClick={() => {
            if (!hasChannelsWithThresholdsLoadedIntoState.current) {
              updateDataLoaded(DataNames.CHANNEL_MONITOR_THRESHOLDS, false);
              getChannelsForMonitorThresholds({
                stationName: stationName ?? '',
                monitorType: props.checkboxName,
                channelNames: allChannelNames,
              }).then((response) => {
                hasChannelsWithThresholdsLoadedIntoState.current = true;
                if (
                  response &&
                  response.data &&
                  stationName &&
                  allMonitorTypesForRollup[stationName]
                ) {
                  updateMonitorTypeForRollup(
                    addChannelsWithThresholdsForMonitorToConfig(
                      allMonitorTypesForRollup[stationName],
                      response.data,
                      props.checkboxName
                    )
                  );
                }
              });
            }
          }}
        >
          {props.children}
        </AccordionSummary>
        {isLoading ? (
          <CircularProgress size={'4rem'} />
        ) : (
          <AccordionDetails>
            <Typography variant={'h6'} className={classes.title}>
              Enable/Disable
            </Typography>
            <Checklist
              checkboxes={allChannelNames}
              checkedBoxes={selectedChannels}
              disabledCheckboxes={disabledCheckboxes}
              nonIdealState={'No channels found'}
              handleToggle={handleToggle}
              helpText={(channelName) =>
                `Check box results in channel ${channelName} for ${props.checkboxName} being excluded from the rollup to the Station Worst of Rollup`
              }
              disabledText={(channelName) =>
                `Channel ${channelName} is disabled for all monitors, enable it above to control it at this level`
              }
              renderRightElement={(channelName) => {
                const monitorTypeConfigs: MonitorTypeConfig[] | undefined =
                  allMonitorTypesForRollup[stationName ?? ''];
                const channelOverrides: ChannelOverrides[] | undefined =
                  monitorTypeConfigs?.find(
                    (monitorTypeRollup) =>
                      monitorTypeRollup.name === props.checkboxName
                  )?.channelOverrides;
                return (
                  <Thresholds
                    isLoading={isLoading}
                    isDuration={isDurationMonitor(props.checkboxName) ?? false}
                    parentName={props.checkboxName}
                    name={channelName}
                    helpTextGood={
                      'Values less than or equal to the Good Threshold will result in a Good status'
                    }
                    helpTextMarginal={
                      'Values less than or equal to the Marginal Threshold will result in a Marginal status'
                    }
                    thresholdsMap={convertChannelOverridesWithThresholdsToThresholdsMap(
                      channelOverrides
                    )}
                    updateGoodThreshold={(name: string, value: string) =>
                      updateMonitorTypeForRollup(
                        updateGoodThresholdForChannel(
                          props.checkboxName,
                          name,
                          value,
                          monitorTypeConfigs
                        )
                      )
                    }
                    updateMarginalThreshold={(name: string, value: string) =>
                      updateMonitorTypeForRollup(
                        updateMarginalThresholdForChannel(
                          props.checkboxName,
                          name,
                          value,
                          monitorTypeConfigs
                        )
                      )
                    }
                  />
                );
              }}
            />
          </AccordionDetails>
        )}
      </Accordion>
    </>
  );
};

/**
 * Creates a checklist of monitor types rollup for the station
 */
export const MonitorTypesRollup: React.FC<MonitorTypesRollupProps> = () => {
  const classes = useStyles();
  const [updateErrorState] = useUpdateErrorState();
  const { allChannels } = useSelectedChannels();
  const [updateDataLoaded] = useUpdateDataLoaded();
  const [confirmToggle, setConfirmToggle] = React.useState({
    open: false,
    newlySelected: [''],
    checkbox: '',
  });
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const hasMonitorThresholdsLoaded = useAppSelector(
    (state) => state.stationControls.loadedData[DataNames.MONITOR_THRESHOLDS]
  );
  const hasStationCapabilityRollupLoaded = useAppSelector(
    (state) =>
      state.stationControls.loadedData[DataNames.STATION_CAPABILITY_ROLLUP]
  );
  const hasChannelCapabilityRollupLoaded = useAppSelector(
    (state) =>
      state.stationControls.loadedData[DataNames.CHANNEL_CAPABILITY_ROLLUP]
  );

  const {
    selectedMonitorTypes,
    supportedMonitorTypes,
    setMonitorTypesForRollup,
  } = useMonitorTypesForRollup(stationName);

  const resolvedMonitorThresholds = useResolveThresholdsForMonitorsInStation(
    {
      stationName: stationName ?? '',
      monitorTypes: supportedMonitorTypes ?? [],
    },
    {
      skip:
        !stationName ||
        !supportedMonitorTypes ||
        !hasStationCapabilityRollupLoaded,
    }
  );

  const resolvedChannelsByMonitorType = useResolveChannelsByMonitorType(
    {
      stationName: stationName ?? '',
    },
    { skip: !stationName }
  );

  const [updateAllMonitorsForAllCapabilityRollups] =
    useUpdateAllMonitorsForAllCapabilityRollups();

  const monitorTypeNames = useMonitorTypeNames(selectedMonitorTypes);

  const data = useAppSelector(
    (state) => state.stationControls.monitorTypesForRollup
  );

  const updateData = React.useCallback(
    (newlyChecked: string[], checkbox: string) => {
      let newlySelectedMonitorTypes;
      if (selectedMonitorTypes != null) {
        newlySelectedMonitorTypes = getUpdatedMonitorTypes(
          selectedMonitorTypes,
          newlyChecked,
          supportedMonitorTypes,
          convertThresholdConfigToThresHoldMap(data[stationName ?? ''])
        );
      }
      if (newlySelectedMonitorTypes != null) {
        if (newlyChecked.length === 0) {
          updateErrorState('Monitors', true, `Must have one Monitor included`);
        } else {
          updateErrorState('Monitors', false, '');
        }
        updateAllMonitorsForAllCapabilityRollups(checkbox, newlyChecked);
        setMonitorTypesForRollup(newlySelectedMonitorTypes);
      } else {
        throw new Error('Unreachable state. See https://xkcd.com/2200/');
      }
    },
    [
      data,
      selectedMonitorTypes,
      setMonitorTypesForRollup,
      stationName,
      supportedMonitorTypes,
      updateAllMonitorsForAllCapabilityRollups,
      updateErrorState,
    ]
  );
  const handleToggle = React.useCallback(
    (newlySelected: string[], checkbox: string) => {
      setConfirmToggle({ open: true, newlySelected, checkbox });
    },
    []
  );

  React.useEffect(() => {
    if (
      resolvedMonitorThresholds.isSuccess &&
      resolvedMonitorThresholds.data &&
      resolvedMonitorThresholds.data.length > 0 &&
      resolvedChannelsByMonitorType.data &&
      !hasMonitorThresholdsLoaded
    ) {
      let configWithQueryData: MonitorTypeConfig[] = [];
      resolvedMonitorThresholds.data?.forEach((monitorThreshold) => {
        const channelOverrides: ChannelOverrides[] = allChannels.map(
          (channel) => ({
            name: channel.name,
            isIncluded: isChannelIncludedForMonitorType(
              resolvedChannelsByMonitorType.data,
              monitorThreshold.monitorType,
              channel.name
            ),
            goodThreshold: undefined,
            marginalThreshold: undefined,
          })
        );
        configWithQueryData.push({
          name: monitorThreshold.monitorType,
          goodThreshold: monitorThreshold.goodThreshold,
          marginalThreshold: monitorThreshold.marginalThreshold,
          isIncluded:
            monitorTypeNames?.find(
              (name) => name === monitorThreshold.monitorType
            ) === monitorThreshold.monitorType,
          channelOverrides,
        });
      });
      setMonitorTypesForRollup(configWithQueryData);
      updateDataLoaded(DataNames.MONITOR_THRESHOLDS, true);
    }
  }, [
    allChannels,
    hasMonitorThresholdsLoaded,
    monitorTypeNames,
    resolvedChannelsByMonitorType.data,
    resolvedMonitorThresholds.data,
    resolvedMonitorThresholds.isSuccess,
    setMonitorTypesForRollup,
    updateDataLoaded,
  ]);

  // Wraps the according wrapper allowing this variables scope to be passed
  const wrapperComponent: React.FC<
    React.PropsWithChildren<{ checkboxName: string }>
  > = React.useCallback(
    ({
      checkboxName,
      children,
    }: React.PropsWithChildren<{ checkboxName: string }>) =>
      hasMonitorThresholdsLoaded ? (
        <AccordionWrapper checkboxName={checkboxName}>
          {children}
        </AccordionWrapper>
      ) : (
        <CircularProgress size={'4rem'} />
      ),
    [hasMonitorThresholdsLoaded]
  );

  if (!stationName) {
    return null;
  }
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
          {`Warning: Modifying a Monitor Type will cause the Monitor Type to be
            modified under each Channel Capability Rollup under each Station Group. This action cannot be undone. `}
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
      <Checklist
        checkboxes={supportedMonitorTypes}
        checkedBoxes={monitorTypeNames}
        disabledCheckboxes={
          hasChannelCapabilityRollupLoaded ? [] : supportedMonitorTypes
        }
        disabledText={(monitorName) =>
          `${monitorName} can not be toggled until channel capabilities have loaded`
        }
        nonIdealState={'No station monitor types'}
        handleToggle={handleToggle}
        listItemClass={classes.listItem}
        buttonClass={classes.listButton}
        alignItems={'flex-start'}
        helpText={(monitorType) =>
          `Check box enables ${monitorType} to be included in the Station Worst Of Rollup`
        }
        WrapperComponent={wrapperComponent}
        renderRightElement={(monitorType) => {
          return (
            <Thresholds
              isLoading={resolvedMonitorThresholds.isLoading}
              isDuration={isDurationMonitor(monitorType) ?? false}
              name={monitorType}
              helpTextGood={helpTextGoodForStationThreshold}
              helpTextMarginal={helpTextMarginalForStationThreshold}
              thresholdsMap={convertMonitorTypeConfigToThresholdMap(
                data[stationName]
              )}
              updateGoodThreshold={(name: string, value: string) =>
                setMonitorTypesForRollup(
                  updateGoodThresholdForMonitor(name, value, data[stationName])
                )
              }
              updateMarginalThreshold={(name: string, value: string) =>
                setMonitorTypesForRollup(
                  updateMarginalThresholdForMonitor(
                    name,
                    value,
                    data[stationName]
                  )
                )
              }
            />
          );
        }}
      />
    </>
  );
};
