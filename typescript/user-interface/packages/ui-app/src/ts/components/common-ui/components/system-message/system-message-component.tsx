/* eslint-disable react/prop-types */
import {
  systemMessageActions,
  useAppDispatch,
  useAppSelector,
  useGetSystemMessageDefinitionQuery,
  useGetUserProfileQuery
} from '@gms/ui-state';
import { useForceGlUpdateOnResizeAndShow } from '@gms/ui-util';
import * as React from 'react';

import { commonClassNames } from '~components/common-ui/config/common-class-names';

import { BaseDisplay } from '../base-display';
import {
  validateAvailableSounds,
  validateConfiguredAudibleNotifications
} from './sound-configuration/sound-configuration-util';
import { SystemMessageTable } from './system-message-table';
import { useSeverityFilters } from './toolbar/severity-filters';
import { SystemMessageToolbar } from './toolbar/system-message-toolbar';
import type { SystemMessageProps } from './types';

/**
 * The system message component
 *
 * @param props the system message component props
 */
// eslint-disable-next-line react/display-name
export const SystemMessage: React.FunctionComponent<SystemMessageProps> = React.memo(props => {
  const [severityFilterMap, setSeverityFilterMap] = useSeverityFilters();

  /** state for enabling and disabling auto scrolling */
  const [isAutoScrollingEnabled, setIsAutoScrollingEnabled] = React.useState(true);
  const dispatch = useAppDispatch();
  const isSoundEnabled = useAppSelector(state => state.app.systemMessage.isSoundEnabled);
  const setIsSoundEnabled = React.useCallback(
    (isEnabled: boolean) => {
      dispatch(systemMessageActions.setIsSoundEnabled(isEnabled));
    },
    [dispatch]
  );

  /** validate available sound files; toast error message for each sound file that missing */
  React.useEffect(validateAvailableSounds, []);

  const userProfileQuery = useGetUserProfileQuery();
  const userProfile = userProfileQuery?.data ?? undefined;
  const systemMessageDefinitionQuery = useGetSystemMessageDefinitionQuery();
  const systemMessageDefinitions = systemMessageDefinitionQuery?.data ?? undefined;

  /** validate the configured audible notifications; toast error message for each sound file that is missing */
  React.useEffect(() => validateConfiguredAudibleNotifications(userProfile?.audibleNotifications), [
    userProfile?.audibleNotifications
  ]);

  /** force update on golden layout resize and show -> ensures that the toolbar is properly sized */
  useForceGlUpdateOnResizeAndShow(props.glContainer);

  return (
    <BaseDisplay
      glContainer={props.glContainer}
      className={`system-message-display ${commonClassNames.sharedTableClasses}`}
    >
      <SystemMessageToolbar
        /* eslint-disable @typescript-eslint/unbound-method */
        addSystemMessages={props.addSystemMessages}
        clearAllSystemMessages={props.clearAllSystemMessages}
        /* eslint-enable @typescript-eslint/unbound-method */
        systemMessagesState={props.systemMessagesState}
        isAutoScrollingEnabled={isAutoScrollingEnabled}
        setIsAutoScrollingEnabled={setIsAutoScrollingEnabled}
        isSoundEnabled={isSoundEnabled}
        setIsSoundEnabled={setIsSoundEnabled}
        systemMessageDefinitions={systemMessageDefinitions}
        severityFilterMap={severityFilterMap}
        setSeverityFilterMap={m => {
          setSeverityFilterMap(m);
        }}
      />
      <SystemMessageTable
        /* eslint-disable @typescript-eslint/unbound-method */
        addSystemMessages={props.addSystemMessages}
        clearAllSystemMessages={props.clearAllSystemMessages}
        /* eslint-enable @typescript-eslint/unbound-method */
        systemMessages={props.systemMessagesState.systemMessages?.filter(
          msg => severityFilterMap?.get(msg.severity) ?? true
        )}
        isAutoScrollingEnabled={isAutoScrollingEnabled}
        setIsAutoScrollingEnabled={setIsAutoScrollingEnabled}
        severityFilterMap={severityFilterMap}
      />
    </BaseDisplay>
  );
});
