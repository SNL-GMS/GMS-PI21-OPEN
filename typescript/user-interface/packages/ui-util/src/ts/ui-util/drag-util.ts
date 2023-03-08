import { uuid } from '@gms/common-util';
import type * as React from 'react';

import { UILogger } from './logger';

const logger = UILogger.create('GMS_LOG_DRAG_UTIL', process.env.GMS_LOG_DRAG_UTIL);

export enum DragEventType {
  APPLICATION_JSON = 'APPLICATION_JSON',
  SOH_ACKNOWLEDGEMENT = 'soh_acknowledgement'
}

/**
 * Returns a native event, if the event passed in was a React synthetic event.
 *
 * @param e the event React gave you
 */
export const getNativeEvent = (e: React.DragEvent<Element>): React.DragEvent<Element> | DragEvent =>
  (e instanceof Event ? e : e?.nativeEvent) ?? e;

/**
 * Returns true if the event passed in has been created with the DragEventType dragType.
 * Events created with the storeDragData function will have been created with one of the
 * corresponding DragEventTypes.
 *
 * @param e the synthetic event to check.
 * @param expectedDragEventType a drag event type to check for
 */
export const dragEventIsOfType: (
  e: React.DragEvent<Element>,
  expectedDragEventType: DragEventType
) => boolean = (e: React.DragEvent<Element>, expectedDragEventType: DragEventType) => {
  const event = getNativeEvent(e);
  if (!event) {
    logger.warn('Undefined event passed into dragEventIsOfType');
    return false;
  }
  const dragSupported =
    event.dataTransfer?.types.indexOf('application/json') >= 0 &&
    event.dataTransfer.types.indexOf(expectedDragEventType as string) >= 0;
  return dragSupported;
};

/**
 * Stores drag data on the native event within the React synthetic event.
 *
 * @param e the synthetic event to modify
 * @param dataPackage the data to store in the drag event
 * @param dragType the type of drag event, which will be checked by dragEventIsOfType
 * @param dragEventType
 */
export const storeDragData = (
  e: React.DragEvent,
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any
  dataPackage: any,
  dragEventType: DragEventType
): void => {
  const event = getNativeEvent(e);
  const jsonData = JSON.stringify(dataPackage);
  event.dataTransfer.setData('application/json', jsonData);
  event.dataTransfer.setData(dragEventType as string, 'true');
};

/**
 *
 * @param e the React DragEvent from which to retrieve data
 * @param expectedDragEventType the type of event we are expecting. Will return undefined if the
 * event type is different than the expected drag type
 */
export const getDragData = <T>(e: React.DragEvent, expectedDragEventType: DragEventType): T => {
  if (dragEventIsOfType(e, expectedDragEventType)) {
    const event = getNativeEvent(e);
    const jsonData = event.dataTransfer.getData('application/json');
    return JSON.parse(jsonData) as T;
  }
  return undefined;
};

export const overrideDragCursor = (e: React.DragEvent, img: Element): void => {
  const event = getNativeEvent(e);
  event.dataTransfer.setDragImage(img, img.clientWidth / 2, img.clientHeight / 2);
};

export class OffScreenWrapper {
  private readonly wrapper: HTMLElement;

  private readonly id: string;

  public constructor() {
    this.wrapper = document.createElement('div');
    this.renderOffScreen(this.wrapper);
    this.id = uuid.asString();
    this.wrapper.id = this.id;
  }

  public getElement(): HTMLElement {
    return this.wrapper;
  }

  public append(element: HTMLElement): void {
    this.wrapper.appendChild(element);
  }

  public destroy(): void {
    document.getElementById(this.id).parentElement.removeChild(this.wrapper);
  }

  // eslint-disable-next-line class-methods-use-this
  private readonly renderOffScreen = (el: HTMLElement) => {
    el.style.setProperty('position', 'absolute');
    el.style.setProperty('top', '-1000px');
    el.style.setProperty('translate', '0, 0'); // Chrome version 106 changes broke multi drag lines 112 and 113 are a work around to fix it
    el.style.setProperty('z-index', '0'); // https://stackoverflow.com/questions/73972272/google-chrome-106-draggable-causing-elements-to-disappear

    document.body.append(el);
  };
}
