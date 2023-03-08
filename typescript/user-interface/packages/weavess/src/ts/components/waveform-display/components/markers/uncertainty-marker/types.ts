export interface UncertaintyMarkerProps {
  /** unique id */
  id: string;

  /** Actual physical css position as a percentage 0-100 */
  position: number;

  /** Actual physical css position of the pick marker */
  pickMarkerPosition: number;

  /** Color of the pick as a string */
  color: string;

  /** Is this uncertainty marker the left side or right side */
  isLeftUncertaintyBar: boolean;
}
