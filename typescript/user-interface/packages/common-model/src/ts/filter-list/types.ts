import type { WorkflowDefinitionId } from '../workflow/types';

export interface FilterListsDefinition {
  preferredFilterListByActivity: FilterListActivity[];
  filterLists: FilterList[];
}
export interface FilterListActivity {
  name: string;
  workflowDefinitionId: WorkflowDefinitionId;
}
export interface FilterList {
  name: string;
  defaultFilterIndex: number;
  filters: Filter[];
}
export interface Filter {
  withinHotKeyCycle: boolean | null;
  unfiltered: boolean | null;
  namedFilter: string | null;
  filterDefinition: FilterDefinition | null;
}

export enum FilterType {
  // format: `FilterComputationType_FilterDesignModel`
  CASCADE = 'CASCADE', // ! TODO Not the correct format
  IIR_BUTTERWORTH = 'IIR_BUTTERWORTH',
  FIR_HAMMING = 'FIR_HAMMING'
}

export enum BandType {
  LOW_PASS = 'LOW_PASS',
  HIGH_PASS = 'HIGH_PASS',
  BAND_PASS = 'BAND_PASS',
  BAND_REJECT = 'BAND_REJECT'
}

export interface FilterDefinition {
  name: string;
  comments: string;
  filterDescription: LinearFilterDescription | CascadedFilterDescription;
}

export interface LinearFilterDefinition extends FilterDefinition {
  filterDescription: LinearFilterDescription;
}

export interface CascadedFilterDefinition extends FilterDefinition {
  filterDescription: CascadedFilterDescription;
}

export interface FilterDescription {
  filterType: FilterType;
  comments: string;
  causal: boolean;
}

export interface LinearFilterParameters {
  sampleRateHz: number;
  sampleRateToleranceHz: number;
  aCoefficients: number[];
  bCoefficients: number[];
  groupDelaySec: number;
}

export interface CascadedFilterParameters {
  sampleRateHz: number;
  sampleRateToleranceHz: number;
  groupDelaySec: number;
}

export interface LinearFilterDescription extends FilterDescription {
  filterType: FilterType.IIR_BUTTERWORTH | FilterType.FIR_HAMMING;
  lowFrequency: number;
  highFrequency: number;
  order: number;
  zeroPhase: boolean;
  passBandType: BandType;
  parameters: LinearFilterParameters;
}

export interface CascadedFilterDescription extends FilterDescription {
  filterType: FilterType.CASCADE;
  parameters: CascadedFilterParameters;
  filterDescriptions: LinearFilterDescription[]; // TODO: add support for Cascades of Cascades
}

export function isLinearFilterDefinition(
  object: FilterDefinition
): object is LinearFilterDefinition {
  return object?.filterDescription.filterType !== FilterType.CASCADE;
}

export function isCascadedFilterDefinition(
  object: FilterDefinition
): object is CascadedFilterDefinition {
  return object?.filterDescription.filterType === FilterType.CASCADE;
}

export function isLinearFilterDescription(
  object: FilterDescription
): object is LinearFilterDescription {
  return object?.filterType !== FilterType.CASCADE;
}

export function isCascadedFilterDescription(
  object: FilterDescription
): object is CascadedFilterDescription {
  return object?.filterType === FilterType.CASCADE;
}
