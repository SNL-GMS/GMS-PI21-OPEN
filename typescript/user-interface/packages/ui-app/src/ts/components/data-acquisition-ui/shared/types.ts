import type React from 'react';

export interface Offset {
  left: number;
  top: number;
}

export interface DragCellProps {
  stationId: string;
  getSelectedStationIds(): string[];
  setSelectedStationIds(ids: string[]): void;
  getSingleDragImage(e: React.DragEvent): HTMLElement;
}
