import type { SystemMessageTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import type { CellRendererParams } from '@gms/ui-core-components';
import type { ColumnDefinition } from '@gms/ui-core-components/lib/components/table/types/column-definition';
import type { SystemMessageDefinitionsQueryProps, SystemMessageSubscription } from '@gms/ui-state';
import type Immutable from 'immutable';
import type * as React from 'react';

export type SystemMessageColumnDefinition = ColumnDefinition<
  { id: string; severity: string },
  any,
  any,
  any,
  any
>;

export interface SystemMessageInfiniteTableDataType {
  id: string;
  severity: string;
}

interface SystemMessageBaseProps extends SystemMessageSubscription.SystemMessageReduxProps {
  glContainer?: GoldenLayout.Container;
}

/**
 *  The system message component props
 */
export type SystemMessageProps = SystemMessageBaseProps & SystemMessageDefinitionsQueryProps;

/**
 * The system message toolbar component props
 */
export interface SystemMessageToolbarProps
  extends SystemMessageSubscription.SystemMessageReduxProps {
  /** true if auto scrolling is enabled for the system message table; false otherwise */
  isAutoScrollingEnabled: boolean;
  /** enables or disables auto scrolling for the system message table */
  setIsAutoScrollingEnabled: React.Dispatch<React.SetStateAction<boolean>>;
  /** true if sound is enabled for system messages; false otherwise */
  isSoundEnabled: boolean;
  /** enables or disables sounds for system messages */
  setIsSoundEnabled: React.Dispatch<React.SetStateAction<boolean>>;

  systemMessageDefinitions: SystemMessageTypes.SystemMessageDefinition[];

  severityFilterMap: Immutable.Map<SystemMessageTypes.SystemMessageSeverity, boolean>;
  setSeverityFilterMap(m: Immutable.Map<SystemMessageTypes.SystemMessageSeverity, boolean>): void;
}

/**
 * The system message table props
 */
export interface SystemMessageTableProps {
  systemMessages: SystemMessageTypes.SystemMessage[];

  /** true if auto scrolling is enabled for the system message table; false otherwise */
  isAutoScrollingEnabled: boolean;

  severityFilterMap: Immutable.Map<SystemMessageTypes.SystemMessageSeverity, boolean>;

  /** enables or disables auto scrolling for the system message table */
  setIsAutoScrollingEnabled: React.Dispatch<React.SetStateAction<boolean>>;

  /**
   *  Adds system messages to the redux store
   *
   * @param messages the system messages to add
   * @param limit (optional) limit the number of messages in the redux state
   * when adding new messages; if set when adding new messages the message list
   * result will not be larger than the size of the `limit` specified.
   * @param pageSizeBuffer (optional) the size of the page buffer; if specified and the
   * limit is reached then messages will be removed at increments of the page buffer size
   */
  addSystemMessages(
    messages: SystemMessageTypes.SystemMessage[],
    limit?: number,
    pageSizeBuffer?: number
  ): void;

  /** Clears (removes) all system messages from the redux store */
  clearAllSystemMessages(): void;
}

export interface SystemMessageTableState {
  /**
   * The system messages, derived from props
   */
  systemMessages: SystemMessageTypes.SystemMessage[];

  /**
   * In order to keep track of the direction in which we've scrolled, we keep
   * track of the last two scroll positions (px from top of scroll container).
   */
  prevScrollPositions: [number, number];

  /**
   * Whether we've scrolled to the latest row or not.
   */
  isScrolledToLatest: boolean;
}

export type BaseCellRendererParams = CellRendererParams<
  { id: string },
  unknown,
  number,
  unknown,
  {
    value: string | number;
    formattedValue: string;
  }
>;

export type SystemMessageCellRendererParams = CellRendererParams<
  { id: string; severity: string },
  unknown,
  number,
  unknown,
  {
    value: string | number;
    formattedValue: string;
  }
>;

export interface NewMessageIndicatorProps {
  isVisible: boolean;
  handleNewMessageIndicatorClick(e: MouseEvent): void;
}

export interface SystemMessageSummaryProps {
  systemMessages: SystemMessageTypes.SystemMessage[];
  severityFilterMap: Immutable.Map<SystemMessageTypes.SystemMessageSeverity, boolean>;
  setSeverityFilterMap(m: Immutable.Map<SystemMessageTypes.SystemMessageSeverity, boolean>): void;
}
