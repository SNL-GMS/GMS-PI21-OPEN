import { optionIs, rankWith } from '@jsonforms/core';

/**
 * Test whether a form input should be considered to be a file upload
 */
export default rankWith(
  5, //increase rank as needed
  optionIs('renderer', 'monitor-types-rollup')
);
