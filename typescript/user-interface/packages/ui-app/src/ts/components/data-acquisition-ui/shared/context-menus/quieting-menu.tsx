import { ContextMenu, Menu, MenuItem } from '@blueprintjs/core';
import { SohTypes } from '@gms/common-model';
import { millisToTimeRemaining, prettifyAllCapsEnumType } from '@gms/common-util';
import { Form, FormTypes, WidgetTypes } from '@gms/ui-core-components';
import { getStore, useAppSelector, useQuietSohStatus } from '@gms/ui-state';
import delay from 'lodash/delay';
import React from 'react';
import { Provider } from 'react-redux';

import type { Offset } from '~components/data-acquisition-ui/shared/types';

// max number of characters for the text area
const MAX_QUIET_COMMENT_CHAR = 1024;

export interface QuietAction {
  stationName: string;
  channelMonitorPairs: SohTypes.ChannelMonitorPair[];
  position: Offset;
  quietingDurationSelections: number[];
  quietUntilMs: number;
  isStale?: boolean;
}

/**
 * Creates the quite dialog form for quieting with a comment.
 *
 * @param props the props
 */
export function QuietWithCommentDialog(props: QuietAction) {
  const { stationName, channelMonitorPairs, quietingDurationSelections } = props;
  const channelNames = channelMonitorPairs.map(p => p.channelName);
  const monitors = channelMonitorPairs.map(p => p.monitorType);
  const callQuietMutation = useQuietSohStatus();
  const userSessionState = useAppSelector(state => state.app.userSession);
  const maxStringLength = 30;
  const formItems: FormTypes.FormItem[] = [];
  formItems.push({
    itemKey: 'stationLabel',
    labelText: `Station`,
    tooltip: stationName,
    itemType: FormTypes.ItemType.Display,
    displayText: stationName
  });
  formItems.push({
    itemKey: 'channelLabel',
    labelText: `Channel`,
    tooltip: channelNames.join(', '),
    itemType: FormTypes.ItemType.Display,
    displayText: channelNames.join(', ')
  });

  let displayText = 'Multiple';
  if (monitors.length <= 1) {
    if (monitors[0].length > maxStringLength) {
      displayText = `${monitors[0].substring(0, maxStringLength)}...`;
    } else {
      displayText = prettifyAllCapsEnumType(
        monitors[0],
        SohTypes.isEnvironmentalIssue(monitors[0])
      );
    }
  }
  formItems.push({
    itemKey: 'monitorLabel',
    labelText: `Monitor`,
    tooltip: monitors.length > 1 ? 'Multiple' : monitors[0],
    itemType: FormTypes.ItemType.Display,
    displayText
  });
  formItems.push({
    itemKey: 'duration',
    labelText: 'Quiet for',
    itemType: FormTypes.ItemType.Input,
    'data-cy': 'quiet-duration',
    value: {
      params: {
        tooltip: 'Select the duration to quiet',
        dropDownItems: quietingDurationSelections,
        dropdownText: quietingDurationSelections.map(q => millisToTimeRemaining(q))
      },
      defaultValue: quietingDurationSelections[0],
      type: WidgetTypes.WidgetInputType.DropDown
    }
  });
  formItems.push({
    itemKey: 'comment',
    labelText: 'Comment',
    itemType: FormTypes.ItemType.Input,
    topAlign: true,
    'data-cy': 'quiet-comment',
    value: {
      params: {
        tooltip: 'Enter the comment',
        maxChar: MAX_QUIET_COMMENT_CHAR
      },
      defaultValue: ``,
      type: WidgetTypes.WidgetInputType.TextArea
    }
  });

  const quietPanel: FormTypes.FormPanel = {
    formItems,
    name: 'QuietWithComment'
  };

  return (
    <div>
      <Form
        header="Quiet"
        defaultPanel={quietPanel}
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        onSubmit={(data: any) => {
          const queryArgs: SohTypes.ChannelMonitorInput = {
            stationName,
            channelMonitorPairs,
            userName: userSessionState.authenticationStatus.userName,
            quietDurationMs: Number(data.duration),
            comment: String(data.comment)
          };
          // eslint-disable-next-line @typescript-eslint/no-floating-promises
          callQuietMutation(queryArgs);
          ContextMenu.hide();
        }}
        onCancel={ContextMenu.hide}
        submitButtonText="Quiet"
        requiresModificationForSubmit
      />
    </div>
  );
}

/**
 * Creates menu item for quieting without a comment.
 */
export function QuiteMenuItem(props: QuietAction) {
  const { stationName, channelMonitorPairs, quietingDurationSelections, isStale } = props;
  const disabled = isStale;
  const callQuietMutation = useQuietSohStatus();
  const userSessionState = useAppSelector(state => state.app.userSession);
  return (
    <MenuItem text="Quiet for..." disabled={disabled} data-cy="quiet-without-comment">
      {quietingDurationSelections.map(duration => (
        <MenuItem
          key={duration}
          text={millisToTimeRemaining(duration)}
          onClick={() => {
            const quietArgs: SohTypes.ChannelMonitorInput = {
              stationName,
              channelMonitorPairs,
              userName: userSessionState.authenticationStatus.userName,
              quietDurationMs: duration
            };
            // eslint-disable-next-line @typescript-eslint/no-floating-promises
            callQuietMutation(quietArgs);
          }}
        />
      ))}
    </MenuItem>
  );
}

/**
 * Creates menu item for quieting with a comment.
 */
export function QuiteWithCommentMenuItem(props: QuietAction) {
  // necessary to get referential stability for memoization
  const clientOffset = 25;
  const TheDialog = React.useMemo(() => {
    return (
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <QuietWithCommentDialog {...props} />
      </Provider>
    );
  }, [props]);
  const { isStale } = props;
  return (
    <MenuItem
      text="Quiet with comment..."
      data-cy="quiet-with-comment"
      disabled={isStale}
      onClick={(e: React.MouseEvent) => {
        e.preventDefault();
        ContextMenu.hide();
        const offset: { left: number; top: number } = {
          left: e.clientX - clientOffset,
          top: e.clientY - clientOffset
        };
        delay(
          () =>
            // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
            ContextMenu.show(TheDialog, offset, undefined, true),
          100
        );
      }}
    />
  );
}

/**
 * Creates menu item for canceling a quite period.
 */
export function CancelMenuItem(props: QuietAction) {
  const { stationName, channelMonitorPairs, quietUntilMs } = props;
  const disabled = !quietUntilMs || quietUntilMs === -1;
  const callQuietMutation = useQuietSohStatus();
  const userSessionState = useAppSelector(state => state.app.userSession);
  return (
    <MenuItem
      text="Cancel quiet period"
      data-cy="quiet-cancel"
      disabled={disabled}
      onClick={() => {
        const quietArgs: SohTypes.ChannelMonitorInput = {
          stationName,
          channelMonitorPairs,
          userName: userSessionState.authenticationStatus.userName,
          quietDurationMs: 0
        };
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        callQuietMutation(quietArgs);
      }}
    />
  );
}

/**
 * Show the quieting context menu
 *
 * @param q the quiet action parameters
 */
export const showQuietingContextMenu = (q: QuietAction): void => {
  const menuJSX: JSX.Element = (
    <Provider store={getStore()}>
      <Menu>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <QuiteMenuItem {...q} />
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <QuiteWithCommentMenuItem {...q} />
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <CancelMenuItem {...q} />
      </Menu>
    </Provider>
  );
  // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
  ContextMenu.show(menuJSX, q.position);
};
