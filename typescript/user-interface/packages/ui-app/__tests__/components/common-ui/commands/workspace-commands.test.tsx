import { getStore } from '@gms/ui-state';
import { renderHook } from '@testing-library/react-hooks';
import React from 'react';
import { Provider } from 'react-redux';

import { useWorkspaceCommands } from '../../../../src/ts/components/common-ui/commands/workspace-commands';
import { CommandType } from '../../../../src/ts/components/common-ui/components/command-palette/types';

describe('Workspace commands', () => {
  const setAuthStatus = jest.fn();
  const store = getStore();
  const result = renderHook(() => useWorkspaceCommands(setAuthStatus), {
    // eslint-disable-next-line react/display-name
    wrapper: (props: React.PropsWithChildren<unknown>) => (
      // eslint-disable-next-line react/destructuring-assignment
      <Provider store={store}>{props.children}</Provider>
    )
  });
  const workspaceCommands = result.result.current;
  it('match snapshot', () => {
    expect(workspaceCommands).toMatchSnapshot();
  });

  it('has a logout command', () => {
    const logoutCommand = workspaceCommands.find(c => c.commandType === CommandType.LOG_OUT);
    expect(logoutCommand).toBeDefined();
  });

  it('has a clear layout command', () => {
    const logoutCommand = workspaceCommands.find(c => c.commandType === CommandType.CLEAR_LAYOUT);
    expect(logoutCommand).toBeDefined();
  });

  it('has a show logs command', () => {
    const logoutCommand = workspaceCommands.find(c => c.commandType === CommandType.SHOW_LOGS);
    expect(logoutCommand).toBeDefined();
  });
});
