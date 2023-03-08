import { SystemMessageTypes } from '@gms/common-model';

export const systemMessages = [
  {
    id: 'mock1',
    category: SystemMessageTypes.SystemMessageCategory.SOH,
    message: 'mock message text',
    severity: SystemMessageTypes.SystemMessageSeverity.CRITICAL,
    subCategory: SystemMessageTypes.SystemMessageSubCategory.STATION,
    time: 1593030714074,
    type: SystemMessageTypes.SystemMessageType.STATION_NEEDS_ATTENTION
  },
  {
    id: 'mock2',
    category: SystemMessageTypes.SystemMessageCategory.SOH,
    message: 'mock message text',
    severity: SystemMessageTypes.SystemMessageSeverity.CRITICAL,
    subCategory: SystemMessageTypes.SystemMessageSubCategory.STATION,
    time: 1593030714075,
    type: SystemMessageTypes.SystemMessageType.STATION_NEEDS_ATTENTION
  }
];
