import type { SystemMessageTypes } from '@gms/common-model';

/**
 * The system message state.
 */
export interface SystemMessageState {
  /** timestamp of when the data was last updated (EPOCH milliseconds) */
  readonly lastUpdated: number;

  /** the latest systems messages */
  readonly latestSystemMessages: SystemMessageTypes.SystemMessage[];

  /** the systems messages */
  readonly systemMessages: SystemMessageTypes.SystemMessage[];

  /** Whether to play sounds */
  readonly isSoundEnabled: boolean;
}
