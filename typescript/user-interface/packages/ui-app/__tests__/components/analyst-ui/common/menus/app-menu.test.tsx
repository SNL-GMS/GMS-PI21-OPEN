import { UserProfileTypes } from '@gms/common-model';
import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import Enzyme from 'enzyme';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import { Provider } from 'react-redux';

import type {
  GLComponentConfig,
  GLComponentConfigList
} from '../../../../../src/ts/components/workspace/components/golden-layout/types';
import type { AboutMenuProps } from '../../../../../src/ts/components/workspace/components/menus/about-menu';
import { AboutMenu } from '../../../../../src/ts/components/workspace/components/menus/about-menu';
import type { AppMenuProps } from '../../../../../src/ts/components/workspace/components/menus/app-menu';
import {
  AppMenu,
  UIThemeMenuItem
} from '../../../../../src/ts/components/workspace/components/menus/app-menu';
import { DeprecatedToolbar } from '../../../../../src/ts/components/workspace/components/toolbar/toolbar-component';
import type { DeprecatedToolbarProps } from '../../../../../src/ts/components/workspace/components/toolbar/types';
import { useQueryStateResult } from '../../../../__data__/test-util-data';

const processingAnalystConfigurationQuery = cloneDeep(useQueryStateResult);

jest.mock('@gms/common-util', () => {
  const actual = jest.requireActual('@gms/common-util');
  return {
    ...actual,
    isIanMode: jest.fn(() => true)
  };
});

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  const mockUserProfile = {
    userId: '1',
    defaultAnalystLayoutName: 'default',
    defaultSohLayoutName: 'default',
    audibleNotifications: [],
    currentTheme: 'GMS Dark Theme',
    workspaceLayouts: [
      {
        name: 'default',
        layoutConfiguration: 'test',
        supportedUserInterfaceModes: [UserProfileTypes.UserMode.IAN]
      }
    ]
  };
  return {
    ...actual,
    useGetUserProfileQuery: jest.fn(() => ({ data: mockUserProfile })),
    useGetProcessingAnalystConfiguration: jest.fn(() => processingAnalystConfigurationQuery)
  };
});

function testComponentRender(container, wrapper) {
  it('should have a consistent snapshot on mount', () => {
    expect(container).toMatchSnapshot();
  });

  it('should have basic props built on render', () => {
    const buildProps = wrapper.props() as AppMenuProps;
    expect(buildProps).toMatchSnapshot();
  });
}

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();
/**
 * Tests the app menu component
 */

const analystUIComponents: Map<string, GLComponentConfig> = new Map([
  [
    'waveform-display',
    {
      type: 'react-component',
      title: 'Waveforms',
      component: 'waveform-display'
    }
  ]
]);

const dataAcqComponents: Map<string, GLComponentConfig> = new Map([
  [
    'soh-overview',
    {
      type: 'react-component',
      title: 'SOH Overview',
      component: 'soh-overview'
    }
  ]
]);

const components: GLComponentConfigList = {};
[...analystUIComponents, ...dataAcqComponents].forEach(([componentName, glComponent]) => {
  components[componentName] = glComponent;
});

const fauxAnalystComponents = new Map([
  [
    'Analyst',
    new Map([
      [
        components['waveform-display'].component,
        { id: components['waveform-display'], value: undefined }
      ]
    ])
  ],
  [
    'Data Acquisition',
    new Map([
      [components['soh-overview'].component, { id: components['soh-overview'], value: undefined }]
    ])
  ]
]);

describe('app-menu', () => {
  const mockUserProfile = {
    userId: '1',
    defaultAnalystLayoutName: 'default',
    defaultSohLayoutName: 'default',
    audibleNotifications: [],
    currentTheme: 'GMS Dark Theme',
    workspaceLayouts: [
      {
        name: 'default',
        layoutConfiguration: 'test',
        supportedUserInterfaceModes: [UserProfileTypes.UserMode.IAN]
      }
    ]
  };
  const showLogs = jest.fn();
  showLogs(
    'this should be in the snapshot, which indicates that the function was plumbed into the right place.'
  );
  const showAboutDialog = jest.fn();
  const logout = jest.fn();
  const appMenu = (
    <Provider store={getStore()}>
      <AppMenu
        components={fauxAnalystComponents}
        openLayoutName=""
        clearLayout={jest.fn()}
        logout={logout}
        openDisplay={jest.fn()}
        openWorkspace={jest.fn()}
        showAboutDialog={showAboutDialog}
        saveWorkspaceAs={jest.fn()}
        getOpenDisplays={() => []}
        showLogs={showLogs}
        logo={undefined}
        userProfile={mockUserProfile}
      />
    </Provider>
  );

  /*
    This is weird, but there is no method to test derived props,
    so this will be smelly until we fully convert to RTL
  */
  const wrapper = Enzyme.mount(<Provider store={getStore()}>{appMenu}</Provider>);
  const { container } = render(<Provider store={getStore()}>{appMenu}</Provider>);
  testComponentRender(container, wrapper);

  it('should be able to click log out', () => {
    Enzyme.mount(<Provider store={getStore()}>{appMenu}</Provider>)
      .find('a.app-menu__logout')
      .simulate('click');
    expect(logout).toBeCalled();
  });

  it('should be able to click About', () => {
    Enzyme.mount(<Provider store={getStore()}>{appMenu}</Provider>)
      .find('a.app-menu__about')
      .simulate('click');
    expect(showAboutDialog).toBeCalled();
  });
});

describe('app-toolbar', () => {
  const props: DeprecatedToolbarProps = {
    components: fauxAnalystComponents,
    clearLayout: () => jest.fn(),
    isAboutDialogOpen: true,
    logo: new Image(),
    logout: () => jest.fn() as any,
    openDisplay: () => jest.fn(),
    openWorkspace: () => jest.fn(),
    showLogPopup: () => jest.fn(),
    showAboutDialog: () => jest.fn(),
    userName: 'Test User',
    getOpenDisplays: () => [],
    userProfile: {
      userId: '1',
      defaultAnalystLayoutName: 'default',
      defaultSohLayoutName: 'default',
      currentTheme: 'GMS Dark Theme',
      workspaceLayouts: [
        {
          name: 'default',
          layoutConfiguration: 'test',
          supportedUserInterfaceModes: [UserProfileTypes.UserMode.IAN]
        }
      ],
      audibleNotifications: []
    },
    isSaveWorkspaceAsDialogOpen: false,
    openLayoutName: '',
    setLayout: undefined,
    saveDialog: undefined,
    setOpenLayoutName: undefined,
    toggleSaveWorkspaceAsDialog: undefined,
    versionInfo: {
      versionNumber: '1',
      commitSHA: '2'
    }
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const testToolbar = <DeprecatedToolbar {...props} />;
  const wrapper: any = Enzyme.mount(<Provider store={getStore()}>{testToolbar}</Provider>);

  const { container } = render(<Provider store={getStore()}>{testToolbar}</Provider>);
  testComponentRender(container, wrapper);
});

describe('about-menu', () => {
  const props: AboutMenuProps = {
    versionNumber: '9.0.1-Snapshot',
    commitSHA: 'FCDA123',
    logo: new Image()
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const testAboutMenu = <AboutMenu {...props} />;
  const wrapper: any = Enzyme.mount(<Provider store={getStore()}>{testAboutMenu}</Provider>);

  const { container } = render(<Provider store={getStore()}>{testAboutMenu}</Provider>);
  testComponentRender(container, wrapper);
});

describe('ui theme menu', () => {
  it('matches snapshot', () => {
    const { container } = render(
      <Provider store={getStore()}>
        <UIThemeMenuItem />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
