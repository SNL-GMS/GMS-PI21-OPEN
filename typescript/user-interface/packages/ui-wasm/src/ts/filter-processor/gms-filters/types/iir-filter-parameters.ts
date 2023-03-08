import type { VectorDouble } from './vector-double';

export interface IIRFilterParametersModule {
  build: (
    sosNumerator: VectorDouble,
    sosDenominator: VectorDouble,
    sosCoefficients: VectorDouble,
    isDesigned: boolean,
    numberOfSos: number,
    groupDelay: number
  ) => IIRFilterParameters;

  buildWithTypedArray: (
    sosNumerator: Float64Array,
    sosDenominator: Float64Array,
    sosCoefficients: Float64Array,
    isDesigned: boolean,
    numberOfSos: number,
    groupDelay: number
  ) => IIRFilterParameters;
}

export interface IIRFilterParameters {
  sosNumerator: VectorDouble;
  sosDenominator: VectorDouble;
  sosCoefficients: VectorDouble;
  isDesigned: boolean;
  numberOfSos: number;
  groupDelay: number;
  getSosNumeratorAsTypedArray: () => Float64Array;
  getSosDenominatorAsTypedArray: () => Float64Array;
  getSosCoefficientsAsTypedArray: () => Float64Array;
  delete: () => void;
}
