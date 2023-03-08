import type { FilterBandType } from './filter-band-type';
import type { FilterDesignModel } from './filter-design-model';
import type { IIRFilterParameters } from './iir-filter-parameters';

export interface LinearIIRFilterDescriptionModule {
  build: (
    iirFilterParameters: IIRFilterParameters,
    filterDesignModel: FilterDesignModel,
    filterBandType: FilterBandType,
    cutoffLow: number,
    cutoffHigh: number,
    filterOrder: number,
    sampleRate: number,
    sampleRateTolerance: number,
    zeroPhase: number,
    taper: number
  ) => LinearIIRFilterDescription;
}

export interface LinearIIRFilterDescription {
  iirFilterParameters: IIRFilterParameters;
  filterDesignModel: {
    value: FilterDesignModel;
  };
  filterBandType: {
    value: FilterBandType;
  };
  cutoffLow: number;
  cutoffHigh: number;
  filterOrder: number;
  sampleRate: number;
  sampleRateTolerance: number;
  zeroPhase: number;
  taper: number;
  delete: () => void;
}
