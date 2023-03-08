import { rankWith, scopeEndsWith } from '@jsonforms/core';

/**
 * Test whether a form input should be considered to be a file upload
 */
export default rankWith(
  3, //increase rank as needed
  scopeEndsWith('file-upload')
);
