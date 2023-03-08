import type { AppState } from '@gms/ui-state';
import { getStore, useAppDispatch } from '@gms/ui-state';
import { appState } from '@gms/ui-state/__tests__/test-util';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import type { ConfirmOpenEventPopupProps } from '../../../../../src/ts/components/analyst-ui/components/events/confirm-open-event-popup';
import {
  getConfirmationWarningVerb,
  IANConfirmOpenEventPopup,
  ianPopupComponentMemoCheck,
  onCloseCallback,
  openEventTriggered,
  parseEventQueryResultAsRawObject,
  proceedToAutoOpenEvent,
  updateEventOpenTriggeredState
} from '../../../../../src/ts/components/analyst-ui/components/events/confirm-open-event-popup';

jest.mock('@gms/ui-state', () => {
  const actualRedux = jest.requireActual('@gms/ui-state');
  const mockDispatchFunc = jest.fn();
  const mockDispatch = () => mockDispatchFunc;
  const mockUseAppDispatch = jest.fn(mockDispatch);
  return {
    ...actualRedux,
    useAppDispatch: mockUseAppDispatch,
    useAppSelector: jest.fn((stateFunc: (state: AppState) => any) => {
      const state: AppState = appState;
      state.app.analyst.mapOpenEventTriggered = true;
      return stateFunc(state);
    }),
    useEventStatusQuery: jest.fn(() => ({
      isSuccess: true,
      data: {
        '7cce53cf-9057-3442-b717-20a370c3c723': {
          stageId: {
            name: 'AL1'
          },
          eventId: '7cce53cf-9057-3442-b717-20a370c3c723',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        '9ac403da-7947-3183-884c-18a67d3aa8de': {
          stageId: {
            name: 'AL1'
          },
          eventId: '9ac403da-7947-3183-884c-18a67d3aa8de',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        '31b3b31a-1c2f-3a37-8206-f111127c0dbd': {
          stageId: {
            name: 'AL1'
          },
          eventId: '31b3b31a-1c2f-3a37-8206-f111127c0dbd',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        '1587965f-b4d4-35af-a842-8a4a024feb0d': {
          stageId: {
            name: 'AL1'
          },
          eventId: '1587965f-b4d4-35af-a842-8a4a024feb0d',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        'eddb904a-6db7-3375-9d28-57aacadb1cb0': {
          stageId: {
            name: 'AL1'
          },
          eventId: 'eddb904a-6db7-3375-9d28-57aacadb1cb0',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        '08fe2621-d8e7-36b0-aec0-da35256a998d': {
          stageId: {
            name: 'AL1'
          },
          eventId: '08fe2621-d8e7-36b0-aec0-da35256a998d',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        'fed33392-d3a4-3aa1-89a8-7a38b875ba4a': {
          stageId: {
            name: 'AL1'
          },
          eventId: 'fed33392-d3a4-3aa1-89a8-7a38b875ba4a',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        '7f975a56-c761-3b65-86ec-a0b37ce6ec87': {
          stageId: {
            name: 'AL1'
          },
          eventId: '7f975a56-c761-3b65-86ec-a0b37ce6ec87',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        'aa68c75c-4a77-387f-97fb-686b2f068676': {
          stageId: {
            name: 'AL1'
          },
          eventId: 'aa68c75c-4a77-387f-97fb-686b2f068676',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        '58c54802-a9fb-3526-8d09-23353a34a7ae': {
          stageId: {
            name: 'AL1'
          },
          eventId: '58c54802-a9fb-3526-8d09-23353a34a7ae',
          eventStatusInfo: {
            eventStatus: 'NOT_STARTED',
            activeAnalystIds: []
          }
        },
        'fba9d881-64f3-32d9-909e-e770223212a0': {
          stageId: {
            name: 'AL1'
          },
          eventId: 'fba9d881-64f3-32d9-909e-e770223212a0',
          eventStatusInfo: {
            eventStatus: 'IN_PROGRESS',
            activeAnalystIds: ['John']
          }
        }
      }
    }))
  };
});

describe('Confirm Open Event Popup', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });
  it('exists', () => {
    expect(IANConfirmOpenEventPopup).toBeDefined();
  });

  // we expect this snapshot to be an empty div because the open event popup is not currently open
  it('matches a snapshot of the popup that is not currently open', () => {
    const mockFunction = jest.fn();
    const { container, unmount } = render(
      <Provider store={getStore()}>
        <IANConfirmOpenEventPopup
          isCurrentlyOpen={false}
          setIsCurrentlyOpen={mockFunction}
          eventId=""
          setEventId={mockFunction}
          parentComponentId=""
        />
      </Provider>
    );
    expect(container).toMatchSnapshot();
    unmount();
  });

  it('parses event query results and returns an analyst collection given an event id', () => {
    const eventId = '58c54802-a9fb-3526-8d09-23353a34a7ae';
    const eventStatus = {
      '58c54802-a9fb-3526-8d09-23353a34a7ae': {
        stageId: {
          name: 'AL1'
        },
        eventId: '58c54802-a9fb-3526-8d09-23353a34a7ae',
        eventStatusInfo: {
          eventStatus: 'NOT_STARTED',
          activeAnalystIds: []
        }
      }
    };
    const expected = [];
    expect(expected).toEqual(parseEventQueryResultAsRawObject(eventStatus, eventId));
  });
  it('parses event query results and returns undefined given an event id not in event status', () => {
    const eventId = 'not in event status';
    const eventStatus = {
      '58c54802-a9fb-3526-8d09-23353a34a7ae': {
        stageId: {
          name: 'AL1'
        },
        eventId: '58c54802-a9fb-3526-8d09-23353a34a7ae',
        eventStatusInfo: {
          eventStatus: 'NOT_STARTED',
          activeAnalystIds: []
        }
      }
    };
    const expected = undefined;
    expect(expected).toEqual(parseEventQueryResultAsRawObject(eventStatus, eventId));
  });
  it('parses event query results and returns undefined when event status info is unavailable', () => {
    const eventId = '58c54802-a9fb-3526-8d09-23353a34a7ae';
    const eventStatus = {
      '58c54802-a9fb-3526-8d09-23353a34a7ae': {
        stageId: {
          name: 'AL1'
        },
        eventId: '58c54802-a9fb-3526-8d09-23353a34a7ae'
      }
    };
    const expected = undefined;
    expect(expected).toEqual(parseEventQueryResultAsRawObject(eventStatus, eventId));
  });
  it('parses event query results and returns undefined given an undefined event status', () => {
    const eventId = '58c54802-a9fb-3526-8d09-23353a34a7ae';
    const eventStatus = undefined;
    const expected = undefined;
    expect(expected).toEqual(parseEventQueryResultAsRawObject(eventStatus, eventId));
  });
  it('parses event query results and returns undefined given a null event status', () => {
    const eventId = '58c54802-a9fb-3526-8d09-23353a34a7ae';
    const eventStatus = null;
    const expected = undefined;
    expect(expected).toEqual(parseEventQueryResultAsRawObject(eventStatus, eventId));
  });
  it('knows when it has an open event triggered from the event list', () => {
    const eventListOpenTriggered = true;
    const mapOpenTriggered = false;
    const parentComponentId = 'event-list';
    const expected = true;
    expect(expected).toEqual(
      openEventTriggered(eventListOpenTriggered, mapOpenTriggered, parentComponentId)
    );
  });
  it('does not have an open event triggered from the event list', () => {
    const eventListOpenTriggered = false;
    const mapOpenTriggered = false;
    const parentComponentId = 'event-list';
    const expected = false;
    expect(expected).toEqual(
      openEventTriggered(eventListOpenTriggered, mapOpenTriggered, parentComponentId)
    );
  });
  it('knows when it has an open event triggered from the map', () => {
    const eventListOpenTriggered = false;
    const mapOpenTriggered = true;
    const parentComponentId = 'map';
    const expected = true;
    expect(expected).toEqual(
      openEventTriggered(eventListOpenTriggered, mapOpenTriggered, parentComponentId)
    );
  });
  it('does not have an open event triggered from the map', () => {
    const eventListOpenTriggered = false;
    const mapOpenTriggered = false;
    const parentComponentId = 'map';
    const expected = false;
    expect(expected).toEqual(
      openEventTriggered(eventListOpenTriggered, mapOpenTriggered, parentComponentId)
    );
  });
  it('will proceed to auto open an event when there are no active analysts', () => {
    const activeAnalysts = [];
    const userName = 'User';
    const eventId = '58c54802-a9fb-3526-8d09-23353a34a7ae';
    const expected = true;
    expect(expected).toEqual(proceedToAutoOpenEvent(activeAnalysts, userName, eventId));
  });
  it('will proceed to auto open an event when there are active analysts and one of them is the requesting analyst', () => {
    const activeAnalysts = ['User'];
    const userName = 'User';
    const eventId = '58c54802-a9fb-3526-8d09-23353a34a7ae';
    const expected = true;
    expect(expected).toEqual(proceedToAutoOpenEvent(activeAnalysts, userName, eventId));
  });
  it('will not proceed to auto open an event when there are active analysts', () => {
    const activeAnalysts = ['User1'];
    const userName = 'User2';
    const eventId = '58c54802-a9fb-3526-8d09-23353a34a7ae';
    const expected = false;
    expect(expected).toEqual(proceedToAutoOpenEvent(activeAnalysts, userName, eventId));
  });
  it('will update event list open triggered', () => {
    const parentComponentId = 'event-list';
    const mockDispatch = useAppDispatch();
    updateEventOpenTriggeredState(mockDispatch, parentComponentId);
    expect(mockDispatch).toHaveBeenCalledTimes(1);
  });
  it('will update map open triggered', () => {
    const parentComponentId = 'map';
    const mockDispatch = useAppDispatch();
    updateEventOpenTriggeredState(mockDispatch, parentComponentId);
    expect(mockDispatch).toHaveBeenCalledTimes(1);
  });
  it('will call the on close callback', () => {
    const parentComponentId = 'map';
    const mockDispatch = useAppDispatch();
    const setEventId = jest.fn();
    const setIsCurrentlyOpen = jest.fn();
    onCloseCallback(mockDispatch, parentComponentId, setIsCurrentlyOpen, setEventId);
    expect(setEventId).toHaveBeenCalledWith(undefined);
    expect(setIsCurrentlyOpen).toHaveBeenCalledWith(false);
    expect(mockDispatch).toHaveBeenCalledTimes(1);
  });
  it('matches a snapshot of the popup that is currently open', () => {
    const mockFunction = jest.fn();
    const { container, unmount } = render(
      <Provider store={getStore()}>
        <IANConfirmOpenEventPopup
          isCurrentlyOpen
          setIsCurrentlyOpen={mockFunction}
          eventId="fba9d881-64f3-32d9-909e-e770223212a0"
          setEventId={mockFunction}
          parentComponentId="map"
        />
      </Provider>,
      { container: document.body }
    );
    expect(container).toMatchSnapshot();
    unmount();
  });
  it('ianPopupComponentMemoCheck returns false when event id changes', () => {
    const prevProps: ConfirmOpenEventPopupProps = {
      isCurrentlyOpen: false,
      setIsCurrentlyOpen: jest.fn(),
      eventId: '1',
      setEventId: jest.fn(),
      parentComponentId: 'map'
    };
    const nextProps: ConfirmOpenEventPopupProps = {
      isCurrentlyOpen: false,
      setIsCurrentlyOpen: jest.fn(),
      eventId: '2',
      setEventId: jest.fn(),
      parentComponentId: 'map'
    };
    expect(ianPopupComponentMemoCheck(prevProps, nextProps)).toBe(false);
  });
  it('ianPopupComponentMemoCheck returns false when is currently open changes', () => {
    const prevProps: ConfirmOpenEventPopupProps = {
      isCurrentlyOpen: false,
      setIsCurrentlyOpen: jest.fn(),
      eventId: '1',
      setEventId: jest.fn(),
      parentComponentId: 'map'
    };
    const nextProps: ConfirmOpenEventPopupProps = {
      isCurrentlyOpen: true,
      setIsCurrentlyOpen: jest.fn(),
      eventId: '1',
      setEventId: jest.fn(),
      parentComponentId: 'map'
    };
    expect(ianPopupComponentMemoCheck(prevProps, nextProps)).toBe(false);
  });
  it('ianPopupComponentMemoCheck returns false when set event id changes', () => {
    const prevProps: ConfirmOpenEventPopupProps = {
      isCurrentlyOpen: false,
      setIsCurrentlyOpen: jest.fn(),
      eventId: '1',
      setEventId: jest.fn(),
      parentComponentId: 'map'
    };
    const nextProps: ConfirmOpenEventPopupProps = {
      isCurrentlyOpen: true,
      setIsCurrentlyOpen: jest.fn(),
      eventId: '1',
      setEventId: jest.fn().mockImplementation(() => '2'),
      parentComponentId: 'map'
    };
    expect(ianPopupComponentMemoCheck(prevProps, nextProps)).toBe(false);
  });
  it('can set the confirmation warning verb to is if there is only one active analyst', () => {
    const expected = ' is ';
    const filteredActiveAnalysts = ['John'];
    expect(getConfirmationWarningVerb(filteredActiveAnalysts)).toEqual(expected);
  });
  it('can set the confirmation warning verb to are if there are more than one active analyst', () => {
    const expected = ' are ';
    const filteredActiveAnalysts = ['John', 'Jane'];
    expect(getConfirmationWarningVerb(filteredActiveAnalysts)).toEqual(expected);
  });
});
