import { TimeTypes } from '@gms/common-model';
import {
  convertDurationToSeconds,
  IS_MODE_SOH,
  isArrayOfObjects,
  isNumber,
  isObject,
  isObjectDefined,
  isString,
  MILLISECONDS_IN_SECOND,
  REGEX_ISO_DATE_TIME,
  REGEX_USER_CURRENT_TIME,
  Timer,
  toEpochSeconds,
  toOSDTime
} from '@gms/common-util';
import type { AxiosTransformer } from 'axios';
import Axios from 'axios';
import produce from 'immer';
import moment from 'moment';
import msgpack from 'msgpack-lite';

export const JSON_ARRIVAL_TIME_VALUE_NAMES = ['arrivalTime', 'startTime'];
export const JSON_DURATION_VALUE_NAMES = ['travelTime', 'duration'];

interface SerializableMatchers {
  [id: string]: FindAndReplaceMatcher[];
}

/**
 * Defines a the properties of a FindReplaceMatcher
 */
interface FindAndReplaceMatcher {
  /** the matcher function that validates the value */
  readonly matcher: (value: unknown) => boolean;
  /** the replace function that performs the value replace */
  readonly replacer: (value: unknown) => any;
}

/**
 * Performs a replace on the provided object and key using the provided matchers.
 *
 * ! Modifies the object in place
 *
 * @param matchers the matchers used to find and replace
 * @param object the object to search
 * @param key the key indexed into the object
 */
const replace = (matchers: SerializableMatchers, object: unknown, key: string): void => {
  /* eslint-disable no-param-reassign */
  if (matchers[key] !== undefined) {
    // use .some() instead of .forEach so we can break out of it by returning true
    // This ensures that once a transform has been called the rest are ignored
    matchers[key].some(frMatcher => {
      if (frMatcher.matcher(object[key])) {
        // transform the existing data with the provided replacer function
        object[key] = frMatcher.replacer(object[key]);
        return true;
      }
      return false;
    });
  }
};
/**
 * A matcher for any fields that are custom objects and need to be processed directly rather
 * then recursively called on each field
 *
 * @param object
 * @param key
 * @returns
 */
const isCustomObject = (object: unknown, key: string) => {
  return (
    isObject(object[key]) &&
    (JSON_ARRIVAL_TIME_VALUE_NAMES.includes(key) || JSON_DURATION_VALUE_NAMES.includes(key))
  );
};

/**
 * Performs a find and replace on the provided object for the given matchers.
 * ! Modifies the object in place
 *
 * @param matchers the matchers used to find and replace, where key is the object key
 * @param object the object to search
 * @param key (optional) the key indexed into the object, will be undefined if
 * at the root of an object
 */
const findAndReplace = (matchers: SerializableMatchers, object: unknown, key?: string): void => {
  /* eslint-disable no-param-reassign */
  if (isObjectDefined(object)) {
    // search the root object; key is undefined
    if (!key) {
      if (isArrayOfObjects(object, true)) {
        object.forEach(item => findAndReplace(matchers, item));
      } else if (isObject(object)) {
        Object.keys(object).forEach(k => findAndReplace(matchers, object, k));
      }
    } else if (isObjectDefined(object[key])) {
      if (isArrayOfObjects(object[key], true)) {
        object[key].forEach(item => findAndReplace(matchers, item));
      } else if (isString(object[key]) || isNumber(object[key]) || isCustomObject(object, key)) {
        // perform replace for the provided matchers
        replace(matchers, object, key);
      } else if (isObject(object[key])) {
        Object.keys(object[key]).forEach(k => findAndReplace(matchers, object[key], k));
      }
    } else {
      // transform the existing data to `undefined`
      object[key] = undefined;
    }
  }
};

/**
 * The deserialize matchers - defines the key/values to search for and their replace functions
 * Multiple matchers can be added to a single field name and will be ran in the order they are added
 * Once a transform has been run no more matchers will be checked so matchers should be added from most common to least
 */
const deserializeMatchers: SerializableMatchers = {};
TimeTypes.JSON_INSTANT_NAMES.forEach(key => {
  if (deserializeMatchers[key] === undefined) deserializeMatchers[key] = [];
  deserializeMatchers[key].push({
    // ** special case: handle the string `CurrentUserTime`
    matcher: value =>
      REGEX_ISO_DATE_TIME.test(value.toString()) || REGEX_USER_CURRENT_TIME.test(value.toString()),
    replacer: value =>
      REGEX_USER_CURRENT_TIME.test(value.toString())
        ? Date.now() / MILLISECONDS_IN_SECOND
        : toEpochSeconds(value.toString())
  });
});
TimeTypes.JSON_DURATION_NAMES.forEach(key => {
  if (deserializeMatchers[key] === undefined) deserializeMatchers[key] = [];
  deserializeMatchers[key].push({
    // Complex objects with become "P0D" when passed to moment.duration so check for string first
    matcher: value => isString(value) && moment.duration(value).isValid(),
    replacer: value => convertDurationToSeconds(value.toString())
  });
});

JSON_ARRIVAL_TIME_VALUE_NAMES.forEach(key => {
  if (deserializeMatchers[key] === undefined) deserializeMatchers[key] = [];
  deserializeMatchers[key].push({
    matcher: (value: any) => value.value !== undefined,
    replacer: (value: any) => {
      if (value.value !== undefined) {
        value.value = REGEX_USER_CURRENT_TIME.test(value.toString())
          ? Date.now() / MILLISECONDS_IN_SECOND
          : toEpochSeconds(value.value.toString());
      }

      if (value.standardDeviation !== undefined) {
        value.standardDeviation = convertDurationToSeconds(value.standardDeviation);
      }
      return value;
    }
  });
});

JSON_DURATION_VALUE_NAMES.forEach(key => {
  if (deserializeMatchers[key] === undefined) deserializeMatchers[key] = [];
  deserializeMatchers[key].push({
    matcher: (value: any) => value.value !== undefined,
    replacer: (value: any) => {
      if (value.value !== undefined) {
        value.value = convertDurationToSeconds(value.value);
      }
      if (value.standardDeviation !== undefined) {
        value.standardDeviation = convertDurationToSeconds(value.standardDeviation);
      }
      return value;
    }
  });
});

/**
 * The serialize matchers - defines the key/values to search for and their replace functions
 * Multiple matchers can be added to a single field name and will be ran in the order they are added
 * Once a transform has been run no more matchers will be checked so matchers should be added from most common to least
 */
const serializeMatchers: SerializableMatchers = {};
TimeTypes.JSON_INSTANT_NAMES.forEach(key => {
  if (serializeMatchers[key] === undefined) serializeMatchers[key] = [];
  serializeMatchers[key].push({
    matcher: value => isNumber(value),
    replacer: value => {
      if (isNumber(value)) return toOSDTime(Number(value));
      return value;
    }
  });
});
TimeTypes.JSON_DURATION_NAMES.forEach(key => {
  if (serializeMatchers[key] === undefined) serializeMatchers[key] = [];
  serializeMatchers[key].push({
    matcher: value => isNumber(value),
    replacer: value => moment.duration(Number(value) * MILLISECONDS_IN_SECOND).toISOString()
  });
});

JSON_ARRIVAL_TIME_VALUE_NAMES.forEach(key => {
  if (serializeMatchers[key] === undefined) serializeMatchers[key] = [];
  serializeMatchers[key].push({
    matcher: (value: any) => value.value !== undefined,
    replacer: (value: any) => {
      value.value = toOSDTime(Number(value.value));
      if (value.standardDeviation !== undefined)
        value.standardDeviation = moment.duration(
          Number(value.standardDeviation) * MILLISECONDS_IN_SECOND
        );

      return value;
    }
  });
});

JSON_DURATION_VALUE_NAMES.forEach(key => {
  if (serializeMatchers[key] === undefined) serializeMatchers[key] = [];
  serializeMatchers[key].push({
    matcher: (value: any) => value.value !== undefined,
    replacer: (value: any) => {
      if (value.value !== undefined) {
        value.value = moment.duration(Number(value.value) * MILLISECONDS_IN_SECOND);
      }
      if (value.standardDeviation !== undefined) {
        value.standardDeviation = moment.duration(
          Number(value.standardDeviation) * MILLISECONDS_IN_SECOND
        );
      }
      return value;
    }
  });
});

/**
 * The Axios built-in response transformers.
 *
 * @returns returns the default Axios Response Transformers as an array
 */
export const axiosDefaultResponseTransformers = (): AxiosTransformer[] => {
  const { transformResponse } = Axios.defaults;
  if (!transformResponse) {
    return [];
  }
  return transformResponse instanceof Array ? transformResponse : [transformResponse];
};

/**
 * The Axios built-in request transformers.
 *
 * @returns returns the default Axios Request Transformers as an array
 */
export const axiosDefaultRequestTransformers = (): AxiosTransformer[] => {
  const { transformRequest } = Axios.defaults;
  if (!transformRequest) {
    return [];
  }
  return transformRequest instanceof Array ? transformRequest : [transformRequest];
};

/**
 * A custom Axios Transformer that decodes message pack encoded data. If the data
 * is not encoded, the value is just returned.
 *
 * @param data The data
 * @param headers The headers
 *
 * @return the transformed data from the configured transformer
 */
export const msgPackDecodeTransformer: AxiosTransformer = (
  data: unknown,
  headers?: unknown
): unknown => {
  if (
    headers &&
    Object.prototype.hasOwnProperty.call(headers, 'content-type') &&
    headers['content-type'] === 'application/msgpack'
  ) {
    Timer.start('[axios]: msgpack decode axios transformer');
    const decoded = msgpack.decode(Buffer.from(data as string));
    Timer.end('[axios]: msgpack decode axios transformer');
    return decoded;
  }
  return data;
};

/**
 * Creates a custom Axios Transformer that uses the provided `matchers` to
 * serialize or deserialize key/values within the data.
 *
 * @param matchers the find and replace matchers
 *
 * @return the Axios transformer
 */
const createTypeTransformer = (matchers: SerializableMatchers): AxiosTransformer => (
  data: unknown
) => {
  if (!data || Object.keys(data).length === 0) {
    return data;
  }
  return produce(data, draft => findAndReplace(matchers, draft));
};

/**
 * A custom Axios type transformers for deserializing types.
 * ! Modifies the object in place for performance
 */
export const deserializeTypeTransformer: AxiosTransformer = createTypeTransformer(
  deserializeMatchers
);

/**
 * A custom Axios type transformers for serializing types and decoding msgPack.
 * ! Modifies the object in place for performance
 */
export const serializeTypeTransformer: AxiosTransformer = createTypeTransformer(serializeMatchers);

const maybeDeserializeTransformer = () => {
  if (process.env.GMS_SW === 'false' || IS_MODE_SOH) {
    return [msgPackDecodeTransformer, deserializeTypeTransformer];
  }
  return [];
};

const maybeSerializeTypeTransformer = () => {
  if (process.env.GMS_SW === 'false' || IS_MODE_SOH) {
    return [serializeTypeTransformer];
  }
  return [];
};

/**
 * The default Axios Response Transformers.
 */
export const defaultResponseTransformers: AxiosTransformer[] = [
  ...axiosDefaultResponseTransformers(),
  ...maybeDeserializeTransformer()
];

/**
 * The default Axios Request Transformers.
 */
export const defaultRequestTransformers: AxiosTransformer[] = [
  ...axiosDefaultRequestTransformers(),
  ...maybeSerializeTypeTransformer()
];
