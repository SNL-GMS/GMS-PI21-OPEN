import type GoldenLayout from '@gms/golden-layout';

import type { Command } from '~components/common-ui/components/command-palette/types';
import { CommandType } from '~components/common-ui/components/command-palette/types';
import type { GLConfig, GLMap } from '~components/workspace';
import {
  createCloseDisplayFunction,
  createOpenDisplayFunction,
  getClosedDisplays,
  getDisplayTitle,
  getOpenDisplays
} from '~components/workspace/components/golden-layout/golden-layout-util';

/**
 * Creates Open Display commands, one per closed display.
 */
export const createOpenDisplayCommands = (
  gl: GoldenLayout,
  glConfig: GLConfig,
  glComponents: GLMap
): Command[] => {
  if (!gl || !glConfig || !glComponents) {
    return [];
  }
  const openDisplay = createOpenDisplayFunction(gl, glConfig);
  const closedDisplayKeys = getClosedDisplays(gl, glComponents);
  return closedDisplayKeys.map(displayKey => ({
    commandType: CommandType.OPEN_DISPLAY,
    searchTags: ['open', 'show', 'display', getDisplayTitle(glComponents, displayKey)],
    displayText: `${CommandType.OPEN_DISPLAY} ${getDisplayTitle(glComponents, displayKey)}`,
    priority: 20,
    action: () => openDisplay(displayKey)
  }));
};

/**
 * Creates Close Display commands, one per closed display
 */
export const createCloseDisplayCommands = (
  gl: GoldenLayout,
  glConfig: GLConfig,
  glComponents: GLMap
): Command[] => {
  if (!gl || !glConfig || !glComponents) {
    return [];
  }
  const closeDisplay = createCloseDisplayFunction(gl);
  const openDisplayKeys = getOpenDisplays(gl);
  return openDisplayKeys.map(displayKey => ({
    commandType: CommandType.CLOSE_DISPLAY,
    searchTags: ['close', 'kill', 'display', getDisplayTitle(glComponents, displayKey)],
    displayText: `${CommandType.CLOSE_DISPLAY} ${getDisplayTitle(glComponents, displayKey)}`,
    priority: 10,
    action: () => closeDisplay(displayKey)
  }));
};
