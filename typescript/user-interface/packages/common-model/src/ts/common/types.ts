// ***************************************
// Mutations
// ***************************************

/** The `operation name` for the client log mutation */
export const ClientLogOperationMutationName = 'clientLog';

// ***************************************
// Queries
// ***************************************
export interface VersionInfo {
  versionNumber: string;
  commitSHA: string;
}

// ***************************************
// Model
// ***************************************

/**
 * System Event wrapper used with subscription message
 */
export interface SystemEvent {
  id: string;
  source: string;
  specversion: string;
  type: string;
  data?: unknown;
}

/**
 * System Event Type used with subscriptions
 */
export interface SystemEventType {
  eventType: string;
}

export interface DistanceToSourceInput {
  sourceType: DistanceSourceType;
  sourceId: string;
}

// Map entry of event id to usernames
export interface EventToUsers {
  eventId: string;
  userNames: string[];
}

// Workspace state
export interface WorkspaceState {
  eventToUsers: EventToUsers[];
}

/**
 * The distance value representing degrees and km.
 */
export interface Distance {
  readonly degrees: number;
  readonly km: number;
}

/**
 * Represents a distance measurement relative to a specified source location
 */
export interface DistanceToSource {
  // The distance
  readonly distance: Distance;

  // The azimuth
  readonly azimuth: number;

  // The source location
  readonly sourceLocation: Location;

  // The type of the source the distance is measured to (e.g. and event)
  readonly sourceType: DistanceSourceType;

  // the unique ID of the source object
  readonly sourceId: string;

  // Which station distance to the source
  readonly stationId: string;
}

/**
 * Creation Type, reflects system change or analyst change
 */
export enum CreatorType {
  Analyst = 'Analyst',
  System = 'System'
}

/**
 * Distance value's units degrees or kilometers
 */
export enum DistanceUnits {
  degrees = 'degrees',
  km = 'km'
}

/**
 * Distance to source type
 */
export enum DistanceSourceType {
  Event = 'Event',
  UserDefined = 'UserDefined'
}

/**
 * Time range in epoch seconds
 */
export interface TimeRange {
  startTimeSecs: number;
  endTimeSecs: number;
}

/**
 * Represents a location specified using latitude (degrees), longitude (degrees),
 * and altitude (kilometers).
 */
export interface Location {
  readonly latitudeDegrees: number;
  readonly longitudeDegrees: number;
  readonly elevationKm: number;
  readonly depthKm?: number;
}

/**
 * Relative Position information relative to a location
 */
export interface Position {
  readonly northDisplacementKm: number;
  readonly eastDisplacementKm: number;
  readonly verticalDisplacementKm: number;
}

/**
 * Log Level to determine different levels
 *
 * ! the log levels must be all lowercase for the loggers
 */
export enum LogLevel {
  error = 'error',
  warn = 'warn',
  client = 'client',
  info = 'info',
  timing = 'timing',
  data = 'data',
  debug = 'debug'
}

/**
 * Client Log Input
 */
export interface ClientLogInput {
  logLevel: LogLevel;
  message: string;
  time: string;
  userName: string;
}

/**
 * Enumeration representing the different types of stations in the monitoring network.
 */
export enum StationType {
  SEISMIC_3_COMPONENT = 'SEISMIC_3_COMPONENT',
  SEISMIC_1_COMPONENT = 'SEISMIC_1_COMPONENT',
  SEISMIC_ARRAY = 'SEISMIC_ARRAY',
  HYDROACOUSTIC = 'HYDROACOUSTIC',
  HYDROACOUSTIC_ARRAY = 'HYDROACOUSTIC_ARRAY',
  INFRASOUND = 'INFRASOUND',
  INFRASOUND_ARRAY = 'INFRASOUND_ARRAY',
  WEATHER = 'WEATHER',
  UNKNOWN = 'UNKNOWN'
}

/**
 * Double Value used in OSD common objects
 */
export interface DoubleValue {
  readonly value: number;
  readonly standardDeviation: number;
  readonly units: Units;
}

/**
 * Units used in DoubleValue part of feature prediction and DoubleValue part of calibration
 */
export enum Units {
  DEGREES = 'DEGREES',
  DECIBELS = 'DECIBELS',
  RADIANS = 'RADIANS',
  SECONDS = 'SECONDS',
  HERTZ = 'HERTZ',
  SECONDS_PER_DEGREE = 'SECONDS_PER_DEGREE',
  SECONDS_PER_RADIAN = 'SECONDS_PER_RADIAN',
  SECONDS_PER_DEGREE_SQUARED = 'SECONDS_PER_DEGREE_SQUARED',
  SECONDS_PER_KILOMETER_SQUARED = 'SECONDS_PER_KILOMETER_SQUARED',
  SECONDS_PER_KILOMETER = 'SECONDS_PER_KILOMETER',
  SECONDS_PER_KILOMETER_PER_DEGREE = 'SECONDS_PER_KILOMETER_PER_DEGREE',
  ONE_OVER_KM = 'ONE_OVER_KM',
  NANOMETERS = 'NANOMETERS',
  NANOMETERS_PER_SECOND = 'NANOMETERS_PER_SECOND',
  NANOMETERS_PER_COUNT = 'NANOMETERS_PER_COUNT',
  UNITLESS = 'UNITLESS',
  MAGNITUDE = 'MAGNITUDE',
  COUNTS_PER_NANOMETER = 'COUNTS_PER_NANOMETER',
  COUNTS_PER_PASCAL = 'COUNTS_PER_PASCAL',
  PASCALS_PER_COUNT = 'PASCALS_PER_COUNT'
}

/**
 * ENUM list of phase types
 */
export enum PhaseType {
  P = 'P',
  S = 'S',
  P3KPbc = 'P3KPbc',
  P4KPdf_B = 'P4KPdf_B',
  P7KPbc = 'P7KPbc',
  P7KPdf_D = 'P7KPdf_D',
  PKiKP = 'PKiKP',
  PKKSab = 'PKKSab',
  PKP2bc = 'PKP2bc',
  PKP3df_B = 'PKP3df_B',
  PKSab = 'PKSab',
  PP_1 = 'PP_1',
  pPKPbc = 'pPKPbc',
  PS = 'PS',
  Rg = 'Rg',
  SKiKP = 'SKiKP',
  SKKSac = 'SKKSac',
  SKPdf = 'SKPdf',
  SKSdf = 'SKSdf',
  sPdiff = 'sPdiff',
  SS = 'SS',
  sSKSdf = 'sSKSdf',
  Lg = 'Lg',
  LR = 'LR',
  P3KPbc_B = 'P3KPbc_B',
  P5KPbc = 'P5KPbc',
  P7KPbc_B = 'P7KPbc_B',
  Pb = 'Pb',
  PKKP = 'PKKP',
  PKKSbc = 'PKKSbc',
  PKP2df = 'PKP2df',
  PKPab = 'PKPab',
  PKSbc = 'PKSbc',
  PP_B = 'PP_B',
  pPKPdf = 'pPKPdf',
  PS_1 = 'PS_1',
  SKKP = 'SKKP',
  SKKSac_B = 'SKKSac_B',
  SKS = 'SKS',
  SKSSKS = 'SKSSKS',
  sPKiKP = 'sPKiKP',
  SS_1 = 'SS_1',
  SSS = 'SSS',
  nNL = 'nNL',
  P3KPdf = 'P3KPdf',
  P5KPbc_B = 'P5KPbc_B',
  P7KPbc_C = 'P7KPbc_C',
  PcP = 'PcP',
  PKKPab = 'PKKPab',
  PKKSdf = 'PKKSdf',
  PKP3 = 'PKP3',
  PKPbc = 'PKPbc',
  PKSdf = 'PKSdf',
  pPdiff = 'pPdiff',
  PPP = 'PPP',
  pSdiff = 'pSdiff',
  Sb = 'Sb',
  SKKPab = 'SKKPab',
  SKKSdf = 'SKKSdf',
  SKS2 = 'SKS2',
  Sn = 'Sn',
  sPKP = 'sPKP',
  SS_B = 'SS_B',
  SSS_B = 'SSS_B',
  nP = 'nP',
  P3KPdf_B = 'P3KPdf_B',
  P5KPdf = 'P5KPdf',
  P7KPdf = 'P7KPdf',
  PcS = 'PcS',
  PKKPbc = 'PKKPbc',
  PKP = 'PKP',
  PKP3ab = 'PKP3ab',
  PKPdf = 'PKPdf',
  Pn = 'Pn',
  pPKiKP = 'pPKiKP',
  PPP_B = 'PPP_B',
  pSKS = 'pSKS',
  ScP = 'ScP',
  SKKPbc = 'SKKPbc',
  SKP = 'SKP',
  SKS2ac = 'SKS2ac',
  SnSn = 'SnSn',
  sPKPab = 'sPKPab',
  sSdiff = 'sSdiff',
  NP_1 = 'NP_1',
  P4KPbc = 'P4KPbc',
  P5KPdf_B = 'P5KPdf_B',
  P7KPdf_B = 'P7KPdf_B',
  Pdiff = 'Pdiff',
  PKKPdf = 'PKKPdf',
  PKP2 = 'PKP2',
  PKP3bc = 'PKP3bc',
  PKPPKP = 'PKPPKP',
  PnPn = 'PnPn',
  pPKP = 'pPKP',
  PPS = 'PPS',
  pSKSac = 'pSKSac',
  ScS = 'ScS',
  SKKPdf = 'SKKPdf',
  SKPab = 'SKPab',
  SKS2df = 'SKS2df',
  sP = 'sP',
  sPKPbc = 'sPKPbc',
  sSKS = 'sSKS',
  P4KPdf = 'P4KPdf',
  P5KPdf_C = 'P5KPdf_C',
  P7KPdf_C = 'P7KPdf_C',
  Pg = 'Pg',
  PKKS = 'PKKS',
  PKP2ab = 'PKP2ab',
  PKP3df = 'PKP3df',
  PKS = 'PKS',
  pP = 'pP',
  pPKPab = 'pPKPab',
  PPS_B = 'PPS_B',
  pSKSdf = 'pSKSdf',
  Sdiff = 'Sdiff',
  SKKS = 'SKKS',
  SKPbc = 'SKPbc',
  SKSac = 'SKSac',
  SP_1 = 'SP_1',
  sPKPdf = 'sPKPdf',
  sSKSac = 'sSKSac',
  Sx = 'Sx',
  Tx = 'Tx',
  tx = 'tx',
  N = 'N',
  Px = 'Px',
  IPx = 'IPx',
  PKhKP = 'PKhKP',
  UNKNOWN = 'UNKNOWN'
}
