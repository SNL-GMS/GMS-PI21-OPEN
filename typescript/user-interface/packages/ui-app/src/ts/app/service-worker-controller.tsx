import { Logger } from '@gms/common-util';
import { nonIdealStateWithNoSpinner, nonIdealStateWithSpinner } from '@gms/ui-core-components';
import { ServiceWorkerMessages } from '@gms/ui-workers';
import React from 'react';

import { registerServiceWorker } from './register-service-worker';
import { ServiceWorkerAlert } from './service-worker-alert';

const logger = Logger.create('GMS_SW_LOG', process.env.GMS_SW_LOG);

/**
 * This creates a number of variables used in the management of the service worker
 * 1. isAlertOpen, a boolean which controls the state of the alert.
 * 2. refreshCallback, a referentially stable function that
 * is provided to the registerServiceWorker function, which triggers the alert popup in case the service worker
 * registration detects that a new service worker needs to be installed
 * 3. skipWaitingFunctionRef, which is a ref that will contain the returned callback that should refresh the page.
 * In this case, we use a ref so that we don't capture an old value.
 * 4. toggleAlert, which is a referentially stable function that toggles whether the alert is open or not.
 */
const useAlert = () => {
  const skipWaitingFunctionRef = React.useRef<() => void>();
  const [isAlertOpen, setIsAlertOpen] = React.useState<boolean>(false);
  const toggleAlert = React.useCallback(() => setIsAlertOpen(isOpen => !isOpen), []);
  const refreshCallback = React.useCallback(
    (cb: () => void) => {
      skipWaitingFunctionRef.current = cb;
      toggleAlert();
    },
    [toggleAlert]
  );
  return { isAlertOpen, refreshCallback, skipWaitingFunctionRef, toggleAlert };
};

/**
 * Calls to register the service worker, and listens for a reply, indicating that it has started correctly.
 *
 * @param refreshCallback the callback function that should be called if we need to ask the user if we can refresh
 * @param setIsServiceWorkerRegistered the setter to disable the service worker registering non ideal state
 * @param setError to set whether an error has occurred
 */
const useServiceWorker = (
  refreshCallback: (cb: () => void) => void,
  setIsServiceWorkerRegistered: React.Dispatch<React.SetStateAction<boolean>>,
  setError: React.Dispatch<React.SetStateAction<false | MessageEvent<string>>>
) => {
  React.useEffect(() => {
    async function startTheEngines() {
      navigator.serviceWorker.addEventListener('message', message => {
        logger.info('[main] received message from service worker', message);
        if (message.data === ServiceWorkerMessages.listenersActiveMessage) {
          setIsServiceWorkerRegistered(true);
        } else {
          logger.error(
            'Unknown service worker message type received. See the following error for message details.'
          );
          setError(message);
          throw new Error(message.data);
        }
      });
      await registerServiceWorker(refreshCallback);
      logger.info('[main] registered service worker');
      await navigator.serviceWorker.ready;
      logger.info('[main] service worker ready');
      if (navigator.serviceWorker.controller) {
        logger.info('[main] postMessage', ServiceWorkerMessages.clientConnectedMessage);
        navigator.serviceWorker.controller.postMessage(
          ServiceWorkerMessages.clientConnectedMessage
        );
      }
    }
    if (process.env.GMS_SW !== 'false') {
      // We don't need to await the return value, we just need to fire this off.
      // eslint-disable-next-line no-void
      void startTheEngines();
    }
  }, [refreshCallback, setError, setIsServiceWorkerRegistered]);
};

function MaybeServiceWorkerNonIdealState({
  children,
  isError,
  isServiceWorkerRegistered
}: React.PropsWithChildren<{
  isError: MessageEvent<string> | undefined;
  isServiceWorkerRegistered: boolean;
}>) {
  if (process.env.GMS_SW !== 'false' && isError) {
    return nonIdealStateWithNoSpinner('Error Registering Service Worker', isError.data);
  }
  if (process.env.GMS_SW !== 'false' && !isServiceWorkerRegistered) {
    return nonIdealStateWithSpinner('Registering Service Worker', 'Please wait');
  }
  return children;
}

type ServiceWorkerControllerProps = React.PropsWithChildren<unknown>;

/**
 * Registers the service worker, and if it is not registered, returns a non ideal state until
 * we get a message from the service worker. If it has a service worker that sends the proper
 * message, this renders its children.
 */
export function ServiceWorkerController({ children }: ServiceWorkerControllerProps) {
  const [isError, setError] = React.useState<MessageEvent<string> | undefined>(undefined);
  const [isServiceWorkerRegistered, setIsServiceWorkerRegistered] = React.useState(false);
  const { isAlertOpen, refreshCallback, skipWaitingFunctionRef, toggleAlert } = useAlert();

  useServiceWorker(refreshCallback, setIsServiceWorkerRegistered, setError);

  return (
    <>
      <ServiceWorkerAlert
        isAlertOpen={isAlertOpen}
        skipWaitingFunctionRef={skipWaitingFunctionRef}
        toggleAlert={toggleAlert}
      />
      <MaybeServiceWorkerNonIdealState
        isError={isError}
        isServiceWorkerRegistered={isServiceWorkerRegistered}
      >
        {children}
      </MaybeServiceWorkerNonIdealState>
    </>
  );
}
