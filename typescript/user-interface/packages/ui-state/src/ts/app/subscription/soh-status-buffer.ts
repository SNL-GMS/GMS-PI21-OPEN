/** Buffers the incoming SOH Status messages from the soh-station-subscription.
 * Then updates redux store when updates are quiet for HALF_SECOND_MS */

import type { SohTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import produce from 'immer';
import * as Immutable from 'immutable';

const logger = UILogger.create('GMS_LOG_SOH_STATUS_BUFFER', process.env.GMS_LOG_SOH_STATUS_BUFFER);

/** Delay used in buffering before updating Redux with latest SOH message */
const QUARTER_SECOND_MS = 250;
const HALF_SECOND_MS = 500;

/* The timestamp to help figure out when to update in Redux */
let lastGatewayMessageTimestamp = 0;

let latestQueuedSohData: Immutable.Map<string, SohTypes.UiStationSoh> = Immutable.Map<
  string,
  SohTypes.UiStationSoh
>();

let stationGroups: SohTypes.StationGroupSohStatus[] = [];
let updateSohMessageInRedux;

/**
 * Returns the most recent (latest) Station and StationGroup SOH.
 */
export const getLatestStationAndGroupSoh = (): SohTypes.StationAndStationGroupSoh => {
  const uiStationSohs: SohTypes.UiStationSoh[] = Array.from(latestQueuedSohData.values());
  return {
    stationGroups,
    stationSoh: uiStationSohs,
    isUpdateResponse: false
  };
};

/**
 * Check if need to send batched SOH Status Changes to UIs
 */
export const checkToSendSohStatusChanges = (): void => {
  // Check to see if we haven't had any data for 1/2 second or the update interval has expired
  // before sending the SOH Station data to UI subscribers
  const now = Date.now();
  if (latestQueuedSohData.size === 0 || now - lastGatewayMessageTimestamp < HALF_SECOND_MS) {
    return;
  }

  // Call method to publish the latest StationSoh entries
  if (updateSohMessageInRedux) {
    updateSohMessageInRedux(getLatestStationAndGroupSoh());
  }

  // Okay clear the latest StationSoh that were queued since sending
  latestQueuedSohData = Immutable.Map<string, SohTypes.UiStationSoh>();

  // Reset timestamps since just published
  lastGatewayMessageTimestamp = now;
};

/**
 * Start timer that checks if new messages should be updated into redux store
 */
export const initializeSohStatusBuffering = (
  updateSohMessage: (stationAndStationGroupSoh: SohTypes.StationAndStationGroupSoh) => void
): void => {
  updateSohMessageInRedux = updateSohMessage;
  setTimeout(() => {
    checkToSendSohStatusChanges();
  }, QUARTER_SECOND_MS);

  setInterval(() => {
    checkToSendSohStatusChanges();
  }, QUARTER_SECOND_MS);
};

/**
 * Add UiStationSoh to the map
 *
 * @param uiStationSoh latest UiStationSoh to add to queue
 * @param isUpdateResponse need to update the cache and queue
 *                         if this is an update from an ack or quiet
 * @return boolean if added to queue and map. If UiStationSoh UUID is already
 * in the map do not add it.
 */
export const addSohForStation = (uiStationSoh: SohTypes.UiStationSoh): void => {
  if (uiStationSoh) {
    latestQueuedSohData = latestQueuedSohData.set(uiStationSoh.stationName, uiStationSoh);
  }
};

/**
 * Add SOH messages to latest list, the list is periodically updated into redux
 *
 * @param messages the messages
 */
export const addLatestSohMessages = (messages: SohTypes.StationAndStationGroupSoh[]): void => {
  // Last time got messages from System Event gateway consumer, helps when to publish
  lastGatewayMessageTimestamp = Date.now();

  messages.forEach(msg => {
    // Fix the SOH message from epoch milliseconds to seconds
    const stationAndStationGroupSoh = produce(msg, draft => {
      draft.stationGroups.forEach(sg => {
        // eslint-disable-next-line no-param-reassign
        sg.time /= 1000;
      });
      draft.stationSoh.forEach(soh => {
        // eslint-disable-next-line no-param-reassign
        soh.time /= 1000;
      });
    });

    try {
      const { isUpdateResponse } = stationAndStationGroupSoh;
      stationGroups = stationAndStationGroupSoh.stationGroups;
      stationAndStationGroupSoh.stationSoh.forEach(s => addSohForStation(s));
      // If this is an update response message send it immediately to be more responsive.
      // Also sending only the update response message allows the UI to filter it and not
      // log Timing Pt C messages. If not filtered will skew the results.
      if (isUpdateResponse) {
        const responseMessage: SohTypes.StationAndStationGroupSoh = {
          stationGroups: stationAndStationGroupSoh.stationGroups,
          stationSoh: stationAndStationGroupSoh.stationSoh,
          isUpdateResponse: true
        };
        updateSohMessageInRedux(responseMessage);
      }
    } catch (error) {
      logger.warn(`Error processing StationAndStationGroupSoh message: ${error}`);
      logger.warn(
        `StationAndStationGroupSoh json: ${JSON.stringify(stationAndStationGroupSoh, undefined, 2)}`
      );
    }
  });
};
