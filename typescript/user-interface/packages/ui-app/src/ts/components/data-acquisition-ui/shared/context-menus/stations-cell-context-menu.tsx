import { ContextMenu, Menu, MenuItem } from '@blueprintjs/core';
import delay from 'lodash/delay';
import uniq from 'lodash/uniq';
import React from 'react';

import { AcknowledgeForm } from '../acknowledge/acknowledge-form';

/**
 * Station SOH context menu props
 */
export interface StationSohContextMenuProps {
  stationNames: string[];
  acknowledgeCallback(stationNames: string[], comment?: string);
}

/**
 * Station SOH context item menu props
 */
export interface StationSohContextMenuItemProps extends StationSohContextMenuProps {
  disabled: boolean;
}

/**
 * Creates menu item text for station acknowledgement
 */
export const getStationAcknowledgementMenuText = (
  stationNames: string[],
  withComment = false
): string => {
  const text =
    stationNames.length > 1 ? `Acknowledge ${stationNames.length} stations` : 'Acknowledge station';
  return withComment ? `${text} with comment...` : text;
};

/**
 * Creates menu item for acknowledging stations without a comment
 */
function AcknowledgeMenuItem(props: StationSohContextMenuItemProps) {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { stationNames, disabled, acknowledgeCallback } = props;
  const stationList = uniq(stationNames);
  return React.createElement(MenuItem, {
    disabled,
    'data-cy': 'acknowledge-without-comment',
    onClick: () => {
      acknowledgeCallback(stationList);
    },
    text: getStationAcknowledgementMenuText(stationList),
    className: 'acknowledge-soh-status'
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } as any);
}

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const acknowledgeOnClick = (e: React.MouseEvent, props: StationSohContextMenuItemProps) => {
  const stationList = uniq(props.stationNames);
  const clientOffset = 25;
  e.preventDefault();
  ContextMenu.hide();
  const offset: { left: number; top: number } = {
    left: e.clientX - clientOffset,
    top: e.clientY - clientOffset
  };
  delay(
    () =>
      // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
      ContextMenu.show(
        React.createElement(AcknowledgeForm, {
          stationNames: stationList,
          acknowledgeStationsByName: (stationNames: string[], comment?: string) => {
            props.acknowledgeCallback(stationNames, comment);
            ContextMenu.hide();
          },
          onClose: ContextMenu.hide
        }),
        offset,
        undefined,
        true
      ),
    100
  );
};

/**
 * Creates menu item for acknowledging stations with a comment
 */
function AcknowledgeWithCommentMenuItem(props: StationSohContextMenuItemProps) {
  const { stationNames, disabled } = props;
  const stationList = uniq(stationNames);
  return React.createElement(MenuItem, {
    disabled,
    'data-cy': 'acknowledge-with-comment',
    onClick: e => acknowledgeOnClick(e, props),
    text: getStationAcknowledgementMenuText(stationList, true),
    className: 'acknowledge-soh-status'
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } as any);
}

/**
 * Context menu for acknowledging station SOH
 */
export function StationSohContextMenu(props: StationSohContextMenuProps): React.ReactElement {
  return React.createElement(
    Menu,
    {}, // empty props
    React.createElement(AcknowledgeMenuItem, {
      ...props,
      disabled: false
    }),
    React.createElement(AcknowledgeWithCommentMenuItem, {
      ...props,
      disabled: false
    })
  );
}

/**
 * Context menu for acknowledging station SOH
 */
export function DisabledStationSohContextMenu(
  props: StationSohContextMenuProps
): React.ReactElement {
  return React.createElement(
    Menu,
    {}, // empty props
    React.createElement(AcknowledgeMenuItem, {
      ...props,
      disabled: true
    }),
    React.createElement(AcknowledgeWithCommentMenuItem, {
      ...props,
      disabled: true
    })
  );
}
