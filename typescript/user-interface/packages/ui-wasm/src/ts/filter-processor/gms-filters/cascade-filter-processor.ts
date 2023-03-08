import type { CascadedFilterDefinition } from '@gms/common-model/lib/filter-list/types';
import { FilterType } from '@gms/common-model/lib/filter-list/types';
import { Timer, uuid } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import produce from 'immer';

import { indexInc, indexOffset, removeGroupDelay, taper } from './constants';
import { gmsFiltersModulePromise } from './gms-filters-module';
import type { CascadedFiltersParameters } from './types/cascade-filter-parameters';
import type { FilterDefinition as GMSFiltersFilterDefinition } from './types/filter-definition';
import type { FilterDescription } from './types/filter-description';
import type { IIRFilterParameters } from './types/iir-filter-parameters';
import type { LinearIIRFilterDescription } from './types/linear-iir-filter-definition';
import type { VectorFilterDescription } from './types/vector-filter-description';
import { getFilterBandType, getFilterComputationType, getFilterDesignModel } from './util';

const logger = UILogger.create('GMS_FILTERS', process.env.GMS_FILTERS);

/**
 * Converts a GMS COI Filter Definition to a GMS Filters Filter Definition
 *
 * ! Must properly free/delete the memory of the returned object
 *
 * @param filterDefinition a GMS COI Filter Definition to convert
 * @returns a converted GMS Filters Filter Definition
 */
export const convertToGMSFiltersFilterDefinition = async (
  filterDefinition: CascadedFilterDefinition
): Promise<GMSFiltersFilterDefinition> => {
  const gmsFiltersModule = await gmsFiltersModulePromise;

  if (filterDefinition.filterDescription.filterType !== FilterType.CASCADE) {
    throw new Error(`FilterType must be of type ${FilterType.CASCADE}`);
  }

  if (
    filterDefinition.filterDescription.filterDescriptions === undefined ||
    filterDefinition.filterDescription.filterDescriptions.length === 0
  ) {
    throw new Error(`Filter Descriptions should be defined for Cascade Filter Definition`);
  }

  filterDefinition.filterDescription.filterDescriptions.forEach(desc => {
    const { aCoefficients, bCoefficients } = desc.parameters;

    if (desc.filterType !== FilterType.IIR_BUTTERWORTH) {
      throw new Error(`FilterTyp type ${FilterType.IIR_BUTTERWORTH} is only supported`);
    }

    if (
      aCoefficients === undefined ||
      bCoefficients === undefined ||
      aCoefficients.length === 0 ||
      aCoefficients.length === 0
    ) {
      throw new Error('Invalid aCoefficients or bCoefficients');
    }

    if (aCoefficients.length !== bCoefficients.length) {
      throw new Error('Invalid aCoefficients or bCoefficients');
    }
  });

  let cascadedFiltersParameters: CascadedFiltersParameters;
  const iirFilterParameters: IIRFilterParameters[] = [];
  const linearIIRFilterDescription: LinearIIRFilterDescription[] = [];
  const filterDescription: FilterDescription[] = [];
  let vectorFilterDescription: VectorFilterDescription;
  let gmsFilterDefinition: GMSFiltersFilterDefinition;

  try {
    // * map GMS Filter Definition to GMS Filter Algorithm Definition

    cascadedFiltersParameters = gmsFiltersModule.CascadedFiltersParameters.build(
      filterDefinition.filterDescription.comments,
      filterDefinition.filterDescription.causal,
      filterDefinition.filterDescription.parameters.sampleRateHz,
      filterDefinition.filterDescription.parameters.sampleRateToleranceHz,
      filterDefinition.filterDescription.parameters.groupDelaySec
    );

    vectorFilterDescription = new gmsFiltersModule.VectorFilterDescription();
    filterDefinition.filterDescription.filterDescriptions.forEach((desc, i) => {
      const { aCoefficients, bCoefficients } = desc.parameters;

      // bCoefficients => sosNumerator
      const sosNumerator = new Float64Array(bCoefficients);

      // aCoefficients => sosDenominator
      const sosDenominator = new Float64Array(aCoefficients);

      // ! sosCoefficients can be ignored for now
      const sosCoefficients = new Float64Array([]);

      const numberOfSos = sosNumerator.length;

      const isDesigned = false;

      const groupDelay = desc.parameters.groupDelaySec;

      iirFilterParameters[i] = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
        sosNumerator,
        sosDenominator,
        sosCoefficients,
        isDesigned,
        numberOfSos,
        groupDelay
      );

      const filterDesignModel = getFilterDesignModel(desc.filterType);

      const filterComputationType = getFilterComputationType(desc.filterType);

      const filterBandType = getFilterBandType(desc.passBandType);

      const cutoffLow = desc.lowFrequency;
      const cutoffHigh = desc.highFrequency;
      const filterOrder = desc.order;
      const sampleRate = desc.parameters.sampleRateHz;
      const sampleRateTolerance = desc.parameters.sampleRateToleranceHz;
      const zeroPhase = +desc.zeroPhase;

      linearIIRFilterDescription[i] = gmsFiltersModule.LinearIIRFilterDescription.build(
        iirFilterParameters[i],
        filterDesignModel,
        filterBandType,
        cutoffLow,
        cutoffHigh,
        filterOrder,
        sampleRate,
        sampleRateTolerance,
        zeroPhase,
        taper
      );

      filterDescription[i] = gmsFiltersModule.FilterDescription.build(
        linearIIRFilterDescription[i],
        filterComputationType,
        desc.comments,
        desc.causal
      );

      vectorFilterDescription.push_back(filterDescription[i]);
    });

    gmsFilterDefinition = gmsFiltersModule.FilterDefinition.build(
      cascadedFiltersParameters,
      vectorFilterDescription,
      filterDefinition.name,
      filterDefinition.comments,
      false, // not designed
      removeGroupDelay,
      vectorFilterDescription.size()
    );
  } catch (e) {
    logger.error('Failed to design filter using GMS cascade filter design', e);
    throw e;
  } finally {
    // ! free any memory used for WASM
    /* eslint-disable no-underscore-dangle */
    cascadedFiltersParameters.delete();
    gmsFiltersModule._free(cascadedFiltersParameters as any);

    filterDefinition.filterDescription.filterDescriptions.forEach((_, i) => {
      iirFilterParameters[i].delete();
      gmsFiltersModule._free(iirFilterParameters[i] as any);

      linearIIRFilterDescription[i].delete();
      gmsFiltersModule._free(linearIIRFilterDescription[i] as any);

      filterDescription[i].delete();
      gmsFiltersModule._free(filterDescription[i] as any);
    });

    gmsFiltersModule._free(vectorFilterDescription as any);
    /* eslint-enable no-underscore-dangle */
  }

  return gmsFilterDefinition;
};

/**
 * Converts a GMS Filters Filter Definition to a GMS COI Filter Definition.
 *
 * @param filterDefinition a GMS COI Filter Definition
 * @param linearIIRFilterDescription a GMS Filters Filter Definition
 * @returns a GMS COI Filter Definition
 */
export const convertFromGMSFiltersFilterDefinition = (
  filterDefinition: CascadedFilterDefinition,
  gmsFilterDefinition: GMSFiltersFilterDefinition
): CascadedFilterDefinition => {
  // * map GMS Filter Algorithm Definition to GMS Filter Definition

  return produce(filterDefinition, draft => {
    draft.filterDescription.comments = gmsFilterDefinition.cascadedFiltersParameters.comments;
    draft.filterDescription.causal = gmsFilterDefinition.cascadedFiltersParameters.isCausal;
    draft.filterDescription.parameters.sampleRateHz =
      gmsFilterDefinition.cascadedFiltersParameters.sampleRate;
    draft.filterDescription.parameters.sampleRateToleranceHz =
      gmsFilterDefinition.cascadedFiltersParameters.sampleRateTolerance;
    draft.filterDescription.parameters.groupDelaySec =
      gmsFilterDefinition.cascadedFiltersParameters.groupDelay;

    for (let i = 0; i < gmsFilterDefinition.filterDescriptions.size(); i += 1) {
      // sosNumerator => bCoefficients
      draft.filterDescription.filterDescriptions[i].parameters.bCoefficients = Array.from(
        gmsFilterDefinition.filterDescriptions
          .get(i)
          .linearIIRFilterDescription.iirFilterParameters.getSosNumeratorAsTypedArray()
      );

      // aCoefficients => sosDenominator
      draft.filterDescription.filterDescriptions[i].parameters.aCoefficients = Array.from(
        gmsFilterDefinition.filterDescriptions
          .get(i)
          .linearIIRFilterDescription.iirFilterParameters.getSosDenominatorAsTypedArray()
      );

      // ! sosCoefficients can be ignored for now

      draft.filterDescription.filterDescriptions[
        i
      ].parameters.groupDelaySec = gmsFilterDefinition.filterDescriptions.get(
        i
      ).linearIIRFilterDescription.iirFilterParameters.groupDelay;
      draft.filterDescription.filterDescriptions[
        i
      ].lowFrequency = gmsFilterDefinition.filterDescriptions.get(
        i
      ).linearIIRFilterDescription.cutoffLow;
      draft.filterDescription.filterDescriptions[
        i
      ].highFrequency = gmsFilterDefinition.filterDescriptions.get(
        i
      ).linearIIRFilterDescription.cutoffHigh;
      draft.filterDescription.filterDescriptions[
        i
      ].order = gmsFilterDefinition.filterDescriptions.get(
        i
      ).linearIIRFilterDescription.filterOrder;
      draft.filterDescription.filterDescriptions[
        i
      ].parameters.sampleRateHz = gmsFilterDefinition.filterDescriptions.get(
        i
      ).linearIIRFilterDescription.sampleRate;
      draft.filterDescription.filterDescriptions[
        i
      ].parameters.sampleRateToleranceHz = gmsFilterDefinition.filterDescriptions.get(
        i
      ).linearIIRFilterDescription.sampleRateTolerance;
      draft.filterDescription.filterDescriptions[
        i
      ].zeroPhase = !!gmsFilterDefinition.filterDescriptions.get(i).linearIIRFilterDescription
        .zeroPhase;

      draft.filterDescription.filterDescriptions[
        i
      ].comments = gmsFilterDefinition.filterDescriptions.get(i).comments;

      draft.filterDescription.filterDescriptions[
        i
      ].causal = gmsFilterDefinition.filterDescriptions.get(i).isCausal;

      draft.name = gmsFilterDefinition.name;
      draft.comments = gmsFilterDefinition.comments;
    }
  });
};

/**
 * Designs a Cascaded Filter Definition
 *
 * @param filterDefinition the filter definition to design
 * @returns the designed filter definition
 */
export const cascadeFilterDesign = async (
  filterDefinition: CascadedFilterDefinition
): Promise<CascadedFilterDefinition> => {
  const id = uuid.asString();

  const gmsFiltersModule = await gmsFiltersModulePromise;

  let gmsFilterDefinition: GMSFiltersFilterDefinition;
  let result: GMSFiltersFilterDefinition;
  let draft: CascadedFilterDefinition;

  try {
    Timer.start(`${id} GMS Filter: cascade filter design`);

    gmsFilterDefinition = await convertToGMSFiltersFilterDefinition(filterDefinition);

    result = gmsFiltersModule.FilterProvider.filterCascadeDesign(gmsFilterDefinition);

    draft = convertFromGMSFiltersFilterDefinition(filterDefinition, result);
  } catch (e) {
    logger.error('Failed to design filter using GMS cascade filter design', e);
    throw e;
  } finally {
    Timer.end(`${id} GMS Filter: cascade filter design`);
    // ! free any memory used for WASM
    /* eslint-disable no-underscore-dangle */
    gmsFilterDefinition.delete();
    gmsFiltersModule._free(gmsFilterDefinition as any);

    result.delete();
    gmsFiltersModule._free(result as any);
    /* eslint-enable no-underscore-dangle */
  }

  return draft;
};

/**
 * Applies a Cascaded Filter Definition to the provided data (filters the data).
 *
 * @param filterDefinition a Cascaded Filter Definition
 * @param data  waveform data
 * @returns the filtered waveform data
 */
export const cascadeFilterApply = async (
  filterDefinition: CascadedFilterDefinition,
  data: Float64Array
): Promise<Float64Array> => {
  const id = uuid.asString();

  const gmsFiltersModule = await gmsFiltersModulePromise;

  Timer.start(`${id} GMS Filter: cascade filter`);

  let gmsFilterDefinition: GMSFiltersFilterDefinition;
  let result: Float64Array;

  try {
    gmsFilterDefinition = await convertToGMSFiltersFilterDefinition(filterDefinition);

    gmsFilterDefinition.isDesigned = true;
    filterDefinition.filterDescription.filterDescriptions.forEach((_, i) => {
      gmsFilterDefinition.filterDescriptions.get(
        i
      ).linearIIRFilterDescription.iirFilterParameters.isDesigned = true;
    });

    result = gmsFiltersModule.FilterProvider.filterCascadeApply(
      gmsFilterDefinition,
      data,
      indexOffset,
      indexInc
    );
  } catch (e) {
    logger.error('Failed to filter using GMS cascade filter', e);
    throw e;
  } finally {
    Timer.end(`${id} GMS Filter: cascade filter`);
    // ! free any memory used for WASM
    /* eslint-disable no-underscore-dangle */
    gmsFilterDefinition.delete();
    gmsFiltersModule._free(gmsFilterDefinition as any);
    /* eslint-enable no-underscore-dangle */
  }

  return result;
};
