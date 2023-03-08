/* eslint-disable react/destructuring-assignment */
import { Checkbox } from '@blueprintjs/core';
import { Displays, UserProfileTypes } from '@gms/common-model';
import { IS_MODE_SOH, SUPPORTED_MODES } from '@gms/common-util';
import GoldenLayout from '@gms/golden-layout';
import { ModalPrompt, SaveOpenDialog } from '@gms/ui-core-components';
import type { SaveableItem } from '@gms/ui-core-components/lib/components/dialog/types';
import type { SetLayoutArgs } from '@gms/ui-state';
import { GLDisplayState } from '@gms/ui-state';
import { getElectron, isCypress, isElectron, UILogger } from '@gms/ui-util';
import elementResizeEvent from 'element-resize-event';
import debounce from 'lodash/debounce';
import isEqual from 'lodash/isEqual';
import uniqBy from 'lodash/uniqBy';
import React from 'react';
import { toast } from 'react-toastify';

import type { Command } from '~components/common-ui/components/command-palette/types';
import { CommandScope, CommandType } from '~components/common-ui/components/command-palette/types';

import { DeprecatedToolbar } from '../toolbar';
import { createCloseDisplayCommands, createOpenDisplayCommands } from './display-commands';
import {
  clearLayout,
  getClosedDisplays,
  getOpenDisplays,
  showLogPopup,
  uniqueLayouts
} from './golden-layout-util';
import type {
  GLComponentValue,
  GLKeyValue,
  GoldenLayoutContextData,
  GoldenLayoutPanelProps,
  GoldenLayoutPanelState
} from './types';
import { GoldenLayoutContext, isGLComponentMap, isGLKeyValue } from './types';

const logger = UILogger.create('GMS_LOG_GOLDEN_LAYOUT', process.env.GMS_LOG_GOLDEN_LAYOUT);

// Electron instance; undefined if not running in electron
const electron = getElectron();

export class GoldenLayoutPanel extends React.PureComponent<
  React.PropsWithChildren<GoldenLayoutPanelProps>,
  GoldenLayoutPanelState
> {
  /** The Golden Layout context - provides the layout configuration */
  // eslint-disable-next-line react/static-property-placement
  public static contextType: React.Context<GoldenLayoutContextData> = GoldenLayoutContext;

  /** The Golden Layout context - provides the layout configuration */
  // eslint-disable-next-line react/static-property-placement
  public declare context: React.ContextType<typeof GoldenLayoutContext>;

  /**
   * Handle to the dom element where we will render the golden-layout workspace
   */
  private glContainerRef: HTMLDivElement;

  private gl: GoldenLayout;

  private closedDisplays: string[] = [];

  private openDisplays: string[] = [];

  public constructor(props: GoldenLayoutPanelProps) {
    super(props);
    this.state = {
      isAboutDialogOpen: false,
      isSaveWorkspaceAsDialogOpen: false,
      isSaveLayoutChangesOpen: false,
      isSaveWorkspaceOnChangeDialogOpen: false,
      isSaveAsDefaultChecked: false,
      saveAsName: '',
      selectedWorkspaceId: this.props.openLayoutName,
      userLayoutToOpen: undefined
    };
  }

  /**
   * On mount, initialize the golden-layout workspace
   */
  public componentDidMount(): void {
    this.configureGoldenLayout();
    this.addGlCommandsToCommandPalette();
    if (this.props.openLayoutName === null) {
      this.props.setOpenLayoutName(this.props.userProfile.defaultLayoutName);
    }
  }

  public componentDidUpdate(prevProps: GoldenLayoutPanelProps): void {
    if (prevProps.openLayoutName === null && this.props.userProfile?.defaultLayoutName) {
      // if we got the user profile for the first time
      this.props.setOpenLayoutName(this.props.userProfile.defaultLayoutName);
    } else if (prevProps.openLayoutName !== this.props.openLayoutName) {
      // if layout name has changed
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        saveAsName: this.props.openLayoutName,
        selectedWorkspaceId: this.props.openLayoutName
      });
    }

    const currentlyClosedDisplays = getClosedDisplays(this.gl, this.context.glComponents);
    const currentlyOpenDisplays = getOpenDisplays(this.gl);
    // if the list of open displays has changed, or if the list of closed ones has
    if (
      !isEqual(currentlyClosedDisplays, this.closedDisplays) ||
      !isEqual(currentlyOpenDisplays, this.openDisplays)
    ) {
      this.closedDisplays = currentlyClosedDisplays;
      this.openDisplays = currentlyOpenDisplays;
      if (this.props.registerCommands) {
        this.props.registerCommands(
          [
            ...createOpenDisplayCommands(this.gl, this.context.config, this.context.glComponents),
            ...createCloseDisplayCommands(this.gl, this.context.config, this.context.glComponents)
          ],
          CommandScope.DISPLAY_MANAGEMENT
        );
      }
    }
  }

  public componentWillUnmount(): void {
    this.destroyGl();
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    this.context.gl = this.gl;
    const { defaultLayoutName } = this.props.userProfile;
    const saveDialog = (
      <SaveOpenDialog
        title="Save Workspace As"
        actionText="Save"
        actionTooltipText="Save this workspace layout"
        itemList={this.getSaveableItemList()}
        selectedId={this.state.selectedWorkspaceId}
        defaultId={defaultLayoutName}
        openedItemId={this.props.openLayoutName}
        titleOfItemList="Existing Workspace: "
        actionCallback={async () => {
          await this.handleAffirmativeAction();
        }}
        cancelCallback={() => {
          this.toggleSaveWorkspaceAsDialog();
        }}
        selectEntryCallback={(id: string) => {
          this.setState({
            selectedWorkspaceId: id,
            saveAsName: id
          });
        }}
        isDialogOpen={this.state.isSaveWorkspaceAsDialogOpen}
      >
        {/* eslint-disable-next-line jsx-a11y/label-has-associated-control */}
        <label>
          Name:
          <input
            name="save-name"
            data-cy="save-workspace-name-field"
            onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
              this.setState({
                saveAsName: event.currentTarget.value,
                selectedWorkspaceId: undefined
              });
            }}
            onKeyPress={async (event: React.KeyboardEvent<HTMLInputElement>) => {
              switch (event.key) {
                case 'Enter':
                  await this.handleAffirmativeAction();
                  break;
                case 'Escape':
                  event.stopPropagation();
                  break;
                default:
              }
            }}
            placeholder="Enter name..."
            value={this.state.saveAsName}
          />
        </label>
        <Checkbox
          name="save-as-default-checkbox"
          data-cy="save-as-default-checkbox"
          label="Save as default"
          checked={this.state.isSaveAsDefaultChecked}
          onChange={() => {
            this.setState(prevState => ({
              isSaveAsDefaultChecked: !prevState.isSaveAsDefaultChecked
            }));
          }}
        />
        {this.props.children}
      </SaveOpenDialog>
    );

    return (
      <div
        ref={ref => {
          this.context.glRef = ref;
        }}
        className="workspace-container"
      >
        <ModalPrompt
          optionalButton
          actionText="Save Changes and Log Out"
          actionCallback={() => {
            this.saveLayoutChangesOnLogout();
          }}
          optionalText="Discard Changes and Log Out"
          optionalCallback={() => {
            this.discardLayoutChangesOnLogout();
          }}
          cancelText="Cancel"
          cancelButtonCallback={() => this.setState({ isSaveLayoutChangesOpen: false })}
          onCloseCallback={() => this.setState({ isSaveLayoutChangesOpen: false })}
          isOpen={this.state.isSaveLayoutChangesOpen}
          title={`Save Changes to "${this.props.openLayoutName}"?`}
          actionTooltipText="Saves your changes to the open workspace"
          optionalTooltipText="Discards your changes to the open workspace"
          cancelTooltipText="Cancel and do not log out"
        />
        <ModalPrompt
          optionalButton
          actionText={`Save Changes and Open "${
            this.state.userLayoutToOpen ? this.state.userLayoutToOpen.name : ''
          }"`}
          actionCallback={async () => {
            const layout = this.getOpenLayout();
            const args: SetLayoutArgs = this.createSetLayoutVariables(layout);
            await this.props.setLayout(args).then(this.openWorkspaceAndSaveState);
          }}
          optionalText={`Discard Changes and Open "
            ${this.state.userLayoutToOpen ? this.state.userLayoutToOpen.name : ''}"`}
          optionalCallback={this.openWorkspaceAndSaveState}
          cancelText="Cancel"
          cancelButtonCallback={() =>
            this.setState({
              isSaveWorkspaceOnChangeDialogOpen: false,
              userLayoutToOpen: undefined
            })
          }
          onCloseCallback={() =>
            this.setState({
              isSaveWorkspaceOnChangeDialogOpen: false,
              userLayoutToOpen: undefined
            })
          }
          isOpen={this.state.isSaveWorkspaceOnChangeDialogOpen}
          title={`Save Changes to "${this.props.openLayoutName}"?`}
          actionTooltipText="Saves your changes to the open workspace"
          optionalTooltipText="Discards your changes to the open workspace"
          cancelTooltipText="Do not save or open new workspace"
        />
        <DeprecatedToolbar
          components={this.context.glComponents}
          logo={this.props.logo}
          userName={this.props.userName}
          isAboutDialogOpen={this.state.isAboutDialogOpen}
          isSaveWorkspaceAsDialogOpen={this.state.isSaveWorkspaceAsDialogOpen}
          openLayoutName={this.props.openLayoutName}
          versionInfo={this.props.versionInfo}
          getOpenDisplays={() => getOpenDisplays(this.gl)}
          userProfile={this.props.userProfile}
          saveDialog={saveDialog}
          setLayout={this.props.setLayout}
          setOpenLayoutName={(name: string) => {
            this.props.setOpenLayoutName(name);
          }}
          clearLayout={clearLayout}
          logout={this.handleLogout}
          openDisplay={displayKey => {
            this.openDisplay(displayKey);
          }}
          openWorkspace={layOut => {
            this.handleOpenWorkspace(layOut);
          }}
          showLogPopup={showLogPopup}
          showAboutDialog={() => {
            this.toggleAboutDialog();
          }}
          toggleSaveWorkspaceAsDialog={() => this.toggleSaveWorkspaceAsDialog()}
        />
        <div
          className="workspace"
          ref={ref => {
            this.glContainerRef = ref;
          }}
        />
      </div>
    );
  }

  /**
   * Registers GL commands, exposing the actions
   * that require the golden layout so that they may be called
   * in the Command Palette
   */
  // eslint-disable-next-line react/sort-comp
  private readonly addGlCommandsToCommandPalette = () => {
    const showAboutDialog: Command = {
      commandType: CommandType.SHOW_ABOUT,
      searchTags: ['about', 'info', 'version'],
      action: this.toggleAboutDialog
    };
    const toggleSaveWorkspace: Command = {
      commandType: CommandType.SAVE_WORKSPACE,
      searchTags: ['save', 'layout', 'workspace'],
      action: this.toggleSaveWorkspaceAsDialog
    };
    const allLayouts = this.getAllWorkspaceLayouts();
    const loadWorkspaceCommands = allLayouts
      ? allLayouts.map(layout => ({
          commandType: CommandType.LOAD_WORKSPACE,
          searchTags: ['load', 'open', 'layout', 'workspace'],
          displayText: `${CommandType.LOAD_WORKSPACE} ${layout.name}`,
          action: () => {
            this.openWorkspace(layout);
          }
        }))
      : [];

    this.props.registerCommands(
      [showAboutDialog, toggleSaveWorkspace, ...loadWorkspaceCommands],
      CommandScope.GOLDEN_LAYOUT
    );
  };

  /**
   * Opens workspace and updates the state
   */
  private readonly openWorkspaceAndSaveState = () => {
    this.openWorkspace(this.state.userLayoutToOpen);
    this.setState({
      userLayoutToOpen: undefined,
      isSaveWorkspaceOnChangeDialogOpen: false
    });
  };

  /**
   * Open a selected workspace layout, adding the layout elements to the GoldenLayout
   *
   * @param userLayout an encoded golden layout
   */
  private readonly handleOpenWorkspace = (userLayout: UserProfileTypes.UserLayout) => {
    if (this.isLayoutChanged()) {
      this.setState({
        isSaveWorkspaceOnChangeDialogOpen: true,
        userLayoutToOpen: userLayout
      });
    } else {
      this.openWorkspace(userLayout);
    }
  };

  private openWorkspace(userLayout: UserProfileTypes.UserLayout) {
    const newLayout: GoldenLayout.Config = this.decodeGoldenLayoutConfig(
      userLayout.layoutConfiguration
    );
    this.gl.root.contentItems.forEach(item => {
      item.remove();
    });
    newLayout.content.forEach(item => {
      this.gl.root.addChild(item);
    });
    this.props.setOpenLayoutName(userLayout.name);
  }

  // Get a list of layouts that are uniq by name - this is a limitation due to the current
  // way layouts are stored in the database. It causes duplicates in the UI.
  private readonly getSaveableItemList = (): SaveableItem[] =>
    uniqBy(
      this.props.userProfile.workspaceLayouts.filter(layout =>
        layout.supportedUserInterfaceModes.includes(this.context.supportedUserInterfaceMode)
      ),
      wl => wl.name
    )
      .map(l => ({ id: l.name, title: l.name }))
      .sort((a, b) => a.title.localeCompare(b.title));

  /**
   * Encodes the GoldenLayout as a string for saving and the like
   */
  private getLayoutAsString() {
    return this.gl ? encodeURI(JSON.stringify(this.gl.toConfig())) : undefined;
  }

  // eslint-disable-next-line class-methods-use-this
  private getMode() {
    return IS_MODE_SOH
      ? UserProfileTypes.DefaultLayoutNames.SOH_LAYOUT
      : // TODO IAN define a default layout name for IAN / ALL
        UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT;
  }

  /**
   * Alert the user that the layout provided is invalid.
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly warnAboutInvalidLayout = (layoutName: string) => {
    logger.warn(
      `Something failed parsing default layout ${layoutName}. Using fallback workspace from current configuration.`
    );
    toast.warn('Cannot open layout—invalid configuration.');
  };

  /**
   * Takes in a serialized golden layout configuration string, and if it is valid,
   * returns the parsed string. Otherwise warns the user and loads the default workspace.
   *
   * @param layoutConfiguration a serialized golden layout configuration string
   * @returns the golden layout config object. Defaults to the default workspace defined
   * in golden-layout-config
   */
  private readonly decodeGoldenLayoutConfig = (
    layoutConfiguration: string
  ): GoldenLayout.Config => {
    let result;
    try {
      result = JSON.parse(decodeURI(layoutConfiguration));
    } catch {
      this.warnAboutInvalidLayout(layoutConfiguration);
      result = this.context.config.workspace;
    }
    if (!result?.settings || !result?.content) {
      this.warnAboutInvalidLayout(layoutConfiguration);
      result = this.context.config.workspace;
    }
    return result;
  };

  /**
   * configure & initialize the golden-layout workspace
   */
  private configureGoldenLayout() {
    if (this.gl) {
      this.destroyGl();
    }

    let defaultConfig: GoldenLayout.Config = this.decodeGoldenLayoutConfig(
      this.props.userProfile.workspaceLayouts.find(
        wl => wl.name === this.props.userProfile.defaultLayoutName
      ).layoutConfiguration
    );

    // If the layout settings was not returned by decodeURI then use the default workspace
    // This might happen if the layoutConfiguration string is corrupted
    if (defaultConfig?.settings === undefined) {
      defaultConfig = this.context.config.workspace;
      logger.warn(
        `Something failed parsing default layout ${this.props.userProfile.defaultLayoutName}. Using fallback workspace from current configuration.`
      );
    }

    defaultConfig.settings.showPopoutIcon = isElectron() && !isCypress();
    logger.info(`Enabling popout window icon for electron: ${isElectron() && !isCypress()}`);

    try {
      const savedConfigStr = localStorage.getItem('gms-analyst-ui-layout');
      if (savedConfigStr) {
        try {
          const savedConfig: GoldenLayout.Config = JSON.parse(savedConfigStr);
          savedConfig.settings.showPopoutIcon = isElectron() && !isCypress();
          this.gl = new GoldenLayout(savedConfig, this.glContainerRef);
          // if an update has changed the names of components, for example, need to start at default again
        } catch (e) {
          this.gl = new GoldenLayout(defaultConfig, this.glContainerRef);
        }
      } else {
        this.gl = new GoldenLayout(defaultConfig, this.glContainerRef);
      }
    } catch (e) {
      this.gl = new GoldenLayout(this.context.config.workspace, this.glContainerRef);
    }

    this.gl.on('itemCreated', e => {
      if (e.config.component && Displays.isValidDisplayName(e.config.component)) {
        // update redux store to indicate that a display has been opened.
        this.props.setGlDisplayState(e.config.component, GLDisplayState.OPEN);
      }
    });
    this.gl.on('itemDestroyed', e => {
      if (e.config.component && Displays.isValidDisplayName(e.config.component)) {
        // update redux store to indicate that a display has been closed.
        this.props.setGlDisplayState(e.config.component, GLDisplayState.CLOSED);
      }
    });

    const resizeDebounceMillis = 100;

    elementResizeEvent(
      this.glContainerRef,
      debounce(() => {
        this.gl.updateSize();
      }, resizeDebounceMillis)
    );

    this.registerComponentsAndGoldenLayout();

    this.gl.on('stateChanged', () => {
      if (electron !== undefined && electron.ipcRenderer !== undefined) {
        electron.ipcRenderer.send('state-changed');
      }
      if (this.gl.isInitialised) {
        const state = JSON.stringify(this.gl.toConfig());
        localStorage.setItem('gms-analyst-ui-layout', state);
      }
    });

    this.gl.on('itemDestroyed', () => {
      const config: GoldenLayout.Config = this.gl.toConfig();

      // FIX for Golden Layout when closing the last item when maximised
      // check if the last item is being destroyed / closed
      if (config.content?.length === 1 && config.content[0].content?.length === 1) {
        // if the last item was destroyed; then delete any golden-layout place holders for minimize/maximize
        // there is no reason to hold on to this place holder since all items have been destroyed
        // this fixes a bug where minimize/maximise fails (not positioned correctly) for any new items that are opened
        [...document.getElementsByClassName('lm_maximise_place')].forEach((item: HTMLElement) => {
          // delete the golden layout place holder item
          item.parentElement.removeChild(item);
        });
      }
    });
  }

  /**
   * Registers an individual component with golden layout
   *
   * @param name the unique name of the component
   * @param component the component
   */
  private readonly registerComponent = (
    name: string,
    component: React.ComponentClass | React.FunctionComponent
  ) => {
    this.gl.registerComponent(name, component);
  };

  /**
   * Registers golden layout components and handles error cases
   */
  private readonly registerComponentsAndGoldenLayout = () => {
    try {
      this.registerComponents();
      this.gl.init(); // throws a error if windows can't be popped out
      this.gl.updateSize();
    } catch (e) {
      logger.warn(
        'Golden Layout could not initialize. Saved config possibly out of date - Attempting reset to default'
      );
      this.gl = new GoldenLayout(this.context.config.workspace, this.glContainerRef);
      this.registerComponents();
      this.gl.init(); // throws a error if windows can't be popped out
      this.gl.updateSize();
    }
  };

  /** Registers all of the golden layout components */
  private readonly registerComponents = () => {
    this.context.glComponents.forEach((item: GLComponentValue) => {
      if (isGLComponentMap(item)) {
        [...item.values()].forEach((c: GLKeyValue) => {
          this.registerComponent(c.id.component, c.value);
        });
      } else if (isGLKeyValue(item)) {
        this.registerComponent(item.id.component, item.value);
      }
    });
  };

  private readonly destroyGl = () => {
    this.gl.destroy();
  };

  /**
   * Opens display based on menu selection checks if a display is maximized and add
   * display to that stack if so, if adding a display when maximized to first level will
   * break golden layout, so need to be surgical when adding displays while maximized
   *
   * @param componentKey key into the component map for a golden layout configuration
   */
  private readonly openDisplay = (componentKey: string) => {
    if (this.gl.root.contentItems[0]) {
      let foundMaximized = false;
      this.gl.root.contentItems[0].contentItems.forEach(content => {
        // Rows and columns have their own content items for each of the check if one is maximized
        if (content.isRow || content.isColumn) {
          content.contentItems.forEach(rowContent => {
            if (rowContent.isMaximised) {
              foundMaximized = true;
              rowContent.addChild(this.context.config.components[componentKey]);
            }
          });
        }
        if (content.isMaximised) {
          foundMaximized = true;
          content.addChild(this.context.config.components[componentKey]);
        }
      });
      // Opening display with nothing maximized so adding to base content items array, i.e new tab
      if (!foundMaximized) {
        this.gl.root.contentItems[0].addChild(this.context.config.components[componentKey]);
      }
    } else {
      this.gl.root.addChild(this.context.config.components[componentKey]);
    }
  };

  /**
   * Shows or hides the SaveWorkspaceAs dialog
   */
  private readonly toggleSaveWorkspaceAsDialog = () => {
    this.setState(prevState => ({
      isSaveWorkspaceAsDialogOpen: !prevState.isSaveWorkspaceAsDialogOpen
    }));
  };

  /**
   * Shows or hides the AboutDialog
   */
  private readonly toggleAboutDialog = () => {
    this.setState(prevState => ({
      isAboutDialogOpen: !prevState.isAboutDialogOpen
    }));
  };

  /**
   * Saves the current configuration then logs out
   */
  private readonly saveLayoutChangesOnLogout = () => {
    const newLayout = this.getOpenLayout();
    const args = this.createSetLayoutVariables(newLayout);
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.props.setLayout(args).then(() => {
      this.finishLogout();
    });
  };

  private readonly handleLogout = () => {
    if (this.isLayoutChanged()) {
      this.setState({ isSaveLayoutChangesOpen: true });
    } else {
      this.finishLogout();
    }
  };

  /**
   * Closes the save layout dialog and finishes logout
   */
  private discardLayoutChangesOnLogout() {
    this.setState({ isSaveLayoutChangesOpen: false });
    this.finishLogout();
  }

  /**
   * Clears local storage and logs out
   */
  private finishLogout() {
    localStorage.removeItem('gms-analyst-ui-layout');
    this.props.logout();
  }

  /**
   * Returns true if the open layout doesn't match the golden layout config
   */
  private isLayoutChanged(): boolean {
    const currentlyOpenLayout = this.props.userProfile.workspaceLayouts.find(
      wl => wl.name === this.props.openLayoutName
    );
    return (
      this.gl &&
      encodeURI(JSON.stringify(this.gl.toConfig())) !== currentlyOpenLayout.layoutConfiguration
    );
  }

  /**
   * Handles the affirmative action for the save dialog.
   */
  private readonly handleAffirmativeAction = async () => {
    if (this.state.saveAsName === '') {
      toast.info('Please give a name for the new layout');
    } else {
      await this.submitSaveAs();
    }
  };

  /**
   * Hides the save dialog and saves the layout
   */
  private readonly submitSaveAs = async () => {
    this.toggleSaveWorkspaceAsDialog();
    const layout: UserProfileTypes.UserLayout = {
      name: this.state.saveAsName,
      supportedUserInterfaceModes: [this.context.supportedUserInterfaceMode],
      layoutConfiguration: this.getLayoutAsString()
    };
    const args: SetLayoutArgs = this.createSetLayoutVariables(layout);
    await this.props.setLayout(args);
    this.addGlCommandsToCommandPalette();
    this.props.setOpenLayoutName(this.state.saveAsName);
  };

  /**
   * Creates the mutation args for the setLayout mutation.
   *
   * @param workspaceLayoutInput the layout configuration and metadata
   * that is being set.
   */
  private readonly createSetLayoutVariables = (
    workspaceLayoutInput: UserProfileTypes.UserLayout = undefined
  ): SetLayoutArgs => ({
    saveAsDefaultLayoutOfType: this.state.isSaveAsDefaultChecked ? this.getMode() : undefined,
    defaultLayoutName: this.getMode(),
    workspaceLayoutInput
  });

  /**
   * Returns the current gl config as a layout
   */
  private readonly getOpenLayout = (): UserProfileTypes.UserLayout => {
    const openLayout = this.props.openLayoutName
      ? this.props.openLayoutName
      : this.props.userProfile.defaultLayoutName;
    if (!this.gl) {
      logger.error('No Golden Layout found while saving layout changes');
    }
    const newLayout: UserProfileTypes.UserLayout = {
      name: openLayout,
      supportedUserInterfaceModes: SUPPORTED_MODES,
      layoutConfiguration: encodeURI(JSON.stringify(this.gl ? this.gl.toConfig() : {}))
    };
    return newLayout;
  };

  /**
   * @returns a list of unique workspace layouts supported
   * by the current supportedUserInterfaceMode
   */
  private readonly getAllWorkspaceLayouts = () =>
    uniqueLayouts(
      this.props.userProfile.workspaceLayouts,
      '',
      this.context.supportedUserInterfaceMode
    );
}
