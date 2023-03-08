/* eslint-disable react/destructuring-assignment */
import { NonIdealState } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type {
  CommonTypes,
  FkTypes,
  LegacyEventTypes,
  ProcessingStationTypes
} from '@gms/common-model';
import { SignalDetectionTypes, WaveformTypes, WorkflowTypes } from '@gms/common-model';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import { WeavessTypes, WeavessUtil } from '@gms/weavess-core';
import isEqual from 'lodash/isEqual';
import React from 'react';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import { determineDetectionColorLegacy } from '~analyst-ui/common/utils/event-util';
import { getFkData, getFkParamsForSd } from '~analyst-ui/common/utils/fk-utils';
import { createUnfilteredWaveformFilter } from '~analyst-ui/common/utils/instance-of-util';
import { filterSignalDetectionsByStationId } from '~analyst-ui/common/utils/signal-detection-util';
import { WeavessDisplay } from '~analyst-ui/components/waveform/weavess-display';
import { systemConfig } from '~analyst-ui/config';
import { gmsColors, semanticColors } from '~scss-config/color-preferences';

import type { FkParams } from '../../types';
import { getPredictedPoint } from '../fk-util';

const logger = UILogger.create('GMS_LOG_FK_PLOTS', process.env.GMS_LOG_FK_PLOTS);

/**
 * FkPlots Props
 */
export interface FkPlotsProps {
  defaultStations: ProcessingStationTypes.ProcessingStation[];
  defaultWaveformFilters: WaveformTypes.WaveformFilter[];
  eventsInTimeRange: LegacyEventTypes.Event[];
  currentOpenEvent?: LegacyEventTypes.Event;
  unassociatedSignalDetectionByColor: string;
  signalDetection: SignalDetectionTypes.SignalDetection;
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[];
  signalDetectionFeaturePredictions: LegacyEventTypes.FeaturePrediction[];
  fstatData: FkTypes.FstatData;
  windowParams: FkTypes.WindowParameters;
  configuration: FkTypes.FkConfiguration;
  contribChannels: {
    id: string;
  }[];
  currentMovieSpectrumIndex: number;
  defaultSignalDetectionPhase?: CommonTypes.PhaseType;

  channelFilters: Record<string, WaveformTypes.WaveformFilter>;
  setChannelFilters(filters: Record<string, WaveformTypes.WaveformFilter>): void;
  changeUserInputFks(
    windowParams: FkTypes.WindowParameters,
    frequencyBand: FkTypes.FrequencyBand
  ): void;
  updateCurrentMovieTimeIndex(time: number): void;
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
  onNewFkParams(sdId: string, fkParams: FkParams, fkConfiguration: FkTypes.FkConfiguration): void;
}

/**
 * FkPlots State
 */
export interface FkPlotsState {
  selectionWindow: {
    startTime: number;
    endTime: number;
  };
}

/**
 * Renders the FK waveform data with Weavess
 */
export class FkPlots extends React.PureComponent<FkPlotsProps, FkPlotsState> {
  /** The precision of displayed lead/lag pair */
  private readonly digitPrecision: number = 1;

  /** Hard-coded height of the waveform panel */
  private readonly waveformPanelHeight: number = 70;

  /** Determines if the selection window should snap or not */
  private selectionWindowSnapMode = false;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: FkPlotsProps) {
    super(props);
    this.state = {
      // eslint-disable-next-line react/no-unused-state
      selectionWindow: undefined
    };
  }

  /**
   * React life update cycle method, that resets the should snap class variable
   *
   * @param prevProps previous component props
   */
  public componentDidUpdate(prevProps: FkPlotsProps): void {
    if (
      !isEqual(
        getFkData(
          SignalDetectionTypes.Util.getCurrentHypothesis(
            prevProps.signalDetection.signalDetectionHypotheses
          ).featureMeasurements
        ),
        getFkData(
          SignalDetectionTypes.Util.getCurrentHypothesis(
            this.props.signalDetection.signalDetectionHypotheses
          ).featureMeasurements
        )
      )
    ) {
      this.selectionWindowSnapMode = false;
    }
  }

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp, complexity
  public render(): JSX.Element {
    const arrivalTime = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.signalDetection.signalDetectionHypotheses
      ).featureMeasurements
    ).arrivalTime.value;

    const timePadding: number = systemConfig.fkPlotTimePadding;

    const selectedFilter: WaveformTypes.WaveformFilter = this.props.channelFilters[
      this.props.signalDetection.station.name
    ]
      ? this.props.channelFilters[this.props.signalDetection.station.name]
      : createUnfilteredWaveformFilter();
    // TODO: Fix call to lookup signalDetection's corresponding waveform
    const channelSegment = undefined;

    // az, slowness, and fstat have the same rate and num samples
    // but we need to calculate the data to send to weavess for beam
    if (!channelSegment || this.fStatDataContainsUndefined(this.props.fstatData)) {
      return (
        <NonIdealState
          icon={IconNames.TIMELINE_LINE_CHART}
          title="Missing waveform data"
          description="Fk plots currently not supported for analyst created SDs"
        />
      );
    }

    // TODO: What times should be used?
    // If waveform has no samples then startTime and endTime will be epoch zero or undefined
    const startTimeSecs = 0;
    // (channelSegment.startTime && channelSegment.startTime > 0) ?? arrivalTime - timePadding / 2;
    let endTimeSecs = 0; // (channelSegment.endTime && channelSegment.endTime > 0) ?? 0;
    if (endTimeSecs <= arrivalTime) {
      endTimeSecs += timePadding;
    }

    const predictedPoint = getPredictedPoint(this.props.signalDetectionFeaturePredictions);

    const signalDetections: WeavessTypes.PickMarker[] = this.buildSignalDetectionPickMarkers();

    // If there are Signal Detections populate Weavess Channel Segment from the FK_BEAM
    // else use the default channel Weavess Channel Segment built
    const beamChannelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
    if (signalDetections && signalDetections.length > 0) {
      // clone to add UNFILTERED
      const allFilters = [...this.props.defaultWaveformFilters, WaveformTypes.UNFILTERED_FILTER];
      allFilters.forEach(filter => {
        logger.warn(`TODO need to implement Channel Segment lookup for Signal Detection!`);
        const signalDetectionChannelSegment: WeavessTypes.ChannelSegment = undefined;
        if (
          signalDetectionChannelSegment &&
          signalDetectionChannelSegment.dataSegments &&
          signalDetectionChannelSegment.dataSegments.length > 0
        ) {
          beamChannelSegmentsRecord[filter.id] = [signalDetectionChannelSegment];
        }
      });
    }

    const KEY = 'data';
    const fStatChannelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
    fStatChannelSegmentsRecord[KEY] = [
      {
        channelName: 'FstatChannel',
        wfFilterId: selectedFilter.id,
        isSelected: false,
        dataSegments: [
          {
            color: semanticColors.waveformRaw,
            pointSize: 2,
            displayType: [WeavessTypes.DisplayType.LINE, WeavessTypes.DisplayType.SCATTER],
            data: {
              startTimeSecs: this.props.fstatData.fstatWf.startTime,
              endTimeSecs: this.props.fstatData.fstatWf.endTime,
              sampleRate: this.props.fstatData.fstatWf.sampleRateHz,
              values: this.props.fstatData.fstatWf.samples
            }
          }
        ]
      }
    ];

    const azimuthChannelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
    azimuthChannelSegmentsRecord[KEY] = [
      {
        channelName: 'AzimuthChannel',
        wfFilterId: selectedFilter.id,
        isSelected: false,
        dataSegments: [
          {
            displayType: [WeavessTypes.DisplayType.LINE, WeavessTypes.DisplayType.SCATTER],
            color: semanticColors.waveformRaw,
            pointSize: 2,
            data: {
              startTimeSecs: this.props.fstatData.azimuthWf.startTime,
              endTimeSecs: this.props.fstatData.azimuthWf.endTime,
              sampleRate: this.props.fstatData.azimuthWf.sampleRateHz,
              values: this.props.fstatData.azimuthWf.samples
            }
          }
        ]
      }
    ];

    const slownessChannelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
    slownessChannelSegmentsRecord[KEY] = [
      {
        channelName: 'SlownessChannel',
        wfFilterId: selectedFilter.id,
        isSelected: false,
        dataSegments: [
          {
            displayType: [WeavessTypes.DisplayType.LINE, WeavessTypes.DisplayType.SCATTER],
            color: semanticColors.waveformRaw,
            pointSize: 2,
            data: {
              startTimeSecs: this.props.fstatData.slownessWf.startTime,
              endTimeSecs: this.props.fstatData.slownessWf.endTime,
              sampleRate: this.props.fstatData.slownessWf.sampleRateHz,
              values: this.props.fstatData.slownessWf.samples
            }
          }
        ]
      }
    ];
    const stations: WeavessTypes.Station[] = [
      // Beam
      {
        id: 'Beam',
        name: 'Beam',
        defaultChannel: {
          id: this.props.signalDetection.station.name,
          name: 'Beam',
          height: this.waveformPanelHeight,
          waveform: {
            channelSegmentId: selectedFilter.id,
            channelSegmentsRecord: beamChannelSegmentsRecord,
            signalDetections
          }
        },
        areChannelsShowing: false
      },
      // Fstat
      {
        id: 'Fstat',
        name: 'Fstat',
        defaultChannel: {
          id: `Fstat-${this.props.signalDetection.station.name}`,
          name: 'Fstat',
          height: this.waveformPanelHeight,
          // set the min to zero, so that WEAVESS does not use the calculated min
          defaultRange: {
            min: 0
          },
          waveform: {
            channelSegmentId: KEY,
            channelSegmentsRecord: fStatChannelSegmentsRecord
          }
        },
        areChannelsShowing: false
      },
      // Azimuth
      {
        id: 'Azimuth',
        name: 'Azimuth',
        defaultChannel: {
          id: `Azimuth-${this.props.signalDetection.station.name}`,
          name: (
            <div style={{ whiteSpace: 'nowrap' }}>
              Azimuth <sup>(&deg;)</sup>
            </div>
          ),
          height: this.waveformPanelHeight,
          // set the min to zero and max to 360, so that WEAVESS does not use the calculated min/max
          defaultRange: {
            min: 0,
            max: 360
          },
          waveform: {
            channelSegmentId: KEY,
            channelSegmentsRecord: azimuthChannelSegmentsRecord
          }
        },
        areChannelsShowing: false
      },
      // Slowness
      {
        id: 'Slowness',
        name: 'Slowness',
        defaultChannel: {
          id: `Slowness-${this.props.signalDetection.station.name}`,
          // eslint-disable-next-line max-len
          name: (
            <div style={{ whiteSpace: 'nowrap' }}>
              Slowness (<sup>s</sup>
              &#8725;
              <sub>&deg;</sub>)
            </div>
          ),
          height: this.waveformPanelHeight,
          // set the min to zero and max to the current maximum slowness,
          // so that WEAVESS does not use the calculated min/max
          defaultRange: {
            min: 0,
            max: this.props.configuration.maximumSlowness
          },
          waveform: {
            channelSegmentId: KEY,
            channelSegmentsRecord: slownessChannelSegmentsRecord
          }
        },
        areChannelsShowing: false
      }
    ];

    // add the Azimuth and Slowness flat lines if the appropriate predicted value exists
    if (predictedPoint) {
      stations[2].defaultChannel.waveform.channelSegmentsRecord[KEY][0].dataSegments.push(
        WeavessUtil.createFlatLineDataSegment(
          startTimeSecs,
          endTimeSecs,
          predictedPoint.azimuth,
          semanticColors.analystOpenEvent
        )
      );
    }

    if (predictedPoint) {
      stations[3].defaultChannel.waveform.channelSegmentsRecord[KEY][0].dataSegments.push(
        WeavessUtil.createFlatLineDataSegment(
          startTimeSecs,
          endTimeSecs,
          predictedPoint.slowness,
          semanticColors.analystOpenEvent
        )
      );
    }

    // Get the SD FK configure to set the start marker lead secs and
    // from there add length to get endMarker in epoch time
    // TODO: How to handle this array.
    const config = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.signalDetection.signalDetectionHypotheses
      ).featureMeasurements
    ).configuration;
    const startMarkerEpoch: number = this.selectionWindowSnapMode
      ? startTimeSecs + this.props.currentMovieSpectrumIndex * this.props.windowParams.stepSize
      : arrivalTime - config.leadFkSpectrumSeconds;
    const endMarkerEpoch: number = startMarkerEpoch + this.props.windowParams.lengthSeconds;
    const viewableInterval: WeavessTypes.TimeRange = {
      startTimeSecs,
      endTimeSecs
    };
    return (
      <div>
        <div className="ag-dark fk-plots-wrapper-1">
          <div className="fk-plots-wrapper-2">
            <WeavessDisplay
              weavessProps={{
                viewableInterval,
                minimumOffset: 0,
                maximumOffset: 0,
                initialConfiguration: {
                  defaultChannel: {
                    disableMeasureWindow: true
                  }
                },
                stations,
                selections: {
                  signalDetections: [this.props.signalDetection.id]
                },
                events: {
                  // stationEvents: {
                  //   defaultChannelEvents: channelEvents
                  // },
                  onUpdateSelectionWindow: this.onUpdateSelectionWindow
                },
                markers: {
                  selectionWindows: [
                    {
                      id: 'selection',
                      startMarker: {
                        id: 'start',
                        color: gmsColors.gmsProminent,
                        lineStyle: WeavessTypes.LineStyle.DASHED,
                        timeSecs: startMarkerEpoch
                      },
                      endMarker: {
                        id: 'end',
                        color: gmsColors.gmsProminent,
                        lineStyle: WeavessTypes.LineStyle.DASHED,
                        timeSecs: endMarkerEpoch,
                        minTimeSecsConstraint:
                          startMarkerEpoch + Number(config.leadFkSpectrumSeconds)
                      },
                      isMoveable: true,
                      color: 'rgba(200,200,200,0.2)'
                    }
                  ]
                }
              }}
              defaultWaveformFilters={this.props.defaultWaveformFilters}
              defaultStations={[]} // {this.props.defaultStations}
              defaultSignalDetectionPhase={this.props.defaultSignalDetectionPhase}
              // Removed due to COI Updates.  Was 'this.props.eventsInTimeRange'
              events={undefined}
              signalDetections={[]}
              selectedSdIds={[]}
              setSelectedSdIds={undefined}
              setMeasurementModeEntries={this.props.setMeasurementModeEntries}
              qcMasksByChannelName={[]}
              measurementMode={{
                // do not allow measurement mode for the fk plots, always force to default
                mode: AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT,
                entries: {}
              }}
              // Added as part of WeavessDisplay cleanup
              currentTimeInterval={undefined}
              currentOpenEventId={this.props.currentOpenEvent?.id}
              analysisMode={WorkflowTypes.AnalysisMode.EVENT_REVIEW}
              selectedStationIds={[this.props.signalDetection.station.name]}
              setSelectedStationIds={undefined}
              sdIdsToShowFk={[]}
              setSdIdsToShowFk={undefined}
              eventStatuses={undefined}
              uiTheme={undefined}
            />
          </div>
        </div>
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  private readonly buildSignalDetectionPickMarkers = (): WeavessTypes.PickMarker[] => {
    const signalDetectionsForStation = this.props.signalDetectionsByStation
      ? filterSignalDetectionsByStationId(
          this.props.signalDetection.station.name,
          this.props.signalDetectionsByStation
        )
      : [];

    return signalDetectionsForStation.map(sd => ({
      timeSecs: SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
          .featureMeasurements
      ).arrivalTime.value,
      uncertaintySecs: SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
          .featureMeasurements
      ).arrivalTime.standardDeviation,
      showUncertaintyBars: false,
      id: sd.id,
      label: SignalDetectionUtils.findPhaseFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
          .featureMeasurements
      ).value.toString(),
      color: determineDetectionColorLegacy(
        sd,
        this.props.eventsInTimeRange,
        this.props.currentOpenEvent ? this.props.currentOpenEvent.id : undefined,
        this.props.unassociatedSignalDetectionByColor
      ),
      // TODO SD no longer has a conflict flag
      isConflicted: false, // sd.hasConflict
      isSelected: false
    }));
  };

  /**
   * Event handler for when a key is pressed
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  // private readonly onKeyPress = (e: React.KeyboardEvent<HTMLDivElement>) => {
  //   // handle the default WEAVESS onKeyPressEvents
  //   if (e.ctrlKey || e.metaKey) {
  //     if (e.key === 'ArrowUp' || e.key === 'ArrowDown') {
  //       const toggleFilterResults = toggleWaveformChannelFilters(
  //         e,
  //         [this.props.signalDetection.stationName],
  //         this.props.defaultWaveformFilters,
  //         this.props.defaultStations,
  //         this.selectedFilterIndex,
  //         this.props.channelFilters
  //       );
  //       this.setSelectedFilterIndex(toggleFilterResults.newFilterIndex);
  //       this.setChannelFilters(toggleFilterResults.channelFilters);
  //     }
  //   }
  // }

  /**
   * Updates the value of the selected filter index
   *
   * @param index index value
   */
  // private readonly setSelectedFilterIndex = (index: number): void => {
  //   this.selectedFilterIndex = index;
  // }

  /**
   * Updates the state of the channel filters
   *
   * @param channelFilters map of channel filters
   */
  // private readonly setChannelFilters = (
  //   channelFilters: Immutable.Map<string, WaveformFilter> = Immutable.Map<string, WaveformFilter>()) => {
  //   this.props.setChannelFilters(channelFilters);
  // }

  /**
   * Call back for drag and drop change of the moveable selection
   *
   * @param verticalMarkers List of markers in the fk plot display
   */
  private readonly onUpdateSelectionWindow = (selection: WeavessTypes.SelectionWindow) => {
    const arrivalTime = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.signalDetection.signalDetectionHypotheses
      ).featureMeasurements
    ).arrivalTime.value;

    const lagTime = this.props.windowParams.lengthSeconds - this.props.windowParams.leadSeconds;
    const newLeadTime = parseFloat(
      (arrivalTime - selection.startMarker.timeSecs).toFixed(this.digitPrecision)
    );
    const newLagTime = parseFloat(
      (selection.endMarker.timeSecs - arrivalTime).toFixed(this.digitPrecision)
    );
    const minimumDeltaSize = 0.1;
    const priorParams = getFkParamsForSd(this.props.signalDetection);
    // If duration hasn't changed update new lead seconds and update user input which sets state
    // else call computeFk via onNewFkParams
    const durationDelta = Math.abs(
      this.props.windowParams.lengthSeconds - (newLagTime + newLeadTime)
    );
    if (durationDelta < minimumDeltaSize) {
      this.selectionWindowSnapMode = true;
      this.props.updateCurrentMovieTimeIndex(selection.startMarker.timeSecs);
    } else if (
      newLeadTime > this.props.windowParams.leadSeconds + minimumDeltaSize ||
      newLeadTime < this.props.windowParams.leadSeconds - minimumDeltaSize ||
      newLagTime > lagTime + minimumDeltaSize ||
      newLagTime < lagTime - minimumDeltaSize
    ) {
      const newParams: FkParams = {
        ...priorParams,
        windowParams: {
          ...priorParams.windowParams,
          lengthSeconds: parseFloat(
            (selection.endMarker.timeSecs - selection.startMarker.timeSecs).toFixed(
              this.digitPrecision
            )
          )
        }
      };
      const priorConfig = getFkData(
        SignalDetectionTypes.Util.getCurrentHypothesis(
          this.props.signalDetection.signalDetectionHypotheses
        ).featureMeasurements
      ).configuration;
      priorConfig.leadFkSpectrumSeconds = newParams.windowParams.leadSeconds;
      this.selectionWindowSnapMode = false;
      this.props.onNewFkParams(this.props.signalDetection.id, newParams, priorConfig);
    }
  };

  /**
   * Checks for any undefined waveforms inside of fstat data
   *
   * @param fstatData as FkTypes.FstatData
   * @returns boolean if defined or not
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly fStatDataContainsUndefined = (fstatData: FkTypes.FstatData): boolean =>
    !fstatData || !fstatData.azimuthWf || !fstatData.fstatWf || !fstatData.slownessWf;
}
