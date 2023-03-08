import type { WeavessContainerDimensions } from '../../shared-types';

/**
 * Props for the ruler component
 */
export interface RulerProps {
  isActive: boolean;
  initialPoint: { x: number; y: number } | undefined;
  computeTimeSecsForMouseXFractionalPosition: (mouseXPositionFraction: number) => number;
  containerDimensions: WeavessContainerDimensions;
  onRulerMouseUp: () => void;
}

/**
 * Props from the label value formatter component
 */
export interface LabelValueProps {
  value: string;
  label: string;
  tooltip: string;
  valueColor?: string;
  styleForValue?: React.CSSProperties;
  containerClass?: string;
}
