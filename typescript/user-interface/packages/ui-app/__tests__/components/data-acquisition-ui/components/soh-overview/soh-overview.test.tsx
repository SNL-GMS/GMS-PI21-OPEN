/* eslint-disable react/function-component-definition */
/* eslint-disable react/jsx-props-no-spreading */
import { uuid } from '@gms/common-util';
import { WithNonIdealStates } from '@gms/ui-core-components';
import { dataAcquisitionActions, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import React from 'react';
import { act } from 'react-dom/test-utils';
import { Provider } from 'react-redux';

import type { SohOverviewProps } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-component';
import { SohOverviewComponent } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-component';
import { StationGroupsLayout } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/station-groups/station-groups-layout';
import { DataAcquisitionNonIdealStateDefs } from '../../../../../src/ts/components/data-acquisition-ui/shared/non-ideal-states';
import { stationAndStationGroupSohStatus } from '../../../../__data__/data-acquisition-ui/soh-overview-data';
import { useQueryStateResult } from '../../../../__data__/test-util-data';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();
window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';
const sohConfigurationQuery = useQueryStateResult;
sohConfigurationQuery.data = sohConfiguration;

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

describe('Soh Component', () => {
  beforeAll(() => {
    // Create a spy on console and provide some mocked implementation
    // In mocking global objects it's usually better than simple `jest.fn()`
    // because you can `un-mock` it in clean way doing `mockRestore`
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    jest.spyOn(console, 'error').mockImplementation((msg: string) => {
      // eslint-disable-next-line jest/no-standalone-expect
      // expect(msg).toEqual('got a failed promise');
    });
  });

  it('should be defined', () => {
    expect(StationGroupsLayout).toBeDefined();
  });

  uuid.asString = () => '1';

  const store = getStore();
  store.dispatch(
    dataAcquisitionActions.setSohStatus({
      isStale: false,
      lastUpdated: 0,
      loading: false,
      stationAndStationGroupSoh: undefined
    })
  );

  const SohOverviewComponentWithProvider = props => (
    <Provider store={store}>
      <SohOverviewComponent
        {...props}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
      />
    </Provider>
  );

  const mockAcknowledge = jest.fn().mockReturnValue(new Promise(jest.fn()));
  const SohOverviewComponentNonIdealState: React.FC<SohOverviewProps> = WithNonIdealStates<
    SohOverviewProps
  >(
    [...DataAcquisitionNonIdealStateDefs.generalSohNonIdealStateDefinitions],
    SohOverviewComponentWithProvider
  );

  const props: SohOverviewProps = {
    acknowledgeStationsByName: jest.fn(),
    selectedStationIds: [],
    setSelectedStationIds: jest.fn(),
    sohStatus: {
      lastUpdated: 0,
      loading: false,
      isStale: false,
      stationAndStationGroupSoh: {
        stationGroups: [],
        stationSoh: [],
        isUpdateResponse: false
      }
    },
    glContainer: undefined,
    sohConfigurationQuery: undefined,
    getSohConfiguration: jest.fn()
  };

  const sohOverviewOrNonIdealState: any = Enzyme.mount(
    <SohOverviewComponentNonIdealState {...props} />
  );

  it('should render non ideal states and match snapshot', () => {
    const { container } = render(<SohOverviewComponentNonIdealState {...props} />);
    expect(container).toMatchSnapshot();
  });

  // TODO rewrite using RTL rerender
  it('should show non-ideal state when the golden layout container is hidden', () => {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    act(() => {
      sohOverviewOrNonIdealState.setProps({
        sohStatus: undefined,
        acknowledgeStationsByName: mockAcknowledge,
        glContainer: { isHidden: true, on: jest.fn(), off: jest.fn() },
        saveStationGroupSohStatus: undefined,
        mutate: undefined
      });
      sohOverviewOrNonIdealState.update();
    });
    expect(sohOverviewOrNonIdealState).toMatchSnapshot();
  });

  it('should show non-ideal state when there is no query', () => {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    act(() => {
      sohOverviewOrNonIdealState.setProps({
        sohStatus: undefined,
        acknowledgeStationsByName: mockAcknowledge,
        glContainer: undefined,
        saveStationGroupSohStatus: undefined,
        mutate: undefined
      });
      sohOverviewOrNonIdealState.update();
    });

    expect(sohOverviewOrNonIdealState).toMatchSnapshot();
  });

  it('should show non-ideal state when there is no station group data', () => {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    act(() => {
      sohOverviewOrNonIdealState.setProps({
        sohStatus: {
          loading: false,
          stationGroupSohStatus: []
        },
        sohConfigurationQuery,
        acknowledgeStationsByName: mockAcknowledge,
        glContainer: undefined,
        saveStationGroupSohStatus: undefined,
        mutate: undefined
      });
      sohOverviewOrNonIdealState.update();
    });
    expect(sohOverviewOrNonIdealState).toMatchSnapshot();
  });

  it('should match snapshot with basic props', () => {
    const realDateNow = Date.now.bind(global.Date);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    const dateNowStub = jest.fn(() => 1530518207007);
    global.Date.now = dateNowStub;

    // eslint-disable-next-line @typescript-eslint/no-floating-promises

    sohOverviewOrNonIdealState.setProps({
      sohStatus: {
        loading: false,
        error: undefined,
        isStale: false,
        stationAndStationGroupSoh: stationAndStationGroupSohStatus
      },
      sohConfigurationQuery,
      acknowledgeStationsByName: mockAcknowledge,
      saveStationGroupSohStatus: undefined,
      mutate: undefined
    });
    act(() => {
      sohOverviewOrNonIdealState.update();
    });

    expect(sohOverviewOrNonIdealState).toMatchSnapshot();
    global.Date.now = realDateNow;
  });
});
