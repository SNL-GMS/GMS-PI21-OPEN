/* eslint-disable react/destructuring-assignment */
import { OffScreenWrapper } from '@gms/ui-util';
import * as React from 'react';

import type { DragCellProps } from '~components/data-acquisition-ui/shared/types';

import { CellImage } from './cell-image';
import { DragInitiator } from './drag-initiator';

/**
 * Creates a wrapper around a cell that handles
 * drag functionality.
 */
export class DragCell extends React.Component<React.PropsWithChildren<DragCellProps>> {
  private readonly dropZoneBaseClass: string = 'drop-zone';

  private readonly dragImagesToCleanUp: OffScreenWrapper[] = [];

  public componentWillUnmount(): void {
    this.cleanUpDragImages();
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <DragInitiator<string[]>
        onDragStart={this.handleDragStart}
        getDragPayload={this.getUpdatedSelection}
        getDragImage={this.getDragImage}
        onDragEnd={this.handleDragEnd}
      >
        {this.props.children}
      </DragInitiator>
    );
  }

  /**
   * If the drag was on a unselected cell, return that cell.
   * otherwise, return the existing selection that contains the
   * dragged cell.
   */
  private readonly getUpdatedSelection = () => {
    const selectedStationIds = this.props.getSelectedStationIds();
    if (selectedStationIds && selectedStationIds.includes(this.props.stationId)) {
      return selectedStationIds;
    }
    return [this.props.stationId];
  };

  /**
   * updates the selection and highlights the drop zone
   */
  private readonly handleDragStart = (e: React.DragEvent) => {
    const theSelection = this.getUpdatedSelection();
    this.props.setSelectedStationIds(theSelection);
    this.highlightDropZone(e);
  };

  private readonly handleDragEnd = (e: React.DragEvent) => {
    this.cleanUpDragImages();
    this.removeParentDropZoneHighlight(e);
  };

  /**
   * Are we dragging multiple cells or not? Assume that if nothing
   * is selected, then we are dragging one cell that will get selected.
   */
  private readonly isDraggingMultipleCells = () => {
    const selectedStationIds = this.props.getSelectedStationIds();
    return selectedStationIds && selectedStationIds.length > 1;
  };

  /**
   * Returns an Element that should be displayed as the drag image
   */
  private readonly getDragImage = (e: React.DragEvent) => {
    if (this.isDraggingMultipleCells()) {
      return this.createMultiDragImage();
    }
    return this.props.getSingleDragImage(e);
  };

  /**
   * Creates an element off screen that the drag image API
   * can display. Must be actually rendered in the browser
   * for the drag API to be able to use the DOM elements as
   * actual images.
   */
  private readonly createMultiDragImage = () => {
    const wrapper = new OffScreenWrapper();
    const message = `${this.props.getSelectedStationIds()?.length ?? '?'} stations selected`;
    const messageCell = new CellImage(0, message, false);
    wrapper.append(messageCell.getElement());
    const background1 = new CellImage(1, message, true);
    wrapper.append(background1.getElement());
    const background2 = new CellImage(2, message, true);
    wrapper.append(background2.getElement());
    this.dragImagesToCleanUp.push(wrapper);
    return wrapper.getElement();
  };

  private readonly validateDropZoneAndWrapper = (e: React.DragEvent) => {
    if (e.target instanceof Element) {
      let dropZoneWrapper: HTMLElement;
      try {
        dropZoneWrapper = e.target.closest(`.${this.dropZoneBaseClass}__wrapper`);
      } catch {
        throw new Error(
          `.${this.dropZoneBaseClass}__wrapper expected but not found. A ${this.dropZoneBaseClass}__wrapper class should appear on a parent of both the ${this.dropZoneBaseClass} and the drag initiator.`
        );
      }
      const dropZone = dropZoneWrapper.querySelector(`.${this.dropZoneBaseClass}`);
      if (!dropZone) {
        throw new Error(
          `.${this.dropZoneBaseClass} expected but not found. A ${this.dropZoneBaseClass} should be defined within a ${this.dropZoneBaseClass}__wrapper that is also a parent of the dragged cell.`
        );
      }
    }
  };

  /**
   * Traverse the DOM to find the drop-zone inside the
   * drop-zone wrapper that also contains this drag cell
   */
  private readonly getDropZones = (e: React.DragEvent) => {
    this.validateDropZoneAndWrapper(e);
    if (e.target instanceof Element) {
      const dropZoneWrapper = e.target.closest(`.${this.dropZoneBaseClass}__wrapper`);
      return dropZoneWrapper.querySelectorAll(`.${this.dropZoneBaseClass}`);
    }
    throw new Error('The dragged item was not an Element');
  };

  /**
   * Add a drop-zone--highlighted class to the drop zone
   */
  private readonly highlightDropZone = (e: React.DragEvent) => {
    if (e.target instanceof Element) {
      const dropZones = this.getDropZones(e);
      dropZones.forEach(dropZone =>
        dropZone.classList.add(`${this.dropZoneBaseClass}--highlighted`)
      );
    }
  };

  /**
   * removes the drop-zone--highlighted class from the drop zone
   */
  private readonly removeParentDropZoneHighlight = (e: React.DragEvent) => {
    if (e.target instanceof Element) {
      const dropZones = this.getDropZones(e);
      dropZones.forEach(dropZone =>
        dropZone.classList.remove(`${this.dropZoneBaseClass}--highlighted`)
      );
    }
  };

  private readonly cleanUpDragImages = () => {
    while (this.dragImagesToCleanUp.length > 0) {
      const cleanMe = this.dragImagesToCleanUp.pop();
      cleanMe.destroy();
    }
  };
}
