/**
 * Converts any properties of Type that match a key in the list of keys to be of the type ReplacementType.
 *
 * For example: given a type Foo { id: number; name: string; }
 * ConvertPropertiesToType<Foo, 'id' | 'string' | 'missing-key', 'Tom' | 'Jerry'>
 * returns { id: 'Tom' | 'Jerry'; name: 'Tom' | 'Jerry'; }
 * Note that it ignores the key 'missing-key' because it was not a match for any keys within T.
 *
 * @param Type the type on which to operate
 * @param Keys a list of keys that should be replaced if they are found in @param Type
 * @param ReplacementType the type that any parameters matching the @param Keys should be replaced with.
 */
export type ConvertPropertiesToType<Type, Keys, ReplacementType> = {
  [Property in keyof Type]: Property extends Keys ? ReplacementType : Type[Property];
};
