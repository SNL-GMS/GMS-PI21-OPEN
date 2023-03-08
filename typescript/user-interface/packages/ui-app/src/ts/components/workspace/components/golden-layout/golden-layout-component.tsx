import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import { VERSION_INFO } from '@gms/common-util';
import type { GLDisplayState } from '@gms/ui-state';
import {
  AppActions,
  resetApiState,
  setGlDisplayState,
  useAppDispatch,
  useGetUserProfileQuery,
  useSetLayout
} from '@gms/ui-state';
import React from 'react';

import { authenticator } from '~app/authentication';
import { CommandPaletteContext } from '~components/common-ui/components/command-palette/command-palette-context';
import type { Command, CommandScope } from '~components/common-ui/components/command-palette/types';

import { GoldenLayoutPanel } from './golden-layout-panel';
import type { GoldenLayoutComponentProps } from './types';

export function GoldenLayoutComponent({
  logo,
  openLayoutName,
  setOpenLayoutName,
  userName,
  setAppAuthenticationStatus
}: GoldenLayoutComponentProps) {
  const context = React.useContext(CommandPaletteContext);

  const setLayout = useSetLayout();
  const userProfileQuery = useGetUserProfileQuery();
  const dispatch = useAppDispatch();
  const registerCommands = React.useCallback(
    (commandsToRegister: Command[], scope: CommandScope) =>
      context?.registerCommands && context.registerCommands(commandsToRegister, scope),
    [context]
  );

  const logout = React.useCallback(() => {
    dispatch(AppActions.reset());
    resetApiState(dispatch);

    authenticator.logout(setAppAuthenticationStatus);
  }, [dispatch, setAppAuthenticationStatus]);

  const setGlDisplayStateCallback = React.useCallback(
    (displayName: string, displayState: GLDisplayState) => {
      dispatch(setGlDisplayState(displayName, displayState));
    },
    [dispatch]
  );

  const maybeUserProfile = userProfileQuery?.data ?? undefined;

  return maybeUserProfile ? (
    <GoldenLayoutPanel
      logo={logo}
      openLayoutName={openLayoutName}
      setOpenLayoutName={setOpenLayoutName}
      setGlDisplayState={setGlDisplayStateCallback}
      userName={userName}
      userProfile={maybeUserProfile}
      versionInfo={VERSION_INFO}
      setLayout={setLayout}
      registerCommands={registerCommands}
      logout={logout}
    />
  ) : (
    <NonIdealState
      action={<Spinner intent={Intent.PRIMARY} />}
      title="Loading Default Layout"
      description="Retrieving default layout for user..."
    />
  );
}
