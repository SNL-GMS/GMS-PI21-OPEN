import type { ToolbarTypes } from '@gms/ui-core-components';
import { PopoverButtonToolbarItem } from '@gms/ui-core-components';
import * as React from 'react';

import { QcMaskFilter } from '~analyst-ui/common/dialogs';
import type { QcMaskDisplayFilters } from '~analyst-ui/config';
import type { MaskDisplayFilter } from '~analyst-ui/config/user-preferences';

const buildQcMaskPicker = (
  maskDisplayFilters: QcMaskDisplayFilters,
  setMaskDisplayFilters: (key: string, maskDisplayFilter: MaskDisplayFilter) => void,
  widthPx: number,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <PopoverButtonToolbarItem
    key={key}
    disabled
    label="QC Masks"
    tooltip="Show/Hide categories of QC masks"
    widthPx={widthPx}
    onPopoverDismissed={() => {
      // Do nothing
    }}
    popoverContent={
      <QcMaskFilter
        maskDisplayFilters={maskDisplayFilters}
        setMaskDisplayFilters={setMaskDisplayFilters}
      />
    }
  />
);

/**
 * QC mask control for the toolbar, or returns the previously created control
 * if none of the parameters have changed.
 *
 * @param maskDisplayFilters The enum encompassing all mask display filters
 * @param setMaskDisplayFilters a function to modify the mask display filters
 * @param key must be unique
 * @returns a toolbar item control for the QC masks
 */
export const useQcMaskControl = (
  maskDisplayFilters: QcMaskDisplayFilters,
  setMaskDisplayFilters: (key: string, maskDisplayFilter: MaskDisplayFilter) => void,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const widthPx = 110;
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () => buildQcMaskPicker(maskDisplayFilters, setMaskDisplayFilters, widthPx, key),
    [maskDisplayFilters, setMaskDisplayFilters, key]
  );
};
