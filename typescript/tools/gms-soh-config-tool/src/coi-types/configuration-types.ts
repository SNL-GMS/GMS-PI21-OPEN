export const stationGroupNamesConfigName = 'soh-control.station-group-names';
export const stationCapabilityRollupConfigName =
  'soh-control.station-capability-rollup';
export const channelCapabilityRollupConfigName =
  'soh-control.channel-capability-rollup';
export const monitorTypesForRollupChannelConfigName =
  'soh-control.soh-monitor-types-for-rollup-channel';

export interface ConfigurationOption<P = Record<string, any>, C = unknown> {
  name: string;
  constraints: Constraint<C>[];
  parameters: P;
  priority?: number;
}

export enum ConstraintType {
  BOOLEAN = 'BOOLEAN',
  DEFAULT = 'DEFAULT',
  NUMERIC_RANGE = 'NUMERIC_RANGE',
  NUMERIC_SCALAR = 'NUMERIC_SCALAR',
  PHASE = 'PHASE',
  STRING = 'STRING',
  TIME_OF_DAY_RANGE = 'TIME_OF_DAY_RANGE',
  TIME_OF_YEAR_RANGE = 'TIME_OF_YEAR_RANGE',
  WILDCARD = 'WILDCARD',
}

export type StationConstraint = Constraint<
  ConstraintType.STRING,
  'StationName',
  {
    type: OperatorType.IN;
    negated: false;
  },
  [string]
>;

export type StationGroupNameConstraint = Constraint<
  ConstraintType.STRING,
  'StationGroupName',
  {
    type: OperatorType.IN;
    negated: false;
  },
  [string]
>;

export type ChannelConstraint = Constraint<
  ConstraintType.STRING,
  'ChannelName',
  {
    type: OperatorType.IN;
    negated: false;
  },
  [string]
>;

export type MonitorTypeConstraint = Constraint<
  ConstraintType.STRING,
  'MonitorType',
  {
    type: OperatorType.IN;
    negated: false;
  },
  [string]
>;

export enum OperatorType {
  IN = 'IN',
  EQ = 'EQ',
}

export interface Operator {
  type: OperatorType;
  negated: boolean;
}

export interface Constraint<
  C = ConstraintType,
  Criterion = string,
  O = Operator,
  V = any
> {
  constraintType: C;
  criterion?: Criterion;
  operator?: O;
  value?: V;
  priority?: number;
}
