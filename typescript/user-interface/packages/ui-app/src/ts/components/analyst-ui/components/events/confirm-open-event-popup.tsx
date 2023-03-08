/* eslint-disable @blueprintjs/classes-constants */
import { Button, Dialog, Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { Logger } from '@gms/common-util/lib/common-util/logger';
import type { AppDispatch, EventStatus } from '@gms/ui-state';
import { analystActions, useAppDispatch, useAppSelector, useEventStatusQuery } from '@gms/ui-state';
import React from 'react';

import { useSetOpenEvent } from './events-util';

/**
 * The type of the props for the {@link ComponentName} component
 */
export interface ConfirmOpenEventPopupProps {
  isCurrentlyOpen: boolean;
  setIsCurrentlyOpen: (isCurrentlyOpen: boolean) => void;
  eventId: string;
  setEventId: (eventId: string) => void;
  parentComponentId: string;
}

/**
 * Ensure we can handle the shape of the event status object
 * and parse out the active analysts
 *
 *
 * @param obj event status object
 * @param eventId event id
 * @returns active analysts or undefined
 */
export const parseEventQueryResultAsRawObject = (
  obj: unknown,
  eventId: string
): string[] | undefined => {
  if (obj !== null && obj !== undefined) {
    const keys = [...Object.keys(obj)];
    let eventStatusObject: EventStatus;
    if (keys.includes(eventId)) {
      eventStatusObject = (obj[eventId] as unknown) as EventStatus;
      const eventObjectKeys = [...Object.keys(eventStatusObject)];
      if (eventObjectKeys.includes('eventStatusInfo')) {
        const eventStatusInfoObject = eventStatusObject.eventStatusInfo;
        return eventStatusInfoObject.activeAnalystIds;
      }
      return undefined;
    }
  }
  return undefined;
};

/**
 * Update redux state indicating that an analyst requested to open an event from either the events list or map
 *
 * @param dispatch AppDispatch
 * @param parentComponentId the parent component in which the popup component exists, events list or map
 */
export const updateEventOpenTriggeredState = (
  dispatch: AppDispatch,
  parentComponentId: string
): void => {
  if (parentComponentId === 'event-list') {
    dispatch(analystActions.setEventListOpenTriggered(false));
  }
  if (parentComponentId === 'map') {
    dispatch(analystActions.setMapOpenTriggered(false));
  }
};

/**
 * Check to see which popup component we should display,
 * either the popup from the events list or map component.
 * We don't want to display both popups.
 *
 * @param eventListOpenTriggered value obtained from redux
 * @param mapOpenTriggered value obtained from redux
 * @param parentComponentId the parent component id
 * @returns returns true or false, indicates whether or not a popup should be displayed
 */
export const openEventTriggered = (
  eventListOpenTriggered: boolean,
  mapOpenTriggered: boolean,
  parentComponentId: string
): boolean => {
  return (
    (eventListOpenTriggered && parentComponentId === 'event-list') ||
    (mapOpenTriggered && parentComponentId === 'map')
  );
};

/**
 * Determines if we should auto open the event without displaying the popup
 *
 * @param activeAnalysts active analysts on an event
 * @param userName current user
 * @param eventId event id
 * @returns true or false
 */
export const proceedToAutoOpenEvent = (
  activeAnalysts: string[],
  userName: string,
  eventId: string
): boolean => {
  return (
    (activeAnalysts?.length === 0 ||
      (activeAnalysts?.length === 1 && activeAnalysts?.includes(userName))) &&
    eventId !== undefined
  );
};

/**
 * Get the verb, e.g. is or are, for the confirmation warning message
 * depending on how many analysts are currently reviewing an event
 *
 * @param filteredActiveAnalysts string array of active analysts
 * @returns a string
 */
export const getConfirmationWarningVerb = (filteredActiveAnalysts: string[]): string => {
  return filteredActiveAnalysts?.length > 1 ? ' are ' : ' is ';
};

/**
 * Callback triggered on dialog close or cancel
 *
 * @param dispatch AppDispatch
 * @param parentComponentId parent component id from which the open event was triggered
 * @param setIsCurrentlyOpen callback to set dialog visibility
 * @param setEventId callback to the current event id
 */
export const onCloseCallback = (
  dispatch: AppDispatch,
  parentComponentId: string,
  setIsCurrentlyOpen: (isCurrentlyOpen: boolean) => void,
  setEventId: (eventId: string) => void
): void => {
  updateEventOpenTriggeredState(dispatch, parentComponentId);
  setEventId(undefined);
  setIsCurrentlyOpen(false);
};

/**
 * Provide a warning to users attempting to open an event that other users may be currently refining it
 */
export function ConfirmOpenEventPopup(props: ConfirmOpenEventPopupProps) {
  // popover content gets no padding by default; add the "bp4-popover2-content-sizing"
  // class to the popover to set nice padding between its border and content.
  const { eventId, setEventId, isCurrentlyOpen, setIsCurrentlyOpen, parentComponentId } = props;
  const openEvent = useSetOpenEvent();
  const useEventStatusQueryResultQuery = useEventStatusQuery();
  const logger = Logger.create(
    'GMS_CONFIRM_OPEN_EVENT_POPUP',
    process.env.GMS_CONFIRM_OPEN_EVENT_POPUP
  );
  const dispatch = useAppDispatch();

  const activeAnalysts = React.useMemo(() => {
    const parsedResult = parseEventQueryResultAsRawObject(
      useEventStatusQueryResultQuery.data,
      eventId
    );
    return parsedResult !== undefined
      ? parsedResult
      : useEventStatusQueryResultQuery.data?.eventStatusInfoMap?.[eventId]?.activeAnalystIds;
  }, [eventId, useEventStatusQueryResultQuery.data]);

  const userName = useAppSelector(state => state.app.userSession.authenticationStatus.userName);
  const eventListOpenTriggered = useAppSelector(
    state => state.app.analyst.eventListOpenEventTriggered
  );
  const mapOpenTriggered = useAppSelector(state => state.app.analyst.mapOpenEventTriggered);
  React.useEffect(() => {
    if (openEventTriggered(eventListOpenTriggered, mapOpenTriggered, parentComponentId)) {
      if (useEventStatusQueryResultQuery.isSuccess) {
        // This should occur when no other analysts have the selected event open.
        if (proceedToAutoOpenEvent(activeAnalysts, userName, eventId)) {
          openEvent(eventId)
            .then(() => {
              logger.debug('confirm open event popover eventId: ', eventId);
            })
            .catch(e => {
              logger.error('Error opening event', e);
            })
            .finally(() => {
              updateEventOpenTriggeredState(dispatch, parentComponentId);
            });
        }
        // This should open the confirmation popup because another analyst has the event open.
        else if (activeAnalysts?.length > 0) {
          setIsCurrentlyOpen(true);
        }
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    eventId,
    useEventStatusQueryResultQuery.isSuccess,
    activeAnalysts,
    eventListOpenTriggered,
    mapOpenTriggered
  ]);

  const filteredActiveAnalysts = activeAnalysts?.filter(username => username !== userName);
  const usersString = filteredActiveAnalysts?.join(', ');
  return (
    <Dialog
      isOpen={isCurrentlyOpen}
      canEscapeKeyClose
      canOutsideClickClose
      title="Open Event Confirmation"
      className="dialog_parent dialog_parent--wide"
      onClose={() => onCloseCallback(dispatch, parentComponentId, setIsCurrentlyOpen, setEventId)}
    >
      <div className="dialog dialog__container bp4-popover-content-dialog-container-left-right-padding">
        <div className="interval-confirmation-contents">
          <div className="interval-confirmation-text">
            <b>{usersString}</b>
            {getConfirmationWarningVerb(filteredActiveAnalysts)}
            already working this event.
            <br />
            Are you sure you want to open it?
          </div>
          <Icon
            icon={IconNames.ERROR}
            className="bp4-popover-content-icon-margin-left"
            iconSize={48}
          />
        </div>
        <div className="dialog__controls">
          <div className="dialog-actions">
            <Button
              onClick={() => {
                updateEventOpenTriggeredState(dispatch, parentComponentId);
                openEvent(eventId).catch(e => logger.error('Error opening event', e));
                setIsCurrentlyOpen(false);
              }}
              text="Open Anyway"
            />
          </div>
          <Button
            onClick={() =>
              onCloseCallback(dispatch, parentComponentId, setIsCurrentlyOpen, setEventId)
            }
            intent="primary"
            text="Cancel"
          />
        </div>
      </div>
    </Dialog>
  );
}

/**
 * Determines whether or not we should re-render the component
 *
 * @param prevProps previous component props
 * @param nextProps current component props
 * @returns true or false
 */
export const ianPopupComponentMemoCheck = (
  prevProps: ConfirmOpenEventPopupProps,
  nextProps: ConfirmOpenEventPopupProps
): boolean => {
  // if false, reload
  if (prevProps.eventId !== nextProps.eventId) return false;
  if (prevProps.setEventId !== nextProps.setEventId) return false;
  if (prevProps.isCurrentlyOpen !== nextProps.isCurrentlyOpen) return false;

  return true;
};

/**
 * Exported open event confirmation notice component
 */
export const IANConfirmOpenEventPopup = React.memo(
  ConfirmOpenEventPopup,
  ianPopupComponentMemoCheck
);
