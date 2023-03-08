import { sleep } from '@gms/common-util';
import type * as React from 'react';
import { act } from 'react-dom/test-utils';

const WAIT_DURATION_MS = 200;

/**
 * Fixes React warning that "An update to Component inside a test was not wrapped in act(...)."
 *
 * @param wrapper the Enzyme wrapper of the mounted component
 * @param callback the function that should be called within the act call.
 */
export const actAndWaitForComponentToPaint = async (wrapper: any, callback: any): Promise<void> => {
  // eslint-disable-next-line @typescript-eslint/await-thenable
  await act(async () => {
    callback();
    await sleep(WAIT_DURATION_MS);
    wrapper.update();
  });
};

/**
 * Creates and dispatches a 'mousemove' event on the document.body element.
 *
 * @param clientX the x coordinate in pixels at which to trigger the mouse move
 * @param clientY the y coordinate in pixels at which to trigger the mouse move
 */
export const documentMoveMouse = (clientX = 75, clientY = 75): void => {
  const mouseEventInit: MouseEventInit = {
    clientX,
    clientY
  };
  const moveMouseEvent = new MouseEvent('mousemove', mouseEventInit);
  document.body.dispatchEvent(moveMouseEvent);
};

/**
 * Creates and dispatches a 'mouseup' event on the document.body element.
 *
 * @param clientX the x coordinate in pixels at which to trigger the mouse up
 * @param clientY the y coordinate in pixels at which to trigger the mouse up
 */
export const documentReleaseMouse = (clientX = 90, clientY = 75): void => {
  // Now release mouse
  const mouseEventUp = {
    clientX,
    clientY
  };
  const mouseUpEvent = new MouseEvent('mouseup', mouseEventUp);
  document.body.dispatchEvent(mouseUpEvent);
};

/**
 * Simulates a click event on an element found within the wrapper matching the given selector.
 *
 * @param wrapper the mounted enzyme wrapper that contains the element to click
 * @param selector a css query selector string or component class to find and click within the wrapper
 */
export const clickElement = (wrapper: any, selector: string | React.ComponentClass): void => {
  const selectionDiv = wrapper.find(selector);
  if (selectionDiv.length) {
    selectionDiv.simulate('mousedown');
  }
};

/**
 * Simulates a drag event on an element found within the wrapper matching the given selector.
 *
 * @param wrapper the mounted enzyme wrapper that contains the element to click
 * @param selector a css query selector string or component class to find and click within the wrapper
 */
export const dragElement = (wrapper: any, selector: string | React.ComponentClass): void => {
  const selectionDiv = wrapper.find(selector);
  if (selectionDiv.length) {
    selectionDiv.simulate('dragstart');
  }
};
