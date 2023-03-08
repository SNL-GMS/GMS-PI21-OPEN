import type GoldenLayout from '@gms/golden-layout';
import defer from 'lodash/defer';
import React from 'react';

import { useForceUpdate } from './custom-hooks';

/**
 * Void callback function
 */
type callback = () => void;

/**
 * Helper function to execute the Golden Layout Container's action
 *
 * @param glContainer the golden layout container
 * @param callbackFn the call back function
 * @param action action to it should execute on
 */
const actionHelper = (
  glContainer: GoldenLayout.Container,
  callbackFn: callback,
  action: string
) => {
  if (glContainer && callbackFn) {
    // force update when the golden-layout container is resized
    glContainer.on(action, () => {
      defer(() => {
        callbackFn();
      });
    });
  }
};

/**
 * Attaches an event handler to the golden-layout event 'show' that will force
 * the component to update when dispatched.
 *
 * @param glContainer the golden-layout container
 * @param callbackFn the callback to invoke on show
 */
export const addGlUpdateOnShow = (
  glContainer: GoldenLayout.Container,
  callbackFn: callback
): void => {
  actionHelper(glContainer, callbackFn, 'show');
};

/**
 * Attaches an event handler to the golden-layout event 'show' that will force
 * the component to update when dispatched.
 *
 * @param glContainer the golden-layout container
 * @param callbackFn the callback to invoke on show
 */
export const addGlUpdateOnHide = (
  glContainer: GoldenLayout.Container,
  callbackFn: callback
): void => {
  actionHelper(glContainer, callbackFn, 'hide');
};

/**
 * Attaches an event handler to the golden-layout event 'show' that will force
 * the component to update when dispatched.
 *
 * @param glContainer the golden-layout container
 * @param component the react component to force update
 */
export const addGlForceUpdateOnShow = (
  glContainer: GoldenLayout.Container,
  component: React.Component
): void => {
  if (glContainer && component) {
    addGlUpdateOnShow(glContainer, () => component.forceUpdate());
  }
};

/**
 * Attaches an event handler to the golden-layout event 'show' that will force
 * the component to update when dispatched.
 *
 * @param glContainer the golden-layout container
 * @param component the react component to force update
 */
export const addGlForceUpdateOnHide = (
  glContainer: GoldenLayout.Container,
  component: React.Component
): void => {
  if (glContainer && component) {
    addGlUpdateOnHide(glContainer, () => component.forceUpdate());
  }
};

/**
 * Attaches an event handler to the golden-layout event 'resize' that will force
 * the component to update when dispatched.
 *
 * @param glContainer the golden-layout container
 * @param callbackFn the callback to invoke on resize
 */
export const addGlUpdateOnResize = (
  glContainer: GoldenLayout.Container,
  callbackFn: callback
): void => {
  if (glContainer && callbackFn) {
    // force update when the golden-layout container is resized
    glContainer.on('resize', () => {
      callbackFn();
    });
  }
};

/**
 * Attaches an event handler to the golden-layout event 'resize' that will force
 * the component to update when dispatched.
 *
 * @param glContainer the golden-layout container
 * @param component the react component to force update
 */
export const addGlForceUpdateOnResize = (
  glContainer: GoldenLayout.Container,
  component: React.Component
): void => {
  if (glContainer && component) {
    addGlUpdateOnResize(glContainer, () => component.forceUpdate());
  }
};

/**
 * A custom hook that will force update a react function component
 * on the golden layout events `resize` and `show`.
 *
 * @param glContainer the golden layout container
 */
export const useForceGlUpdateOnResizeAndShow = (glContainer: GoldenLayout.Container): void => {
  const forceUpdate = useForceUpdate();
  React.useEffect(() => {
    if (glContainer) {
      addGlUpdateOnShow(glContainer, forceUpdate);
      addGlUpdateOnResize(glContainer, forceUpdate);
    } else {
      window.addEventListener('resize', forceUpdate);
    }
    return () => {
      // removeEventListener is a no-op if there is no event that matches
      // so we don't need to check
      window.removeEventListener('resize', forceUpdate);

      if (glContainer && glContainer.off) {
        glContainer.off('show');
        glContainer.off('resize');
      }
    };
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [glContainer]);
};
