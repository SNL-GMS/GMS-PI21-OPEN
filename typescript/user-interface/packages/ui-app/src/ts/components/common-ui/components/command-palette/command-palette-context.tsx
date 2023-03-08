import * as React from 'react';

import type { Command, CommandScope } from './types';

/**
 * Stores data and functions used by commands
 */
export interface CommandPaletteContextData {
  commandPaletteIsVisible: boolean;
  registerCommands(commandsToRegister: Command[], scope: CommandScope);
  setCommandPaletteVisibility(visibility: boolean): void;
}

export const CommandPaletteContext: React.Context<CommandPaletteContextData> = React.createContext<
  CommandPaletteContextData
>(undefined);
