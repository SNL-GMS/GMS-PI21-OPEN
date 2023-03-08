import { EventTypes } from '@gms/common-model';

import type { EventRow } from '../../../../../src/ts/components/analyst-ui/components/events/types';
import { EdgeTypes } from '../../../../../src/ts/components/analyst-ui/components/events/types';

export const dummyData: EventRow = {
  time: 12345,
  edgeEventType: EdgeTypes.BEFORE,
  activeAnalysts: ['Chillas', 'Echidnas', 'I&T', 'Platform', 'SMEs'],
  conflict: true,
  depthKm: 0.611111111,
  id: '67026c63-0a6f-4aad-b5f8-f359ccc681e3',
  latitudeDegrees: 41.34311111111,
  longitudeDegrees: 129.036,
  magnitudeMb: 5.211111111,
  magnitudeMs: 4.911111111,
  magnitudeMl: 5.011111111,
  confidenceSemiMajorAxis: 120.2511111111,
  confidenceSemiMinorAxis: 67.4111111111,
  coverageSemiMajorAxis: 820.2444444444,
  coverageSemiMinorAxis: 677.4944444444,
  preferred: 'Chillas',
  region: 'Global',
  status: EventTypes.EventStatus.IN_PROGRESS,
  isOpen: false,
  rejected: 'true'
};

export const dummyData2: EventRow = {
  time: 12345,
  edgeEventType: EdgeTypes.INTERVAL,
  activeAnalysts: ['Bob', 'David', 'Bill'],
  conflict: false,
  depthKm: 0.844444444,
  id: '67026c63-0a6f-4aad-b5f8-f359ccc681e4',
  latitudeDegrees: 41.34744444444,
  longitudeDegrees: 129.03844444444,
  magnitudeMb: 5.244444444,
  magnitudeMs: 4.944444444,
  magnitudeMl: 5.044444444,
  confidenceSemiMajorAxis: 120.2511111111,
  confidenceSemiMinorAxis: 67.4111111111,
  coverageSemiMajorAxis: 820.2444444444,
  coverageSemiMinorAxis: 677.4944444444,
  preferred: 'Bob',
  region: 'Universe',
  status: EventTypes.EventStatus.COMPLETE,
  isOpen: false,
  rejected: 'false'
};
export const dummyData3: EventRow = {
  time: 123456789,
  edgeEventType: EdgeTypes.INTERVAL,
  activeAnalysts: ['Larry', 'Moe', 'Curly'],
  conflict: false,
  depthKm: 0.544444444,
  id: '67026c63-0a6f-4aad-b5f8-f359ccc681e5',
  latitudeDegrees: 141.41344444444,
  longitudeDegrees: 29.03644444444,
  magnitudeMb: 1.144444444,
  magnitudeMs: 1.044444444,
  magnitudeMl: 1.644444444,
  confidenceSemiMajorAxis: 120.2511111111,
  confidenceSemiMinorAxis: 67.4111111111,
  coverageSemiMajorAxis: 820.2444444444,
  coverageSemiMinorAxis: 677.4944444444,
  preferred: 'Larry',
  region: 'Cinematic',
  status: EventTypes.EventStatus.IN_PROGRESS,
  isOpen: false,
  rejected: 'false'
};

export const dummyData4: EventRow = {
  time: 789456123,
  edgeEventType: EdgeTypes.AFTER,
  activeAnalysts: [],
  conflict: false,
  depthKm: 0.977777777,
  id: '67026c63-0a6f-4aad-b5f8-f359ccc681e6',
  latitudeDegrees: 42.34777777777,
  longitudeDegrees: 139.03877777777,
  magnitudeMb: 6.277777777,
  magnitudeMs: 5.977777777,
  magnitudeMl: 6.077777777,
  confidenceSemiMajorAxis: 120.2511111111,
  confidenceSemiMinorAxis: 67.4111111111,
  coverageSemiMajorAxis: 820.2444444444,
  coverageSemiMinorAxis: 677.4944444444,
  preferred: '',
  region: 'Multiverse',
  status: EventTypes.EventStatus.NOT_STARTED,
  isOpen: false,
  rejected: 'false'
};
