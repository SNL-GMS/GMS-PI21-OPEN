import type { MinMax } from '../toolbar/types';

// Types for Loading Spinner
export interface NumericInputProps {
  value: number;
  tooltip: string;
  widthPx?: number;
  disabled?: boolean;
  minMax?: MinMax;
  step?: number;
  cyData?: string;

  /**
   * How long to debounce the onChange call in milliseconds.
   */
  onChangeDebounceMs?: number;

  /**
   * How long to wait before showing the red DANGER intent styling if the text in the component is invalid
   * Defaults to 500ms
   */
  waitToShowErrorMs?: number;
  onChange(val: number): void;
}
// Types for Loading Spinner
export interface NumericInputState {
  intermediateValue: number;
}
