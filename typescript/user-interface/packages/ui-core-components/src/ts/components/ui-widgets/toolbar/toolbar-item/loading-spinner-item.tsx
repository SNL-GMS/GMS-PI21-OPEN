import React from 'react';

import { LoadingSpinner } from '../../loading-spinner';
import type { ToolbarItemBase, ToolbarItemElement } from '../types';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isLoadingSpinnerToolbarItem(
  object: unknown
): object is LoadingSpinnerToolbarItemProps {
  return (object as LoadingSpinnerToolbarItemProps).itemsToLoad !== undefined;
}

/**
 * Properties to pass to the {@link LoadingSpinnerToolbarItem}
 *
 * @see {@link ToolbarItemBase} for base properties.
 */
export interface LoadingSpinnerToolbarItemProps extends ToolbarItemBase {
  /** count of items to load */
  itemsToLoad: number;

  /** number of items currently loaded */
  itemsLoaded?: number;

  /** hide the label 'Loading' */
  hideTheWordLoading?: boolean;

  /** hid the number of items loaded */
  hideOutstandingCount?: boolean;
}

/**
 * Represents an item loading spinner used within a toolbar with optional counts
 *
 * @param loadingSpinnerItem the item to display {@link LoadingSpinnerItem}
 */
// eslint-disable-next-line react/function-component-definition
export const LoadingSpinnerToolbarItem: React.FC<LoadingSpinnerToolbarItemProps> = ({
  itemsToLoad,
  itemsLoaded,
  hideTheWordLoading,
  hideOutstandingCount,
  style,
  onlyShowIcon,
  label,
  widthPx,
  cyData
}: LoadingSpinnerToolbarItemProps): ToolbarItemElement => {
  return (
    <div style={style ?? {}}>
      <LoadingSpinner
        hideTheWordLoading={hideTheWordLoading}
        hideOutstandingCount={hideOutstandingCount}
        onlyShowSpinner={onlyShowIcon ?? false}
        itemsLoaded={itemsLoaded}
        itemsToLoad={itemsToLoad}
        label={label}
        widthPx={widthPx}
        data-cy={cyData}
      />
    </div>
  );
};
