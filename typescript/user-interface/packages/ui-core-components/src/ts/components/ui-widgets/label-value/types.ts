import type React from 'react';

/**
 * app determines the default styling for the label-value toolbar item, defaults to soh
 */
export interface LabelValueProps {
  containerClass?: string;
  ianApp?: boolean;
  label: string;
  numeric?: boolean;
  styleForValue?: React.CSSProperties;
  tooltip: string;
  value: string | JSX.Element;
  valueColor?: string;
}
