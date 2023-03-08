import { CacheTypes } from '@gms/common-model';

import { HistoryEntryAction } from '../../src/ts/components/analyst-ui/components/history/history-stack/types';
import type {
  HistoryContextData,
  HistoryEntryPointer
} from '../../src/ts/components/analyst-ui/components/history/types';

export const historyPointer: HistoryEntryPointer = {
  entryType: HistoryEntryAction.undo,
  isEventMode: false,
  isChangeIncluded: jest.fn().mockReturnValue(true),
  entryId: '2'
};

export const historyList: CacheTypes.History[] = [
  {
    id: '12345',
    description: CacheTypes.UserActionDescription.REJECT_DETECTION,
    changes: [
      {
        id: '3044534095aet34452tewd',
        active: true,
        eventId: 'earrterta9dgjdfogiesrg2451qe',
        conflictCreated: false,
        hypothesisChangeInformation: {
          id: 'aiuesdf93qtfhsadsa90dgad',
          hypothesisId: 'serysezdfgzdesrtdfxgwerts',
          type: CacheTypes.HypothesisType.SignalDetectionHypothesis,
          parentId: 'qet[esguodfgoesgdfg',
          userAction: 'Test Rejected a Signal Detection'
        }
      }
    ],
    redoPriorityOrder: 1
  },
  {
    id: '23456',
    description: CacheTypes.UserActionDescription.UPDATE_MULTIPLE_DETECTIONS_RE_PHASE,
    changes: [
      {
        id: 'eagaeugdug8equfiuad',
        active: true,
        eventId: 'aesfdsigjodgfeg',
        conflictCreated: false,
        hypothesisChangeInformation: {
          id: 'adgiagidjoaeg',
          hypothesisId: 'adsogdjgoidfjgasdfsdf',
          type: CacheTypes.HypothesisType.SignalDetectionHypothesis,
          parentId: 'oiergoeijgoiejg',
          userAction: CacheTypes.UserActionDescription.UPDATE_EVENT_FROM_SIGNAL_DETECTION_CHANGE
        }
      },
      {
        id: 'sdfgsdgrsthrh',
        active: true,
        eventId: 'rtyrthhgsrhh',
        conflictCreated: false,
        hypothesisChangeInformation: {
          id: 'sergrthdthfhrg',
          hypothesisId: 'rtgsrtgrsthr',
          type: CacheTypes.HypothesisType.SignalDetectionHypothesis,
          parentId: 'htrshrhtrtstgrt',
          userAction: CacheTypes.UserActionDescription.UPDATE_DETECTION_RE_PHASE
        }
      },
      {
        id: '1[urq3oetj980u34q4tr',
        active: false,
        eventId: '134ijq3tioq43pt',
        conflictCreated: false,
        hypothesisChangeInformation: {
          id: 'qoiejgqoi3j134jt',
          hypothesisId: '3iqtoegjeaoir',
          type: CacheTypes.HypothesisType.SignalDetectionHypothesis,
          parentId: 'qoergo3ut834gq3',
          userAction: CacheTypes.UserActionDescription.UPDATE_DETECTION_RE_PHASE
        }
      },
      {
        id: '1[earjgoisedfjgdfgj',
        active: true,
        eventId: 'dfgijsldgjfdohijosrh',
        conflictCreated: false,
        hypothesisChangeInformation: {
          id: 'eroigjsfodgijsroigj',
          hypothesisId: 'srtohkrphrp',
          type: CacheTypes.HypothesisType.SignalDetectionHypothesis,
          parentId: 'spogjkpsohkpo',
          userAction: CacheTypes.UserActionDescription.CREATE_EVENT
        }
      }
    ],
    redoPriorityOrder: 1
  },
  {
    id: 'w4542653636536',
    description: CacheTypes.UserActionDescription.REJECT_DETECTION,
    changes: [
      {
        id: 'sfhofskhprtoh',
        active: false,
        eventId: 'srhjrstlhrt',
        conflictCreated: true,
        hypothesisChangeInformation: {
          id: 'rthrstphohkpr',
          hypothesisId: 'trshrthsrtth',
          type: CacheTypes.HypothesisType.SignalDetectionHypothesis,
          parentId: 'rthoshijostr',
          userAction: 'Test Conflicted Signal Detection'
        }
      }
    ],
    redoPriorityOrder: 1
  }
];

export const providerState: HistoryContextData = {
  glContainer: { width: 500 } as any,
  openEventId: '1',
  eventsInTimeRange: [],
  undoEventHistory: jest.fn(),
  undoEventHistoryById: jest.fn(),
  redoEventHistory: jest.fn(),
  redoEventHistoryById: jest.fn(),
  undoHistory: jest.fn(),
  undoHistoryById: jest.fn(),
  redoHistory: jest.fn(),
  redoHistoryById: jest.fn(),
  historyActionIntent: historyPointer,
  setHistoryActionIntent: jest.fn(),
  historyActionInProgress: 0,
  historyList
};
