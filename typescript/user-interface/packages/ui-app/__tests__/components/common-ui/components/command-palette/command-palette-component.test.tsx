import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import { CommandPaletteComponent } from '../../../../../src/ts/components/common-ui/components/command-palette/command-palette-component';
import type { CommandPaletteComponentProps } from '../../../../../src/ts/components/common-ui/components/command-palette/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

describe('Command Palette Component', () => {
  it('should be defined', () => {
    expect(CommandPaletteComponent).toBeDefined();
  });

  const props: CommandPaletteComponentProps = {
    commandPaletteIsVisible: true,
    keyPressActionQueue: undefined,
    setCommandPaletteVisibility: jest.fn(),
    setKeyPressActionQueue: jest.fn()
  };

  it('matches a snapshot', () => {
    const { container } = render(
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <CommandPaletteComponent {...props} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
