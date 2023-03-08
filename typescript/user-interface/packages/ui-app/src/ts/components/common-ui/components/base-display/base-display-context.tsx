import type GoldenLayout from '@gms/golden-layout';
import React from 'react';

/**
 * Used for providing access to the base display size and a reference to the
 * golden layout container.
 */
export interface BaseDisplayContextData {
  /**
   * A reference to the golden layout container
   */
  glContainer: GoldenLayout.Container;

  /**
   * the width of the display in px, inclusive of the padding on the sides
   */
  widthPx: number;

  /**
   * the height of the display in px, inclusive of the padding on the sides
   */
  heightPx: number;
}

/**
 * Instantiate the Base Display Context and set up the defaults as undefined.
 */
export const BaseDisplayContext: React.Context<BaseDisplayContextData> = React.createContext<
  BaseDisplayContextData
>(undefined);
