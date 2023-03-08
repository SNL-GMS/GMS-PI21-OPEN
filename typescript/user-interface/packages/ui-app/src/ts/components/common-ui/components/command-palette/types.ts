import type { SearchResult, SelectionManager } from '@gms/ui-util';

/**
 * A broad category of command.
 */
export enum CommandType {
  SHOW_ABOUT = 'Help: About',
  ACKNOWLEDGE = 'SOH: Acknowledge',
  SELECT_STATION = 'SOH: Select Station',
  OPEN_DISPLAY = 'Displays: Open Display',
  CLOSE_DISPLAY = 'Displays: Close Display',
  LOG_OUT = 'User: Log Out',
  CLEAR_LAYOUT = 'Developer: Clear Layout',
  SHOW_LOGS = 'Developer: Show Logs',
  SHOW_KEYBOARD_SHORTCUTS = 'Help: Show Keyboard Shortcuts',
  SAVE_WORKSPACE = 'Workspace: Save Workspace As',
  LOAD_WORKSPACE = 'Workspace: Open Workspace'
}

/**
 * Used as a key for registering commands. Registering a command at a specific
 * scope will overwrite all previously registered commands at that scope.
 */
export enum CommandScope {
  ANALYST = 'ANALYST',
  COMMON = 'COMMON',
  DEV = 'DEV',
  DISPLAY_MANAGEMENT = 'DISPLAY_MANAGEMENT',
  GOLDEN_LAYOUT = 'GOLDEN_LAYOUT',
  SOH = 'SOH'
}

/**
 * Tye type of a command.
 */
export interface Command {
  // the command enum corresponding to this command
  commandType: CommandType;

  // the text for the command palette list
  displayText?: string;

  // A list of strings that should be searched for this command
  searchTags?: string[];

  // higher priority appears closer to the top of the list. Treated as 0 by default.
  priority?: number;

  // the function to call when the command is executed.
  action(): void;
}

export interface CommandPaletteComponentReduxProps {
  commandPaletteIsVisible: boolean;
  keyPressActionQueue: Record<string, number>;
  setCommandPaletteVisibility(visibility: boolean): void;
  setKeyPressActionQueue(actions: Record<string, number>): void;
}

export type CommandPaletteComponentProps = CommandPaletteComponentReduxProps;

export interface CommandPaletteProps {
  isVisible: boolean;
  defaultSearchTerms?: string[];
  commandActions: Command[];
}

export interface CommandPaletteState {
  defaultSearchResults: SearchResult<Command>[];
}

export interface SearchableCommandPaletteProps extends CommandPaletteProps {
  commandActions: Command[];
  defaultSearchResults?: SearchResult<Command>[];
}

export const scrollOptions: ScrollIntoViewOptions = {
  behavior: 'smooth',
  block: 'nearest',
  inline: 'nearest'
};

export interface CommandPaletteResultProps {
  searchResult: SearchResult<Command>;
  isSelected: boolean;
}

export interface CommandPaletteResultListProps {
  searchResults: SearchResult<Command>[];
  selectedResult: SearchResult<Command>;
}

export interface CommandPaletteInputProps {
  selectionManager: SelectionManager<Command>;
  getSearchTerm(): string;
  setSearchTerm(term: string): void;
}

export interface CommandPaletteOverlayProps {
  commandActions: Command[];
  showCommandPalette: boolean;
}
