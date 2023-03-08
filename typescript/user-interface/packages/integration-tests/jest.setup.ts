// eslint-disable-next-line @typescript-eslint/no-explicit-any
const globalAny: any = global;

// TODO: Remove this `raf` polyfill once the below issue is sorted
// https://github.com/facebookincubator/create-react-app/issues/3199#issuecomment-332842582
// @see https://medium.com/@barvysta/warning-react-depends-on-requestanimationframe-f498edd404b3
// eslint-disable-next-line no-multi-assign
export const raf = (globalAny.requestAnimationFrame = cb => {
  setTimeout(cb, 0);
});

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
jest.setTimeout(60000);
