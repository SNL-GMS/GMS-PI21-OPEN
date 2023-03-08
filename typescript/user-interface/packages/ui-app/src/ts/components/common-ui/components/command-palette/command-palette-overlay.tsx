import { OverlayWrapper } from '@gms/ui-core-components';
import * as React from 'react';

import { CommandPalette } from './command-palette';
import { CommandPaletteContext } from './command-palette-context';
import type { CommandPaletteOverlayProps } from './types';
import { hasCommandListChanged } from './utils';

/**
 * Wraps the command palette in an overlay, and manages that state.
 */
function BaseCommandPaletteOverlay(props: CommandPaletteOverlayProps) {
  const { showCommandPalette, commandActions } = props;
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { setCommandPaletteVisibility } = React.useContext(CommandPaletteContext);
  return (
    <OverlayWrapper
      isOpen={showCommandPalette}
      onClose={() => {
        setCommandPaletteVisibility(false);
      }}
    >
      <CommandPalette
        isVisible={showCommandPalette}
        commandActions={commandActions}
        defaultSearchTerms={['open display', 'close display']}
      />
    </OverlayWrapper>
  );
}

/**
 * Memoized so that it only updates if the command list has changed.
 * Compares CommandType and DisplayText. If these are unchanged, the
 * command palette will not update.
 */
export const CommandPaletteOverlay = React.memo(
  BaseCommandPaletteOverlay,
  (prevProps, nextProps) =>
    nextProps.showCommandPalette &&
    hasCommandListChanged(prevProps.commandActions, nextProps.commandActions)
);
