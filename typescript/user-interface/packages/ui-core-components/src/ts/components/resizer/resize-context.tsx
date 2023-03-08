import * as React from 'react';

export interface ResizeData {
  height: number;
  isResizing: boolean;
  containerHeight: number;
  setIsResizing(isIt: boolean): void;
  setHeight(height: number): void;
}

export const ResizeContext: React.Context<ResizeData> = React.createContext<ResizeData>(undefined);
