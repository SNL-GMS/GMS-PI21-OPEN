/* eslint-disable @typescript-eslint/no-use-before-define */
/* eslint-disable react/destructuring-assignment */
import { ContextMenu, Menu, MenuItem } from '@blueprintjs/core';
import type {
  CommonTypes,
  ConfigurationTypes,
  EventTypes,
  LegacyEventTypes
} from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import type { EventStatus } from '@gms/ui-state';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import produce from 'immer';
import type { WritableDraft } from 'immer/dist/internal';
import includes from 'lodash/includes';
import React from 'react';

import { EventUtils, SignalDetectionUtils } from '~analyst-ui/common/utils';
import {
  getAssocStatusColor,
  getAssocStatusString
} from '~analyst-ui/common/utils/signal-detection-util';
import { systemConfig } from '~analyst-ui/config';

import { SignalDetectionDetails } from '../dialogs';
import { PhaseSelectionMenu } from '../dialogs/phase-selection-menu';
import { getSignalDetectionAssociationStatus } from '../utils/event-util';

const logger = UILogger.create('GMS_LOG_SIGNAL_DETECTION', process.env.GMS_LOG_SIGNAL_DETECTION);

/**
 * DetectionRePhaser
 * a callback which executes re-phase logic
 * function to initiate re-phasing
 */
export type DetectionRePhaser = (sdIds: string[], phase: string) => void;

/**
 * DetectionRejecter
 * function to initiate rejecting detection
 */
export type DetectionRejecter = (sdIds: string[]) => void;

/**
 * DetectionFkGenerator
 * function to generate detections for fk's
 */
export type DetectionFkGenerator = () => void;

export type SignalDetectionAssociator = (
  signalDetectionHypoIds: string[],
  eventHypothesisId: string,
  associate: boolean
) => void;

export interface SignalDetectionContextMenuProps {
  signalDetections: SignalDetectionTypes.SignalDetection[];
  selectedSds: SignalDetectionTypes.SignalDetection[];
  sdIdsToShowFk: string[];
  currentOpenEventId: string;
  measurementMode: AnalystWorkspaceTypes.MeasurementMode;
  changeAssociation(args: any): Promise<void>;
  associateToNewEvent(args: any): Promise<void>;
  updateDetections?(args: any): Promise<void>;
  rejectDetections(args: any): Promise<void>;
  setSelectedSdIds(id: string[]): void;
  setSdIdsToShowFk?(signalDetectionIds: string[]): void;
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
  events?: EventTypes.Event[];
  eventStatuses?: Record<string, EventStatus>;
  uiTheme?: ConfigurationTypes.UITheme;
  // Used to position Signal Detection details popover
  clientX?: number;
  clientY?: number;
}
export class SignalDetectionContextMenu extends React.Component<
  SignalDetectionContextMenuProps,
  any
> {
  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp, complexity
  public render(): JSX.Element {
    // TODO: fix when SDs are associated to events
    // eslint-disable-next-line max-len
    const anyInConflictAndNotAssociatedToOpenEvent = false;

    // TODO: fix when SDs are associated to events
    const anyInConflict = false;
    /* SignalDetectionUtils.isInConflict(this.props.selectedSds); */

    const selectedSdIds = this.props.selectedSds.map(sd => sd.id);
    let allRejected = true;
    this.props.selectedSds.forEach(sd => {
      if (!SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).rejected) {
        allRejected = false;
      }
    });

    // TODO: remove allRejected = true to enable menu items
    allRejected = true;

    return (
      <Menu>
        <MenuItem
          text="Open signal detection details"
          label="(Alt + click)"
          disabled={selectedSdIds.length !== 1 || !this.props.clientX || !this.props.clientY}
          data-cy="show-sd-details"
          shouldDismissPopover={false}
          onClick={() => {
            this.showSdDetailsPopover(
              this.props.selectedSds[0],
              this.props.clientX,
              this.props.clientY
            );
          }}
        />
        <MenuItem
          text="Set phase"
          label="(Ctrl + s)"
          disabled={
            this.props.updateDetections === undefined ||
            selectedSdIds.length === 0 ||
            allRejected ||
            anyInConflictAndNotAssociatedToOpenEvent
          }
          data-cy="set-phase"
        >
          {setPhaseContextMenu(selectedSdIds, this.props.updateDetections)}
        </MenuItem>
        <MenuItem text="Event association" disabled={allRejected} data-cy="association-menu">
          {this.eventAssociationContextMenu(this.props.selectedSds, undefined)}
        </MenuItem>
        <MenuItem
          text="Show FK"
          disabled={
            this.props.setSdIdsToShowFk === undefined ||
            (selectedSdIds.length > 0 && canDisplayFkForSds(this.props.selectedSds)) ||
            allRejected ||
            anyInConflictAndNotAssociatedToOpenEvent
          }
          onClick={this.setSdIdsToShowFk}
          data-cy="show-fk"
        />
        <MenuItem
          text="Reject"
          disabled={
            this.props.rejectDetections === undefined ||
            selectedSdIds.length === 0 ||
            allRejected ||
            anyInConflict
          }
          onClick={() => this.rejectDetections(selectedSdIds)}
          data-cy="reject-sd"
        />
        {this.props.setMeasurementModeEntries
          ? this.buildMeasurementModeContextMenuItem(selectedSdIds)
          : undefined}
      </Menu>
    );
  }

  /**
   * Displays a blueprint context menu for event association.
   *
   * @param signalDetections a list of signal detections
   * @returns the event association context menu
   */
  private readonly eventAssociationContextMenu = (
    signalDetections: SignalDetectionTypes.SignalDetection[],
    event: LegacyEventTypes.Event
  ): JSX.Element[] => {
    const sdHypotheses = signalDetections.map(sd =>
      SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
    );

    const associatedInList: boolean =
      sdHypotheses.filter(sdHyp =>
        EventUtils.isAssociatedToCurrentEventHypothesisLegacy(sdHyp, event)
      ).length > 0;
    const unassociatedInList: boolean =
      sdHypotheses.filter(
        sdHyp => !EventUtils.isAssociatedToCurrentEventHypothesisLegacy(sdHyp, event)
      ).length > 0;
    const menuOptions = [];
    menuOptions.push(
      <MenuItem
        text="Associate to new event"
        onClick={() => {
          this.associateToNewEvent(signalDetections.map(sd => sd.id));
        }}
        data-cy="associate-to-new"
        key="assocnew"
      />
    );
    menuOptions.push(
      // eslint-disable-next-line no-nested-ternary
      associatedInList && unassociatedInList
        ? [
            <MenuItem
              text="Associate to currently open event"
              onClick={() => {
                const sdIdList = signalDetections
                  .filter(
                    sd =>
                      !EventUtils.isAssociatedToCurrentEventHypothesisLegacy(
                        SignalDetectionTypes.Util.getCurrentHypothesis(
                          sd.signalDetectionHypotheses
                        ),
                        event
                      )
                  )
                  .map(sdHyp => sdHyp.id);
                this.unassociateOrAssociateSignalDetections(sdIdList, event, true);
              }}
              disabled={this.props.currentOpenEventId === undefined}
              data-cy="associate-to-open"
              key="assocopen"
            />,
            <MenuItem
              text="Unassociate from currently open event"
              onClick={() => {
                const sdIdList = signalDetections
                  .filter(sd =>
                    EventUtils.isAssociatedToCurrentEventHypothesisLegacy(
                      SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses),
                      event
                    )
                  )
                  .map(sdHyp => sdHyp.id);
                this.unassociateOrAssociateSignalDetections(sdIdList, event, false);
              }}
              data-cy="unassociate-to-open"
              key="unassocopen"
            />
          ]
        : // eslint-disable-next-line no-nested-ternary
        associatedInList
        ? [
            <MenuItem
              text="Unassociate from currently open event"
              data-cy="unassociate-to-open"
              onClick={() => {
                const sdIdList = signalDetections.map(sd => sd.id);
                this.unassociateOrAssociateSignalDetections(sdIdList, event, false);
              }}
              key="unassocopen"
            />
          ]
        : unassociatedInList
        ? [
            <MenuItem
              text="Associate to currently open event"
              onClick={() => {
                const sdIdList = signalDetections.map(sd => sd.id);
                this.unassociateOrAssociateSignalDetections(sdIdList, event, true);
              }}
              data-cy="associate-to-open"
              disabled={this.props.currentOpenEventId === undefined}
              key="assocopen"
            />
          ]
        : null
    );

    return menuOptions;
  };

  /**
   * Build measurement mode menu item to add to context menu
   *
   * @returns MenuItem
   */
  private readonly buildMeasurementModeContextMenuItem = (selectedSdIds: string[]): JSX.Element => {
    const measurementModeEntries = this.props.measurementMode.entries;
    // TODO: When converted from legacy events, look up associated signal detection hypothesis IDs
    const associatedSignalDetectionHypothesisIds = [];
    const areAllSelectedAssociatedAndAutoShow =
      this.props.measurementMode.mode === AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT &&
      this.props.selectedSds.every(
        sd =>
          includes(
            systemConfig.measurementMode.phases,
            SignalDetectionUtils.findPhaseFeatureMeasurementValue(
              SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
                .featureMeasurements
            ).value
          ) &&
          includes(
            associatedSignalDetectionHypothesisIds,
            SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).id.id
          ) &&
          !measurementModeEntries[sd.id]
      );

    const areAllSelectedSdsMarkedAsMeasurementEntriesToShow = this.props.selectedSds.every(
      sd => !measurementModeEntries[sd.id]
    );
    return (
      <MenuItem text="Measure" data-cy="measure" disabled>
        <MenuItem
          text={
            areAllSelectedSdsMarkedAsMeasurementEntriesToShow || areAllSelectedAssociatedAndAutoShow
              ? 'Hide A5/2'
              : 'Show A5/2'
          }
          onClick={() =>
            this.toggleShownSDs(
              selectedSdIds,
              areAllSelectedSdsMarkedAsMeasurementEntriesToShow,
              areAllSelectedAssociatedAndAutoShow
            )
          }
          data-cy="show-hide-measure"
        />
        <MenuItem
          text="Hide all A5/2"
          data-cy="hide-all"
          disabled={
            !(
              this.props.measurementMode.mode ===
                AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT ||
              Object.keys(measurementModeEntries).length !== 0
            )
          }
          onClick={() => this.hideMeasurementModeEntries(associatedSignalDetectionHypothesisIds)}
        />
        {this.props.measurementMode.mode ===
        AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT ? (
          <MenuItem
            text="Show all A5/2 for associated"
            onClick={() => {
              this.showMeasurementModeEntries(associatedSignalDetectionHypothesisIds);
            }}
          />
        ) : undefined}
      </MenuItem>
    );
  };

  /**
   * Method called by hide measurement mode menu onClick to update measurement mode entries to hide
   *
   * @param associatedSignalDetectionHypothesisIds list of signal detection hypos
   */
  private readonly hideMeasurementModeEntries = (
    associatedSignalDetectionHypothesisIds: string[]
  ) => {
    // clear out all the additional measurement mode entries
    const updatedEntries = produce(
      this.props.measurementMode.entries,
      (draft: WritableDraft<Record<string, boolean>>) => {
        Object.keys(draft).forEach(key => {
          draft[key] = false;
        });

        if (
          this.props.measurementMode.mode === AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT
        ) {
          // hide all auto show
          this.props.signalDetections
            .filter(
              sd =>
                includes(
                  associatedSignalDetectionHypothesisIds,
                  SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).id.id
                ) &&
                includes(
                  systemConfig.measurementMode.phases,
                  SignalDetectionUtils.findPhaseFeatureMeasurementValue(
                    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
                      .featureMeasurements
                  ).value
                )
            )
            .forEach(sd => {
              draft[sd.id] = false;
            });
        }
      }
    );

    this.props.setMeasurementModeEntries(updatedEntries);
  };

  /**
   Method called by show measurement mode menu onClick to update measurement mode entries to show
   *
   * @param associatedSignalDetectionHypothesisIds list of signal detection hypos
   */
  private readonly showMeasurementModeEntries = (
    associatedSignalDetectionHypothesisIds: string[]
  ) => {
    // Clear out all the additional measurement mode entries
    const updatedEntries = produce(this.props.measurementMode.entries, draft => {
      associatedSignalDetectionHypothesisIds.forEach(assocSDHypId => {
        // Retrieve the SD for the given hypotheses ID
        const signalDetection = this.props.signalDetections.find(
          sd =>
            SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).id.id ===
            assocSDHypId
        );

        if (
          includes(
            systemConfig.measurementMode.phases,
            SignalDetectionUtils.findPhaseFeatureMeasurementValue(
              SignalDetectionTypes.Util.getCurrentHypothesis(
                signalDetection.signalDetectionHypotheses
              ).featureMeasurements
            ).value
          )
        ) {
          draft[signalDetection.id] = true;
        }
      });
    });

    this.props.setMeasurementModeEntries(updatedEntries);
  };

  /**
   *
   * show or hide all selected sds
   */
  private readonly toggleShownSDs = (
    selectedSdIds: string[],
    areAllSelectedSdsMarkedAsMeasurementEntriesToShow: boolean,
    areAllSelectedAssociatedAndAutoShow: boolean
  ) => {
    const updatedEntires = produce(
      this.props.measurementMode.entries,
      (draft: WritableDraft<Record<string, boolean>>) => {
        selectedSdIds.forEach(id => {
          draft[id] = !(
            areAllSelectedSdsMarkedAsMeasurementEntriesToShow || areAllSelectedAssociatedAndAutoShow
          );
        });
      }
    );
    this.props.setMeasurementModeEntries(updatedEntires);
  };

  /**
   * Rejects the signal detections for the provided ids.
   *
   * @param sdIds the signal detection ids to reject
   */
  private readonly rejectDetections = (sdIds: string[]) => {
    const input: SignalDetectionTypes.RejectDetectionsMutationArgs = {
      detectionIds: sdIds
    };
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.props
      .rejectDetections({
        variables: input
      })
      .catch(err => logger.error(`Failed to reject detections: ${err.message}`));
  };

  /**
   * Returns true if the provided signal detection can be used to generate
   * an FK.
   *
   * @param signalDetection the signal detection to check if it can be used to
   * generate an FK.
   * @returns true if the signal detection can be used to generate an FK; false otherwise
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly canGenerateFk = (
    signalDetection: SignalDetectionTypes.SignalDetection
  ): boolean => {
    const fmPhase = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(signalDetection.signalDetectionHypotheses)
        .featureMeasurements
    );
    if (!fmPhase) {
      return false;
    }
    return (
      systemConfig.nonFkSdPhases
        // eslint-disable-next-line newline-per-chained-call
        .findIndex(phase => phase.toLowerCase() === fmPhase.value.toString().toLowerCase()) === -1
    );
  };

  /**
   * Sets or updates the signal detection ids to show FK based on
   * the selected signal detections.
   */
  private readonly setSdIdsToShowFk = () => {
    const sdIdsToShowFk = this.props.selectedSds
      .filter(sd => sd && this.canGenerateFk(sd))
      .map(sd => sd.id);
    if (sdIdsToShowFk.length > 0) {
      this.props.setSdIdsToShowFk([...this.props.sdIdsToShowFk, ...sdIdsToShowFk]);
    }
    this.props.setSelectedSdIds(this.props.selectedSds.map(sd => sd.id));
  };

  /**
   * Unassociate or associate the signal detections for the provided event.
   *
   * @param signalDetectionIds the signal detection hypothesis ids
   * @param event the event to unassociate or associate too
   * @param associate boolean flag indicating if we are associating or unassociating
   * to the provided event
   */
  private readonly unassociateOrAssociateSignalDetections = (
    signalDetectionIds: string[],
    event: LegacyEventTypes.Event,
    associate: boolean
  ): void => {
    if (!event) {
      return;
    }
    const input: LegacyEventTypes.ChangeSignalDetectionAssociationsMutationArgs = {
      eventHypothesisId: event.currentEventHypothesis.eventHypothesis.id,
      signalDetectionIds,
      associate
    };
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.props
      .changeAssociation({
        variables: input
      })
      .catch(err => logger.error(`Failed to change association: ${err.message}`));
  };

  private readonly associateToNewEvent = (sdIds: string[]) => {
    const input = {
      signalDetectionIds: sdIds
    };
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.props
      .associateToNewEvent({
        variables: input
      })
      .catch(err => logger.error(`Failed to associate to new event: ${err.message}`));
  };

  /**
   *  Show SD Details popover will display using signal detection
   *  details popover at position X,Y
   *
   * @param sd signal detection to populate the details display with
   * @param clientX X position on screen
   * @param clientY Y position on screen
   */
  private readonly showSdDetailsPopover = (
    sd: SignalDetectionTypes.SignalDetection,
    clientX: number,
    clientY: number
  ): void => {
    const assocStatus = getSignalDetectionAssociationStatus(
      sd,
      this.props.events,
      this.props.currentOpenEventId,
      this.props.eventStatuses
    );
    const assocColor = getAssocStatusColor(assocStatus, this.props.uiTheme);
    const assocStatusString = getAssocStatusString(assocStatus);
    // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
    ContextMenu.show(
      <SignalDetectionDetails detection={sd} color={assocColor} assocStatus={assocStatusString} />,
      { left: clientX, top: clientY },
      undefined,
      true
    );
  };
}

/**
 * Displays a blueprint context menu for selecting a signal detection phase.
 *
 * @param sdIds string array of signal detection ids
 * @param rePhaser mutation that updates a detection
 * @returns the phase selection context menu
 */
export function setPhaseContextMenu(
  sdIds: string[],
  rePhaser: (args: any) => Promise<void>
): JSX.Element {
  return (
    <PhaseSelectionMenu
      sdPhases={systemConfig.defaultSdPhases}
      prioritySdPhases={systemConfig.prioritySdPhases}
      onBlur={phase => {
        rePhaseDetections(sdIds, phase, rePhaser);
      }}
      onEnterForPhases={phase => {
        rePhaseDetections(sdIds, phase, rePhaser);
        ContextMenu.hide();
      }}
      onPhaseClicked={phase => {
        rePhaseDetections(sdIds, phase, rePhaser);
        ContextMenu.hide();
      }}
    />
  );
}

/**
 * Returns true if the provided signal detections can be used to display an FK.
 *
 * @param sds a list of signal detections
 * @returns true if the signal detections can be used to display an FK; false otherwise
 */
function canDisplayFkForSds(sds: SignalDetectionTypes.SignalDetection[]): boolean {
  sds.forEach(sd => {
    const fmPhase = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
        .featureMeasurements
    );
    if (!fmPhase) {
      return false;
    }
    if (
      systemConfig.nonFkSdPhases
        // eslint-disable-next-line newline-per-chained-call
        .findIndex(phase => phase === fmPhase.value) < 0
    ) {
      return true;
    }
    return false;
  });
  return false;
}

/**
 * Rephases the provided signal detection ids.
 *
 * @param phase the signal detection phase to set
 * @param detectionRephaser the mutation for rephasing a signal detection
 */
function rePhaseDetections(
  sdIds: string[],
  phase: CommonTypes.PhaseType,
  detectionRephaser: (args: any) => Promise<void>
) {
  const input: SignalDetectionTypes.UpdateDetectionsMutationArgs = {
    detectionIds: sdIds,
    input: {
      phase
    }
  };
  // eslint-disable-next-line @typescript-eslint/no-floating-promises
  detectionRephaser({
    variables: input
  }).catch(err => logger.error(`Failed to re-phase detection: ${err.message}`));
}
