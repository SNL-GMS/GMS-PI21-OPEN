import type { Weavess } from '@gms/weavess';
import * as React from 'react';

export interface WeavessContextData {
  weavessRef: Weavess;
  setWeavessRef: (weavess: Weavess) => void;
}

export const WeavessContext: React.Context<WeavessContextData> = React.createContext<
  WeavessContextData
>(undefined);
