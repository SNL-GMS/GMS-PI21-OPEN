import { WorkflowTypes } from '@gms/common-model';
import type { TimeRange } from '@gms/common-model/lib/common/types';
import type { StationGroup } from '@gms/common-model/lib/workflow/types';
import { AnalysisMode } from '@gms/common-model/lib/workflow/types';
import clone from 'lodash/clone';
import cloneDeep from 'lodash/cloneDeep';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { initialState } from '../../../../src/ts/app/state/reducer';
import * as Operations from '../../../../src/ts/app/state/workflow/operations';
import type { AppState } from '../../../../src/ts/app/store';
import { appState } from '../../../test-util';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);

const mockInitialAppState = cloneDeep(initialState);

jest.mock(
  '../../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice',
  () => {
    const actual = jest.requireActual(
      '../../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice'
    );
    return {
      ...actual,
      processingConfigurationApiSlice: {
        endpoints: {
          getProcessingAnalystConfiguration: {
            select: jest.fn(() =>
              jest.fn(() => ({
                data: {
                  leadBufferDuration: 900,
                  lagBufferDuration: 900
                }
              }))
            )
          }
        }
      }
    };
  }
);

jest.mock('../../../../src/ts/app/api/station-definition/station-definition-api-slice', () => {
  const actual = jest.requireActual(
    '../../../../src/ts/app/api/station-definition/station-definition-api-slice'
  );
  return {
    ...actual,
    stationDefinitionSlice: {
      endpoints: {
        getStationGroupsByNames: {
          ...actual.stationDefinitionSlice.endpoints.getStationGroupsByNames,
          select: jest.fn(() =>
            jest.fn(() => ({
              data: {}
            }))
          )
        }
      }
    }
  };
});

jest.mock('worker-rpc', () => ({
  RpcProvider: jest.fn().mockImplementation(() => {
    // eslint-disable-next-line no-var

    const mockRpc = jest.fn(async () => {
      return new Promise(resolve => {
        resolve([]);
      });
    });
    return { rpc: mockRpc };
  })
}));

describe('workflow operations', () => {
  // setting this because the initial zoom interval (due to the default lead and lag in the initial state) is -900, 900
  mockInitialAppState.waveform.viewableInterval = {
    startTimeSecs: -901,
    endTimeSecs: 905
  };

  it('should open an interval', () => {
    const mockAppState = cloneDeep(appState);
    mockAppState.app = mockInitialAppState;
    const store: MockStore<AppState, any> = mockStoreCreator(mockAppState);

    const timeRange: TimeRange = {
      startTimeSecs: 1,
      endTimeSecs: 2
    };

    const stationGroup: StationGroup = {
      effectiveAt: 1,
      name: 'name',
      description: 'description'
    };

    const openIntervalName = '1';
    const openActivityNames = ['2'];
    const analysisMode = WorkflowTypes.AnalysisMode.SCAN;

    store.dispatch(
      Operations.setOpenInterval(
        timeRange,
        stationGroup,
        openIntervalName,
        openActivityNames,
        analysisMode
      )
    );
    expect(
      store
        .getActions()
        .filter(action => action.type !== 'stationDefinitionApi/executeQuery/pending')
    ).toMatchSnapshot();
  });

  it('should open an interval with no change', () => {
    const updatedInitialState: AppState = cloneDeep(appState);
    updatedInitialState.app = clone(mockInitialAppState);

    const timeRange: TimeRange = {
      startTimeSecs: 1,
      endTimeSecs: 2
    };

    const stationGroup: StationGroup = {
      effectiveAt: 1,
      name: 'name',
      description: 'description'
    };

    const openIntervalName = '1';
    const openActivityNames = ['2'];

    const analysisMode = WorkflowTypes.AnalysisMode.SCAN;

    updatedInitialState.app.workflow = {
      timeRange,
      openIntervalName,
      openActivityNames,
      stationGroup,
      analysisMode: AnalysisMode.SCAN
    };
    const store: MockStore<AppState, any> = mockStoreCreator(updatedInitialState);

    store.dispatch(
      Operations.setOpenInterval(
        timeRange,
        stationGroup,
        openIntervalName,
        openActivityNames,
        analysisMode
      )
    );
    expect(
      store
        .getActions()
        .filter(action => action.type !== 'stationDefinitionApi/executeQuery/pending')
    ).toMatchSnapshot();
  });

  it('should close an interval activity', () => {
    const timeRange: TimeRange = {
      startTimeSecs: 1,
      endTimeSecs: 2
    };

    const stationGroup: StationGroup = {
      effectiveAt: 1,
      name: 'name',
      description: 'description'
    };
    const openIntervalName = '1';
    const openActivityNames = ['2'];

    const analysisMode = WorkflowTypes.AnalysisMode.SCAN;

    const mockAppState: AppState = cloneDeep(appState);
    mockAppState.app = {
      ...mockInitialAppState,
      analyst: {
        ...mockInitialAppState.analyst
      },
      workflow: {
        timeRange,
        stationGroup,
        openIntervalName,
        openActivityNames,
        analysisMode
      }
    };
    const store: MockStore<AppState, any> = mockStoreCreator(mockAppState);

    expect(
      store
        .getActions()
        .filter(action => action.type !== 'stationDefinitionApi/executeQuery/pending')
    ).toMatchSnapshot();
  });

  it('should close a stage interval', () => {
    const timeRange: TimeRange = {
      startTimeSecs: 1,
      endTimeSecs: 2
    };

    const stationGroup: StationGroup = {
      effectiveAt: 1,
      name: 'name',
      description: 'description'
    };
    const openIntervalName = '1';
    const openActivityNames = ['2', '3'];

    const analysisMode = WorkflowTypes.AnalysisMode.SCAN;

    const mockAppState: AppState = cloneDeep(appState);
    mockAppState.app = {
      ...mockInitialAppState,
      analyst: {
        ...mockInitialAppState.analyst
      },
      workflow: {
        timeRange,
        stationGroup,
        openIntervalName,
        openActivityNames,
        analysisMode
      }
    };
    const store: MockStore<AppState, any> = mockStoreCreator(mockAppState);

    expect(
      store
        .getActions()
        .filter(action => action.type !== 'stationDefinitionApi/executeQuery/pending')
    ).toMatchSnapshot();
  });

  it('should close an activity and remove selected', () => {
    const timeRange: TimeRange = {
      startTimeSecs: 1,
      endTimeSecs: 2
    };

    const stationGroup: StationGroup = {
      effectiveAt: 1,
      name: 'name',
      description: 'description'
    };
    const openIntervalName = '1';
    const openActivityNames = ['2', '3'];

    const analysisMode = WorkflowTypes.AnalysisMode.SCAN;

    const mockAppState: AppState = cloneDeep(appState);
    mockAppState.app = {
      ...mockInitialAppState,
      analyst: {
        ...mockInitialAppState.analyst
      },
      workflow: {
        timeRange,
        stationGroup,
        openIntervalName,
        openActivityNames,
        analysisMode
      }
    };
    const store: MockStore<AppState, any> = mockStoreCreator(mockAppState);

    store.dispatch(Operations.setClosedInterval(openActivityNames[0], false));
    expect(store.getActions()).toMatchSnapshot();

    store.dispatch(Operations.setClosedInterval(openActivityNames[0], true));
    expect(store.getActions()).toMatchSnapshot();
  });
});
