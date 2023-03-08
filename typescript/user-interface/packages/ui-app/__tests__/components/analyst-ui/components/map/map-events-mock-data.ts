import { EdgeTypes } from '../../../../../src/ts/components/analyst-ui/components/events/types';
import type { MapEventSource } from '../../../../../src/ts/components/analyst-ui/components/map/types';

export const mockedPreferredEventsResult: MapEventSource[] = [
  {
    id: 'ff1fb419-d872-400d-8a82-5e9428dc7f66',
    edgeEventType: EdgeTypes.INTERVAL,
    time: 1648052100,
    activeAnalysts: [],
    conflict: true,
    depthKm: 0,
    latitudeDegrees: 45,
    longitudeDegrees: 45,
    magnitudeMb: 2.3,
    magnitudeMs: undefined,
    magnitudeMl: undefined,
    confidenceSemiMajorAxis: 135.03471,
    confidenceSemiMinorAxis: 55.068661,
    coverageSemiMajorAxis: 135.03471,
    coverageSemiMinorAxis: 55.068661,
    preferred: '',
    region: 'Global',
    isOpen: false,
    status: undefined,
    rejected: 'false'
  },
  {
    id: '3c1dd730-2897-4e5c-9c69-85359c8d725f',
    edgeEventType: EdgeTypes.INTERVAL,
    time: 1648052100,
    activeAnalysts: [],
    conflict: true,
    depthKm: 0,
    latitudeDegrees: 75,
    longitudeDegrees: -117,
    magnitudeMb: 2.3,
    magnitudeMs: undefined,
    magnitudeMl: undefined,
    confidenceSemiMajorAxis: 135.03471,
    confidenceSemiMinorAxis: 55.068661,
    coverageSemiMajorAxis: 135.03471,
    coverageSemiMinorAxis: 55.068661,
    preferred: '',
    region: 'Global',
    isOpen: false,
    status: undefined,
    rejected: 'false'
  }
];

export const mockedNonPreferredEventsResult: MapEventSource[] = [
  {
    id: 'ff1fb419-d872-400d-8a82-5e9428dc7f66',
    edgeEventType: EdgeTypes.INTERVAL,
    time: 1648052100,
    activeAnalysts: [],
    conflict: true,
    depthKm: 0,
    latitudeDegrees: 45,
    longitudeDegrees: 45,
    magnitudeMb: 2.3,
    magnitudeMs: undefined,
    magnitudeMl: undefined,
    confidenceSemiMajorAxis: 135.03471,
    confidenceSemiMinorAxis: 55.068661,
    coverageSemiMajorAxis: 135.03471,
    coverageSemiMinorAxis: 55.068661,
    preferred: '',
    region: 'Global',
    isOpen: false,
    status: undefined,
    rejected: 'False'
  },
  {
    id: '3c1dd730-2897-4e5c-9c69-85359c8d725f',
    edgeEventType: EdgeTypes.INTERVAL,
    time: 1648052100,
    activeAnalysts: [],
    conflict: true,
    depthKm: 0,
    latitudeDegrees: 75,
    longitudeDegrees: -117,
    magnitudeMb: 2.3,
    magnitudeMs: undefined,
    magnitudeMl: undefined,
    confidenceSemiMajorAxis: 135.03471,
    confidenceSemiMinorAxis: 55.068661,
    coverageSemiMajorAxis: 135.03471,
    coverageSemiMinorAxis: 55.068661,
    preferred: '',
    region: 'Global',
    isOpen: false,
    status: undefined,
    rejected: 'False'
  }
];