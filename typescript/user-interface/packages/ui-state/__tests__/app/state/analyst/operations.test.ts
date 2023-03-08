import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import {
  AnalystWorkspaceOperations,
  AnalystWorkspaceTypes
} from '../../../../src/ts/app/state/analyst';
import { analystSlice } from '../../../../src/ts/app/state/analyst/analyst-slice';
import { initialState } from '../../../../src/ts/app/state/reducer';
import type { AppState } from '../../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let store: MockStore<AppState, any>;

describe('state analyst-workspace operations', () => {
  describe('operations', () => {
    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    describe('open event', () => {
      it('should set the open event (undefined) starting with initial state', () => {
        store.dispatch(AnalystWorkspaceOperations.setOpenEventId(undefined, undefined, undefined));
        const actions = store.getActions();
        expect(actions).toMatchSnapshot();
      });

      it('should set the open event (undefined)', () => {
        store = mockStoreCreator({
          app: {
            ...initialState,
            analyst: {
              ...initialState.analyst,
              selectedSdIds: ['selected-signal-detection-id'],
              openEventId: 'event',
              selectedEventIds: ['selected-event-id'],
              sdIdsToShowFk: ['signal-detection-id'],
              measurementMode: {
                mode: AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT,
                entries: { a: true }
              },
              selectedSortType: AnalystWorkspaceTypes.WaveformSortType.distance
            }
          }
        } as any);
        const expectedActions = [
          {
            type: analystSlice.actions.setOpenEventId.type,
            payload: undefined
          },
          {
            type: analystSlice.actions.setSelectedEventIds.type,
            payload: []
          },
          {
            type: analystSlice.actions.setSelectedSortType.type,
            payload: AnalystWorkspaceTypes.WaveformSortType.stationNameAZ
          },
          {
            type: analystSlice.actions.setMeasurementModeEntries.type,
            payload: {}
          },
          {
            type: analystSlice.actions.setSelectedLocationSolutionSetId.type,
            payload: undefined
          },
          {
            type: analystSlice.actions.setSelectedLocationSolutionId.type,
            payload: undefined
          },
          {
            type: analystSlice.actions.setSelectedPreferredLocationSolutionSetId.type,
            payload: undefined
          },
          {
            type: analystSlice.actions.setSelectedPreferredLocationSolutionId.type,
            payload: undefined
          },
          {
            type: analystSlice.actions.setSelectedSdIds.type,
            payload: []
          },
          {
            type: analystSlice.actions.setSdIdsToShowFk.type,
            payload: []
          },
          {
            type: analystSlice.actions.setMode.type,
            payload: AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT
          }
        ];
        store.dispatch(AnalystWorkspaceOperations.setOpenEventId(undefined, undefined, undefined));
        const actions = store.getActions();
        expect(actions).toEqual(expectedActions);
      });
    });

    describe('measurement mode', () => {
      it('should set the mode to default', () => {
        const mode = AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT;
        const expectedActions = [{ type: analystSlice.actions.setMode.type, payload: mode }];

        store.dispatch(AnalystWorkspaceOperations.setMode(mode));
        const actions = store.getActions();
        expect(actions).toEqual(expectedActions);
      });

      it('should set the mode to measurement', () => {
        const mode = AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT;
        const expectedActions = [{ type: analystSlice.actions.setMode.type, payload: mode }];

        store.dispatch(AnalystWorkspaceOperations.setMode(mode));
        const actions = store.getActions();
        expect(actions).toEqual(expectedActions);
      });

      it('should set the measurements entries', () => {
        const entries = { a: true };
        const expectedActions = [
          {
            type: analystSlice.actions.setMeasurementModeEntries.type,
            payload: entries
          }
        ];

        store.dispatch(AnalystWorkspaceOperations.setMeasurementModeEntries(entries));
        const actions = store.getActions();
        expect(actions).toEqual(expectedActions);
      });
    });

    describe('location', () => {
      it('should set the location solution', () => {
        const locationSolutionSetId = 'location-solution-set-id';
        const locationSolutionId = 'location-solution-id';
        const expectedActions = [
          {
            type: analystSlice.actions.setSelectedLocationSolutionSetId.type,
            payload: locationSolutionSetId
          },
          {
            type: analystSlice.actions.setSelectedLocationSolutionId.type,
            payload: locationSolutionId
          }
        ];
        store.dispatch(
          AnalystWorkspaceOperations.setSelectedLocationSolution(
            locationSolutionSetId,
            locationSolutionId
          )
        );
        const actions = store.getActions();
        expect(actions).toEqual(expectedActions);
      });

      it('should set the preferred location solution', () => {
        const preferredLocationSolutionSetId = 'preferred-location-solution-set-id';
        const preferredLocationSolutionId = 'preferred-location-solution-id';
        const expectedActions = [
          {
            type: analystSlice.actions.setSelectedPreferredLocationSolutionSetId.type,
            payload: preferredLocationSolutionSetId
          },
          {
            type: analystSlice.actions.setSelectedPreferredLocationSolutionId.type,
            payload: preferredLocationSolutionId
          }
        ];
        store.dispatch(
          AnalystWorkspaceOperations.setSelectedPreferredLocationSolution(
            preferredLocationSolutionSetId,
            preferredLocationSolutionId
          )
        );
        const actions = store.getActions();
        expect(actions).toEqual(expectedActions);
      });
    });
  });
});
