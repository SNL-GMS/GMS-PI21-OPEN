/* eslint-disable react/prop-types */
import { IconNames } from '@blueprintjs/icons';
import { SystemMessageTypes } from '@gms/common-model';
import { DeprecatedToolbar, DeprecatedToolbarTypes } from '@gms/ui-core-components';
import React, { useState } from 'react';

import { gmsLayout } from '~scss-config/layout-preferences';

import { useBaseDisplaySize } from '../../base-display/base-display-hooks';
import { SoundConfiguration } from '../sound-configuration';
import type { SystemMessageToolbarProps } from '../types';
import { SystemMessageSummary } from './system-message-summary';

const marginForToolbarPx = 16;

/**
 * System message toolbar component
 */
// eslint-disable-next-line react/display-name
export const SystemMessageToolbar: React.FunctionComponent<SystemMessageToolbarProps> = React.memo(
  props => {
    /*
     * Left toolbar items
     */

    const systemMessageSummary: DeprecatedToolbarTypes.CustomItem = {
      rank: 1,
      type: DeprecatedToolbarTypes.ToolbarItemType.CustomItem,
      label: 'Number of system messages by severity',
      tooltip: SystemMessageTypes.SystemMessageSeverity.CRITICAL,
      widthPx: 200,
      element: (
        <SystemMessageSummary
          systemMessages={props.systemMessagesState.systemMessages}
          severityFilterMap={props.severityFilterMap}
          setSeverityFilterMap={m => props.setSeverityFilterMap(m)}
        />
      )
    };

    const leftToolbarItemDefs: DeprecatedToolbarTypes.ToolbarItem[] = [systemMessageSummary];

    /*
     * Right toolbar items
     */

    let rightItemCount = 1;

    const enableDisableAutoScrolling: DeprecatedToolbarTypes.SwitchItem = {
      // eslint-disable-next-line no-plusplus
      rank: rightItemCount++,
      type: DeprecatedToolbarTypes.ToolbarItemType.Switch,
      label: 'Auto scroll',
      labelRight: 'Auto scroll',
      cyData: 'system-message-auto-scroll',
      icon: props.isAutoScrollingEnabled ? IconNames.PAUSE : IconNames.PLAY,
      tooltip: props.isAutoScrollingEnabled ? 'Disable auto scrolling' : 'Enable auto scrolling',
      widthPx: 100,
      value: props.isAutoScrollingEnabled,
      onChange: () => props.setIsAutoScrollingEnabled(!props.isAutoScrollingEnabled)
    };

    const clearSystemMessages: DeprecatedToolbarTypes.ButtonItem = {
      // eslint-disable-next-line no-plusplus
      rank: rightItemCount++,
      type: DeprecatedToolbarTypes.ToolbarItemType.Button,
      label: 'Clear list',
      labelRight: 'Clear list',
      cyData: 'system-message-clear',
      onlyShowIcon: true,
      icon: IconNames.TRASH,
      tooltip: 'Clear all system messages from the list',
      widthPx: 100,
      onClick: () => props.clearAllSystemMessages()
    };

    const enableDisableSounds: DeprecatedToolbarTypes.ButtonItem = {
      // eslint-disable-next-line no-plusplus
      rank: rightItemCount++,
      type: DeprecatedToolbarTypes.ToolbarItemType.Button,
      label: 'Sounds',
      onlyShowIcon: true,
      icon: props.isSoundEnabled ? IconNames.VOLUME_UP : IconNames.VOLUME_OFF,
      tooltip: props.isSoundEnabled ? 'Disable sound' : 'Enable sound',
      widthPx: 30,
      onClick: () => props.setIsSoundEnabled(!props.isSoundEnabled)
    };

    // ! Disabling sound toolbar options until they are functional - DO NOT DELETE
    const [configureSoundsVisible, setConfigureSoundsVisible] = useState(false);
    const configureSounds: DeprecatedToolbarTypes.ButtonItem = {
      // WARNING: If you add more toolbar entries, rightItemCount should increase: ie: rightItemCount++
      // Not added here to avoid code smell
      rank: rightItemCount,
      type: DeprecatedToolbarTypes.ToolbarItemType.Button,
      label: 'Configure sounds',
      tooltip: 'Configure the sounds',
      widthPx: 125,
      onClick: () => {
        setConfigureSoundsVisible(!configureSoundsVisible);
      }
    };

    const rightToolbarItemDefs: DeprecatedToolbarTypes.ToolbarItem[] = [
      enableDisableSounds,
      enableDisableAutoScrolling,
      clearSystemMessages,
      configureSounds
    ];

    const [widthPx] = useBaseDisplaySize();

    return (
      <>
        <DeprecatedToolbar
          toolbarWidthPx={
            widthPx - marginForToolbarPx > 0 ? widthPx - gmsLayout.displayPaddingPx * 2 : 0
          }
          itemsRight={rightToolbarItemDefs}
          minWhiteSpacePx={1}
          itemsLeft={leftToolbarItemDefs}
        />
        <SoundConfiguration
          onToggle={() => setConfigureSoundsVisible(!configureSoundsVisible)}
          isVisible={configureSoundsVisible}
          systemMessageDefinitions={props.systemMessageDefinitions}
        />
      </>
    );
  }
);
