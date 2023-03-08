import { Timer } from '@gms/common-util';
import * as React from 'react';

import { CommandPaletteContext } from '../components/command-palette/command-palette-context';
import { CommandScope } from '../components/command-palette/types';
import type { CommandRegistrarProps } from './types';
import { useWorkspaceCommands } from './workspace-commands';

/**
 * Registers Common UI commands with the command palette.
 * Does not render anything, but updates the registered commands in the CommandPalette context
 */
// eslint-disable-next-line react/function-component-definition
export const CommandRegistrarComponent: React.FunctionComponent<CommandRegistrarProps> = props => {
  Timer.start('Registering workspace commands');
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { registerCommands } = React.useContext(CommandPaletteContext);
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { setAppAuthenticationStatus } = props;
  const workspaceCommands = useWorkspaceCommands(setAppAuthenticationStatus);
  const commandSignature = workspaceCommands.map(c => c.displayText).join();
  React.useEffect(() => {
    registerCommands([...workspaceCommands], CommandScope.COMMON);
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [commandSignature]);
  Timer.end('Registering workspace commands');
  return null; // this component just registers commands. It doesn't render anything.
};
