import type { QcMaskTypes } from '@gms/common-model';

import type { CoordinatesPx } from '../qc-mask-form/types';
import type { QcMaskDialogBoxType } from '../types';

/**
 * QcMaskOverlap Props
 */
export interface QcMaskOverlapProps {
  masks: QcMaskTypes.QcMask[];
  // If set, any context menus opened as children will be created at given x/y coordinates
  contextMenuCoordinates?: CoordinatesPx;
  openNewContextMenu(
    x: number,
    y: number,
    mask: QcMaskTypes.QcMask,
    dialogType: QcMaskDialogBoxType
  );
  selectMask(mask: QcMaskTypes.QcMask);
}

/**
 * QcMaskOverlap  State
 */
export interface QcMaskOverlapState {
  selectedMask: QcMaskTypes.QcMask;
  createDialogOfType?: QcMaskDialogBoxType;
}
export interface QcMaskTableButtonParams {
  onClick(x: number, y: number, params: any);
}
