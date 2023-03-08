import { rankWith, optionIs } from '@jsonforms/core';

/**
 * Test whether a form input should be considered to be a file upload
 */
export default rankWith(
  3, //increase rank as needed
  optionIs('renderer', 'ISOTime')
);
