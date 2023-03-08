/* eslint-disable react/destructuring-assignment */
import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { CacheTypes } from '@gms/common-model';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '@gms/ui-util';
import React from 'react';

import {
  DataType,
  nonIdealStateMessages,
  TableDataState,
  TableInvalidState
} from '~analyst-ui/common/utils/table-invalid-state';

import { HistoryPanel } from './history-panel';
import type { HistoryContextData, HistoryEntryPointer, HistoryProps, HistoryState } from './types';
import { HistoryContext } from './types';

// Fix in the future when converted to useEvent hook
const dummyEventsInTimeRangeQuery = {
  isLoading: false,
  data: undefined
};

/**
 * A history component, containing the UI for the event-level and
 * global-level undo/redo stacks
 */
export class HistoryComponent extends React.Component<HistoryProps, HistoryState> {
  private readonly historyContextData: HistoryContextData = {
    eventsInTimeRange: undefined,
    glContainer: this.props.glContainer,
    historyList: this.props.historyQuery.history,
    historyActionIntent: this.state.actionIntentPointer,
    openEventId: this.props.openEventId,
    historyActionInProgress: this.props.historyActionInProgress,
    redoEventHistory: async quantity => {
      await this.redoEventHistory(quantity);
    },
    redoEventHistoryById: async id => {
      await this.redoEventHistoryById(id);
    },
    redoHistory: async quantity => {
      await this.redoHistory(quantity);
    },
    redoHistoryById: async id => {
      await this.redoHistoryById(id);
    },
    setHistoryActionIntent: pointer => {
      if (pointer) {
        this.setState({
          actionIntentPointer: {
            entryId: pointer.entryId,
            entryType: pointer.entryType,
            isEventMode: pointer.isEventMode,
            isIncluded: this.generateIsIncludedFunction(pointer),
            isChangeIncluded: change => pointer.isChangeIncluded(change)
          }
        });
      } else {
        this.setState({ actionIntentPointer: undefined });
      }
    },
    undoEventHistory: async quantity => {
      await this.undoEventHistory(quantity);
    },
    undoEventHistoryById: async id => {
      await this.undoEventHistoryById(id);
    },
    undoHistory: async quantity => {
      await this.undoHistory(quantity);
    },
    undoHistoryById: async id => {
      await this.undoHistoryById(id);
    }
  };

  /**
   * Constructor
   */
  public constructor(props: HistoryProps) {
    super(props);
    this.state = {
      actionIntentPointer: undefined
    };
  }

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount(): void {
    addGlForceUpdateOnShow(this.props.glContainer, this);
    addGlForceUpdateOnResize(this.props.glContainer, this);
  }

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp, complexity
  public render(): JSX.Element {
    // If the golden-layout container is not visible, do not attempt to render
    // the component, this is to prevent JS errors that may occur when trying to
    // render the component while the golden-layout container is hidden
    if (this.props.glContainer && this.props.glContainer.isHidden) {
      return <NonIdealState />;
    }

    // eslint-disable-next-line no-nested-ternary
    const dataState: TableDataState = this.props.currentTimeInterval
      ? this.props.historyQuery &&
        this.props.historyQuery.history &&
        this.props.historyQuery.history.length > 0
        ? TableDataState.READY
        : TableDataState.NO_HISTORY
      : TableDataState.NO_INTERVAL;

    const nonIdealState =
      dataState !== TableDataState.READY ? (
        <TableInvalidState
          visual={IconNames.UNDO}
          message={dataState}
          dataType={DataType.HISTORY}
          noEventMessage={nonIdealStateMessages.history.get(dataState)}
        />
      ) : undefined;
    const eventsLoading =
      dummyEventsInTimeRangeQuery && dummyEventsInTimeRangeQuery.isLoading ? (
        <NonIdealState
          icon={IconNames.UNDO}
          action={<Spinner intent={Intent.PRIMARY} />}
          title="Loading:"
          description="Events..."
        />
      ) : undefined;
    const isNonIdealState = nonIdealState !== undefined || eventsLoading !== undefined;

    return (
      <HistoryContext.Provider value={this.historyContextData}>
        {isNonIdealState ? eventsLoading || nonIdealState : <HistoryPanel />}
      </HistoryContext.Provider>
    );
  }

  // eslint-disable-next-line class-methods-use-this
  private generateIsIncludedFunction(
    pointer: HistoryEntryPointer
  ): (entry: CacheTypes.History) => boolean {
    return pointer.isIncluded !== undefined
      ? (entry: CacheTypes.History) => pointer.isIncluded(entry)
      : (entry: CacheTypes.History) =>
          entry &&
          entry.changes &&
          entry.changes.reduce(
            (isIncluded: boolean, change: CacheTypes.HistoryChange) =>
              isIncluded || (pointer && pointer.isChangeIncluded(change)),
            false
          );
  }

  /**
   * Executes the mutation to undo event history.
   *
   * @param numberOfItems the number of items to undo (defaults to 1)
   */
  private async undoEventHistory(numberOfItems = 1) {
    const variables: { numberOfItems: number } = {
      numberOfItems
    };
    this.props.incrementHistoryActionInProgress();
    await this.props.undoEventHistory({ variables }).then(() => {
      this.props.decrementHistoryActionInProgress();
    });
  }

  /**
   * Executes the mutation to redo event history.
   *
   * @param numberOfItems the number of items to redo (defaults to 1)
   */
  private async redoEventHistory(numberOfItems = 1) {
    const variables: { numberOfItems: number } = {
      numberOfItems
    };
    this.props.incrementHistoryActionInProgress();
    await this.props.redoEventHistory({ variables }).then(() => {
      this.props.decrementHistoryActionInProgress();
    });
  }

  /**
   * Executes the mutation to undo event history by id.
   *
   * @param id the unique history id
   */
  private async undoEventHistoryById(id: string) {
    const variables: { id: string } = {
      id
    };
    await this.props.undoEventHistoryById({ variables });
  }

  /**
   * Executes the mutation to redo event history by id.
   *
   * @param id the unique history id
   */
  private async redoEventHistoryById(id: string) {
    const variables: { id: string } = {
      id
    };
    await this.props.redoEventHistoryById({ variables });
  }

  /**
   * Executes the mutation to undo event history.
   *
   * @param numberOfItems the number of items to undo (defaults to 1)
   */
  private async undoHistory(numberOfItems = 1) {
    const variables: { numberOfItems: number } = {
      numberOfItems
    };
    this.props.incrementHistoryActionInProgress();
    await this.props.undoHistory({ variables }).then(() => {
      this.props.decrementHistoryActionInProgress();
    });
  }

  /**
   * Executes the mutation to redo event history.
   *
   * @param numberOfItems the number of items to redo (defaults to 1)
   */
  private async redoHistory(numberOfItems = 1) {
    const variables: { numberOfItems: number } = {
      numberOfItems
    };
    this.props.incrementHistoryActionInProgress();
    await this.props.redoHistory({ variables }).then(() => {
      this.props.decrementHistoryActionInProgress();
    });
  }

  /**
   * Executes the mutation to undo history by id.
   *
   * @param id the unique history id
   */
  private async undoHistoryById(id: string) {
    const variables: { id: string } = {
      id
    };
    await this.props.undoHistoryById({ variables });
  }

  /**
   * Executes the mutation to redo history by id.
   *
   * @param id the unique history id
   */
  private async redoHistoryById(id: string) {
    const variables: { id: string } = {
      id
    };
    await this.props.redoHistoryById({ variables });
  }
}
