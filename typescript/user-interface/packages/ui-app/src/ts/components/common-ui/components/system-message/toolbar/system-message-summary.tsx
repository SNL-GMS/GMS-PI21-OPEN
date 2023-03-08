import { SystemMessageTypes } from '@gms/common-model';
import { classList } from '@gms/ui-util';
import * as React from 'react';

import type { SystemMessageSummaryProps } from '../types';

const NUM_DIGITS_TO_DISPLAY = 4;

/**
 * Filters the provided system messages by severity
 *
 * @param systemMessages the system messages to filter
 * @param severity the severity to filter
 */
const filterBySeverity = (
  systemMessages: SystemMessageTypes.SystemMessage[],
  severity: SystemMessageTypes.SystemMessageSeverity
) => (systemMessages ? systemMessages.filter(msg => msg.severity === severity) : []);

interface PrefixedDisplayNumberProps {
  className?: string;
  digits: number;
  value: number;
}

function PrefixedDisplayNumber(props: PrefixedDisplayNumberProps) {
  const { digits, value, className } = props;
  const prefixLength = digits - value.toString().length;

  return (
    <span className={`system-message-summary__count ${className ?? ''}`}>
      {prefixLength > 0 && (
        <span className="system-message-summary__count--prefix">
          {new Array(prefixLength).fill(0)}
        </span>
      )}
      <span className="system-message-summary__count--main">{value}</span>
    </span>
  );
}

export interface SummaryEntryProps {
  severity: SystemMessageTypes.SystemMessageSeverity;
  value: number;
  isShown: boolean;
  toggleFilter(severity: SystemMessageTypes.SystemMessageSeverity): void;
}

function SummaryEntry(props: SummaryEntryProps) {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { severity, value, isShown, toggleFilter } = props;
  return (
    // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
    <span
      className={classList(
        {
          'system-message-summary__entry--disabled': !isShown
        },
        'system-message-summary__entry'
      )}
      data-severity={severity}
      onClick={() => toggleFilter(severity)}
    >
      {severity}
      :
      <PrefixedDisplayNumber
        data-severity={severity}
        digits={NUM_DIGITS_TO_DISPLAY}
        value={value}
      />
    </span>
  );
}

export function SystemMessageSummary(props: SystemMessageSummaryProps) {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { systemMessages, severityFilterMap, setSeverityFilterMap } = props;
  return (
    <div className="system-message-summary">
      {Object.keys(SystemMessageTypes.SystemMessageSeverity).map(
        (messageSeverity: SystemMessageTypes.SystemMessageSeverity) => (
          <SummaryEntry
            key={messageSeverity}
            severity={messageSeverity}
            value={filterBySeverity(systemMessages, messageSeverity).length}
            toggleFilter={(s: string) => {
              if (severityFilterMap) {
                const newMap = severityFilterMap?.set(
                  SystemMessageTypes.SystemMessageSeverity[s],
                  !severityFilterMap?.get(SystemMessageTypes.SystemMessageSeverity[s])
                );
                setSeverityFilterMap(newMap);
              }
            }}
            isShown={severityFilterMap?.get(messageSeverity) ?? false}
          />
        )
      )}
    </div>
  );
}
