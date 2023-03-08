// TODO fix file scoped eslint disable
/* eslint-disable class-methods-use-this */
/* eslint-disable react/no-this-in-sfc */
/* eslint-disable @typescript-eslint/restrict-plus-operands */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable no-nested-ternary */
/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable react/destructuring-assignment */
import { Button, ContextMenu, Icon, Intent, Menu, MenuItem } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { EventTypes, StationTypes } from '@gms/common-model';
import { CommonTypes, SignalDetectionTypes, WaveformTypes, WorkflowTypes } from '@gms/common-model';
import { recordLength, Timer } from '@gms/common-util';
import { AnalystWaveformTypes, AnalystWorkspaceTypes } from '@gms/ui-state';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import { addGlUpdateOnResize, addGlUpdateOnShow, HotkeyListener, UILogger } from '@gms/ui-util';
import type { WeavessTypes } from '@gms/weavess-core';
import { WeavessUtil } from '@gms/weavess-core';
import produce from 'immer';
import defer from 'lodash/defer';
import find from 'lodash/find';
import includes from 'lodash/includes';
import isEqual from 'lodash/isEqual';
import memoizeOne from 'memoize-one';
import React, { startTransition } from 'react';
import { toast } from 'react-toastify';
import ResizeObserver from 'resize-observer-polyfill';

import { HideStationMenuItem } from '~analyst-ui/common/menus';
import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import {
  isPeakTroughInWarning,
  sortAndOrderSignalDetections
} from '~analyst-ui/common/utils/signal-detection-util';
import { toggleWaveformChannelFilters } from '~analyst-ui/common/utils/waveform-util';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import type { MaskDisplayFilter } from '~analyst-ui/config/user-preferences';

import { WaveformControls } from './components/waveform-controls';
import type { FixedScaleValue } from './components/waveform-controls/scaling-options';
import { AmplitudeScalingOptions } from './components/waveform-controls/scaling-options';
import { WaveformLoadingIndicator } from './components/waveform-loading-indicator';
import type { WaveformDisplayProps, WaveformDisplayState } from './types';
import { KeyDirection } from './types';
import { getStationContainingChannel } from './utils';
import type { WeavessContextData } from './weavess-context';
import { WeavessContext } from './weavess-context';
import { WeavessDisplay } from './weavess-display';
import * as WaveformUtil from './weavess-stations-util';

const logger = UILogger.create('GMS_LOG_WAVEFORM', process.env.GMS_LOG_WAVEFORM);

/**
 * Primary waveform display component.
 */
export class WaveformPanel extends React.PureComponent<WaveformDisplayProps, WaveformDisplayState> {
  /** The type of the Weavess context, so this component knows how it's typed */
  public static readonly contextType: React.Context<WeavessContextData> = WeavessContext;

  /** The Weavess context. We store a ref to our Weavess instance in here. */
  public declare readonly context: React.ContextType<typeof WeavessContext>;

  /** Index of currently selected filter */
  private selectedFilterIndex = -1;

  /** A Ref to the waveform display div */
  private waveformDisplayRef: HTMLDivElement | undefined;

  private readonly weavessConfiguration: WeavessTypes.Configuration;

  /** Last channel height set in weavess channels */
  private lastChannelHeight;

  /**
   * The custom callback functions that we want to pass down to weavess.
   */
  private readonly weavessEventHandlers: WeavessTypes.Events;

  private readonly memoizedGetSelections: (
    selectedChannels: string[]
  ) => {
    channels: string[];
  };

  private readonly resizeObserver: ResizeObserver;

  private isShuttingDown = false;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: WaveformDisplayProps) {
    super(props);
    this.resizeObserver = new ResizeObserver(() => {
      this.updateStationHeights();
      if (this.context.weavessRef) {
        this.context.weavessRef.refresh();
      }
    });
    this.lastChannelHeight = -1;

    this.memoizedGetSelections = memoizeOne(this.getSelections);
    this.weavessEventHandlers = this.buildWeavessEvents();
    this.weavessConfiguration = {
      shouldRenderWaveforms: true,
      shouldRenderSpectrograms: false,
      hotKeys: {
        amplitudeScale: systemConfig.defaultWeavessHotKeyOverrides.amplitudeScale,
        amplitudeScaleSingleReset:
          systemConfig.defaultWeavessHotKeyOverrides.amplitudeScaleSingleReset,
        amplitudeScaleReset: systemConfig.defaultWeavessHotKeyOverrides.amplitudeScaleReset,
        maskCreate: systemConfig.defaultWeavessHotKeyOverrides.qcMaskCreate
      },
      backgroundColor: this.props.uiTheme.colors.gmsBackground,
      waveformDimPercent: this.props.uiTheme.colors.waveformDimPercent,
      defaultChannel: {
        disableMeasureWindow: false,
        disableMaskModification: true
      },
      nonDefaultChannel: {
        disableMeasureWindow: false,
        disableMaskModification: false
      }
    };
    this.state = {
      weavessStations: [],
      loadingWaveforms: false,
      loadingWaveformsPercentComplete: 0,
      maskDisplayFilters: userPreferences.colors.maskDisplayFilters,
      analystNumberOfWaveforms:
        this.props.analysisMode === WorkflowTypes.AnalysisMode.EVENT_REVIEW
          ? systemConfig.eventRefinement.numberOfWaveforms
          : systemConfig.eventGlobalScan.numberOfWaveforms,
      // the range of waveform data displayed initially
      currentTimeInterval: props.currentTimeInterval,
      isMeasureWindowVisible: false,
      amplitudeScaleOption: AmplitudeScalingOptions.AUTO,
      currentOpenEventId: undefined,
      fixedScaleVal: 0,
      scaleAmplitudeChannelName: undefined,
      scaledAmplitudeChannelMinValue: -1,
      scaledAmplitudeChannelMaxValue: 1
    };
  }

  /**
   * Updates the derived state from the next props.
   *
   * @param nextProps The next (new) props
   * @param prevState The previous state
   */
  public static getDerivedStateFromProps(
    nextProps: WaveformDisplayProps,
    prevState: WaveformDisplayState
  ): Partial<WaveformDisplayState> {
    const hasTimeIntervalChanged = !isEqual(
      nextProps.currentTimeInterval,
      prevState.currentTimeInterval
    );

    if (hasTimeIntervalChanged || nextProps.currentOpenEventId !== prevState.currentOpenEventId) {
      return {
        weavessStations: hasTimeIntervalChanged ? [] : prevState.weavessStations,
        currentTimeInterval: nextProps.currentTimeInterval,
        currentOpenEventId: nextProps.currentOpenEventId
      };
    }

    // return null to indicate no change to state.
    return null;
  }

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount() {
    if (this.isShuttingDown) {
      return;
    }
    const callback = () => {
      this.forceUpdate();
      if (this.context.weavessRef) {
        this.context.weavessRef.refresh();
      }
    };
    addGlUpdateOnShow(this.props.glContainer, callback);
    addGlUpdateOnResize(this.props.glContainer, callback);
    this.updateWeavessStations();
    if (this.waveformDisplayRef) {
      this.resizeObserver.observe(this.waveformDisplayRef);
    }
  }

  /**
   * Invoked when the component has rendered.
   *
   * @param prevProps The previous props
   */
  public componentDidUpdate(prevProps) {
    this.maybeUpdateNumWaveforms(prevProps);

    this.maybeUpdateFilters();

    this.maybeUpdatePredictedPhases(prevProps);

    this.maybeUpdateBaseStationTime();

    if (
      prevProps.featurePredictionQuery !== this.props.featurePredictionQuery &&
      !this.props.featurePredictionQuery.data?.isRequestingDefault &&
      this.props.phaseToAlignOn === prevProps.phaseToAlignOn
    ) {
      this.zoomAlignSort();
    }

    if (
      prevProps.shouldShowTimeUncertainty !== this.props.shouldShowTimeUncertainty ||
      prevProps.shouldShowPredictedPhases !== this.props.shouldShowPredictedPhases ||
      prevProps.stationsVisibility !== this.props.stationsVisibility ||
      prevProps.channelSegments !== this.props.channelSegments ||
      prevProps.signalDetections !== this.props.signalDetections ||
      prevProps.selectedSortType !== this.props.selectedSortType ||
      prevProps.currentOpenEventId !== this.props.currentOpenEventId ||
      prevProps.selectedSdIds !== this.props.selectedSdIds ||
      prevProps.phaseToAlignOn !== this.props.phaseToAlignOn ||
      prevProps.alignWaveformsOn !== this.props.alignWaveformsOn ||
      prevProps.baseStationTime !== this.props.baseStationTime
    ) {
      this.updateWeavessStations();
    }
  }

  /**
   * Cleanup and stop any in progress Waveform queries
   */
  public componentWillUnmount(): void {
    this.resizeObserver.unobserve(this.waveformDisplayRef);
    this.isShuttingDown = true;
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Checks the analysis mode, and sets waveforms display amount based on result
   */
  private readonly maybeUpdateNumWaveforms = (prevProps: WaveformDisplayProps) => {
    if (this.props.analysisMode !== prevProps.analysisMode) {
      const numWaveforms =
        this.props.analysisMode === WorkflowTypes.AnalysisMode.EVENT_REVIEW
          ? systemConfig.eventRefinement.numberOfWaveforms
          : systemConfig.eventGlobalScan.numberOfWaveforms;
      this.setAnalystNumberOfWaveforms(numWaveforms);
    }
  };

  /**
   * ! legacy code
   * Legacy code that toggles filters on key press
   */
  private readonly maybeUpdateFilters = () => {
    if (this.props.keyPressActionQueue && this.props.keyPressActionQueue.get) {
      const maybeToggleUp = this.props.keyPressActionQueue[
        AnalystWorkspaceTypes.AnalystKeyAction.TOGGLE_FILTERS_UP
      ];
      // eslint-disable-next-line no-restricted-globals
      if (!isNaN(maybeToggleUp) && maybeToggleUp > 0) {
        this.handleChannelFilterToggle(KeyDirection.UP);
        this.props.setKeyPressActionQueue(
          produce(this.props.keyPressActionQueue, draft => {
            draft[AnalystWorkspaceTypes.AnalystKeyAction.TOGGLE_FILTERS_UP] = maybeToggleUp - 1;
          })
        );
      }
      const maybeToggleDown = this.props.keyPressActionQueue[
        AnalystWorkspaceTypes.AnalystKeyAction.TOGGLE_FILTERS_DOWN
      ];
      // eslint-disable-next-line no-restricted-globals
      if (!isNaN(maybeToggleDown) && maybeToggleDown > 0) {
        this.handleChannelFilterToggle(KeyDirection.DOWN);
        this.props.setKeyPressActionQueue(
          produce(this.props.keyPressActionQueue, draft => {
            draft[AnalystWorkspaceTypes.AnalystKeyAction.TOGGLE_FILTERS_DOWN] = maybeToggleDown - 1;
          })
        );
      }
    }
  };

  /**
   * If predicted phases have changed, force update weavess stations
   */
  private readonly maybeUpdatePredictedPhases = (prevProps: WaveformDisplayProps) => {
    if (
      this.props.shouldShowPredictedPhases &&
      prevProps.featurePredictionQuery.data?.receiverLocationsByName !==
        this.props.featurePredictionQuery.data?.receiverLocationsByName
    ) {
      this.updateWeavessStations();
    }
  };

  /**
   * If offsets have changed, force update viewableRange
   */
  private readonly maybeUpdateOffset = () => {
    if (this.state.weavessStations.length > 0) {
      const maxOffset = Math.max(
        ...this.state.weavessStations.map(station =>
          Math.max(
            station.defaultChannel.timeOffsetSeconds,
            ...(station.areChannelsShowing
              ? station.nonDefaultChannels.map(c => c.timeOffsetSeconds)
              : [])
          )
        )
      );

      const minOffset = Math.min(
        ...this.state.weavessStations.map(station =>
          Math.min(
            station.defaultChannel.timeOffsetSeconds,
            ...(station.areChannelsShowing
              ? station.nonDefaultChannels.map(c => c.timeOffsetSeconds)
              : [])
          )
        )
      );

      if (this.props.minimumOffset !== minOffset) {
        this.props.setMinimumOffset(minOffset);
      }
      if (this.props.maximumOffset !== maxOffset) {
        this.props.setMaximumOffset(maxOffset);
      }
    }
  };

  private readonly maybeUpdateBaseStationTime = () => {
    if (this.state.weavessStations.length > 0) {
      const { baseStationTime } = this.state.weavessStations[0].defaultChannel;
      if (this.props.baseStationTime !== baseStationTime) {
        this.props.setBaseStationTime(baseStationTime);
      }
    }
  };

  /**
   * @returns the list of stations from the station definition query. This result is memoized
   * so that the list is referentially stable between renders if the result of the query has
   * not changed. Note that this list can be empty.
   */
  private readonly getStations = (): StationTypes.Station[] => this.props.stationsQuery.data;

  /**
   * @returns a list of all of the weavess stations that are visible (should be rendered,
   * not necessarily on screen).
   */
  private readonly getAllVisibleWeavessStations = () =>
    this.state.weavessStations.filter(weavessStation =>
      this.props.isStationVisible(weavessStation.name)
    );

  private readonly getSelections = (channels: string[]) => ({
    channels
  });

  /**
   * ! Legacy Code
   * Returns the current open event.
   */
  private readonly currentOpenEvent = (): EventTypes.Event =>
    this.props.events ?? []
      ? this.props.events.find(e => e.id === this.props.currentOpenEventId)
      : undefined;

  /**
   * ! Legacy Code
   * Returns the weavess event handler configuration.
   *
   * @returns the events
   */
  private readonly buildWeavessEvents = (): WeavessTypes.Events => {
    const channelEvents: WeavessTypes.ChannelEvents = {
      labelEvents: {
        onChannelExpanded: this.onStationExpanded,
        onChannelCollapsed: this.onStationCollapse,
        onContextMenu: this.onLabelContextMenu
      },
      events: {
        onMeasureWindowUpdated: this.onMeasureWindowUpdated
      },
      onKeyPress: this.onChannelKeyPress
    };

    return {
      onZoomChange: this.onZoomChange,
      stationEvents: {
        defaultChannelEvents: channelEvents,
        nonDefaultChannelEvents: channelEvents
      },
      onMeasureWindowResize: this.onMeasureWindowResize,
      onResetAmplitude: this.resetChannelScaledAmplitude,
      onMount: this.props.onWeavessMount
    };
  };

  /**
   * Returns a custom measure window label for measurement mode.
   *
   * @returns a custom measure window label
   */
  private readonly getCustomMeasureWindowLabel = (): React.FunctionComponent<
    WeavessTypes.LabelProps
  > => {
    if (this.props.measurementMode.mode === AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT) {
      // TODO PERFORMANCE ISSUE
      // eslint-disable-next-line react/display-name, react/no-unstable-nested-components
      React.memo((props: WeavessTypes.LabelProps) => {
        return this.buildCustomMeasureWindowLabel(props);
      });
    }
    return undefined;
  };

  /**
   * Returns a custom measure window label for measurement mode.
   *
   * @returns a custom measure window label
   */
  private readonly buildCustomMeasureWindowLabel = (
    props: WeavessTypes.LabelProps
  ): JSX.Element => {
    if (this.props.measurementMode.mode === AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT) {
      // Find selected signal detection
      let sdId: string;
      if (this.getSignalDetections() && this.props.selectedSdIds.length === 1) {
        sdId = this.getSignalDetections()
          .map(s => s.id)
          .find(id => id === this.props.selectedSdIds[0]);
      }

      let sd: SignalDetectionTypes.SignalDetection;
      if (sdId) {
        sd = this.getSignalDetections().find(s => s.id === sdId);
      }

      // If Signal detection not found return
      if (!sd) {
        // eslint-disable-next-line react/jsx-no-useless-fragment
        return <>{props.channel.name}</>;
      }

      // If got this far there is a signal detection selected
      const amplitudeMeasurementValue: SignalDetectionTypes.AmplitudeMeasurementValue = SignalDetectionUtils.findAmplitudeFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
          .featureMeasurements,
        SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE_A5_OVER_2
      );

      const arrivalTime: number = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
          .featureMeasurements
      ).arrivalTime.value;

      let amplitude: number;
      let period: number;
      let troughTime: number;
      let peakTime: number;
      let isWarning = true;
      let periodTitle = 'Error: No measurement value available for period';
      let amplitudeTitle = 'Error: No measurement value available for amplitude';
      if (amplitudeMeasurementValue) {
        amplitude = amplitudeMeasurementValue.amplitude.value;
        period = amplitudeMeasurementValue.period;
        troughTime = amplitudeMeasurementValue.startTime;
        peakTime = troughTime + period / 2; // display only period/2
        isWarning = isPeakTroughInWarning(arrivalTime, period, troughTime, peakTime);
        periodTitle = !isWarning
          ? 'Period value'
          : `Warning: Period value must be between` +
            `[${systemConfig.measurementMode.peakTroughSelection.warning.min} - ` +
            `${systemConfig.measurementMode.peakTroughSelection.warning.max}]'`;
        amplitudeTitle = 'Amplitude value';
      }

      const warningIcon = isWarning ? (
        <Icon
          title={periodTitle}
          icon={IconNames.WARNING_SIGN}
          color={systemConfig.measurementMode.peakTroughSelection.warning.textColor}
        />
      ) : undefined;

      return this.buildMeasureWindowDiv(
        props,
        amplitudeMeasurementValue,
        amplitude,
        amplitudeTitle,
        periodTitle,
        sd.id,
        warningIcon
      );
    }
    return undefined;
  };

  /**
   * Builds the measure window div if amplitude feature measurement
   * was found in currently selected Signal Detection
   *
   * @param props
   * @param amplitudeMeasurementValue
   * @param amplitude
   * @param amplitudeTitle
   * @param periodTitle
   * @param signalDetectionId
   * @param warningIcon
   * @returns JSX.Element
   */
  private readonly buildMeasureWindowDiv = (
    props: WeavessTypes.LabelProps,
    amplitudeMeasurementValue: SignalDetectionTypes.AmplitudeMeasurementValue,
    amplitude: number,
    amplitudeTitle: string,
    periodTitle: string,
    signalDetectionId: string,
    warningIcon: JSX.Element
  ): JSX.Element => {
    return (
      <>
        {props.channel.name}
        <>
          <br />
          <div title={amplitudeTitle} style={{ whiteSpace: 'nowrap' }}>
            A5/2:&nbsp;
            {amplitudeMeasurementValue ? (
              // eslint-disable-next-line @typescript-eslint/no-magic-numbers
              amplitude.toFixed(3)
            ) : (
              <Icon title={amplitudeTitle} icon={IconNames.ERROR} intent={Intent.DANGER} />
            )}
          </div>
          <div title={periodTitle} style={{ whiteSpace: 'nowrap' }}>
            Period:
            {amplitudeMeasurementValue ? (
              <span
                title={periodTitle}
                style={{
                  color: warningIcon
                    ? systemConfig.measurementMode.peakTroughSelection.warning.textColor
                    : undefined
                }}
              >
                {' '}
                {amplitudeMeasurementValue.period.toFixed(3)}
                s&nbsp;
                {warningIcon}
              </span>
            ) : (
              <Icon title={periodTitle} icon={IconNames.ERROR} intent={Intent.DANGER} />
            )}
          </div>
          <Button
            small
            text="Next"
            onClick={(event: React.MouseEvent<HTMLElement>) => {
              event.stopPropagation();
              this.selectNextAmplitudeMeasurement(signalDetectionId);
              // eslint-disable-next-line @typescript-eslint/no-floating-promises
              this.props
                .markAmplitudeMeasurementReviewed({
                  variables: { signalDetectionIds: [signalDetectionId] }
                })
                .catch(e => {
                  logger.error(`failed to mark amplitude as reviewed: ${e}`);
                });
            }}
          />
        </>
      </>
    );
  };

  /**
   * ! Legacy Code
   * Sets the mode.
   *
   * @param mode the mode configuration to set
   */
  private readonly setMode = (mode: AnalystWorkspaceTypes.WaveformDisplayMode) => {
    this.props.setMode(mode);

    // auto select the first signal detection if switching to MEASUREMENT mode
    if (mode === AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT) {
      const currentOpenEvent = this.currentOpenEvent();

      if (currentOpenEvent) {
        const associatedSignalDetectionHypothesisIds = currentOpenEvent.overallPreferred?.associatedSignalDetectionHypotheses
          // .filter(hypo => !hypo.rejected) broken legacy code
          .map(hypothesis => hypothesis.id.id);

        const signalDetections = this.getSignalDetections().filter(sd =>
          this.checkIfSdIsFmPhaseAndAssociated(sd, associatedSignalDetectionHypothesisIds)
        );

        let signalDetectionToSelect: SignalDetectionTypes.SignalDetection;
        // Broken legacy code data types have changed
        const distances = []; // getDistanceToStationsForPreferredLocationSolutionId(currentOpenEvent,this.getStations(),this.props.currentStageName,signalDetections);
        if (signalDetections.length > 0) {
          // sort the signal detections
          const sortedEntries = sortAndOrderSignalDetections(
            signalDetections,
            this.props.selectedSortType,
            distances
          );
          signalDetectionToSelect = sortedEntries.shift();
          this.props.setSelectedSdIds([signalDetectionToSelect.id]);
        } else {
          this.props.setSelectedSdIds([]);
        }

        // mark the measure window as being visible; measurement mode auto shows the measure window
        this.setState({ isMeasureWindowVisible: true });
        // auto set the waveform alignment to align on the default phase
        this.setWaveformAlignment(
          AlignWaveformsOn.PREDICTED_PHASE,
          this.props.defaultSignalDetectionPhase,
          this.props.shouldShowPredictedPhases
        );

        // auto zoom the waveform display to match the zoom of the measure window
        if (signalDetectionToSelect) {
          const arrivalTime: number = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
            SignalDetectionTypes.Util.getCurrentHypothesis(
              signalDetectionToSelect.signalDetectionHypotheses
            ).featureMeasurements
          ).arrivalTime.value;
          const {
            startTimeOffsetFromSignalDetection
          } = systemConfig.measurementMode.displayTimeRange;
          const {
            endTimeOffsetFromSignalDetection
          } = systemConfig.measurementMode.displayTimeRange;
          const startTimeSecs = arrivalTime + startTimeOffsetFromSignalDetection;
          const endTimeSecs = arrivalTime + endTimeOffsetFromSignalDetection;

          // adjust the zoom time window for the selected alignment
          this.onZoomChange({ startTimeSecs, endTimeSecs });
        }
      }
    } else {
      // leaving measurement mode; mark the measurement window as not visible
      this.setState({ isMeasureWindowVisible: false });
    }
  };

  /**
   * ! Legacy Code
   * Check if the signal detection is FM Phase and Associated.
   *
   * @param sd the signal detection
   * @param associatedSignalDetectionHypothesisIds string ids
   * @returns a boolean determining if sd is associated and a measurement phase
   */
  private readonly checkIfSdIsFmPhaseAndAssociated = (
    sd: SignalDetectionTypes.SignalDetection,
    associatedSignalDetectionHypothesisIds: string[]
  ): boolean => {
    const phase = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
        .featureMeasurements
    ).value;
    // return if associated and a measurement phase
    return (
      includes(
        associatedSignalDetectionHypothesisIds,
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).id.id
      ) && includes(systemConfig.measurementMode.phases, phase)
    );
  };

  /**
   * Updates the weavess stations based on the current state and props.
   * ! This is an expensive operation so use this function sparingly
   */
  private readonly updateWeavessStations = () => {
    if (
      !this.props.currentTimeInterval ||
      !this.props.stationsQuery.data ||
      !this.props.processingAnalystConfigurationQuery?.data
    ) {
      return;
    }
    startTransition(() => {
      Timer.start('[waveform] update weavess stations and build params');
      const stationHeight = this.calculateStationHeight();
      const createWeavessStationsParameters = WaveformUtil.populateCreateWeavessStationsParameters(
        this.props,
        this.state,
        stationHeight
      );
      // Set the height used to create weavess stations
      this.lastChannelHeight = stationHeight;
      // Set the newly created Weavess Stations on the state
      this.setState(
        prevState => ({
          weavessStations: WaveformUtil.createWeavessStations(
            createWeavessStationsParameters,
            this.props.selectedSortType,
            prevState.weavessStations
          )
        }),
        () => {
          this.maybeUpdateOffset();
        }
      );
      Timer.end('[waveform] update weavess stations and build params');
    });
  };

  /**
   * Toggle the measure window visibility within weavess.
   */
  private readonly toggleMeasureWindowVisibility = () => {
    if (this.context && this.context.weavessRef) {
      this.context.weavessRef.toggleMeasureWindowVisibility();
      // we use defer to ensure that the weavess state updates have occurred before we
      // make any changes to our station height, which will be different with the measure
      // window open vs closed.
      defer(this.updateWeavessStations);
    }
  };

  /**
   * The function for injecting a right click context menu for labels into weavess
   *
   * @param e the mouse click event, used to determine menu position
   * @param channelId
   * @param isDefaultChannel describes weather a weavess top-level channel (station) has been clicked or a weavess sub-channel (channel) has been clicked
   */
  private readonly onLabelContextMenu = (
    e: React.MouseEvent<HTMLDivElement>,
    channelId: string,
    amplitudeMinValue: number,
    amplitudeMaxValue: number,
    isDefaultChannel: boolean
  ) => {
    const showHideMenuItem = (
      <HideStationMenuItem
        stationName={channelId}
        hideStationCallback={() => {
          if (isDefaultChannel) {
            this.hideStation(channelId);
          } else {
            throw new Error(`Hide station context menu should not be used on channel ${channelId}`);
          }
        }}
      />
    );

    /** Find the WeavessChannel to check if a waveform is loaded */
    const weavessChannel: WeavessTypes.Channel = WeavessUtil.findChannelInStations(
      this.state.weavessStations,
      channelId
    );

    // Check to see if there is a waveform loaded
    let disabledScaleAllChannel = true;
    if (weavessChannel) {
      disabledScaleAllChannel = recordLength(weavessChannel?.waveform?.channelSegmentsRecord) === 0;
    }

    const scaleAmplitudeChannelMenuItem = (
      <MenuItem
        data-cy="scale-all-channels"
        text="Scale all channels to match this one"
        disabled={disabledScaleAllChannel}
        onClick={() => {
          this.scaleAllAmplitudes(channelId, amplitudeMinValue, amplitudeMaxValue);
        }}
      />
    );

    const hideChannelMenuItem = (
      <MenuItem
        data-cy={`hide-${channelId}`}
        text={`Hide ${channelId}`}
        onClick={() => {
          // Can't hide stations thru this context menu
          // Shouldn't happen since menu item is not installed on default channels
          if (isDefaultChannel) {
            throw new Error(`Hide channel context menu should not be used on station ${channelId}`);
          }
          this.hideChannel(channelId);
        }}
      />
    );

    const showAllHiddenChannelsMenuItem = (
      <MenuItem
        data-cy="show-hidden-channels"
        text="Show all hidden channels"
        onClick={() => {
          // Only show all hidden channels from a default channel (station)
          if (!isDefaultChannel) {
            throw new Error(
              `Show all channels context menu should not be used on a child channel ${channelId}`
            );
          }
          this.showAllChannels(channelId);
        }}
      />
    );
    const menu = isDefaultChannel ? (
      <Menu>
        {showHideMenuItem} {showAllHiddenChannelsMenuItem} {scaleAmplitudeChannelMenuItem}
      </Menu>
    ) : (
      <Menu>
        {hideChannelMenuItem} {scaleAmplitudeChannelMenuItem}
      </Menu>
    );

    // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
    ContextMenu.show(menu, { left: e.clientX, top: e.clientY }, undefined, true);
  };

  /**
   * Event handler for station expansion. Will set the areChannelsShowing flag as true
   * then it calls load waveforms for all the station's channels.
   *
   * @param stationName the name of the expanded station
   */
  private readonly onStationExpanded = (stationName: string) => {
    this.props.setStationExpanded(stationName);
  };

  /**
   * Event handler for station collapsing. Sets the station visibility changes object
   * to be collapsed.
   *
   * @param stationName the name of the collapsed station
   */
  private readonly onStationCollapse = (stationName: string) => {
    this.props.setStationExpanded(stationName, false);
  };

  /**
   * Event handler that is invoked and handled when the Measure Window is updated.
   *
   * @param isVisible true if the measure window is updated
   */
  private readonly onMeasureWindowUpdated = (isVisible: boolean) => {
    this.setState({
      isMeasureWindowVisible: isVisible
    });
  };

  /**
   * Callback passed to Weavess for when the measure window is resized.
   *
   * @param heightPx the height of the new measure window. This is unused in this, but is
   * provided by Weavess
   */
  private readonly onMeasureWindowResize = (heightPx: number) => {
    this.updateWeavessStations();
  };

  /**
   * Event handler for when a key is pressed within a channel
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param clientX x location of where the key was pressed
   * @param clientY y location of where the key was pressed
   */
  private readonly onChannelKeyPress = (
    e: React.KeyboardEvent<HTMLDivElement>,
    clientX?: number,
    clientY?: number
  ) => {
    /* No Op for Now */
  };

  /**
   * @returns true if the event includes a panning key (a or d, or shift a or shift d).
   */
  private readonly isPanningKey = (e: React.KeyboardEvent) => {
    if (e.altKey || e.ctrlKey) return false;
    return (
      (e.shiftKey && e.key.toLowerCase() === 'a') ||
      (e.shiftKey && e.key.toLowerCase() === 'd') ||
      e.key.toLowerCase() === 'a' ||
      e.key.toLowerCase() === 'd'
    );
  };

  /**
   * Pans left or right, and correctly determines if new data should load.
   */
  private readonly handlePanKey = (e: React.KeyboardEvent) => {
    if (e.shiftKey && e.key.toLowerCase() === 'a') {
      this.pan(WaveformTypes.PanType.Left, true);
    } else if (e.shiftKey && e.key.toLowerCase() === 'd') {
      this.pan(WaveformTypes.PanType.Right, true);
    }
  };

  /**
   * Event handler for when a key is pressed
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param clientX x location of where the key was pressed
   * @param clientY y location of where the key was pressed
   */
  private readonly onKeyPress = (
    e: React.KeyboardEvent<HTMLDivElement>,
    clientX?: number,
    clientY?: number
  ) => {
    // Hot key definitions
    const amplitudeScaleAllChannelHotKey =
      systemConfig.defaultWeavessHotKeyOverrides.amplitudeScaleAllChannel;
    const signalDetectionUncertaintyToggleHotKey =
      systemConfig.defaultWaveformHotKeyOverrides.signalDetectionUncertaintyToggle;
    if (this.isPanningKey(e)) {
      this.handlePanKey(e);
    } else if (
      // check for scale all channels to selected channel
      amplitudeScaleAllChannelHotKey &&
      HotkeyListener.isHotKeyCommandSatisfied(e.nativeEvent, amplitudeScaleAllChannelHotKey)
    ) {
      this.scaleAllAmplitudesUsingSelectedChannel();
    } else if (
      signalDetectionUncertaintyToggleHotKey &&
      HotkeyListener.isHotKeyCommandSatisfied(e.nativeEvent, signalDetectionUncertaintyToggleHotKey)
    ) {
      this.props.setShouldShowTimeUncertainty(!this.props.shouldShowTimeUncertainty);
    } else if (e.key === 'Escape' && this.state.isMeasureWindowVisible) {
      this.toggleMeasureWindowVisibility();
    } else if (e.altKey) {
      this.onAltKeyPress(e);
    } else if (e.ctrlKey || e.metaKey) {
      this.onCtrlOrMetaKeyPress(e);
    } else if (e.key.toLowerCase() === 'p') {
      this.onAlignHotkey();
    }
  };

  /**
   *
   * Process a zoom hotkey.  Keypress has already been validated so no params needed
   *
   */
  private readonly onAlignHotkey = () => {
    const defaultPhaseAlignment =
      this.props.processingAnalystConfigurationQuery.data.zasDefaultAlignmentPhase ??
      CommonTypes.PhaseType.P;
    if (this.props.currentOpenEventId) {
      if (
        this.props.alignWaveformsOn === AlignWaveformsOn.TIME ||
        this.props.alignWaveformsOn === AlignWaveformsOn.OBSERVED_PHASE ||
        this.props.phaseToAlignOn !== defaultPhaseAlignment
      ) {
        this.setWaveformAlignment(AlignWaveformsOn.PREDICTED_PHASE, defaultPhaseAlignment, true);
      } else {
        this.setWaveformAlignment(
          AlignWaveformsOn.TIME,
          undefined,
          this.props.shouldShowPredictedPhases
        );
      }
    } else {
      toast.info('Open an event to change waveform alignment', {
        toastId: 'Open an event to change waveform alignment'
      });
    }
  };

  /**
   * ! Legacy Code - setters don't do anything
   * Process key press if alt key is also held down
   *
   * @param e
   */
  private readonly onAltKeyPress = (e: React.KeyboardEvent<HTMLDivElement>): void => {
    switch (e.nativeEvent.code) {
      case 'KeyN':
        this.selectNextAmplitudeMeasurement(this.props.selectedSdIds[0]);
        break;
      case 'KeyA':
        toast.info('Alignment is disabled');
        break;
      default:
    }
  };

  /**
   * Process key press if ctrl or meta key is also held down
   *
   * @param e
   */
  private readonly onCtrlOrMetaKeyPress = (e: React.KeyboardEvent<HTMLDivElement>): void => {
    switch (e.key) {
      case '-':
        this.setAnalystNumberOfWaveforms(this.state.analystNumberOfWaveforms + 1);
        break;
      case '=':
        this.setAnalystNumberOfWaveforms(this.state.analystNumberOfWaveforms - 1);
        break;
      case 'ArrowLeft':
        this.pan(WaveformTypes.PanType.Left, false);
        e.preventDefault();
        break;
      case 'ArrowRight':
        this.pan(WaveformTypes.PanType.Right, false);
        e.preventDefault();
        break;
      default:
      // no-op
    }
  };

  /**
   * ! Legacy Code
   * Updates the value of the selected filter index
   *
   * @param index index value
   */
  private readonly setSelectedFilterIndex = (index: number): void => {
    this.selectedFilterIndex = index;
  };

  /**
   * ! Legacy Code
   * Set the mask filters selected in the qc mask legend.
   *
   * @param key the unique key identifier
   * @param maskDisplayFilter the mask display filter
   */
  private readonly setMaskDisplayFilters = (key: string, maskDisplayFilter: MaskDisplayFilter) => {
    const prevMaskDisplayFilters = this.state.maskDisplayFilters;
    this.setState(
      {
        maskDisplayFilters: {
          ...prevMaskDisplayFilters,
          [key]: maskDisplayFilter
        }
      },
      this.updateWeavessStations
    );
  };

  /**
   * ! Legacy Code
   * Select the next amplitude measurement when in measurement mode
   *
   * @param signalDetectionId current selected signal detection Id
   */
  private readonly selectNextAmplitudeMeasurement = (signalDetectionId: string): void => {
    if (this.props.measurementMode.mode !== AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT) {
      return;
    }

    const currentOpenEvent = this.currentOpenEvent();
    if (currentOpenEvent) {
      // eslint-disable-next-line max-len
      const associatedSignalDetectionHypothesisIds = currentOpenEvent.overallPreferred.associatedSignalDetectionHypotheses
        // .filter(hypo => !hypo.rejected) broken legacy code
        .map(hypothesis => hypothesis.id.id);

      // get all of the signal detections for the viewable stations
      const stationIds = this.props.stationsQuery.data.map(station => station.name);
      const signalDetections = this.getSignalDetections().filter(sd =>
        includes(stationIds, sd.station.name)
      );
      // Broken legacy code data types have changed
      const distances = []; // getDistanceToStationsForPreferredLocationSolutionId(currentOpenEvent,this.getStations(),this.props.currentStageName,signalDetections);
      // sort the signal detections
      const sortedEntries = sortAndOrderSignalDetections(
        signalDetections,
        this.props.selectedSortType,
        distances
      );

      let nextSignalDetectionToSelect: SignalDetectionTypes.SignalDetection;
      if (sortedEntries.length > 0) {
        const foundIndex: number = sortedEntries.findIndex(sd => sd.id === signalDetectionId);
        let index = foundIndex + 1;
        if (index >= sortedEntries.length) {
          index = 0;
        }

        const isAssociatedSdAndInPhaseList = (sd: SignalDetectionTypes.SignalDetection) =>
          this.checkIfSdIsFmPhaseAndAssociated(sd, associatedSignalDetectionHypothesisIds);

        // ensure that the selected index is for an associated signal detection and in the
        // list of phase measurements; increment until start searching from the current index found above
        nextSignalDetectionToSelect = find(sortedEntries, isAssociatedSdAndInPhaseList, index);

        // if the signal detection id is undefined, continue searching, but at index 0
        if (!nextSignalDetectionToSelect) {
          nextSignalDetectionToSelect = find(sortedEntries, isAssociatedSdAndInPhaseList);
        }
      }
      this.props.setSelectedSdIds([nextSignalDetectionToSelect.id]);
    }
  };

  /**
   * Display the number of waveforms chosen by the analyst
   * Also updates the state variable containing the Weavess stations
   */
  private readonly updateStationHeights = (): void => {
    this.setState(
      produce<WaveformDisplayState>(draft => {
        const height = this.calculateStationHeight();

        if (this.lastChannelHeight !== height) {
          this.lastChannelHeight = height;

          draft.weavessStations.forEach((station, stationIndex) => {
            draft.weavessStations[stationIndex].defaultChannel.height = height;

            if (station.nonDefaultChannels && station.nonDefaultChannels.length > 0) {
              draft.weavessStations[stationIndex].nonDefaultChannels.forEach(
                (nonDefaultChannel, index) => {
                  draft.weavessStations[stationIndex].nonDefaultChannels[index].height = height;
                }
              );
            }
          });
        }
      })
    );
  };

  /**
   * Calculate height for the station based of number of display
   */
  private readonly calculateStationHeight = (): number => {
    const canvasBoundingRect = this.context.weavessRef?.waveformPanelRef?.getCanvasBoundingClientRect();
    let height;
    if (canvasBoundingRect?.height) {
      height = canvasBoundingRect.height / this.state.analystNumberOfWaveforms - 1; // account for 1 pixel border
    } else if (this.lastChannelHeight > 0) {
      height = this.lastChannelHeight;
    } else {
      logger.warn(`Failed to calculate station heights falling back to system default`);
      height = systemConfig.defaultWeavessConfiguration.stationHeightPx;
    }
    return Math.round(height);
  };

  /**
   *
   * Sets the waveform alignment and adjust the sort type if necessary.
   *
   * @param alignWaveformsOn the waveform alignment setting
   * @param phaseToAlignOn the phase to align on
   * @param shouldShowPredictedPhases true if predicted phases should be displayed
   */
  private readonly setWaveformAlignment = (
    alignWaveformsOn: AlignWaveformsOn,
    phaseToAlignOn: CommonTypes.PhaseType,
    shouldShowPredictedPhases: boolean
  ) => {
    this.props.setAlignWaveformsOn(alignWaveformsOn);
    this.props.setPhaseToAlignOn(phaseToAlignOn);
    this.props.setShouldShowPredictedPhases(shouldShowPredictedPhases);
  };

  /**
   * Sets the number of waveforms to be displayed.
   *
   * @param value the number of waveforms to display (number)
   * @param valueAsString the number of waveforms to display (string)
   */
  public readonly setAnalystNumberOfWaveforms = (value: number, valueAsString?: string): void => {
    const base = 10;
    let analystNumberOfWaveforms = value;

    if (valueAsString) {
      // eslint-disable-next-line no-param-reassign
      valueAsString = valueAsString.replace(/e|\+|-/, '');
      // eslint-disable-next-line no-restricted-globals
      analystNumberOfWaveforms = isNaN(parseInt(valueAsString, base))
        ? this.state.analystNumberOfWaveforms
        : parseInt(valueAsString, base);
    }

    // Minimum number of waveforms must be 1
    if (analystNumberOfWaveforms < 1) {
      analystNumberOfWaveforms = 1;
    }

    if (this.state.analystNumberOfWaveforms !== analystNumberOfWaveforms) {
      this.setState(
        {
          analystNumberOfWaveforms
        },
        () => {
          this.updateStationHeights();
        }
      );
    }
  };

  /**
   * Toasts a notification when the user hits the panning boundary.
   */
  private readonly toastPanningBoundaryReached = () =>
    toast.info(`Panning boundary reached`, { toastId: 'panningBoundaryReached' });

  /**
   * Pan the waveform display if more data is needed the pan method
   * call will dispatch the new viewable interval. The returned zoom interval
   * is commanded in the Weavess' waveform panel.
   *
   * @param panDirection the pan direction
   */
  private readonly pan = (
    panDirection: WaveformTypes.PanType,
    shouldLoadAdditionalData: boolean
  ) => {
    if (this.context.weavessRef?.waveformPanelRef) {
      const zoomInterval = this.props.pan(panDirection, {
        shouldLoadAdditionalData,
        onPanningBoundaryReached: this.toastPanningBoundaryReached
      });
      this.context.weavessRef?.waveformPanelRef.zoomToTimeWindow(zoomInterval);
    }
  };

  /**
   * Called when ZAS button is clicked.
   * This sets the sort type to distance, sets the default alignment phase, aligns waveforms on predicted phase,
   * ensure show predicted phases, ensures stations with signal detections associated to current open event are
   * visible. Dispatch these changes first and then set state that will trigger the actual zooming. This is done
   * to get the waveform display into the right state before zoom because the state was not being dispatched before
   * the needed zoom calculations were made.
   */
  private readonly zoomAlignSort = () => {
    const defaultPhaseAlignment =
      this.props.processingAnalystConfigurationQuery.data.zasDefaultAlignmentPhase ??
      CommonTypes.PhaseType.P;

    // Sort
    if (this.props.selectedSortType !== AnalystWorkspaceTypes.WaveformSortType.distance) {
      this.props.setSelectedSortType(AnalystWorkspaceTypes.WaveformSortType.distance);
    }
    // Align
    if (this.props.alignWaveformsOn !== AnalystWorkspaceTypes.AlignWaveformsOn.PREDICTED_PHASE) {
      this.props.setAlignWaveformsOn(AnalystWorkspaceTypes.AlignWaveformsOn.PREDICTED_PHASE);
    }
    if (this.props.phaseToAlignOn !== defaultPhaseAlignment) {
      this.props.setPhaseToAlignOn(defaultPhaseAlignment);
    }
    if (!this.props.shouldShowPredictedPhases) {
      this.props.setShouldShowPredictedPhases(true);
    }

    // Zoom
    if (this.props.currentOpenEventId === null || this.props.currentOpenEventId === undefined) {
      this.context.weavessRef?.waveformPanelRef.zoomToTimeWindow(this.props.viewableInterval);
    } else {
      const calculatedZoomInterval = WaveformUtil.calculateZoomIntervalForCurrentOpenEvent(
        this.props,
        true
      );
      if (calculatedZoomInterval) {
        this.context.weavessRef?.waveformPanelRef.zoomToTimeWindow(calculatedZoomInterval);
      } else if (!this.props.featurePredictionQuery.isFetching) {
        this.context.weavessRef?.waveformPanelRef.zoomToTimeWindow(this.props.viewableInterval);
        toast.info(
          `Unable to calculate zoom interval, check feature prediction data and station data has loaded`,
          { toastId: 'zasUnableToCalculateInterval' }
        );
      }
    }
  };

  /**
   * Handle when the zoom changes within weavess. Catches errors that result from asynchronously updating
   * zoom interval—which can result in errors if the user changes intervals before this function
   * gets called.
   *
   * @param timeRange
   */
  private readonly onZoomChange = (timeRange: CommonTypes.TimeRange): void => {
    try {
      this.props.setZoomInterval(timeRange);
    } catch (error) {
      // this is an expected case when switching intervals while zoom updates are pending.
      // We should handle this gracefully. Throw all other errors.
      if (error.message !== AnalystWaveformTypes.ZOOM_INTERVAL_TOO_LARGE_ERROR_MESSAGE) throw error;
    }
  };

  /**
   * Remove scaled amplitude of all channels if set
   */
  private readonly resetChannelScaledAmplitude = (): void => {
    // If scaled to channel is set unset it
    if (this.state.scaleAmplitudeChannelName !== undefined) {
      this.setState({
        scaleAmplitudeChannelName: undefined,
        scaledAmplitudeChannelMinValue: -1,
        scaledAmplitudeChannelMaxValue: 1
      });
    }
  };

  /**
   * ! Legacy Code
   * Handles when a filter is toggled
   *
   * @param direction the keypress that triggered the toggle
   */
  private readonly handleChannelFilterToggle = (direction: KeyDirection) => {
    if (this.context.weavessRef) {
      const toggleFilterResults = toggleWaveformChannelFilters(
        direction,
        this.props.selectedStationIds,
        this.props.processingAnalystConfigurationQuery.data.defaultFilters,
        this.props.stationsQuery.data,
        this.selectedFilterIndex,
        this.props.channelFilters
      );
      this.setSelectedFilterIndex(toggleFilterResults.newFilterIndex);
      this.props.setChannelFilters(toggleFilterResults.channelFilters);
    }
  };

  /**
   * Set amplitude scaling option called by Waveform Control's Scaling Option
   *
   * @param option AmplitudeScalingOptions (fixed or auto)
   */
  private readonly setAmplitudeScaleOption = (option: AmplitudeScalingOptions) => {
    this.setState({
      amplitudeScaleOption: option,
      scaleAmplitudeChannelName: undefined,
      scaledAmplitudeChannelMinValue: -1,
      scaledAmplitudeChannelMaxValue: 1
    });
    if (this.context.weavessRef) {
      this.context.weavessRef.resetWaveformPanelAmplitudes();
    }
  };

  /**
   * Set fixed scale value when scaling option is set to Fixed
   *
   * @param val FixedScaleValue (number or current)
   */
  private readonly setFixedScaleVal = (val: FixedScaleValue) => {
    if (this.isShuttingDown) {
      return;
    }
    this.setState({
      fixedScaleVal: val,
      scaleAmplitudeChannelName: undefined,
      scaledAmplitudeChannelMinValue: -1,
      scaledAmplitudeChannelMaxValue: 1
    });
    this.resetAmplitudes();
  };

  /**
   * Reset amplitude in the waveform panels
   */
  private readonly resetAmplitudes = () => {
    if (this.context.weavessRef) {
      this.context.weavessRef.resetWaveformPanelAmplitudes();
    }
  };

  /**
   * Simply gets the signal detections out of the query, for ease of use.
   */
  private readonly getSignalDetections = () => this.props.signalDetections;

  /**
   * Sets the visibility for provided channel to false (not to show even if parent station is expanded)
   *
   * @param channelName the name of the channel to hide
   */
  private hideChannel(channelName: string): void {
    const parentStation = getStationContainingChannel(channelName, this.getStations());
    this.props.setChannelVisibility(parentStation, channelName, false);
  }

  /**
   * Sets the visibility for provided station to false (not visible)
   *
   * @param channelName
   */
  private hideStation(stationName: string): void {
    this.props.setStationVisibility(stationName, false);
  }

  /**
   * Sets the visibility for all channels belonging to the named station to true
   *
   * @param stationName the name of the station for which to show all of its channels
   */
  private showAllChannels(stationName: string): void {
    this.props.showAllChannels(stationName);
  }

  /**
   * Call to scale all amplitudes using the selected channel if one is selected, if not
   * warns User and returns
   */
  private scaleAllAmplitudesUsingSelectedChannel(): void {
    // Only perform scale all channels operation if 1 channel is selected.
    // If no channel is selected ignore the key sequence
    if (this.props.selectedStationIds.length === 0) {
      toast.info('Please select a channel to scale');
      return;
    }
    if (this.props.selectedStationIds.length > 1) {
      toast.warn('Cannot scale to channel when more than one channel is selected');
      return;
    }

    if (this.context.weavessRef?.waveformPanelRef) {
      const channelName = this.props.selectedStationIds[0];
      /** Find the WeavessChannel to check if a waveform is loaded */
      const weavessChannel: WeavessTypes.Channel = WeavessUtil.findChannelInStations(
        this.state.weavessStations,
        channelName
      );

      // Check to see if there is a waveform loaded
      if (recordLength(weavessChannel?.waveform?.channelSegmentsRecord) === 0) {
        toast.warn(`${channelName} has no waveform loaded to scale from`);
        return;
      }

      // Look up the channel amplitudes from Weaves (in case the channel has been manually scaled)
      const yBounds: WeavessTypes.YAxisBounds = this.context.weavessRef.waveformPanelRef.getChannelWaveformYAxisBounds(
        channelName
      );
      if (yBounds) {
        this.scaleAllAmplitudes(channelName, yBounds.minAmplitude, yBounds.maxAmplitude);
      } else {
        logger.warn(`Failed to find Amplitude for channel ${channelName}`);
      }
    }
  }

  /**
   * Sets all other channel's amplitudes to this channel's amplitudes. It does this by
   * setting the state that is then passed to the WeavessStations
   *
   * @param name Name of channel from which the amplitudes values are referenced
   * @param amplitudeMinValue Min value from reference channel
   * @param amplitudeMaxValue Max value from reference channel
   * @param isDefaultChannel Is this a station are a child channel
   */
  private scaleAllAmplitudes(
    channelName: string,
    amplitudeMinValue: number,
    amplitudeMaxValue: number
  ): void {
    // Reset any manual scaling before setting amplitude values of selected channel
    this.resetAmplitudes();
    this.setState({
      scaleAmplitudeChannelName: channelName,
      scaledAmplitudeChannelMinValue: amplitudeMinValue,
      scaledAmplitudeChannelMaxValue: amplitudeMaxValue
    });
  }

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp, complexity
  public render(): JSX.Element {
    Timer.start('[ui waveform panel] render');
    const stations = this.getAllVisibleWeavessStations();

    // eslint-disable-next-line max-len
    const customMeasureWindowLabel: React.FunctionComponent<WeavessTypes.LabelProps> = this.getCustomMeasureWindowLabel();

    Timer.end('[ui waveform panel] render');

    return (
      // eslint-disable-next-line jsx-a11y/no-static-element-interactions
      <div
        className="waveform-display-container"
        data-cy="waveform-display-container"
        tabIndex={-1}
        onKeyDown={e => {
          this.onKeyPress(e);
        }}
        ref={ref => {
          if (ref) {
            this.waveformDisplayRef = ref;
          }
        }}
      >
        <WaveformControls
          currentSortType={this.props.selectedSortType}
          currentTimeInterval={this.state.currentTimeInterval}
          viewableTimeInterval={this.props.viewableInterval}
          currentOpenEventId={this.props.currentOpenEventId}
          analystNumberOfWaveforms={this.state.analystNumberOfWaveforms}
          showPredictedPhases={this.props.shouldShowPredictedPhases}
          maskDisplayFilters={this.state.maskDisplayFilters}
          alignWaveformsOn={this.props.alignWaveformsOn}
          phaseToAlignOn={this.props.phaseToAlignOn}
          alignablePhases={this.props.alignablePhases}
          defaultPhaseAlignment={
            this.props.processingAnalystConfigurationQuery.data.zasDefaultAlignmentPhase ??
            CommonTypes.PhaseType.P
          }
          measurementMode={this.props.measurementMode}
          defaultSignalDetectionPhase={this.props.defaultSignalDetectionPhase}
          setDefaultSignalDetectionPhase={this.props.setDefaultSignalDetectionPhase}
          setWaveformAlignment={this.setWaveformAlignment}
          setSelectedSortType={this.props.setSelectedSortType}
          setAnalystNumberOfWaveforms={this.setAnalystNumberOfWaveforms}
          setMaskDisplayFilters={this.setMaskDisplayFilters}
          setMode={this.setMode}
          toggleMeasureWindow={this.toggleMeasureWindowVisibility}
          pan={this.pan}
          zoomAlignSort={this.zoomAlignSort}
          onKeyPress={this.onKeyPress}
          isMeasureWindowVisible={this.state.isMeasureWindowVisible}
          amplitudeScaleOption={this.state.amplitudeScaleOption}
          fixedScaleVal={this.state.fixedScaleVal}
          setAmplitudeScaleOption={this.setAmplitudeScaleOption}
          setFixedScaleVal={this.setFixedScaleVal}
          featurePredictionQueryDataUnavailable={
            (this.props.featurePredictionQuery.data === null ||
              this.props.featurePredictionQuery.data === undefined ||
              this.props.featurePredictionQuery.data?.receiverLocationsByName === null ||
              this.props.featurePredictionQuery.data?.receiverLocationsByName === undefined) &&
            !this.props.featurePredictionQuery.isFetching
          }
        />
        <WaveformLoadingIndicator />
        <WeavessDisplay
          weavessProps={{
            viewableInterval: this.props.viewableInterval,
            currentInterval: this.props.currentTimeInterval,
            minimumOffset: this.props.minimumOffset,
            maximumOffset: this.props.maximumOffset,
            baseStationTime: this.props.baseStationTime,
            showMeasureWindow:
              this.props.measurementMode.mode ===
              AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT,
            stations,
            events: this.weavessEventHandlers,
            selections: this.memoizedGetSelections(this.props.selectedStationIds),
            initialConfiguration: this.weavessConfiguration,
            customMeasureWindowLabel,
            flex: false,
            panRatio: this.props.processingAnalystConfigurationQuery.data?.waveformPanRatio
          }}
          defaultWaveformFilters={
            this.props.processingAnalystConfigurationQuery?.data?.defaultFilters ?? []
          }
          defaultStations={this.props.stationsQuery.data ?? []}
          events={this.props.events ?? []}
          qcMasksByChannelName={this.props.qcMaskQuery.data ?? []}
          measurementMode={this.props.measurementMode}
          defaultSignalDetectionPhase={this.props.defaultSignalDetectionPhase}
          setMeasurementModeEntries={this.props.setMeasurementModeEntries}
          amplitudeScaleOption={this.state.amplitudeScaleOption}
          fixedScaleVal={this.state.fixedScaleVal}
          scaleAmplitudeChannelName={this.state.scaleAmplitudeChannelName}
          scaledAmplitudeChannelMinValue={this.state.scaledAmplitudeChannelMinValue}
          scaledAmplitudeChannelMaxValue={this.state.scaledAmplitudeChannelMaxValue}
          selectedSdIds={this.props.selectedSdIds}
          signalDetections={this.getSignalDetections()}
          setSelectedSdIds={this.props.setSelectedSdIds}
          setSelectedStationIds={this.props.setSelectedStationIds}
          currentTimeInterval={this.props.currentTimeInterval}
          currentOpenEventId={this.props.currentOpenEventId}
          analysisMode={this.props.analysisMode}
          selectedStationIds={this.props.selectedStationIds}
          sdIdsToShowFk={this.props.sdIdsToShowFk}
          setSdIdsToShowFk={this.props.setSdIdsToShowFk}
          eventStatuses={this.props.eventStatuses}
          uiTheme={this.props.uiTheme}
          stationsAssociatedToCurrentOpenEvent={this.props.stationsAssociatedWithCurrentOpenEvent}
        />
      </div>
    );
  }
}
