export const clientConnectedMessage = 'CLIENT_CONNECTED' as const;
export const listenersActiveMessage = 'LISTENERS_ACTIVE' as const;
export const skipWaitingMessage = 'SKIP_WAITING' as const;

export type ServiceWorkerMessage =
  | typeof clientConnectedMessage
  | typeof listenersActiveMessage
  | typeof skipWaitingMessage;
