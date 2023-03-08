import { MILLISECONDS_IN_SECOND, SECONDS_IN_MINUTES } from '@gms/common-util';
import { getStore } from '@gms/ui-state';
import { waitForComponentToPaint } from '@gms/ui-state/__tests__/test-util';
import React from 'react';
import { Provider } from 'react-redux';

// eslint-disable-next-line max-len
import { CommandRegistrarComponent } from '../../../../src/ts/components/common-ui/commands/command-registrar-component';
import { CommandPaletteContext } from '../../../../src/ts/components/common-ui/components/command-palette/command-palette-context';
import { commandPaletteContextData } from '../../../__data__/common-ui/command-palette-context-data';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

Date.now = jest.fn().mockReturnValue(() => MILLISECONDS_IN_SECOND * SECONDS_IN_MINUTES);

describe('Common Command Registrar Component', () => {
  const wrapper = Enzyme.mount(
    <Provider store={getStore()}>
      <CommandPaletteContext.Provider value={commandPaletteContextData}>
        <CommandRegistrarComponent setAppAuthenticationStatus={jest.fn()} />
      </CommandPaletteContext.Provider>
    </Provider>
  );

  it('registers commands after component updates', async () => {
    await waitForComponentToPaint(wrapper);
    wrapper.update();
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(commandPaletteContextData.registerCommands).toHaveBeenCalledTimes(1);
  });
});
