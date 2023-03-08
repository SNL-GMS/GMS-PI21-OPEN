import { Logger, UI_BASE_PATH, UI_URL } from '@gms/common-util';
import { ServiceWorkerMessages } from '@gms/ui-workers';
import delay from 'lodash/delay';

const REFRESH_DELAY_MS = 300;
const logger = Logger.create('GMS_SW_LOG', process.env.GMS_SW_LOG);

/**
 * For a provided service worker registration, this calls the callback if that service worker is
 * waiting or when it triggers the `installed` event.
 */
function listenForWaitingServiceWorker(
  reg: ServiceWorkerRegistration,
  callback: (r: ServiceWorkerRegistration) => void
) {
  function awaitStateChange() {
    reg.installing.addEventListener('statechange', ev => {
      const state = (ev.target as ServiceWorker)?.state;
      if (state === 'installed') {
        logger.info('Service worker already installed');
        callback(reg);
      }
    });
  }
  if (!reg) return;
  if (reg.waiting) {
    callback(reg);
    return;
  }
  if (reg.installing) awaitStateChange();
  reg.addEventListener('updatefound', awaitStateChange);
}

/**
 * Needed to avoid refresh loops
 */
let refreshing = false;

/**
 * Higher order function which creates a function which calls the provided
 * refreshCallback which triggers the skip waiting message on the service worker,
 * making it take control, which will in turn cause the page to refresh.
 */
const promptUserToRefresh = (refreshCallback: (cb: () => void) => void) => reg => {
  refreshCallback(() => reg.waiting.postMessage(ServiceWorkerMessages.skipWaitingMessage));
};

function refresh() {
  if (refreshing) return;
  refreshing = true;
  delay(() => window.location.reload(), REFRESH_DELAY_MS);
}

/**
 * Registers the sw.js file. Sets up listeners to prompt user to refresh if a new service
 * worker gets installed as a result. Also sets up an auto refresh if we detect that we are not being
 * controlled by a service worker, which can happen if the user clears their cache.
 *
 * @param refreshCallback the callback to toggle the alert. Takes the function that refreshes the page
 * as an argument.
 */
export const registerServiceWorker = async (
  refreshCallback: (cb: () => void) => void
): Promise<void> => {
  logger.info('[main] Registering service worker', `${UI_URL}${UI_BASE_PATH}/sw.js`);
  const reg = await navigator.serviceWorker.register(`${UI_URL}${UI_BASE_PATH}/sw.js`);
  navigator.serviceWorker.addEventListener('controllerchange', refresh);
  listenForWaitingServiceWorker(reg, promptUserToRefresh(refreshCallback));
  if (!navigator.serviceWorker.controller && reg?.active) {
    // Need to refresh so that the new service worker can take control
    logger.info('[main] Calling refresh to load new service worker');
    refresh();
  }
};
