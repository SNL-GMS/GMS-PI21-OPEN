import type { electronAPIType } from '../electron/preload';
export const windowAPI = window as (typeof window & { electronAPI: electronAPIType });