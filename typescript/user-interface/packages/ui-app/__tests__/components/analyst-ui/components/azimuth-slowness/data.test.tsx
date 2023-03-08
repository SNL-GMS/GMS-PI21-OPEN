import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';

import { eventData, legacyEventData } from '../../../../__data__/event-data';
import {
  endTimeSeconds,
  eventIds,
  signalDetectionsIds,
  startTimeSeconds,
  timeBlock
} from '../../../../__data__/test-util-data';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('Data should be verified', () => {
  describe('FK Data', () => {
    it('should be the correct time block', () => {
      expect(endTimeSeconds - startTimeSeconds).toBeGreaterThanOrEqual(timeBlock);
    });

    it('TimeInterval is mocked properly', () => {
      expect(endTimeSeconds - startTimeSeconds).toBeGreaterThanOrEqual(timeBlock);
    });

    it('should have valid signal detection data', () => {
      expect(signalDetectionsData).toMatchSnapshot();
    });

    it('should have valid signalDetectionIds', () => {
      expect(signalDetectionsIds).toMatchSnapshot();
    });

    it('should have valid event data', () => {
      expect(eventData).toMatchSnapshot();
    });

    it('should have valid legacy event data', () => {
      expect(legacyEventData).toMatchSnapshot();
    });

    it('should have valid eventIds', () => {
      expect(eventIds).toMatchSnapshot();
    });
  });
});
