/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { Button, Classes, Dialog, Popover } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { useKeyboardShortcutsDisplayVisibility, useUiTheme } from '@gms/ui-state';
import { getOS, OSTypes, UILogger } from '@gms/ui-util';
import React from 'react';

import { KeyMark } from '~common-ui/components/keyboard-shortcuts/key-mark';

import { KeyboardShortcuts } from '../../../common-ui/components/keyboard-shortcuts/keyboard-shortcuts';
import { KeyContext } from '../../types';
import { AboutMenu, AppMenu } from '../menus';
import type { DeprecatedToolbarProps } from './types';

const logger = UILogger.create('GMS_LOG_TOOLBAR', process.env.GMS_LOG_TOOLBAR);

/**
 * * triggerAnimation
 * toggles the animation class on the app logo so that it flashes.
 *
 * @param AnimationTarget the reference object from the useRef hook that tracks the animation target
 */
export const triggerAnimation = (
  AnimationTarget: React.MutableRefObject<HTMLSpanElement>
): void => {
  if (AnimationTarget.current) {
    // eslint-disable-next-line no-param-reassign
    AnimationTarget.current.className = 'workspace-logo__label';
    // Placing a call to get to object's current offset width between adding/removing classes
    // forces the browser to render the "animation-less" version of the component
    // This is the current recommended solution on stack overflow
    // This shouldn't impact performance - as css animations already cause reflows
    const assignmentToForceReflow = AnimationTarget.current.offsetWidth;
    logger.debug(`${assignmentToForceReflow} required output to ignore sonar lint rule`);
    // eslint-disable-next-line no-param-reassign
    AnimationTarget.current.className = 'workspace-logo__label keypress-signifier';
  }
};

/**
 * * Toolbar
 * Build the Toolbar, including logo and app-level menu
 */
// eslint-disable-next-line react/function-component-definition
export const DeprecatedToolbar: React.FunctionComponent<DeprecatedToolbarProps> = props => {
  // The width and height of the logo
  const logoSize = {
    width: 35,
    height: 33
  };
  const AnimationTarget = React.useRef<HTMLSpanElement>(undefined);

  const keyContext = React.useContext(KeyContext);
  const { shouldTriggerAnimation } = keyContext;
  React.useEffect(() => triggerAnimation(AnimationTarget), [shouldTriggerAnimation]);
  const [uiTheme] = useUiTheme();

  // eslint-disable-next-line @typescript-eslint/unbound-method
  const {
    clearLayout,
    getOpenDisplays,
    logout,
    openDisplay,
    openWorkspace,
    showAboutDialog,
    showLogPopup,
    toggleSaveWorkspaceAsDialog
  } = props;

  const {
    isKeyboardShortcutsDialogOpen,
    closeKeyboardShortcuts
  } = useKeyboardShortcutsDisplayVisibility();

  return (
    <nav className={`${Classes.NAVBAR} workspace-navbar`}>
      <div className="workspace-navbar__image-label">
        <img
          src={props.logo}
          alt="GMS Logo"
          height={logoSize.height}
          width={logoSize.width}
          className="workspace-logo"
          data-cy="workspace-logo"
        />
        <span
          className="workspace-logo__label"
          ref={AnimationTarget}
          onAnimationEnd={e => {
            e.currentTarget.className = 'workspace-logo__label';
          }}
        >
          GMS
        </span>
        {props.saveDialog}
      </div>
      <Popover
        content={
          <AppMenu
            components={props.components}
            clearLayout={clearLayout}
            openLayoutName={props.openLayoutName}
            getOpenDisplays={getOpenDisplays}
            logo={props.logo}
            logout={logout}
            openDisplay={openDisplay}
            openWorkspace={openWorkspace}
            saveWorkspaceAs={toggleSaveWorkspaceAsDialog}
            showAboutDialog={showAboutDialog}
            showLogs={showLogPopup}
            userProfile={props.userProfile}
          />
        }
      >
        <Button
          data-cy="username"
          className="app-menu-button"
          rightIcon={IconNames.USER}
          text={props.userName}
          minimal
        />
      </Popover>
      <Dialog
        isOpen={props.isAboutDialogOpen}
        onClose={showAboutDialog}
        className={uiTheme.isDarkMode ? Classes.DARK : 'gms-light-mode'}
        title="About"
      >
        <AboutMenu
          commitSHA={props.versionInfo.commitSHA}
          versionNumber={props.versionInfo.versionNumber}
          logo={props.logo}
        />
      </Dialog>
      <Dialog
        isOpen={isKeyboardShortcutsDialogOpen}
        onClose={closeKeyboardShortcuts}
        className={`${
          uiTheme.isDarkMode ? Classes.DARK : 'gms-light-mode'
        } keyboard-shortcuts__dialog`}
        title={
          <span>
            Keyboard Shortcuts <KeyMark>{getOS() === OSTypes.MAC ? '⌘' : 'Ctrl'}</KeyMark>
            <KeyMark>/</KeyMark>
          </span>
        }
      >
        <KeyboardShortcuts />
      </Dialog>
    </nav>
  );
};
