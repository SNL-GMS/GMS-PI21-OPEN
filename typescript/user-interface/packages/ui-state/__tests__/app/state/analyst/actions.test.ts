import { CommonTypes } from '@gms/common-model';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { AnalystWorkspaceTypes } from '../../../../src/ts/app/state/analyst';
import { analystSlice } from '../../../../src/ts/app/state/analyst/analyst-slice';
import { initialState } from '../../../../src/ts/app/state/reducer';
import type { AppState } from '../../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);
let store: MockStore<AppState, Redux.AnyAction>;

describe('state analyst-workspace actions', () => {
  describe('internal actions', () => {
    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    it('should set the mode to default', () => {
      const mode = AnalystWorkspaceTypes.WaveformDisplayMode.DEFAULT;
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setMode.type,
        payload: mode
      };
      expect(analystSlice.actions.setMode(mode)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setMode(mode));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the mode to measurement', () => {
      const mode = AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT;
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setMode.type,
        payload: mode
      };
      expect(analystSlice.actions.setMode(mode)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setMode(mode));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the open event id', () => {
      const id = 'event-id';
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setOpenEventId.type,
        payload: id
      };
      expect(analystSlice.actions.setOpenEventId(id)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setOpenEventId(id));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the measurement mode entries', () => {
      const entries = { a: true };
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setMeasurementModeEntries.type,
        payload: entries
      };
      expect(analystSlice.actions.setMeasurementModeEntries(entries)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setMeasurementModeEntries(entries));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the selected location solution set id', () => {
      const id = 'location-solution-set-id';
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setSelectedLocationSolutionSetId.type,
        payload: id
      };
      expect(analystSlice.actions.setSelectedLocationSolutionSetId(id)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setSelectedLocationSolutionSetId(id));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the selected location solution id', () => {
      const id = 'location-solution-id';
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setSelectedLocationSolutionId.type,
        payload: id
      };
      expect(analystSlice.actions.setSelectedLocationSolutionId(id)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setSelectedLocationSolutionId(id));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the selected preferred location solution set id', () => {
      const id = 'location-preferred-solution-set-id';
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setSelectedPreferredLocationSolutionSetId.type,
        payload: id
      };
      expect(analystSlice.actions.setSelectedPreferredLocationSolutionSetId(id)).toEqual(
        expectedAction
      );

      store.dispatch(analystSlice.actions.setSelectedPreferredLocationSolutionSetId(id));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the selected preferred location solution id', () => {
      const id = 'location-solution-id';
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setSelectedPreferredLocationSolutionId.type,
        payload: id
      };
      expect(analystSlice.actions.setSelectedPreferredLocationSolutionId(id)).toEqual(
        expectedAction
      );

      store.dispatch(analystSlice.actions.setSelectedPreferredLocationSolutionId(id));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });
  });

  describe('actions', () => {
    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    it('should set the default signal detection phase', () => {
      const phase = CommonTypes.PhaseType.Lg;
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setDefaultSignalDetectionPhase.type,
        payload: phase
      };
      expect(analystSlice.actions.setDefaultSignalDetectionPhase(phase)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setDefaultSignalDetectionPhase(phase));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the selected sort type', () => {
      const sortType = AnalystWorkspaceTypes.WaveformSortType.distance;
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setSelectedSortType.type,
        payload: sortType
      };
      expect(analystSlice.actions.setSelectedSortType(sortType)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setSelectedSortType(sortType));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the selected signal detection ids', () => {
      const ids = ['1', '2', '3'];
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setSelectedSdIds.type,
        payload: ids
      };
      expect(analystSlice.actions.setSelectedSdIds(ids)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setSelectedSdIds(ids));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the selected event ids', () => {
      const ids = ['1', '2', '3'];
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setSelectedEventIds.type,
        payload: ids
      };
      expect(analystSlice.actions.setSelectedEventIds(ids)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setSelectedEventIds(ids));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the signal detections ids to show fk', () => {
      const ids = ['1', '2', '3'];
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setSdIdsToShowFk.type,
        payload: ids
      };
      expect(analystSlice.actions.setSdIdsToShowFk(ids)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setSdIdsToShowFk(ids));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the channel filters', () => {
      const filters = {
        a: {
          id: 'id',
          name: 'name',
          description: 'description',
          filterType: 'filter-type',
          filterPassBandType: 'filter-pass-band-type',
          lowFrequencyHz: 0,
          highFrequencyHz: 9,
          order: 1,
          filterSource: 'filter-source',
          filterCausality: 'filter-causality',
          zeroPhase: true,
          sampleRate: 5,
          sampleRateTolerance: 2,
          validForSampleRate: false,
          aCoefficients: [1, 2, 3],
          // eslint-disable-next-line @typescript-eslint/no-magic-numbers
          bCoefficients: [5, 6, 7],
          groupDelaySecs: 4
        }
      };
      const expectedAction: Redux.AnyAction = {
        type: analystSlice.actions.setChannelFilters.type,
        payload: filters
      };
      expect(analystSlice.actions.setChannelFilters(filters)).toEqual(expectedAction);

      store.dispatch(analystSlice.actions.setChannelFilters(filters));
      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });
  });
});
