/* eslint-disable react/destructuring-assignment */
import { SystemMessageTypes } from '@gms/common-model';
import { useAppSelector, useGetUserProfileQuery } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import head from 'lodash/head';
import last from 'lodash/last';
import partialRight from 'lodash/partialRight';
import sortBy from 'lodash/sortBy';
import * as React from 'react';
import { toast } from 'react-toastify';

import { userPreferences } from '~components/common-ui/config/user-preferences';

const logger = UILogger.create('GMS_LOG_SYSTEM_MESSAGE', process.env.GMS_LOG_SYSTEM_MESSAGE);

const BASE_SOUNDS_PATH = userPreferences.baseSoundsPath;

const severityOrder = {
  INFO: 2,
  WARNING: 1,
  CRITICAL: 0
};

/**
 * Pick a single system message based on highest severity and most recent.
 *
 * @param latestMessages - System Messages received.
 * @returns - undefined or a single SystemMessage.
 */
const pickSystemMessage = (
  latestMessages: SystemMessageTypes.SystemMessage[]
): SystemMessageTypes.SystemMessage | undefined => {
  const sortBySeverity = partialRight(sortBy, [a => severityOrder[a.severity]]);
  const sortByDateAndId = partialRight(sortBy, [a => a.time, a => a.id]);

  let mostSevereList: SystemMessageTypes.SystemMessage[] = sortBySeverity(latestMessages);
  const mostSevere = head(mostSevereList)?.severity;
  mostSevereList = mostSevereList.filter(systemMessage => systemMessage.severity === mostSevere);
  const sorted: SystemMessageTypes.SystemMessage = last(sortByDateAndId(mostSevereList));
  return sorted;
};

export function SystemMessageAlertPlayer() {
  const isSoundEnabled = useAppSelector(state => state.app.systemMessage.isSoundEnabled);
  const latestSystemMessages = useAppSelector(
    state => state.app.systemMessage.latestSystemMessages
  );
  const userProfile = useGetUserProfileQuery()?.data;
  const audibleNotifications = userProfile?.audibleNotifications ?? [];
  const refs = React.useRef(new Map<string, HTMLAudioElement>());

  const configuredSounds: SystemMessageTypes.SystemMessage[] = latestSystemMessages?.filter(
    message =>
      audibleNotifications?.find(
        notification =>
          SystemMessageTypes.SystemMessageType[message.type] ===
          SystemMessageTypes.SystemMessageType[notification.notificationType]
      )
  );

  const sorted: SystemMessageTypes.SystemMessage = pickSystemMessage(configuredSounds);

  const sound = sorted
    ? audibleNotifications?.find(
        notification =>
          SystemMessageTypes.SystemMessageType[notification.notificationType] ===
          SystemMessageTypes.SystemMessageType[sorted.type]
      )?.fileName
    : undefined;

  const shouldUseEffect = latestSystemMessages
    ? JSON.stringify(sortBy([...latestSystemMessages], [a => a.id]))
    : undefined;

  React.useEffect(() => {
    if (sound && refs.current.has(sound) && isSoundEnabled) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      refs.current
        .get(sound)
        .play()
        .catch(e => {
          logger.error(`Failed to play alert ${sound}: ${e}`);
          toast.error(userPreferences.configuredAudibleNotificationFileNotFound(sound));
        });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shouldUseEffect]);

  return (
    // eslint-disable-next-line react/jsx-no-useless-fragment
    <>
      {audibleNotifications &&
        audibleNotifications.map(notification => (
          // eslint-disable-next-line jsx-a11y/media-has-caption
          <audio
            ref={ref => {
              refs.current.set(notification.fileName, ref);
            }}
            key={`${JSON.stringify(notification)}`}
            src={`${BASE_SOUNDS_PATH}${notification.fileName}`}
          />
        ))}
    </>
  );
}
