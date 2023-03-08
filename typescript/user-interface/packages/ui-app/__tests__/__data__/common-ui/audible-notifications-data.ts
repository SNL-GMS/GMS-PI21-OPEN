import type { UserProfileTypes } from '@gms/common-model';
import { SystemMessageTypes } from '@gms/common-model';

export const audibleNotifications: UserProfileTypes.AudibleNotification[] = [
  {
    fileName: 'yellow-submarine.mp3',
    notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED
  },
  {
    fileName: 'BVW1048.mp4',
    notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED
  }
];
