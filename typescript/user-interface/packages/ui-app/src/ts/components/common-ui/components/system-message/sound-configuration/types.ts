import type { CellRendererParams, Row } from '@gms/ui-core-components';

export const ALL_SEVERITIES = 'All severities';
export const ALL_CATEGORIES = 'All categories';
export const ALL_SUBCATEGORIES = 'All subcategories';

export enum FILTER_TYPE {
  SEVERITY = 0,
  CATEGORY,
  SUBCATEGORY
}

export interface SoundConfigurationRow extends Row {
  hasNotificationStatusError: boolean;
  sound: {
    availableSounds: { [key: string]: string };
    selectedSound: string;
    onSelect(e: string): void;
  };
  category: string;
  subcategory: string;
  severity: string;
  message: string;
}

export interface SelectedOptions {
  selectedSeverity: string;
  selectedCategory: string;
  selectedSubcategory: string;
}

export type SoundConfigurationRendererParams = CellRendererParams<
  SoundConfigurationRow,
  any,
  number | string,
  any,
  {
    value: string | number;
    formattedValue: string;
  }
>;
