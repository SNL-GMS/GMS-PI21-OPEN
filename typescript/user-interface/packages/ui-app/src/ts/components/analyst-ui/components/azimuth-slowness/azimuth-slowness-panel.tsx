/* eslint-disable react/destructuring-assignment */

import { ContextMenu } from '@blueprintjs/core';
import type { FkTypes } from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import isEqual from 'lodash/isEqual';
import React from 'react';

import { getFkData } from '~analyst-ui/common/utils/fk-utils';

import { FkThumbnailBlueprintContextMenu } from './components/context-menus/fk-context-menu';
import { FkDisplay } from './components/fk-display/fk-display';
import { FkThumbnailList } from './components/fk-thumbnail-list/fk-thumbnail-list';
import { FkThumbnailsControls } from './components/fk-thumbnail-list/fk-thumbnails-controls';
import { createFkInput, fkNeedsReview, getSortedSignalDetections } from './components/fk-util';
import type { AzimuthSlownessPanelProps, AzimuthSlownessPanelState, FkParams } from './types';

/**
 * An intermediary between AzimuthSlownessComponent and the other components so that event handling is simpler
 */
export class AzimuthSlownessPanel extends React.Component<
  AzimuthSlownessPanelProps,
  AzimuthSlownessPanelState
> {
  /** Used to constrain the max width of the thumbnail drag resize */
  private azimuthSlownessContainer: HTMLDivElement;

  /** Used to drag & resize this element */
  private fkThumbnailsContainer: HTMLDivElement;

  /** The inner container for the thumbnail */
  private fkThumbnailsInnerContainer: HTMLDivElement;

  /** Index for the spectrum index for the arrival time fk, used for thumbnails */
  private arrivalTimeMovieSpectrumIndex: number;

  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: AzimuthSlownessPanelProps) {
    super(props);
    let index = 0;
    if (props.displayedSignalDetection) {
      const arrivalTime = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(
          props.displayedSignalDetection.signalDetectionHypotheses
        ).featureMeasurements
      ).arrivalTime.value;
      index = this.calculateMovieIndex(arrivalTime);
    }
    this.state = {
      currentMovieSpectrumIndex: index
    };
  }

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount(): void {
    this.props.adjustFkInnerContainerWidth(
      this.fkThumbnailsContainer,
      this.fkThumbnailsInnerContainer
    );
  }

  public componentDidUpdate(prevProps: AzimuthSlownessPanelProps): void {
    if (
      this.props.displayedSignalDetection &&
      !isEqual(this.props.displayedSignalDetection, prevProps.displayedSignalDetection)
    ) {
      const arrivalTime = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(
          this.props.displayedSignalDetection.signalDetectionHypotheses
        ).featureMeasurements
      ).arrivalTime.value;
      this.setArrivalTimeMovieSpectrumIndex(this.props.displayedSignalDetection);
      this.updateCurrentMovieTimeIndex(arrivalTime);
    }
    if (this.props.fkThumbnailColumnSizePx !== prevProps.fkThumbnailColumnSizePx) {
      this.props.adjustFkInnerContainerWidth(
        this.fkThumbnailsContainer,
        this.fkThumbnailsInnerContainer
      );
    }
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const anyDisplayedFksNeedReview = this.getReviewableSds().length > 0;
    if (!this.arrivalTimeMovieSpectrumIndex && this.props.displayedSignalDetection) {
      this.setArrivalTimeMovieSpectrumIndex(this.props.displayedSignalDetection);
    }
    return (
      // eslint-disable-next-line jsx-a11y/no-static-element-interactions
      <div
        ref={ref => {
          this.azimuthSlownessContainer = ref;
        }}
        className="azimuth-slowness-container"
        data-cy="azimuth-slowness"
        // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex
        tabIndex={0}
        onKeyDown={this.onKeyDown}
      >
        <div
          ref={ref => {
            this.fkThumbnailsContainer = ref;
          }}
          className="azimuth-slowness-thumbnails"
          style={{ width: `${this.props.fkThumbnailColumnSizePx}px` }}
        >
          <div className="azimuth-slowness-thumbnails__control-container">
            <FkThumbnailsControls
              updateFkThumbnail={this.props.updateFkThumbnailSize}
              updateFkFilter={this.props.updateFkFilter}
              anyDisplayedFksNeedReview={anyDisplayedFksNeedReview}
              onlyOneFkIsSelected={this.props.selectedSdIds.length === 1}
              widthPx={this.props.fkThumbnailColumnSizePx}
              nextFk={() => {
                this.nextFk();
              }}
              currentFilter={this.props.filterType}
              clearSelectedUnassociatedFks={this.clearSelectedUnassociatedFks}
            />
          </div>
          <div className="azimuth-slowness-thumbnails__wrapper-1">
            <div
              ref={ref => {
                this.fkThumbnailsInnerContainer = ref;
              }}
              className="azimuth-slowness-thumbnails__wrapper-2"
            >
              <FkThumbnailList
                thumbnailSizePx={this.props.fkThumbnailSizePx}
                sortedSignalDetections={this.props.signalDetectionsToDraw}
                unassociatedSdIds={this.props.signalDetectionsToDraw
                  .filter(
                    sdToDraw =>
                      !this.props.associatedSignalDetections.find(aSd => aSd.id === sdToDraw.id)
                  )
                  .map(sd => sd.id)}
                signalDetectionIdsToFeaturePrediction={
                  this.props.signalDetectionsIdToFeaturePredictions
                }
                selectedSdIds={this.props.selectedSdIds}
                setSelectedSdIds={this.props.setSelectedSdIds}
                clearSelectedUnassociatedFks={this.clearSelectedUnassociatedFks}
                fkUnitsForEachSdId={this.props.fkUnitsForEachSdId}
                markFksForSdIdsAsReviewed={this.markFksForSdIdsAsReviewedIfTheyNeedToBeAccepted}
                showFkThumbnailContextMenu={this.showFkThumbnailMenu}
                arrivalTimeMovieSpectrumIndex={this.arrivalTimeMovieSpectrumIndex}
              />
            </div>
          </div>
        </div>
        {/* drag handle divider */}
        {/* eslint-disable-next-line jsx-a11y/no-static-element-interactions */}
        <div className="azimuth-slowness-divider" onMouseDown={this.onThumbnailDividerDrag}>
          <div className="azimuth-slowness-divider__spacer" />
        </div>
        <FkDisplay
          defaultStations={this.props.defaultStations}
          defaultWaveformFilters={this.props.defaultWaveformFilters}
          eventsInTimeRange={this.props.eventsInTimeRange}
          currentOpenEvent={this.props.openEvent}
          unassociatedSignalDetectionByColor={this.props.unassociatedSignalDetectionByColor}
          signalDetection={this.props.displayedSignalDetection}
          signalDetectionsByStation={this.props.signalDetectionsByStation}
          signalDetectionFeaturePredictions={
            this.props.featurePredictionsForDisplayedSignalDetection
          }
          widthPx={this.props.fkDisplayWidthPx}
          numberOfOutstandingComputeFkMutations={this.props.numberOfOutstandingComputeFkMutations}
          heightPx={this.props.fkDisplayHeightPx}
          multipleSelected={this.props.selectedSdIds && this.props.selectedSdIds.length > 1}
          anySelected={this.props.selectedSdIds && this.props.selectedSdIds.length > 0}
          userInputFkFrequency={this.props.userInputFkFrequency}
          channelFilters={this.props.channelFilters}
          fkUnit={this.props.fkUnitForDisplayedSignalDetection}
          userInputFkWindowParameters={this.props.userInputFkWindowParameters}
          onNewFkParams={this.onNewFkParams}
          defaultSignalDetectionPhase={this.props.defaultSignalDetectionPhase}
          changeUserInputFks={this.props.changeUserInputFks}
          setFkUnitForSdId={this.props.setFkUnitForSdId}
          updateCurrentMovieTimeIndex={this.updateCurrentMovieTimeIndex}
          fkFrequencyThumbnails={this.props.fkFrequencyThumbnails}
          setChannelFilters={this.props.setChannelFilters}
          currentMovieSpectrumIndex={this.state.currentMovieSpectrumIndex}
          arrivalTimeMovieSpectrumIndex={this.arrivalTimeMovieSpectrumIndex}
          setMeasurementModeEntries={this.props.setMeasurementModeEntries}
        />
      </div>
    );
  }

  /**
   * Set arrival time movie spectrum index for initial load and update current displayed state
   *
   * @param signalDetection current signal detection
   */
  private readonly setArrivalTimeMovieSpectrumIndex = (
    signalDetection: SignalDetectionTypes.SignalDetection
  ): void => {
    const arrivalTime = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(signalDetection.signalDetectionHypotheses)
        .featureMeasurements
    ).arrivalTime.value;

    this.arrivalTimeMovieSpectrumIndex = this.calculateMovieIndex(arrivalTime);
  };

  /**
   * Selects the next fk that needs review
   */
  private readonly nextFk = () => {
    if (!this.props.displayedSignalDetection) {
      throw Error('Selected Signal Detection not found in client cache');
    }
    const reviewableSds = this.getReviewableSds();
    if (
      reviewableSds.length === 1 &&
      reviewableSds.find(sd => sd.id === this.props.displayedSignalDetection.id)
    ) {
      this.props.setSelectedSdIds([]);
      this.markFksForSdIdsAsReviewedIfTheyNeedToBeAccepted([
        this.props.displayedSignalDetection.id
      ]);
    } else {
      const needsReviewSds = reviewableSds.filter(
        sd => sd.id !== this.props.displayedSignalDetection.id
      );
      const sortedNeedsReviewSds = getSortedSignalDetections(
        needsReviewSds,
        this.props.selectedSortType,
        this.props.distances
      );
      this.props.setSelectedSdIds([sortedNeedsReviewSds[0].id]);
      this.markFksForSdIdsAsReviewedIfTheyNeedToBeAccepted([
        this.props.displayedSignalDetection.id
      ]);
    }
  };

  /**
   * Handles key presses on az slow
   *
   * @param e keyboard event
   */
  private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
    if (e.nativeEvent.code === 'KeyN' && (e.metaKey || e.ctrlKey)) {
      if (this.getReviewableSds().length > 0 && this.props.selectedSdIds.length === 1) {
        this.nextFk();
      }
    }
  };

  /**
   * Clear selected UnassociatedFks
   */
  private readonly clearSelectedUnassociatedFks = (): void => {
    const selectedAndUnassociatedSdIds = this.props.selectedSdIds.filter(
      sdId => !this.props.associatedSignalDetections.find(assocSd => assocSd.id === sdId)
    );
    const selectedAndAssociatedSdIds = this.props.selectedSdIds.filter(sdId =>
      this.props.associatedSignalDetections.find(assocSd => assocSd.id === sdId)
    );
    const sdsIdsToShowFk = this.props.sdIdsToShowFk.filter(
      sdId => !selectedAndUnassociatedSdIds.find(unSdId => unSdId === sdId)
    );
    this.props.setSelectedSdIds(selectedAndAssociatedSdIds);
    this.props.setSdIdsToShowFk(sdsIdsToShowFk);
  };

  /**
   * Shows the fk thumbnail menu
   *
   * @param x offset from left
   * @param y offset from top
   */
  private readonly showFkThumbnailMenu = (x: number, y: number) => {
    const selectedAndUnassociatedSdIds = this.props.selectedSdIds.filter(
      sdId => !this.props.associatedSignalDetections.find(assocSd => assocSd.id === sdId)
    );
    const stageIntervalContextMenu = FkThumbnailBlueprintContextMenu(
      this.clearSelectedUnassociatedFks,
      selectedAndUnassociatedSdIds.length > 0
    );
    // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
    ContextMenu.show(
      stageIntervalContextMenu,
      {
        left: x,
        top: y
      },
      undefined,
      true
    );
  };

  /**
   * Start a drag on mouse down on the divider
   */
  private readonly onThumbnailDividerDrag = (e: React.MouseEvent<HTMLDivElement>) => {
    let prevPosition = e.clientX;
    let currentPos = e.clientX;
    let diff = 0;
    const maxWidthPct = 0.8;
    const maxWidthPx = this.azimuthSlownessContainer.clientWidth * maxWidthPct;

    const onMouseMove = (e2: MouseEvent) => {
      currentPos = e2.clientX;
      diff = currentPos - prevPosition;
      prevPosition = currentPos;
      const widthPx = this.fkThumbnailsContainer.clientWidth + diff;
      if (widthPx < maxWidthPx) {
        this.props.setFkThumbnailColumnSizePx(widthPx);
      }
    };

    const onMouseUp = () => {
      document.body.removeEventListener('mousemove', onMouseMove);
      document.body.removeEventListener('mouseup', onMouseUp);
    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  };

  /**
   * Handles new FK Request when frequency and/or window params change
   */
  private readonly onNewFkParams = (
    sdId: string,
    fkParams: FkParams,
    fkConfiguration: FkTypes.FkConfiguration
  ): void => {
    const fkData = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.displayedSignalDetection.signalDetectionHypotheses
      ).featureMeasurements
    );
    if (fkData) {
      const fkInput: FkTypes.FkInput = createFkInput(
        this.props.displayedSignalDetection,
        fkParams.frequencyPair,
        fkParams.windowParams,
        fkConfiguration
      );
      this.props.computeFkAndUpdateState(fkInput);
    }
  };

  /**
   * @param sdIds a list of candidate sd ids
   */
  private readonly markFksForSdIdsAsReviewedIfTheyNeedToBeAccepted = (sdIds: string[]) => {
    const sdIdsThatCanBeReviewed = this.getReviewableSds().map(sd => sd.id);
    const sdIdsToReview = sdIds.filter(sdId =>
      sdIdsThatCanBeReviewed.find(sdReviewableId => sdReviewableId === sdId)
    );
    if (sdIdsToReview.length > 0) {
      this.props.markFksForSdIdsAsReviewed(sdIdsToReview);
    }
  };

  /**
   * Calculates the index of the spectrum to display
   *
   * @param time the time in epoch seconds
   * @returns a index
   */
  private readonly calculateMovieIndex = (time: number): number => {
    // TODO: Fix call to lookup displayedSignalDetection's corresponding waveform
    const channelSegment = undefined;
    if (channelSegment === undefined) {
      return 0;
    }

    //! TODO should this be the first x value in the data segment?
    // const { startTime } = channelSegment.dataSegments[0].data.values[];
    const startTime = 0;
    const { stepSize } = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(
        this.props.displayedSignalDetection.signalDetectionHypotheses
      ).featureMeasurements
    );
    const index = Math.max((time - startTime) / stepSize, 0);

    return Math.round(index);
  };

  /**
   * Updates the state of the current movie index
   *
   * @param time start time in seconds of the fk movie
   */
  private readonly updateCurrentMovieTimeIndex = (time: number): void => {
    this.setState({ currentMovieSpectrumIndex: this.calculateMovieIndex(time) });
  };

  /**
   * Gets the list of fks that need review and are associated to the currently open event
   */
  private readonly getReviewableSds = () =>
    this.props.signalDetectionsToDraw.filter(
      sdToDraw =>
        this.props.associatedSignalDetections.find(aSd => aSd.id === sdToDraw.id) &&
        fkNeedsReview(sdToDraw)
    );
}
