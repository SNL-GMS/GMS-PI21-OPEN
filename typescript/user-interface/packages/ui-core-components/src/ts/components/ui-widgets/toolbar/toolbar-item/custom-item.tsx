import type React from 'react';

import type { ToolbarItemBase, ToolbarItemElement } from '../types';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isCustomToolbarItem(object: unknown): object is CustomItemProps {
  return (object as CustomItemProps).element !== undefined;
}

/**
 * type for the custom item toolbar item
 */
export interface CustomItemProps extends ToolbarItemBase {
  element: JSX.Element;
}

export const CustomToolbarItem: React.FC<CustomItemProps> = ({
  element
}: CustomItemProps): ToolbarItemElement => {
  return element;
};
