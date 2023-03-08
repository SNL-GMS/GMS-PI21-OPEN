import type { CommonTypes } from '@gms/common-model';

export const isApiGatewayConnected = (object: CommonTypes.SystemEvent): boolean =>
  object.type === 'api-gateway-connected';
