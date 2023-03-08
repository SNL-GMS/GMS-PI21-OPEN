import { SohTypes } from '@gms/common-model';
import { uuid } from '@gms/common-util';
import type { Container } from '@gms/golden-layout';
import type { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import { WithNonIdealStates } from '@gms/ui-core-components';
import { dataAcquisitionActions, FilterableSOHTypes, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import type { EnvironmentProps } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/soh-environment-component';
import { EnvironmentComponent } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-environment/soh-environment-component';
import { DataAcquisitionNonIdealStateDefs } from '../../../../../src/ts/components/data-acquisition-ui/shared/non-ideal-states';
import { useQueryStateResult } from '../../../../__data__/test-util-data';

// mock the uuid
uuid.asString = jest.fn().mockImplementation(() => '12345789');

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetSohConfigurationQuery: jest.fn(() => ({
      data: sohConfiguration,
      isLoading: false
    }))
  };
});

uuid.asString = jest.fn().mockReturnValue('1e872474-b19f-4325-9350-e217a6feddc0');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();
window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

describe('SohEnvironmentComponent class', () => {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  global.Date.now = jest.fn(() => 1530518207007);

  const glWidth = 1000;
  const glHeight = 500;

  const myHiddenGLContainer: Container = {
    // Container
    width: glWidth,
    height: glHeight,
    parent: undefined,
    tab: undefined,
    title: 'container-title',
    layoutManager: undefined,
    isHidden: true,
    setState: jest.fn(),
    extendState: jest.fn(),
    getState: jest.fn(),
    getElement: jest.fn(),
    hide: jest.fn(),
    show: jest.fn(),
    setSize: jest.fn(),
    setTitle: jest.fn(),
    close: jest.fn(),
    // EventEmitter
    on: jest.fn(),
    emit: jest.fn(),
    trigger: jest.fn(),
    unbind: jest.fn(),
    off: jest.fn()
  };

  // messing with normal stuff
  const myGLContainer: Container = {
    // Container
    width: glWidth,
    height: glHeight,
    parent: undefined,
    tab: undefined,
    title: 'container-title',
    layoutManager: undefined,
    isHidden: false,
    setState: jest.fn(),
    extendState: jest.fn(),
    getState: jest.fn(),
    getElement: jest.fn(),
    hide: jest.fn(),
    show: jest.fn(),
    setSize: jest.fn(),
    setTitle: jest.fn(),
    close: jest.fn(),
    // EventEmitter
    on: jest.fn(),
    emit: jest.fn(),
    trigger: jest.fn(),
    unbind: jest.fn(),
    off: jest.fn()
  };

  const channel: SohTypes.ChannelSoh = {
    allSohMonitorValueAndStatuses: [
      {
        status: SohTypes.SohStatusSummary.GOOD,
        value: 1,
        valuePresent: true,
        monitorType: SohTypes.SohMonitorType.ENV_ZEROED_DATA,
        hasUnacknowledgedChanges: false,
        contributing: false,
        thresholdMarginal: 1,
        thresholdBad: 10,
        quietUntilMs: 1
      }
    ],
    channelName: 'AAA111',
    channelSohStatus: SohTypes.SohStatusSummary.GOOD
  };

  const sohStatus: any = {
    loading: false,
    stationAndStationGroupSoh: {
      stationSoh: [
        {
          channelSohs: [channel],
          stationName: 'A'
        }
      ]
    }
  };

  const sohConfigurationQuery: any = useQueryStateResult;
  sohConfigurationQuery.data = sohConfiguration;

  const myHiddenProps: EnvironmentProps = {
    glContainer: myHiddenGLContainer,
    selectedStationIds: ['A', 'B', 'C'],
    setSelectedStationIds: jest.fn(),
    sohConfigurationQuery: undefined,
    getSohConfiguration: jest.fn(() => ({ unsubscribe: jest.fn() })) as any,
    selectedAceiType: jest.fn() as any,
    setSelectedAceiType: jest.fn() as any,
    sohStatus: undefined,
    channelStatusesToDisplay: {
      [FilterableSOHTypes.GOOD]: false,
      [FilterableSOHTypes.MARGINAL]: false,
      [FilterableSOHTypes.BAD]: false,
      [FilterableSOHTypes.NONE]: false
    },
    monitorStatusesToDisplay: {
      [FilterableSOHTypes.GOOD]: false,
      [FilterableSOHTypes.MARGINAL]: false,
      [FilterableSOHTypes.BAD]: false,
      [FilterableSOHTypes.NONE]: false
    },
    setChannelStatusesToDisplay: jest.fn(),
    setMonitorStatusesToDisplay: jest.fn()
  };

  const EnvironmentPanelNonIdealState = WithNonIdealStates<EnvironmentProps>(
    [
      ...DataAcquisitionNonIdealStateDefs.generalSohNonIdealStateDefinitions,
      ...DataAcquisitionNonIdealStateDefs.stationSelectedSohNonIdealStateDefinitions,
      ...DataAcquisitionNonIdealStateDefs.channelSohNonIdealStateDefinitions
    ] as any[],
    EnvironmentComponent
  );

  it('should match not ready snapshot', () => {
    // messing with non ideal and stuff
    const { container } = render(
      // eslint-disable-next-line react/jsx-props-no-spreading
      <EnvironmentPanelNonIdealState {...myHiddenProps} />
    );
    expect(container).toMatchSnapshot();
  });

  const myProps: EnvironmentProps = {
    sohStatus,
    glContainer: myGLContainer,
    selectedStationIds: ['A'],
    setSelectedStationIds: jest.fn(),
    sohConfigurationQuery,
    getSohConfiguration: jest.fn(() => ({ unsubscribe: jest.fn() })) as any,
    selectedAceiType: jest.fn() as any,
    setSelectedAceiType: jest.fn() as any,
    channelStatusesToDisplay: {
      [FilterableSOHTypes.GOOD]: true,
      [FilterableSOHTypes.MARGINAL]: true,
      [FilterableSOHTypes.BAD]: true,
      [FilterableSOHTypes.NONE]: false
    },
    monitorStatusesToDisplay: {
      [FilterableSOHTypes.GOOD]: true,
      [FilterableSOHTypes.MARGINAL]: true,
      [FilterableSOHTypes.BAD]: true,
      [FilterableSOHTypes.NONE]: true
    },
    setChannelStatusesToDisplay: jest.fn(),
    setMonitorStatusesToDisplay: jest.fn()
  };

  const store = getStore();
  store.dispatch(
    dataAcquisitionActions.setSohStatus({
      isStale: false,
      lastUpdated: 0,
      loading: false,
      stationAndStationGroupSoh: undefined
    })
  );

  const sohEnvironmentComponent = Enzyme.mount(
    <Provider store={store}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <EnvironmentComponent {...myProps} />
    </Provider>
  );

  it('should be defined', () => {
    expect(EnvironmentComponent).toBeDefined();
  });

  it('should get station env info', () => {
    sohEnvironmentComponent.update();
    const stationEnvInfo = sohEnvironmentComponent
      .find(EnvironmentComponent)
      .instance()
      .getStation();
    expect(stationEnvInfo).toBeDefined();
  });

  // it.skip('should acknowledge channel monitor status', () => {
  //   const stationName = 'AAA';
  //   const sohMonType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.ENV_CLIPPED;
  //   const chanMonPair: SohTypes.ChannelMonitorPair = {
  //     channelName: 'AAA111',
  //     monitorType: sohMonType
  //   };
  //   const channelPairs: SohTypes.ChannelMonitorPair[] = [chanMonPair];
  // });

  it('should create filter dropdown', () => {
    const dropDownItem: DeprecatedToolbarTypes.CheckboxDropdownItem = sohEnvironmentComponent
      .find(EnvironmentComponent)
      .instance()
      .makeFilterDropDown();
    expect(dropDownItem).toBeDefined();
  });

  it('should match snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <EnvironmentComponent {...myProps} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
