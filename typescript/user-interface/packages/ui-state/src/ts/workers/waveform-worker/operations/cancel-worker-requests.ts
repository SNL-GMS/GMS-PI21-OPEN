import Immutable from 'immutable';

let controllers: Immutable.List<AbortController> = Immutable.List();

/**
 * Cancels all pending requests on the worker thread. Note, this does not mean that no promises will resolve,
 * it simply cancels the Axios requests, which rejects with the message "canceled"
 */
export const cancelWorkerRequests = (): void => {
  controllers.forEach(controller => controller.abort());
  controllers = controllers.clear();
};

/**
 * @param controller an AbortController that should be used to cancel requests when {@link cancelWorkerRequests} is called
 */
export const addController = (controller: AbortController): void => {
  controllers = controllers.push(controller);
};

/**
 * @param controller remove controller
 */
export const removeController = (controller: AbortController): void => {
  const cIndex = controllers.findIndex(c => c === controller);
  if (cIndex === -1) {
    throw new Error('AbortController not found, cannot remove.');
  }
  controllers.delete(cIndex);
};
