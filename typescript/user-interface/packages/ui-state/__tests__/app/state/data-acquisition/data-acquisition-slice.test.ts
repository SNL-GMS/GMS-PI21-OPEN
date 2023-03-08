import { SohTypes, StationTypes } from '@gms/common-model';
import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import {
  dataAcquisitionInitialState,
  dataAcquisitionSlice
} from '../../../../src/ts/app/state/data-acquisition/data-acquisition-slice';
import type * as Types from '../../../../src/ts/app/state/data-acquisition/types';
import { initialState } from '../../../../src/ts/app/state/reducer';
import type { AppState } from '../../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);
let store: MockStore<AppState, Redux.AnyAction>;

describe('state data acquisition slice', () => {
  it('defined', () => {
    expect(dataAcquisitionSlice).toBeDefined();
    expect(dataAcquisitionInitialState).toBeDefined();
  });

  it('should return the initial state', () => {
    expect(dataAcquisitionSlice.reducer(undefined, createAction(undefined))).toMatchSnapshot();
    expect(dataAcquisitionSlice.reducer(undefined, createAction(''))).toMatchSnapshot();
    expect(dataAcquisitionSlice.reducer(undefined, createAction('sampleAction'))).toMatchSnapshot();
  });

  it('initial state is correct', () => {
    expect(dataAcquisitionInitialState).toMatchSnapshot();
  });

  describe('actions', () => {
    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    it('should set selected processing station', () => {
      const action: Redux.AnyAction = {
        type: dataAcquisitionSlice.actions.setSelectedProcessingStation.type,
        payload: {
          id: 'station id',
          name: 'station name',
          stationType: StationTypes.StationType.HYDROACOUSTIC,
          description: 'description',
          defaultChannel: undefined,
          networks: [
            {
              id: '12345',
              name: 'network name',
              monitoringOrganization: 'monitoring org'
            }
          ],
          modified: false,
          location: {
            latitudeDegrees: 0,
            longitudeDegrees: 0,
            elevationKm: 0,
            depthKm: 0
          },
          sites: [],
          dataAcquisition: undefined,
          latitude: 0,
          longitude: 0,
          elevation: 0
        }
      };
      const expectedState: Types.DataAcquisitionState = {
        ...dataAcquisitionInitialState,
        selectedProcessingStation: {
          ...action.payload
        }
      };
      expect(dataAcquisitionSlice.reducer(dataAcquisitionInitialState, action)).toEqual(
        expectedState
      );

      expect(dataAcquisitionSlice.actions.setSelectedProcessingStation(action.payload)).toEqual(
        action
      );
      store.dispatch(dataAcquisitionSlice.actions.setSelectedProcessingStation(action.payload));

      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set unmodified processing station', () => {
      const action: Redux.AnyAction = {
        type: dataAcquisitionSlice.actions.setUnmodifiedProcessingStation.type,
        payload: {
          id: 'station id',
          name: 'station name',
          stationType: StationTypes.StationType.HYDROACOUSTIC,
          description: 'description',
          defaultChannel: undefined,
          networks: [
            {
              id: '12345',
              name: 'network name',
              monitoringOrganization: 'monitoring org'
            }
          ],
          modified: false,
          location: {
            latitudeDegrees: 0,
            longitudeDegrees: 0,
            elevationKm: 0,
            depthKm: 0
          },
          sites: [],
          dataAcquisition: undefined,
          latitude: 0,
          longitude: 0,
          elevation: 0
        }
      };
      const expectedState: Types.DataAcquisitionState = {
        ...dataAcquisitionInitialState,
        unmodifiedProcessingStation: {
          ...action.payload
        }
      };
      expect(dataAcquisitionSlice.reducer(dataAcquisitionInitialState, action)).toEqual(
        expectedState
      );

      expect(dataAcquisitionSlice.actions.setUnmodifiedProcessingStation(action.payload)).toEqual(
        action
      );
      store.dispatch(dataAcquisitionSlice.actions.setUnmodifiedProcessingStation(action.payload));

      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set Soh Status', () => {
      const action: Redux.AnyAction = {
        type: dataAcquisitionSlice.actions.setSohStatus.type,
        payload: {
          lastUpdated: 123456789,
          isStale: false,
          loading: true,
          stationAndStationGroupSoh: undefined
        }
      };
      const expectedState: Types.DataAcquisitionState = {
        ...dataAcquisitionInitialState,
        data: {
          sohStatus: {
            ...action.payload
          }
        }
      };
      expect(dataAcquisitionSlice.reducer(dataAcquisitionInitialState, action)).toEqual(
        expectedState
      );

      expect(dataAcquisitionSlice.actions.setSohStatus(action.payload)).toEqual(action);
      store.dispatch(dataAcquisitionSlice.actions.setSohStatus(action.payload));

      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set selected Acei type', () => {
      const action: Redux.AnyAction = {
        type: dataAcquisitionSlice.actions.setSelectedAceiType.type,
        payload: SohTypes.AceiType.AMPLIFIER_SATURATION_DETECTED
      };
      const expectedState: Types.DataAcquisitionState = {
        ...dataAcquisitionInitialState,
        selectedAceiType: action.payload
      };
      expect(dataAcquisitionSlice.reducer(dataAcquisitionInitialState, action)).toEqual(
        expectedState
      );

      expect(dataAcquisitionSlice.actions.setSelectedAceiType(action.payload)).toEqual(action);
      store.dispatch(dataAcquisitionSlice.actions.setSelectedAceiType(action.payload));

      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });
  });
});
