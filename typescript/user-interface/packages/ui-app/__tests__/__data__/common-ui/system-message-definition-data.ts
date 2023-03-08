import type { SystemMessageTypes } from '@gms/common-model';

const systemMessageDefinitions: SystemMessageTypes.SystemMessageDefinition[] = [
  {
    systemMessageType: 'CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED',
    systemMessageCategory: 'SOH',
    systemMessageSubCategory: 'USER',
    systemMessageSeverity: 'INFO',
    template: 'Station %s Channel %s %s quiet period canceled by user %s'
  },
  {
    systemMessageType: 'CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED',
    systemMessageCategory: 'SOH',
    systemMessageSubCategory: 'USER',
    systemMessageSeverity: 'INFO',
    template:
      "Station %s Channel %s %s status change acknowledged by user %s|Station %s Channel %s %s status change acknowledged by user %s with comment '%s'"
  }
];

export { systemMessageDefinitions };
