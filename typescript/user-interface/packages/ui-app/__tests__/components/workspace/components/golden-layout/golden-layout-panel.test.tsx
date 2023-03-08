import { UserMode } from '@gms/common-model/lib/user-profile/types';
import React from 'react';

import { GoldenLayoutPanel } from '../../../../../src/ts/components/workspace/components/golden-layout/golden-layout-panel';
import type { GoldenLayoutPanelProps } from '../../../../../src/ts/components/workspace/components/golden-layout/types';
// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

jest.mock('@gms/golden-layout');

const props: GoldenLayoutPanelProps = {
  logo: undefined,
  userName: 'test',
  openLayoutName: 'test',
  versionInfo: {
    commitSHA: 'test',
    versionNumber: '1'
  },
  setGlDisplayState: jest.fn(),
  userProfile: {
    defaultAnalystLayoutName: 'test',
    audibleNotifications: undefined,
    defaultSohLayoutName: 'test',
    userId: 'test',
    currentTheme: 'GMS Dark Theme',
    workspaceLayouts: [
      {
        name: 'test',
        supportedUserInterfaceModes: [UserMode.SOH, UserMode.IAN],
        layoutConfiguration: JSON.stringify({
          data: 'test',
          settings: { showPopoutIcon: undefined }
        })
      }
    ],
    defaultLayoutName: 'test'
  },
  // eslint-disable-next-line no-promise-executor-return
  setLayout: jest.fn(async () => new Promise(resolve => resolve())),
  setOpenLayoutName: jest.fn(),
  registerCommands: jest.fn(),
  logout: jest.fn()
};

describe('Golden layout class Panel', () => {
  const goldenLayoutPanel: any = new GoldenLayoutPanel(props);
  goldenLayoutPanel.context = {
    supportedUserInterfaceMode: [UserMode.SOH, UserMode.IAN],
    config: {
      workspace: undefined,
      components: []
    },
    glComponents: []
  };
  it('should be defined', () => {
    expect(goldenLayoutPanel).toBeDefined();
  });
  it('can handle getOpenLayout', () => {
    const expectedResults = {
      layoutConfiguration: '%7B%7D',
      name: 'test',
      supportedUserInterfaceModes: ['LEGACY', 'IAN', 'SOH']
    };
    expect(goldenLayoutPanel.getOpenLayout()).toEqual(expectedResults);
  });

  it('can handle submitSaveAs', async () => {
    await goldenLayoutPanel.submitSaveAs();
    expect(goldenLayoutPanel).toBeDefined();
  });

  it('can handle handleAffirmativeAction', async () => {
    await goldenLayoutPanel.handleAffirmativeAction();
    expect(goldenLayoutPanel).toBeDefined();
  });
  it('can handle finishLogout', () => {
    goldenLayoutPanel.finishLogout();
    expect(goldenLayoutPanel.props.logout).toHaveBeenCalled();
  });

  it('can handle isLayoutChanged', () => {
    expect(goldenLayoutPanel.isLayoutChanged()).toBeUndefined();
  });

  it('can handle discardLayoutChangesOnLogout', () => {
    goldenLayoutPanel.discardLayoutChangesOnLogout();
    expect(goldenLayoutPanel).toBeDefined();
  });
  it('can handle handleLogout', () => {
    goldenLayoutPanel.handleLogout();
    expect(goldenLayoutPanel).toBeDefined();
  });
  it('can handle saveLayoutChangesOnLogout', () => {
    goldenLayoutPanel.saveLayoutChangesOnLogout();
    expect(goldenLayoutPanel).toBeDefined();
  });
  it('can handle toggleAboutDialog', () => {
    goldenLayoutPanel.toggleAboutDialog();
    expect(goldenLayoutPanel).toBeDefined();
  });
  it('can handle toggleSaveWorkspaceAsDialog', () => {
    goldenLayoutPanel.toggleSaveWorkspaceAsDialog();
    expect(goldenLayoutPanel).toBeDefined();
  });
  it('can handle registerComponent', () => {
    const component: React.FunctionComponent = () => <div>test</div>;
    goldenLayoutPanel.gl = {
      registerComponent: jest.fn()
    };
    goldenLayoutPanel.registerComponent('test', component);
    expect(goldenLayoutPanel.gl.registerComponent).toHaveBeenCalled();
  });
  it('can handle openDisplay when a display is maximized', () => {
    goldenLayoutPanel.gl = {
      registerComponent: jest.fn(),
      root: {
        contentItems: [
          {
            contentItems: [
              {
                isRow: true,
                isMaximised: true,
                addChild: jest.fn(),
                contentItems: [
                  {
                    isMaximised: true,
                    addChild: jest.fn()
                  }
                ]
              }
            ]
          }
        ],
        addChild: jest.fn()
      }
    };
    goldenLayoutPanel.openDisplay('test');
    expect(goldenLayoutPanel).toBeDefined();
  });

  it('can handle openDisplay when no other displays are open', () => {
    goldenLayoutPanel.gl = {
      registerComponent: jest.fn(),
      root: {
        contentItems: [],
        addChild: jest.fn()
      }
    };
    goldenLayoutPanel.openDisplay('test');
    expect(goldenLayoutPanel).toBeDefined();
  });
  it('can handle componentDidUpdate', () => {
    goldenLayoutPanel.gl = {
      updateSize: jest.fn(),
      registerComponent: jest.fn(),
      destroy: jest.fn(),
      toConfig: jest.fn(() => [{ content: [] }]),
      root: {
        contentItems: [
          {
            contentItems: [
              {
                isRow: true,
                isMaximised: true,
                addChild: jest.fn(),
                contentItems: [
                  {
                    isMaximised: true,
                    addChild: jest.fn()
                  }
                ]
              }
            ]
          }
        ],
        addChild: jest.fn()
      }
    };
    goldenLayoutPanel.registerComponentsAndGoldenLayout = jest.fn();
    goldenLayoutPanel.componentDidUpdate(props);
    expect(goldenLayoutPanel).toBeDefined();
  });
});
