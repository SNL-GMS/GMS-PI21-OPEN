import { SystemMessageTypes } from '@gms/common-model';

export const systemMessages: SystemMessageTypes.SystemMessage[] = [
  {
    id: '1',
    time: 0,
    category: SystemMessageTypes.SystemMessageCategory.SOH,
    severity: SystemMessageTypes.SystemMessageSeverity.INFO,
    subCategory: SystemMessageTypes.SystemMessageSubCategory.USER,
    message: 'CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED',
    type: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED
  },
  {
    id: '1',
    time: 0,
    category: SystemMessageTypes.SystemMessageCategory.SOH,
    severity: SystemMessageTypes.SystemMessageSeverity.INFO,
    subCategory: SystemMessageTypes.SystemMessageSubCategory.USER,
    message: 'CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED',
    type: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED
  }
];
