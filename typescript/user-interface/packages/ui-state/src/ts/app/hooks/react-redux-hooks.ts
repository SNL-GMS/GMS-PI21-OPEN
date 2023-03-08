import type { TypedUseSelectorHook } from 'react-redux';
import { useDispatch, useSelector } from 'react-redux';

import type { AppDispatch, AppState } from '../store';

/**
 * Typed Redux `useDispatch` hook.
 */
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const useAppDispatch = () => useDispatch<AppDispatch>();

/**
 * Typed Redux `useSelector` hook.
 */
export const useAppSelector: TypedUseSelectorHook<AppState> = useSelector;
