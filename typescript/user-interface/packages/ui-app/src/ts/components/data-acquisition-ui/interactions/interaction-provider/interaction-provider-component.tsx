/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import React from 'react';

import type { InteractionProviderProps } from './types';
import { InteractionContext } from './types';

/**
 * Provides one implementation of redux capabilities and provides them to child components via a context
 */
export function InteractionProvider(props: React.PropsWithChildren<InteractionProviderProps>) {
  const toggleCommandPaletteVisibility = React.useCallback(() => {
    props.setCommandPaletteVisibility(!props.commandPaletteIsVisible);
  }, [props]);
  const memoizedToggleCommandPaletteVisibility = React.useMemo(
    () => ({
      toggleCommandPaletteVisibility
    }),
    [toggleCommandPaletteVisibility]
  );

  return (
    <InteractionContext.Provider value={memoizedToggleCommandPaletteVisibility}>
      {props.children}
    </InteractionContext.Provider>
  );
}
