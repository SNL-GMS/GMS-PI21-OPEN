import type { ConvertPropertiesToType } from '../type-util/type-util';
import type { AllTimeKeys } from './types';

/**
 * Converts Time properties to the type `string` to match OSD time types.
 */
export type ToOSDTime<T> = ConvertPropertiesToType<T, AllTimeKeys, string>;

/**
 * Converts Time properties to the type `number` to match UI time types.
 */
export type ToUITime<T> = ConvertPropertiesToType<T, AllTimeKeys, number>;
