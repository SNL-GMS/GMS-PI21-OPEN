import type { Location, Position, StationType } from '../common/types';

// ***************************************
// Model
// ***************************************

/**
 * DataAcquistion used by the Data Acq UIs as status on stations
 */
export interface DataAcquisition {
  dataAcquisition: string;
  interactiveProcessing: string;
  automaticProcessing: string;
  acquisition: string;
  pkiStatus: string;
  pkiInUse: string;
  processingPartition: string;
  storeOnDataAcquisitionPartition: string;
}
/**
 * Reference Channel Definition
 */
export interface ReferenceChannel {
  id: string;
  name?: string;
  channelType: string;
  sampleRate: number;
  position?: Position;
  actualTime?: string;
  systemTime?: string;
  depth?: number;
}

/**
 * Reference Site Definition
 */
export interface ReferenceSite {
  id: string;
  name?: string;
  channels: ReferenceChannel[];
  location: Location;
}

export interface ReferenceStation {
  id: string;
  name?: string;
  stationType: StationType;
  description: string;
  defaultChannel: ReferenceChannel;
  networks: {
    id: string;
    name: string;
    monitoringOrganization: string;
  }[];
  modified: boolean;
  location: Location;
  sites: ReferenceSite[];
  dataAcquisition: DataAcquisition;
  latitude: number;
  longitude: number;
  elevation: number;
}
