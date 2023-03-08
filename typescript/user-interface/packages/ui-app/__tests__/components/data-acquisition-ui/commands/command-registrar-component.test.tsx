import { MILLISECONDS_IN_SECOND, SECONDS_IN_MINUTES } from '@gms/common-util';
import { sohStatus } from '@gms/ui-state/__tests__/__data__/soh-status-data';
import { waitForComponentToPaint } from '@gms/ui-state/__tests__/test-util';
import React from 'react';

import { CommandPaletteContext } from '../../../../src/ts/components/common-ui/components/command-palette/command-palette-context';
// eslint-disable-next-line max-len
import { CommandRegistrarComponent } from '../../../../src/ts/components/data-acquisition-ui/commands/command-registrar-component';
import { commandPaletteContextData } from '../../../__data__/common-ui/command-palette-context-data';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

Date.now = jest.fn().mockReturnValue(() => MILLISECONDS_IN_SECOND * SECONDS_IN_MINUTES);

describe('Common Command Registrar Component', () => {
  const wrapper = Enzyme.mount(
    <CommandPaletteContext.Provider value={commandPaletteContextData}>
      <CommandRegistrarComponent
        acknowledgeStationsByName={jest.fn()}
        selectedStationIds={['ABC', 'TEST']}
        setSelectedStationIds={jest.fn()}
        sohStatus={sohStatus}
      />
    </CommandPaletteContext.Provider>
  );

  it('registers commands after component updates', async () => {
    await waitForComponentToPaint(wrapper);
    wrapper.update();
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(commandPaletteContextData.registerCommands).toHaveBeenCalledTimes(1);
  });
});
