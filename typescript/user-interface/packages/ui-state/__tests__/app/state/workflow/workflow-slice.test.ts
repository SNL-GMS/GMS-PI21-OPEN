import type { CommonTypes } from '@gms/common-model';
import { WorkflowTypes } from '@gms/common-model';
import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { initialState } from '../../../../src/ts/app/state/reducer';
import {
  workflowInitialState,
  workflowSlice
} from '../../../../src/ts/app/state/workflow/workflow-slice';
import type { AppState } from '../../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);
let store: MockStore<AppState, Redux.AnyAction>;

describe('workflow slice', () => {
  describe('exists', () => {
    it('exists', () => {
      expect(workflowInitialState).toBeDefined();
      expect(workflowSlice).toBeDefined();
      expect(workflowInitialState).toMatchSnapshot();
    });
  });

  describe('reducer', () => {
    it('should return the initial state', () => {
      expect(workflowSlice.reducer(undefined, createAction(undefined))).toEqual(
        workflowInitialState
      );
      expect(workflowSlice.reducer(undefined, createAction(''))).toEqual(workflowInitialState);
      expect(workflowSlice.reducer(workflowInitialState, createAction(undefined))).toEqual(
        workflowInitialState
      );
      expect(workflowSlice.reducer(workflowInitialState, createAction(''))).toEqual(
        workflowInitialState
      );
    });
  });
  describe('actions', () => {
    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    it('should set the time range', () => {
      const timeRange: CommonTypes.TimeRange = {
        startTimeSecs: 1,
        endTimeSecs: 2
      };
      const action: Redux.AnyAction = {
        type: workflowSlice.actions.setTimeRange.type,
        payload: timeRange
      };

      expect(workflowSlice.reducer(workflowInitialState, action)).toEqual({
        ...workflowInitialState,
        timeRange
      });

      expect(workflowSlice.actions.setTimeRange(timeRange)).toEqual(action);

      store.dispatch(workflowSlice.actions.setTimeRange(timeRange));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set the station group', () => {
      const stationGroup: WorkflowTypes.StationGroup = {
        effectiveAt: 1,
        name: 'name',
        description: 'description'
      };
      const action: Redux.AnyAction = {
        type: workflowSlice.actions.setStationGroup.type,
        payload: stationGroup
      };

      expect(workflowSlice.reducer(workflowInitialState, action)).toEqual({
        ...workflowInitialState,
        stationGroup
      });

      expect(workflowSlice.actions.setStationGroup(stationGroup)).toEqual(action);

      store.dispatch(workflowSlice.actions.setStationGroup(stationGroup));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set the openIntervalName', () => {
      const openIntervalName = '1';
      const action: Redux.AnyAction = {
        type: workflowSlice.actions.setOpenIntervalName.type,
        payload: openIntervalName
      };

      expect(workflowSlice.reducer(workflowInitialState, action)).toEqual({
        ...workflowInitialState,
        openIntervalName
      });

      expect(workflowSlice.actions.setOpenIntervalName(openIntervalName)).toEqual(action);

      store.dispatch(workflowSlice.actions.setOpenIntervalName(openIntervalName));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set the openActivityNames', () => {
      const openActivityNames = ['1'];
      const action: Redux.AnyAction = {
        type: workflowSlice.actions.setOpenActivityNames.type,
        payload: openActivityNames
      };

      expect(workflowSlice.reducer(workflowInitialState, action)).toEqual({
        ...workflowInitialState,
        openActivityNames
      });

      expect(workflowSlice.actions.setOpenActivityNames(openActivityNames)).toEqual(action);

      store.dispatch(workflowSlice.actions.setOpenActivityNames(openActivityNames));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set the analysis modes', () => {
      const analysisMode = WorkflowTypes.AnalysisMode.EVENT_REVIEW;
      const action: Redux.AnyAction = {
        type: workflowSlice.actions.setAnalysisMode.type,
        payload: analysisMode
      };

      expect(workflowSlice.reducer(workflowInitialState, action)).toEqual({
        ...workflowInitialState,
        analysisMode
      });

      expect(workflowSlice.actions.setAnalysisMode(analysisMode)).toEqual(action);

      store.dispatch(workflowSlice.actions.setAnalysisMode(analysisMode));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });
  });
});
