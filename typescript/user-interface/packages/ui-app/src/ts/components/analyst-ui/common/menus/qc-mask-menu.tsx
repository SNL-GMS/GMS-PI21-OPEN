import { Menu, MenuItem } from '@blueprintjs/core';
import type { QcMaskTypes } from '@gms/common-model';
import React from 'react';

import { QcMaskDialogBoxType } from '../dialogs/types';

/**
 * QcMaskDialogOpener
 * function to initiate opening of QcMaskDialog
 */
export type QcMaskDialogOpener = (
  eventX: number,
  eventY: number,
  qcMask: QcMaskTypes.QcMask,
  qcMaskDialogType: QcMaskDialogBoxType
) => void;

export function getOnClickOpenDialog(eventX, eventY, mask, openDialog, qcMaskDialogBoxType) {
  return e => {
    e.stopPropagation();
    openDialog(eventX, eventY, mask, qcMaskDialogBoxType);
  };
}
/**
 * Creates the appropriate blueprint context menu
 *
 * @param eventX x coordinate of event
 * @param eventY y coordinate of event
 * @param mask mask to modify or reject
 * @param openDialog function to open mask dialog
 * @param isRejected is mask rejected or not
 *
 * @returns jsx for an blueprint menu
 */
export function QcMaskMenu(
  eventX: number,
  eventY: number,
  mask: QcMaskTypes.QcMask,
  openDialog: QcMaskDialogOpener,
  isRejected: boolean
): JSX.Element {
  if (isRejected) {
    return (
      <Menu>
        <MenuItem
          text="View"
          disabled={false}
          onClick={getOnClickOpenDialog(eventX, eventY, mask, openDialog, QcMaskDialogBoxType.View)}
        />
      </Menu>
    );
  }

  return (
    <Menu>
      <MenuItem
        text="Modify"
        disabled={false}
        onClick={getOnClickOpenDialog(eventX, eventY, mask, openDialog, QcMaskDialogBoxType.Modify)}
      />
      <MenuItem
        text="Reject"
        disabled={false}
        onClick={getOnClickOpenDialog(eventX, eventY, mask, openDialog, QcMaskDialogBoxType.Reject)}
      />
    </Menu>
  );
}
