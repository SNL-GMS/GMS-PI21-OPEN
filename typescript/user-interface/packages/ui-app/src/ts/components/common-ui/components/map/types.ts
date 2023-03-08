import type GoldenLayout from '@gms/golden-layout';
import type { Viewer } from 'cesium';
import type React from 'react';
import type { EntityCesiumReadonlyProps } from 'resium/dist/types/src/Entity/Entity';

export interface MapProps {
  glContainer?: GoldenLayout.Container;
  doMultiSelect?: boolean;
  minHeightPx: number;
  selectedStations?: string[];
  selectedEvents?: string[];
  selectedSdIds?: string[];
  entities?: JSX.Element[];
  dataSources?: JSX.Element[];
  handlers?: React.FunctionComponent<any>[];
}

export interface MapDataSourceProps {
  setCurrentlySelectedEntity(entity: EntityCesiumReadonlyProps): void;
}

export interface MapHandlerProps {
  viewer: Viewer;
  selectedStations?: string[];
  selectedEvents?: string[];
}

export enum TILE_LOAD_STATUS {
  NOT_LOADED = 'NOT_LOADED',
  LOADING = 'LOADING',
  LOADED = 'LOADED'
}
