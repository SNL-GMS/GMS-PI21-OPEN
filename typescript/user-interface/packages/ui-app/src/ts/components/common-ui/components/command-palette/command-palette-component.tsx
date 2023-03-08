/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import Immutable from 'immutable';
import * as React from 'react';

import type { CommandPaletteContextData } from './command-palette-context';
import { CommandPaletteContext } from './command-palette-context';
import { CommandPaletteOverlay } from './command-palette-overlay';
import type { Command, CommandPaletteComponentProps, CommandScope } from './types';

/**
 * Takes data and functions passed in from redux and updates the command palette context, which
 * is used by the command palette.
 * Adds the command palette overlay as a sibling after this component's children
 */
export const CommandPaletteComponent: React.FunctionComponent<React.PropsWithChildren<
  CommandPaletteComponentProps
  // eslint-disable-next-line react/function-component-definition
>> = ({
  children,
  commandPaletteIsVisible,
  setCommandPaletteVisibility
}: React.PropsWithChildren<CommandPaletteComponentProps>) => {
  const [commandActions, setCommandActions] = React.useState<
    Immutable.Map<CommandScope, Command[]>
  >(Immutable.Map());

  /**
   * Updates the commandAction map. Add or overwrite the list of commands keyed on the CommandScope.
   * Note, you can delete commands by registering an empty array for a given scope.
   *
   * @param commandsToRegister a list of command actions to register
   * @param scope the key to use to register these commands.
   */
  const registerCommands = (commandsToRegister: Command[], scope: CommandScope) => {
    setCommandActions(prevActions => prevActions.set(scope, commandsToRegister));
  };

  const commandPaletteContextData: CommandPaletteContextData = React.useMemo(
    () => ({
      commandPaletteIsVisible,
      registerCommands,
      setCommandPaletteVisibility
    }),
    [commandPaletteIsVisible, setCommandPaletteVisibility]
  );

  return (
    <CommandPaletteContext.Provider value={commandPaletteContextData}>
      {children}
      <CommandPaletteOverlay
        commandActions={commandActions.reduce(
          (allActions, actionList) => [...allActions, ...actionList],
          []
        )}
        showCommandPalette={commandPaletteIsVisible}
      />
    </CommandPaletteContext.Provider>
  );
};
