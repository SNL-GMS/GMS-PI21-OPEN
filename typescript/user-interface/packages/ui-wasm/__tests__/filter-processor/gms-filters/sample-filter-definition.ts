/* eslint-disable @typescript-eslint/no-magic-numbers */
import type {
  CascadedFilterDefinition,
  CascadedFilterDescription,
  LinearFilterDefinition,
  LinearFilterDescription
} from '@gms/common-model/lib/filter-list/types';
import { BandType, FilterType } from '@gms/common-model/lib/filter-list/types';

const linearFilterDescription: LinearFilterDescription = Object.freeze({
  filterType: FilterType.IIR_BUTTERWORTH,
  causal: true,
  comments: 'Test description comments',
  highFrequency: 0.8,
  lowFrequency: 0.3,
  order: 2,
  passBandType: BandType.BAND_PASS,
  zeroPhase: true,
  parameters: {
    aCoefficients: [4.4, 5.5, 6.6],
    bCoefficients: [1.1, 2.2, 3.3],
    groupDelaySec: 1,
    sampleRateHz: 40,
    sampleRateToleranceHz: 20
  }
});

export const sampleFilterDefinition: LinearFilterDefinition = Object.freeze({
  name: 'Sample Filter Definition Name',
  comments: 'Sample Filter Definition Comments',
  filterDescription: linearFilterDescription
});

const linearFilterDescriptionDesigned: LinearFilterDescription = Object.freeze({
  filterType: FilterType.IIR_BUTTERWORTH,
  causal: true,
  comments: 'Test description comments',
  highFrequency: 0.8,
  lowFrequency: 0.3,
  order: 2,
  passBandType: BandType.BAND_PASS,
  zeroPhase: true,
  parameters: {
    aCoefficients: [1, -1.9621912635723384],
    bCoefficients: [0.03863156463858642, 0],
    groupDelaySec: 0,
    sampleRateHz: 40,
    sampleRateToleranceHz: 20
  }
});

export const sampleFilterDefinitionDesigned: LinearFilterDefinition & {
  filterDescription: LinearFilterDescription;
} = Object.freeze({
  name: 'Sample Filter Definition Name',
  comments: 'Sample Filter Definition Comments',
  filterDescription: linearFilterDescriptionDesigned
});

const cascadedFilterDescription: CascadedFilterDescription = Object.freeze({
  comments: 'description comments',
  causal: true,
  filterType: FilterType.CASCADE,
  filterDescriptions: [
    {
      causal: true,
      comments: 'Test description 1 comments',
      filterType: FilterType.IIR_BUTTERWORTH,
      highFrequency: 0.8,
      lowFrequency: 0.3,
      order: 1,
      passBandType: BandType.BAND_PASS,
      zeroPhase: false,
      parameters: {
        aCoefficients: [4.4, 5.5, 6.6],
        bCoefficients: [1.1, 2.2, 3.3],
        groupDelaySec: 1,
        sampleRateHz: 30,
        sampleRateToleranceHz: 25
      }
    } as LinearFilterDescription,
    {
      causal: false,
      comments: 'Test description 2 comments',
      filterType: FilterType.IIR_BUTTERWORTH,
      highFrequency: 0.9,
      lowFrequency: 0.4,
      order: 2,
      passBandType: BandType.BAND_PASS,
      zeroPhase: true,
      parameters: {
        aCoefficients: [10.1, 11.11, 12.12],
        bCoefficients: [7.7, 8.8, 9.9],
        groupDelaySec: 2,
        sampleRateHz: 40,
        sampleRateToleranceHz: 20
      }
    } as LinearFilterDescription
  ],
  parameters: {
    groupDelaySec: 1,
    sampleRateHz: 40,
    sampleRateToleranceHz: 20
  }
});

export const sampleCascadedFilterDefinition: CascadedFilterDefinition = Object.freeze({
  name: 'Sample Cascaded Filter Definition Name',
  comments: 'Sample Cascaded Filter Definition Comments',
  filterDescription: cascadedFilterDescription
});

const cascadedFilterDescriptionDesigned: CascadedFilterDescription = Object.freeze({
  comments: 'description comments',
  causal: true,
  filterType: FilterType.CASCADE,
  filterDescriptions: [
    {
      causal: true,
      comments: 'Test description 1 comments',
      filterType: FilterType.IIR_BUTTERWORTH,
      highFrequency: 0.8,
      lowFrequency: 0.3,
      order: 1,
      passBandType: BandType.BAND_PASS,
      zeroPhase: false,
      parameters: {
        aCoefficients: [1],
        bCoefficients: [0.04979797785108003],
        groupDelaySec: 0,
        sampleRateHz: 30,
        sampleRateToleranceHz: 25
      }
    } as LinearFilterDescription,
    {
      causal: false,
      comments: 'Test description 2 comments',
      filterType: FilterType.IIR_BUTTERWORTH,
      highFrequency: 0.9,
      lowFrequency: 0.4,
      order: 2,
      passBandType: BandType.BAND_PASS,
      zeroPhase: true,
      parameters: {
        aCoefficients: [1, -1.9567334488416415],
        bCoefficients: [0.03857248988702733, 0],
        groupDelaySec: 0,
        sampleRateHz: 40,
        sampleRateToleranceHz: 20
      }
    } as LinearFilterDescription
  ],
  parameters: {
    groupDelaySec: 0,
    sampleRateHz: 40,
    sampleRateToleranceHz: 20
  }
});

export const sampleCascadedFilterDefinitionDesigned: CascadedFilterDefinition = Object.freeze({
  name: 'Sample Cascaded Filter Definition Name',
  comments: 'Sample Cascaded Filter Definition Comments',
  filterDescription: cascadedFilterDescriptionDesigned
});

export const smallSampleData = new Float64Array([1, 2, 3, 4, 5, 6, 7, 8, 9]);

export const smallSampleDataResult = new Float64Array([1, 2, 3, 4, 5, 6, 7, 8, 9]);
