/* eslint-disable @typescript-eslint/no-magic-numbers */
export interface RGB {
  red: number;
  green: number;
  blue: number;
}

/**
 * A single hex character, case insensitive
 */
export type HexCharacter =
  | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
  | 'a'
  | 'A'
  | 'b'
  | 'B'
  | 'c'
  | 'C'
  | 'd'
  | 'D'
  | 'e'
  | 'E'
  | 'f'
  | 'F';

/**
 * So named because it represents a binary octet, between 0 and 255 in decimal.
 */
// eslint-disable-next-line prettier/prettier
export type HexOctet = `${HexCharacter}${HexCharacter}`;

/**
 * A hex color string. Enforces that the string begins with `#`, and that it be followed by either
 * three of @interface HexOctet, or by three of @interface HexCharacter.
 * Typescript does not support union types of the necessary size, so we have to use a plain string type
 * along with type guards.
 *
 * @example: #facade, #c0ffee, #000000, #123, #abc
 *
 * @see https://www.typescriptlang.org/docs/handbook/2/template-literal-types.html
 */
export type Hex = `#${string}`;

export interface GMSColors {
  gmsMain: string;
  gmsRecessed: string;
  gmsProminent: string;
  gmsSoft: string;
  gmsBackground: string;
  gmsProminentBackground: string;
  gmsPopoverBackground: string;
  gmsTransparent: string;

  gmsChartTickLabelDisabled: string;

  gmsInputHighlight: string;
  gmsTableHighlightOddRow: string;
  gmsTableHighlightEvenRow: string;
  gmsTableRequiresReviewOddRow: string;
  gmsTableRequiresReviewEvenRow: string;
  gmsTableSelection: string;
  gmsTableChangeMarker: string;
  gmsTableSubsetSelected: string;

  gmsWarning: string;
}

export interface SemanticColors {
  analystOpenEvent: string;
  analystToWork: string;
  analystUnassociated: string;
  analystComplete: string;
  waveformIntervalBoundary: string;
  waveformRaw: string;
  qcAnalystDefined: string;
  qcChannelProcessing: string;
  qcDataAuthentication: string;
  qcRejected: string;
  qcStationSOH: string;
  qcWaveformQuality: string;

  dataAcqOk: string;
  dataAcqWarning: string;
  dataAcqStrongWarning: string;
}
