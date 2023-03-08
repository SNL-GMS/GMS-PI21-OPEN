import v4 from 'uuid/v4';

/**
 * Generate a UUID string
 */
function asString(): string {
  return v4().toString();
}

/**
 * Generate a UUID string
 */
export const uuid4 = (): string => v4().toString();

export const uuid = {
  asString
};
