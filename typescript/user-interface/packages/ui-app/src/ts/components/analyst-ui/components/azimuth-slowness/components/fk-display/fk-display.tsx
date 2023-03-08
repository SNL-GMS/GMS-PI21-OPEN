/* eslint-disable react/destructuring-assignment */
import { NonIdealState } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type {
  CommonTypes,
  FkTypes,
  LegacyEventTypes,
  ProcessingStationTypes,
  WaveformTypes
} from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import { DeprecatedToolbar, DeprecatedToolbarTypes } from '@gms/ui-core-components';
import isEqual from 'lodash/isEqual';
import React from 'react';

import { getFkData, getFkParamsForSd } from '~analyst-ui/common/utils/fk-utils';
import { userPreferences } from '~analyst-ui/config/';

import {
  LeadLagPairs,
  LeadLagPairsAndCustom,
  LeadLagValuesAndDisplayString,
  MAX_HEIGHT_OF_FK_PLOTS_PX,
  MAX_WIDTH_OF_FK_PROPERTIES_PX,
  SIZE_OF_FK_RENDERING_AXIS_PX
} from '../../constants';
import type {
  FkConfigurationWithUnits,
  FkParams,
  FkUnits,
  LeadLagPairAndString
} from '../../types';
import type { AnalystCurrentFk } from '../fk-rendering/fk-rendering';
import { FkRendering } from '../fk-rendering/fk-rendering';
import { FilterType, FkThumbnailSize } from '../fk-thumbnail-list/fk-thumbnails-controls';
import { FkPlots } from './fk-plots';
import { FkProperties } from './fk-properties';

const ADDITIONAL_MARGINS_FOR_TOOLBAR_PX = 0;

/**
 * Azimuth Slowness Redux Props
 */
export interface FkDisplayProps {
  defaultStations: ProcessingStationTypes.ProcessingStation[];
  defaultWaveformFilters: WaveformTypes.WaveformFilter[];
  eventsInTimeRange: LegacyEventTypes.Event[];
  currentOpenEvent: LegacyEventTypes.Event;
  unassociatedSignalDetectionByColor: string;
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[];
  signalDetection: SignalDetectionTypes.SignalDetection;
  signalDetectionFeaturePredictions: LegacyEventTypes.FeaturePrediction[];
  widthPx: number;
  heightPx: number;
  multipleSelected: boolean;
  anySelected: boolean;
  userInputFkWindowParameters: FkTypes.WindowParameters;
  userInputFkFrequency: FkTypes.FrequencyBand;
  numberOfOutstandingComputeFkMutations: number;
  fkUnit: FkUnits;
  fkFrequencyThumbnails: FkTypes.FkFrequencyThumbnail[];
  currentMovieSpectrumIndex: number;
  arrivalTimeMovieSpectrumIndex: number;
  channelFilters: Record<string, WaveformTypes.WaveformFilter>;
  defaultSignalDetectionPhase?: CommonTypes.PhaseType;
  setChannelFilters(filters: Record<string, WaveformTypes.WaveformFilter>): void;
  onNewFkParams(sdId: string, fkParams: FkParams, fkConfiguration: FkTypes.FkConfiguration): void;
  updateCurrentMovieTimeIndex(time: number): void;
  changeUserInputFks(
    windowParams: FkTypes.WindowParameters,
    frequencyBand: FkTypes.FrequencyBand
  ): void;
  setFkUnitForSdId(fkId: string, fkUnit: FkUnits);
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
}

/**
 * Azimuth Slowness State
 */
export interface FkDisplayState {
  fkThumbnailSizePx: FkThumbnailSize;
  filterType: FilterType;
  analystCurrentFk: AnalystCurrentFk;
}

/**
 * Azimuth Slowness primary component
 * Displays the FK plot and details of selected fk
 */
export class FkDisplay extends React.Component<FkDisplayProps, FkDisplayState> {
  /** The precision of displayed lead/lag pair */
  private readonly digitPrecision: number = 1;

  /** FK plots container reference */
  private fkPlotsContainerRef: HTMLElement | null;

  // Reference so we can calculate width
  public constructor(props: FkDisplayProps) {
    super(props);
    this.state = {
      // eslint-disable-next-line react/no-unused-state
      fkThumbnailSizePx: FkThumbnailSize.MEDIUM,
      // eslint-disable-next-line react/no-unused-state
      filterType: FilterType.firstP,
      analystCurrentFk: { x: undefined, y: undefined }
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React component lifecycle
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const { signalDetection, multipleSelected, anySelected } = this.props;
    if (this.props.multipleSelected || !signalDetection) {
      let message = 'No SD selected';
      if (multipleSelected && anySelected) {
        message = 'Multiple FKs Selected';
      } else if (anySelected) {
        message = 'No FK data for selected SD';
      }

      const icon = multipleSelected ? IconNames.MULTI_SELECT : IconNames.HEAT_GRID;

      return <NonIdealState icon={icon} title={message} />;
    }
    const selectedFk = signalDetection
      ? getFkData(
          SignalDetectionTypes.Util.getCurrentHypothesis(signalDetection.signalDetectionHypotheses)
            .featureMeasurements
        )
      : undefined;
    const fstatData = selectedFk?.fstatData;
    const selectedSd = signalDetection;
    const customLL = this.isCustomLeadLength(selectedFk.windowLead, selectedFk.windowLength);
    const toolbarItems: DeprecatedToolbarTypes.ToolbarItem[] = [];
    const fkWindow: DeprecatedToolbarTypes.LabelValueItem = {
      type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
      rank: 1,
      tooltip: 'FK Window',
      label: 'FK Window',
      value: ''
    };
    toolbarItems.push(fkWindow);
    const presetWindowDropdown: DeprecatedToolbarTypes.DropdownItem = {
      type: DeprecatedToolbarTypes.ToolbarItemType.Dropdown,
      rank: 2,
      tooltip: 'Choose a preset FK Window',
      label: 'FK Window',
      widthPx: 140,
      dropdownOptions: customLL ? LeadLagPairsAndCustom : LeadLagPairs,
      value: customLL
        ? LeadLagPairsAndCustom.CUSTOM
        : this.getLeadLengthPairByValue(selectedFk.windowLead, selectedFk.windowLength)
            .leadLagPairs,
      onChange: value => {
        this.onNewLeadLagPreset(value);
      }
    };
    toolbarItems.push(presetWindowDropdown);
    const leadInput: DeprecatedToolbarTypes.NumericInputItem = {
      type: DeprecatedToolbarTypes.ToolbarItemType.NumericInput,
      rank: 3,
      tooltip: 'Sets new lead for FK Window',
      label: 'Lead (s): ',
      value: parseFloat(
        this.props.userInputFkWindowParameters.leadSeconds.toFixed(this.digitPrecision)
      ),
      step: 0.5,
      minMax: {
        // do not allow the lead time to be before signal detection arrival time
        min: 0,
        max: 600
      },
      onChange: value => {
        this.onLeadTimeChanged(value);
      }
    };
    toolbarItems.push(leadInput);

    const durationInput: DeprecatedToolbarTypes.NumericInputItem = {
      type: DeprecatedToolbarTypes.ToolbarItemType.NumericInput,
      rank: 4,
      tooltip: 'Sets new duration for FK Window',
      label: 'Duration (s): ',
      value: parseFloat(
        this.props.userInputFkWindowParameters.lengthSeconds.toFixed(this.digitPrecision)
      ),
      step: 0.5,
      minMax: {
        min: this.props.userInputFkWindowParameters.leadSeconds,
        // TODO: Get SD Arrival time and calculate the lead position in time; then determine the max duration
        max: 9999
      },
      // requireEnterForOnChange: true,
      onChange: value => {
        this.onLengthChanged(value);
      }
    };
    toolbarItems.push(durationInput);
    const stepInput: DeprecatedToolbarTypes.NumericInputItem = {
      type: DeprecatedToolbarTypes.ToolbarItemType.NumericInput,
      rank: 5,
      tooltip: 'Sets new step size for continuous FK',
      label: 'Step Size (s): ',
      value: parseFloat(
        this.props.userInputFkWindowParameters.stepSize.toFixed(this.digitPrecision)
      ),
      step: 0.5,
      minMax: {
        min: 0.1,
        // TODO: Get SD Arrival time and calculate the lead position in time; then determine the max duration
        max: 1000
      },
      // requireEnterForOnChange: true,
      onChange: value => {
        this.onStepSizeChanged(value);
      }
    };
    toolbarItems.push(stepInput);

    const toolbarLeftItems: DeprecatedToolbarTypes.ToolbarItem[] = [];
    const fkLoadingSpinner: DeprecatedToolbarTypes.LoadingSpinnerItem = {
      tooltip: 'Number of compute of fk calls sent out that have not returned',
      label: 'pending fk request(s)',
      type: DeprecatedToolbarTypes.ToolbarItemType.LoadingSpinner,
      rank: 1,
      itemsToLoad: this.props.numberOfOutstandingComputeFkMutations,
      hideTheWordLoading: true,
      widthPx: 200
    };
    toolbarLeftItems.push(fkLoadingSpinner);
    const fkRenderingLengthPx = this.getFkRenderingLengthPx();
    return (
      <div className="azimuth-slowness-data-display">
        <div className="azimuth-slowness-data-display__wrapper">
          <div className="fk-image-and-details-container">
            <FkRendering
              data={signalDetection}
              signalDetectionFeaturePredictions={this.props.signalDetectionFeaturePredictions}
              analystCurrentFk={this.state.analystCurrentFk}
              updateCurrentFk={this.updateCurrentFk}
              fkUnitDisplayed={this.props.fkUnit}
              renderingHeightPx={fkRenderingLengthPx}
              renderingWidthPx={fkRenderingLengthPx}
              currentMovieSpectrumIndex={this.props.currentMovieSpectrumIndex}
            />
            <FkProperties
              defaultStations={this.props.defaultStations}
              signalDetection={signalDetection}
              signalDetectionFeaturePredictions={this.props.signalDetectionFeaturePredictions}
              analystCurrentFk={this.state.analystCurrentFk}
              userInputFkFrequency={this.props.userInputFkFrequency}
              userInputFkWindowParameters={this.props.userInputFkWindowParameters}
              onNewFkParams={this.props.onNewFkParams}
              fkUnitDisplayed={this.props.fkUnit}
              fkFrequencyThumbnails={this.props.fkFrequencyThumbnails}
              onFkConfigurationChange={this.onFkConfigurationChange}
              currentMovieSpectrumIndex={this.props.currentMovieSpectrumIndex}
              arrivalTimeMovieSpectrumIndex={this.props.arrivalTimeMovieSpectrumIndex}
            />
          </div>
        </div>
        <div className="fk-plots__toolbar">
          <DeprecatedToolbar
            toolbarWidthPx={
              this.fkPlotsContainerRef
                ? this.fkPlotsContainerRef.clientWidth - ADDITIONAL_MARGINS_FOR_TOOLBAR_PX
                : ADDITIONAL_MARGINS_FOR_TOOLBAR_PX
            }
            itemsRight={toolbarItems}
            itemsLeft={toolbarLeftItems}
          />
        </div>
        {!signalDetection ? (
          <div />
        ) : (
          <div
            ref={ref => {
              this.fkPlotsContainerRef = ref;
            }}
          >
            <FkPlots
              defaultStations={this.props.defaultStations}
              defaultWaveformFilters={this.props.defaultWaveformFilters}
              eventsInTimeRange={this.props.eventsInTimeRange}
              currentOpenEvent={this.props.currentOpenEvent}
              unassociatedSignalDetectionByColor={this.props.unassociatedSignalDetectionByColor}
              signalDetection={selectedSd}
              signalDetectionsByStation={this.props.signalDetectionsByStation}
              signalDetectionFeaturePredictions={this.props.signalDetectionFeaturePredictions}
              fstatData={fstatData}
              configuration={
                getFkData(
                  SignalDetectionTypes.Util.getCurrentHypothesis(
                    this.props.signalDetection.signalDetectionHypotheses
                  ).featureMeasurements
                ).configuration
              }
              windowParams={{
                leadSeconds: this.props.userInputFkWindowParameters.leadSeconds,
                lengthSeconds: this.props.userInputFkWindowParameters.lengthSeconds,
                stepSize: this.props.userInputFkWindowParameters.stepSize
              }}
              contribChannels={selectedFk.contribChannels}
              defaultSignalDetectionPhase={this.props.defaultSignalDetectionPhase}
              channelFilters={this.props.channelFilters}
              changeUserInputFks={this.props.changeUserInputFks}
              setChannelFilters={this.props.setChannelFilters}
              onNewFkParams={this.props.onNewFkParams}
              updateCurrentMovieTimeIndex={this.props.updateCurrentMovieTimeIndex}
              currentMovieSpectrumIndex={this.props.currentMovieSpectrumIndex}
              setMeasurementModeEntries={this.props.setMeasurementModeEntries}
            />
          </div>
        )}
      </div>
    );
  }

  public componentDidUpdate(prevProps: FkDisplayProps): void {
    if (!this.props.signalDetection) {
      return;
    }
    const currentFkParams = getFkParamsForSd(this.props.signalDetection);
    if (this.props.signalDetection && !prevProps.signalDetection) {
      this.props.changeUserInputFks(currentFkParams.windowParams, currentFkParams.frequencyPair);
      return;
    }
    if (prevProps.signalDetection && this.props.signalDetection) {
      if (prevProps.signalDetection.id !== this.props.signalDetection.id) {
        this.props.changeUserInputFks(currentFkParams.windowParams, currentFkParams.frequencyPair);
        return;
      }
      const prevFkParams = getFkParamsForSd(prevProps.signalDetection);
      if (!isEqual(prevFkParams, currentFkParams)) {
        this.props.changeUserInputFks(currentFkParams.windowParams, currentFkParams.frequencyPair);
      }
    }
  }

  /**
   * Update the current fk point
   *
   * @param point The X,Y location to draw the black fk dot
   */
  private readonly updateCurrentFk = (point: AnalystCurrentFk) => {
    this.setState({
      analystCurrentFk: point
    });
  };

  /**
   * Handles change on Lead time control
   *
   * @param newLeadSeconds New time for the Lead in the plots
   */
  private readonly onLeadTimeChanged = (newLeadSeconds: number) => {
    const priorParams = getFkParamsForSd(this.props.signalDetection);
    const newParams: FkParams = {
      ...priorParams,
      windowParams: {
        ...priorParams.windowParams,
        leadSeconds: newLeadSeconds
      }
    };
    const priorConfig = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.signalDetection.signalDetectionHypotheses
      ).featureMeasurements
    ).configuration;
    this.props.onNewFkParams(this.props.signalDetection.id, newParams, priorConfig);
  };

  /**
   * Handles change on lag time control
   *
   * @param newLagTime New time for the Lag in the plots
   */
  private readonly onLengthChanged = (length: number) => {
    const priorParams = getFkParamsForSd(this.props.signalDetection);
    const newParams: FkParams = {
      ...priorParams,
      windowParams: {
        ...priorParams.windowParams,
        lengthSeconds: length
      }
    };
    const priorConfig = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.signalDetection.signalDetectionHypotheses
      ).featureMeasurements
    ).configuration;
    this.props.onNewFkParams(this.props.signalDetection.id, newParams, priorConfig);
  };

  /**
   * Handles change on step size control
   *
   * @param stepSize seconds between Spectrums calculated
   */
  private readonly onStepSizeChanged = (stepSize: number) => {
    const priorParams = getFkParamsForSd(this.props.signalDetection);
    const newParams: FkParams = {
      ...priorParams,
      windowParams: {
        ...priorParams.windowParams,
        stepSize
      }
    };
    const priorConfig = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.signalDetection.signalDetectionHypotheses
      ).featureMeasurements
    ).configuration;
    this.props.onNewFkParams(this.props.signalDetection.id, newParams, priorConfig);
  };

  /**
   * Calculates the dimensions of the fk rendering
   *
   * @returns Length of side of fk rendering
   */
  private readonly getFkRenderingLengthPx = (): number => {
    // If the rendering has extra horizontal and vertical room, allocate it space!
    const extraWidthPx =
      this.props.widthPx -
      userPreferences.azimuthSlowness.minFkLengthPx -
      MAX_WIDTH_OF_FK_PROPERTIES_PX -
      SIZE_OF_FK_RENDERING_AXIS_PX;
    const extraHeightPx =
      this.props.heightPx -
      userPreferences.azimuthSlowness.minFkLengthPx -
      MAX_HEIGHT_OF_FK_PLOTS_PX -
      SIZE_OF_FK_RENDERING_AXIS_PX;
    if (extraWidthPx > 0 && extraHeightPx > 0) {
      // Because the FK rendering is a square, we can't make it longer than it is tall (or vice versa)
      const min = Math.min(extraWidthPx, extraHeightPx);
      if (min > 0) {
        // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
        const potentialLength = min + userPreferences.azimuthSlowness.minFkLengthPx;
        if (potentialLength > userPreferences.azimuthSlowness.maxFkLengthPx) {
          return userPreferences.azimuthSlowness.maxFkLengthPx;
        }
        return potentialLength;
      }
    }
    return userPreferences.azimuthSlowness.minFkLengthPx;
  };

  /**
   * Gets an object containing the enum value for the lead/length pair
   *
   * @param lead Lead for the fk
   * @param length Length for the fk
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly getLeadLengthPairByValue = (
    lead: number,
    length: number
  ): LeadLagPairAndString =>
    LeadLagValuesAndDisplayString.find(
      llpv => llpv.windowParams.leadSeconds === lead && llpv.windowParams.lengthSeconds === length
    );

  /**
   * Gets a lead/length pair by the enum
   *
   * @param enumVal Name of the preset pair
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly getLeadLengthPairByName = (enumVal: any): LeadLagPairAndString =>
    LeadLagValuesAndDisplayString.find(llpv => llpv.leadLagPairs === enumVal);

  /**
   * Determines if the selected Lead Length is one of the presets
   *
   * @param lead Lead for the fk
   * @param length Length for the fk
   */
  private readonly isCustomLeadLength = (lead: number, length: number): boolean => {
    const maybeValues = this.getLeadLengthPairByValue(lead, length);
    return maybeValues === undefined;
  };

  /**
   * Handles the change in the drop down menu
   *
   * @param value new leadLag from drop down
   */
  private readonly onNewLeadLagPreset = (value: any): void => {
    const newPair = this.getLeadLengthPairByName(value);
    if (newPair) {
      const priorParams = getFkParamsForSd(this.props.signalDetection);
      const newParams: FkParams = {
        ...priorParams,
        windowParams: {
          ...priorParams.windowParams,
          lengthSeconds: newPair.windowParams.lengthSeconds,
          leadSeconds: newPair.windowParams.leadSeconds
        }
      };
      const priorConfig = getFkData(
        SignalDetectionTypes.Util.getCurrentHypothesis(
          this.props.signalDetection.signalDetectionHypotheses
        ).featureMeasurements
      ).configuration;
      this.props.onNewFkParams(this.props.signalDetection.id, newParams, priorConfig);
    }
  };

  /**
   * Reconfigures display and/or calls gateway when new fk configuration is entered
   *
   * @param fkConfigurationWithUnits as FkConfigurationWithUnits
   */
  private readonly onFkConfigurationChange = (
    fkConfigurationWithUnits: FkConfigurationWithUnits
  ) => {
    const newFkConfiguration: FkTypes.FkConfiguration = {
      contributingChannelsConfiguration: fkConfigurationWithUnits.contributingChannelsConfiguration,
      maximumSlowness: fkConfigurationWithUnits.maximumSlowness,
      mediumVelocity: fkConfigurationWithUnits.mediumVelocity,
      normalizeWaveforms: fkConfigurationWithUnits.normalizeWaveforms,
      numberOfPoints: fkConfigurationWithUnits.numberOfPoints,
      useChannelVerticalOffset: fkConfigurationWithUnits.useChannelVerticalOffset,
      leadFkSpectrumSeconds: fkConfigurationWithUnits.leadFkSpectrumSeconds
    };
    const previousFk = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.signalDetection.signalDetectionHypotheses
      ).featureMeasurements
    );
    const previousFkConfiguration = previousFk.configuration;
    if (!this.fKConfigurationsAreEqual(previousFkConfiguration, newFkConfiguration)) {
      this.props.onNewFkParams(
        this.props.signalDetection.id,
        {
          frequencyPair: {
            minFrequencyHz: previousFk.lowFrequency,
            maxFrequencyHz: previousFk.highFrequency
          },
          windowParams: {
            stepSize: previousFk.stepSize,
            leadSeconds: previousFk.windowLead,
            lengthSeconds: previousFk.windowLength
          }
        },
        newFkConfiguration
      );
    }
    if (fkConfigurationWithUnits.fkUnitToDisplay !== this.props.fkUnit) {
      this.props.setFkUnitForSdId(
        this.props.signalDetection.id,
        fkConfigurationWithUnits.fkUnitToDisplay
      );
    }
  };

  /**
   * Checks to see if fk configurations are equal
   *
   * @param a configuration 1
   * @param b configuration 2
   *
   * @returns true or false based on result
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly fKConfigurationsAreEqual = (
    a: FkTypes.FkConfiguration,
    b: FkTypes.FkConfiguration
  ): boolean => {
    if (
      a.maximumSlowness === b.maximumSlowness &&
      a.mediumVelocity === b.mediumVelocity &&
      a.normalizeWaveforms === b.normalizeWaveforms &&
      a.numberOfPoints === b.numberOfPoints &&
      a.useChannelVerticalOffset === b.useChannelVerticalOffset &&
      a.contributingChannelsConfiguration.length === b.contributingChannelsConfiguration.length
    ) {
      let isUnequal = false;
      a.contributingChannelsConfiguration.forEach(cccA => {
        const maybeMatchingTracker = b.contributingChannelsConfiguration.find(
          cccB => cccA.id === cccB.id
        );
        if (maybeMatchingTracker) {
          if (maybeMatchingTracker.enabled !== cccA.enabled) {
            isUnequal = true;
          }
        } else {
          isUnequal = true;
        }
      });
      return !isUnequal;
    }
    return false;
  };
}
