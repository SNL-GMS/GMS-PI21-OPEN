import { CacheTypes } from '@gms/common-model';

import { HistoryEntryAction } from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/types';
import type { HistoryEntryPointer } from '../../../../../src/ts/components/analyst-ui/components/history/types';
import * as Utils from '../../../../../src/ts/components/analyst-ui/components/history/utils/history-utils';
import { historyList } from '../../../../__data__/history-data';
import { testPermutationsFalsy, testPermutationsUndefined } from '../../../../utils/general-utils';

describe('history utils', () => {
  const firstEventId = historyList[0].changes[0].eventId;
  test('can get number of undos', () => {
    expect(Utils.getNumberOfUndos).toBeDefined();
    expect(Utils.getNumberOfUndos(historyList)).toEqual(2);
  });

  test('can get number of redos', () => {
    expect(Utils.getNumberOfRedos).toBeDefined();
    expect(Utils.getNumberOfRedos(historyList)).toBeGreaterThan(0);
  });

  test('can determine if an entry does or does not affect an event', () => {
    expect(Utils.doesEntryAffectEvent).toBeDefined();
    expect(Utils.doesEntryAffectEvent(historyList[0], firstEventId)).toBeTruthy();
    expect(Utils.doesEntryAffectEvent(historyList[1], firstEventId)).toBeFalsy();
  });

  test('can determine if a change does or does not affect an event', () => {
    expect(Utils.doesChangeAffectEvent).toBeDefined();
    expect(Utils.doesChangeAffectEvent(historyList[0].changes[0], firstEventId)).toBeTruthy();
    expect(Utils.doesChangeAffectEvent(historyList[1].changes[0], firstEventId)).toBeFalsy();
  });

  test('can get the index of the last included of a type', () => {
    const isIncluded = jest.fn();
    isIncluded.mockReturnValue(true);
    expect(Utils.getIndexOfLastIncludedOfType).toBeDefined();

    const testParams = [historyList, isIncluded, HistoryEntryAction.undo];
    testPermutationsUndefined(Utils.getIndexOfLastIncludedOfType, testParams);

    expect(
      Utils.getIndexOfLastIncludedOfType(historyList, isIncluded, HistoryEntryAction.undo)
    ).toBeGreaterThan(-1);
    expect(
      Utils.getIndexOfLastIncludedOfType(historyList, isIncluded, HistoryEntryAction.undo)
    ).toBeGreaterThan(-1);
    expect(Utils.getIndexOfLastIncludedOfType([], isIncluded, HistoryEntryAction.redo)).toEqual(-1);
    expect(isIncluded).toHaveBeenCalled();
  });

  test('can get the last included of a type', () => {
    const isIncluded = jest.fn();
    isIncluded.mockReturnValue(true);
    expect(Utils.getLastIncludedOfType).toBeDefined();

    const testParams = [historyList, isIncluded, HistoryEntryAction.undo];
    testPermutationsUndefined(Utils.getLastIncludedOfType, testParams);

    expect(
      Utils.getLastIncludedOfType(historyList, isIncluded, HistoryEntryAction.undo)
    ).toBeDefined();
    expect(
      Utils.getLastIncludedOfType(historyList, isIncluded, HistoryEntryAction.undo).id
    ).toEqual(historyList[1].id);
    expect(isIncluded).toHaveBeenCalled();
  });

  test('can get next ordered redo action from history list', () => {
    expect(Utils.getNextOrderedRedo).toBeDefined();

    expect(Utils.getNextOrderedRedo(undefined)).toBeUndefined();

    const nextRedo = Utils.getNextOrderedRedo(historyList);
    expect(nextRedo).toBeDefined();
    expect(nextRedo.redoPriorityOrder).toEqual(1);
  });

  test('can check if a history change performs an action', () => {
    expect(Utils.canPerformAction).toBeDefined();

    const historyChanges = historyList[1].changes;

    const testParams = [historyChanges, HistoryEntryAction.undo];
    testPermutationsUndefined(Utils.canPerformAction, testParams);

    expect(Utils.canPerformAction(historyChanges, HistoryEntryAction.undo)).toBeTruthy();
    expect(Utils.canPerformAction(historyChanges, HistoryEntryAction.redo)).toBeTruthy();
    expect(Utils.canPerformAction([historyChanges[0]], HistoryEntryAction.redo)).toBeFalsy();
  });

  test('can get history action type from history', () => {
    expect(Utils.getHistoryActionType).toBeDefined();

    const historyEntry = historyList[1];
    expect(Utils.getHistoryActionType(historyEntry)).toEqual(HistoryEntryAction.undo);
  });

  test('can get a history index from a history list', () => {
    expect(Utils.getHistoryIndex).toBeDefined();

    expect(Utils.getHistoryIndex(undefined, 'arbitrary string')).toEqual(-1);
    expect(Utils.getHistoryIndex(historyList, undefined)).toEqual(-1);
    expect(Utils.getHistoryIndex(historyList, historyList[1].id)).toEqual(1);
  });

  test('can determine if a history change is affected by an action', () => {
    expect(Utils.isAffected).toBeDefined();

    const mockChangeIncluded = jest.fn().mockReturnValue(true);
    const historyPointer: HistoryEntryPointer = {
      entryType: HistoryEntryAction.undo,
      isEventMode: false,
      isChangeIncluded: mockChangeIncluded,
      entryId: historyList[1].id
    };
    const testChange = historyList[0].changes[0];
    const historyIndex = Utils.getHistoryIndex(historyList, historyPointer.entryId);
    testPermutationsFalsy(Utils.isAffected, [testChange, 0, historyPointer, historyList]);

    expect(Utils.isAffected(testChange, historyIndex, historyPointer, historyList)).toEqual(true);
    expect(Utils.isAffected(testChange, 0, historyPointer, historyList)).toEqual(false);
  });

  test('can filter out irrelevant changes', () => {
    expect(Utils.filterIrrelevantChanges).toBeDefined();

    expect(Utils.filterIrrelevantChanges(undefined)).toBeUndefined();
    expect(Utils.filterIrrelevantChanges(historyList[1]).changes).toHaveLength(
      historyList[1].changes.length - 1
    );
  });

  test('can format create event history', () => {
    expect(Utils.formatCreateEventHistory).toBeDefined();

    expect(Utils.formatCreateEventHistory(undefined)).toBeUndefined();

    const formattedEntry = Utils.formatCreateEventHistory(historyList[1]);
    expect(formattedEntry).toBeTruthy();
    expect(formattedEntry.changes).toHaveLength(historyList[1].changes.length - 1);
    expect(formattedEntry.description).toEqual(CacheTypes.UserActionDescription.CREATE_EVENT);
  });
});
