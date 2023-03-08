import { CommonKeyAction } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';

import {
  handleHotkey,
  InteractionConsumer
} from '../../../../../src/ts/components/data-acquisition-ui/interactions/interaction-consumer/interaction-consumer-component';
import type { InteractionConsumerProps } from '../../../../../src/ts/components/data-acquisition-ui/interactions/interaction-consumer/types';
import { InteractionProvider } from '../../../../../src/ts/components/data-acquisition-ui/interactions/interaction-provider/interaction-provider-component';
import type { InteractionProviderProps } from '../../../../../src/ts/components/data-acquisition-ui/interactions/interaction-provider/types';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(document as any).getElementById = jest.fn(() => ({
  dataset: { hotkeyListenerAttached: true }
}));

describe('Interaction consumer component', () => {
  const interactionConsumerProps: InteractionConsumerProps = {
    keyPressActionQueue: {},
    setKeyPressActionQueue: jest.fn()
  };

  const interactionProviderProps: InteractionProviderProps = {
    commandPaletteIsVisible: true,
    setCommandPaletteVisibility: jest.fn()
  };

  interactionConsumerProps.keyPressActionQueue[CommonKeyAction.OPEN_COMMAND_PALETTE] = 0;
  const { container } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <InteractionProvider {...interactionProviderProps}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <InteractionConsumer {...interactionConsumerProps} />{' '}
    </InteractionProvider>
  );

  const wrapper = Enzyme.shallow(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <InteractionProvider {...interactionProviderProps}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <InteractionConsumer {...interactionConsumerProps} />{' '}
    </InteractionProvider>
  );
  it('is defined', () => {
    expect(container).toBeDefined();
  });
  it('matches snapshot with hot key listener', () => {
    expect(container).toMatchSnapshot();
  });

  it('matches snapshot with no hot key listener', () => {
    // set up window alert and open so we don't see errors
    (document as any).getElementById = jest.fn(() => ({
      dataset: { hotkeyListenerAttached: false }
    }));

    const { container: noHotKeyContainer } = render(
      // eslint-disable-next-line react/jsx-props-no-spreading
      <InteractionProvider {...interactionProviderProps}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <InteractionConsumer {...interactionConsumerProps} />{' '}
      </InteractionProvider>
    );

    expect(noHotKeyContainer).toMatchSnapshot();
  });

  it('handle hotkey', () => {
    const event: any = {
      repeat: false,
      ctrlKey: true,
      shiftKey: true,
      code: 'KeyP',
      stopPropagation: jest.fn(),
      preventDefault: jest.fn()
    };
    handleHotkey(interactionConsumerProps)(event);
    expect(wrapper).toBeDefined();
  });
});
