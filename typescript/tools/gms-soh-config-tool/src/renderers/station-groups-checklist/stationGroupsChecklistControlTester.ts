import { optionIs, rankWith } from '@jsonforms/core';

/**
 * Test whether a form input should be considered to be a file upload
 */
export default rankWith(
  4, //increase rank as needed
  optionIs('renderer', 'station-groups-checklist')
);
