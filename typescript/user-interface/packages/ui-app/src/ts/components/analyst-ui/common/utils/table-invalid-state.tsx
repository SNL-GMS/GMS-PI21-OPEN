import type { IconName } from '@blueprintjs/core';
import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import { capitalizeFirstLetters } from '@gms/common-util';
import React from 'react';

/**
 * Invalid state props for invalid state component
 */
export interface TableInvalidStateProps {
  message: TableDataState;
  dataType: DataType;
  noEventMessage?: string;
  visual?: IconName | JSX.Element;
}

/**
 * Table Data state for invalid state logic
 */
export enum TableDataState {
  NO_EVENTS,
  NO_SDS,
  NO_EVENT_OPEN,
  NO_HISTORY,
  NO_INTERVAL,
  READY
}

/**
 * Data type for invalid state
 */
export enum DataType {
  EVENT = 'event',
  HISTORY = 'undo/redo snapshot',
  SD = 'signal detection'
}

export const nonIdealStateMessages = {
  title: new Map([
    [TableDataState.NO_SDS, 'Loading:'],
    [TableDataState.NO_HISTORY, 'No event selected'],
    [TableDataState.NO_EVENTS, 'Loading:'],
    [TableDataState.NO_EVENT_OPEN, 'No undo/redo history available']
  ]),
  history: new Map([
    [TableDataState.NO_INTERVAL, 'Open an interval to view history'],
    [TableDataState.NO_HISTORY, 'Perform an action to see undo/redo entries']
  ])
};

function getDescription(props: TableInvalidStateProps) {
  switch (props.message) {
    case TableDataState.NO_SDS:
    case TableDataState.NO_EVENTS:
      return `${capitalizeFirstLetters(props.dataType)}s...`;
    case TableDataState.NO_INTERVAL:
      return `Open interval to load ${props.dataType}s`;
    case TableDataState.NO_EVENT_OPEN:
    case TableDataState.NO_HISTORY:
      return props.noEventMessage ? props.noEventMessage : 'No Message to Display';
    default:
      return 'No Message to Display';
  }
}
/**
 * Handles invalid state rendering for event/sd/location/magnitude table (more to come)
 *
 * @param props props for non ideal state/spinner
 */
export function TableInvalidState(props: TableInvalidStateProps) {
  const { visual, message, dataType } = props;
  return (
    <NonIdealState
      icon={visual}
      action={
        message === TableDataState.NO_SDS || message === TableDataState.NO_EVENTS ? (
          <Spinner intent={Intent.PRIMARY} />
        ) : null
      }
      title={
        message === TableDataState.NO_INTERVAL
          ? `No ${dataType}s loaded`
          : nonIdealStateMessages.title.get(message)
      }
      description={getDescription(props)}
    />
  );
}
