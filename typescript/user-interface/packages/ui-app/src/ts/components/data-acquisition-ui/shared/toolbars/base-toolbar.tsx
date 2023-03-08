/* eslint-disable react/destructuring-assignment */
import type { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import { DeprecatedToolbar } from '@gms/ui-core-components';
import * as React from 'react';

import { gmsLayout } from '~scss-config/layout-preferences';

/** the toolbar margin in pixels */
export const TOOLBAR_MARGIN_PIXELS = 16;

/** The base toolbar props */
export interface BaseToolbarProps extends Partial<DeprecatedToolbarTypes.ToolbarProps> {
  widthPx: number;
}

/**
 * Renders the base toolbar.
 *
 * @param props the props
 */
export function BaseToolbar(props: BaseToolbarProps) {
  // adjust the rank of the left items - ensure uniqueness
  let leftItemCount = 1;
  if (props.itemsRight) {
    props.itemsRight.forEach(item => {
      // eslint-disable-next-line no-plusplus, no-param-reassign
      item.rank = leftItemCount++;
    });
  }

  // adjust the rank of the right items - ensure uniqueness
  let rightItemCount = 1;
  if (props.itemsLeft) {
    props.itemsLeft.forEach(item => {
      // eslint-disable-next-line no-plusplus, no-param-reassign
      item.rank = rightItemCount++;
    });
  }

  const leftToolbarItemDefs: DeprecatedToolbarTypes.DeprecatedToolbarItemBase[] = props.itemsLeft
    ? [...props.itemsLeft]
    : [];

  const rightToolbarItemDefs: DeprecatedToolbarTypes.DeprecatedToolbarItemBase[] = props.itemsRight
    ? [...props.itemsRight]
    : [];

  return (
    <DeprecatedToolbar
      toolbarWidthPx={
        props.widthPx - TOOLBAR_MARGIN_PIXELS > 0
          ? props.widthPx -
            parseInt(
              gmsLayout.displayPadding.substring(0, gmsLayout.displayPadding.length - 2),
              10
            ) *
              2
          : 0
      }
      minWhiteSpacePx={1}
      // eslint-disable-next-line react/jsx-props-no-spreading
      {...props}
      itemsRight={rightToolbarItemDefs}
      itemsLeft={leftToolbarItemDefs}
    />
  );
}
