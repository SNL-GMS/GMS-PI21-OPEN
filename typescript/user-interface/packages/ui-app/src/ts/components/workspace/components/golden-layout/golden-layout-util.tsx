import type { UserProfileTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import { UILogger } from '@gms/ui-util';
import flatMap from 'lodash/flatMap';
import uniqBy from 'lodash/uniqBy';
import xor from 'lodash/xor';

import type { GLComponentValue, GLConfig, GLConfigResult, GLMap } from './types';
import { isGLComponentMap } from './types';

/**
 * Gets displays from a generic gl "content" item
 *
 * @param content - may be a component or a collection of components
 * ! Note that content is given to us as an any because gl.toConfig returns an any
 * ! and this is a dependency that we cannot change.
 * We enforce a type here to make the object easier to query, but it is not
 * guaranteed to match the return type.
 * From Golden Layout's type definitions: glConfig is a serialisable object.
 */
export const getDisplaysFromContent = (content: GLConfigResult): string[] =>
  // eslint-disable-next-line no-nested-ternary
  content.component
    ? [content.component]
    : content.content
    ? flatMap(content.content, getDisplaysFromContent)
    : [];

/**
 * Gets all open components (displays) from a gl config
 *
 * @param glConfig - the configuration to get a component from
 * ! Note that content is given to us as an any because gl.toConfig returns an any
 * ! and this is a dependency that we cannot change.
 * We enforce a type here to make the object easier to query, but it is not
 * guaranteed to match the return type.
 * From Golden Layout's type definitions: glConfig is a serialisable object.
 */
export const getDisplaysFromConfig = (glConfig: GLConfigResult): string[] => {
  const { content } = glConfig;
  const displays: string[] = flatMap(content, getDisplaysFromContent);
  return displays;
};

/**
 * Gets the currently open displays by their component name
 */
export const getOpenDisplays = (gl: GoldenLayout): string[] => {
  if (gl) {
    const currentConfig = gl.toConfig();
    const openDisplays = getDisplaysFromConfig(currentConfig);
    return openDisplays;
  }
  return [];
};

/**
 * returns the display key strings that correspond to the component
 * (glComponent.id.component)
 *
 * @param glComponents the map of components in golden layout
 */
export const getAllDisplayKeys = (glComponents: GLMap): string[] => {
  const displayKeys: string[] = [];
  glComponents.forEach((value: GLComponentValue) => {
    if (isGLComponentMap(value)) {
      value.forEach((v, key) => {
        displayKeys.push(key);
      });
    } else {
      displayKeys.push(value.id.component);
    }
  });
  return displayKeys;
};

/**
 * returns the title of a display
 *
 * @param glComponents the map of golden layout components
 * @param keyToFind the displayKey for a goldenLayout component
 */
export const getDisplayTitle = (glComponents: GLMap, keyToFind: string): string => {
  let theTitle: string;
  glComponents.forEach((value: GLComponentValue) => {
    if (isGLComponentMap(value)) {
      value.forEach((v, key) => {
        if (key === keyToFind) {
          theTitle = v.id.title;
        }
      });
    } else {
      theTitle = value.id.title;
    }
  });
  return theTitle;
};

/**
 *
 * @param gl the GoldenLayout object for the current golden layout instance
 * @param glComponents the map of all configured components in the golden layout
 */
export const getClosedDisplays = (gl: GoldenLayout, glComponents: GLMap): string[] => {
  if (gl) {
    const allKeys = getAllDisplayKeys(glComponents);
    const openDisplayKeys = getOpenDisplays(gl);
    const closedDisplays = xor(allKeys, openDisplayKeys);
    return closedDisplays;
  }
  return [];
};

/**
 * A higher order function that returns a function that opens a display indicated by a string
 *
 * @returns a function that opens a display indicated by the componentKey
 */
export const createOpenDisplayFunction = (gl: GoldenLayout, glConfig: GLConfig) => (
  componentKey: string
): void => {
  if (gl.root.contentItems[0]) {
    gl.root.contentItems[0].addChild(glConfig.components[componentKey]);
  } else {
    gl.root.addChild(glConfig.components[componentKey]);
  }
};

/**
 * Gets displays from a generic gl "content" item
 *
 * @param content - may be a component or a collection of components
 */
export const removeDisplaysFromContent = (content: any, key: string): void => {
  if (content.isComponent && content.config.component === key) {
    content.remove();
  }
  if (content.contentItems && content.contentItems.length) {
    content.contentItems.forEach(item => removeDisplaysFromContent(item, key));
  }
};

export const createCloseDisplayFunction = (gl: GoldenLayout) => (componentKey: string): void => {
  gl.root.contentItems.forEach(contentItem => removeDisplaysFromContent(contentItem, componentKey));
};

/**
 * Clears golden layout from local storage
 */
export const clearLayout = (): void => {
  localStorage.removeItem('gms-analyst-ui-layout');
  // eslint-disable-next-line no-restricted-globals
  location.reload();
};

/**
 * Pops out a new window that shows the logs
 */
export const showLogPopup = (): void => {
  UILogger.showLogPopUpWindow();
};

/**
 * Filters a list of layouts to give results that are uniq by name and
 * valid in the current user interface mode. We must ensure uniqueness
 *  due to the current way layouts are stored in the database. It causes duplicates in the UI.
 *
 * @param layouts the list of layout which should be filtered
 * @param defaultLayoutName if provided, this is filtered out
 * @param supportedUserInterfaceMode the
 */
export const uniqueLayouts = (
  layouts: UserProfileTypes.UserLayout[],
  defaultLayoutName: string,
  supportedUserInterfaceMode: UserProfileTypes.UserMode
): UserProfileTypes.UserLayout[] =>
  uniqBy(
    layouts
      .filter(wl => wl.name !== defaultLayoutName)
      .filter(layout => layout.supportedUserInterfaceModes.includes(supportedUserInterfaceMode)),
    wl => wl.name
  );
