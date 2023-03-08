/**
 * Sanitize and encode the cookie string
 *
 * @param  cookie The user-submitted string
 * @returns  The sanitized cookie
 */
export const sanitizeCookie = (cookie: string): string => window.escape(cookie);

/**
 * gets the  value of a cookie
 *
 * @param cookieId the key of the cookie for which to get the value
 * @returns the value of a cookie, or an empty string if unset.
 */
export const getCookie = (cookieId: string): string => {
  // Call sanitizeCookie for each piece of the cookie
  const sanitizedCookieId = sanitizeCookie(cookieId);
  // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
  const cookies = document['cookie'].split(';');
  let result = '';
  cookies.forEach(cookie => {
    const crumbs = cookie.split('=');
    if (crumbs.length === 2) {
      const key = crumbs[0].trim();
      const value = crumbs[1].trim();
      if (key === sanitizedCookieId) {
        result = value;
      }
    }
  });
  return result;
};

/**
 * Sets the value of a cookie.
 *
 * @param cookieId the key of the cookie
 * @param value the string value to which to set the cookie.
 */
export const setCookie = (cookieId: string, value: string): void => {
  if (cookieId && value) {
    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    document['cookie'] = `${sanitizeCookie(cookieId)}=${sanitizeCookie(value)}`;
  }
};

/**
 * Sets a cookie to be deleted.
 *
 * @param cookieId the key of the cookie to remove
 * @param value optionally set the cookie value
 */
export const deleteCookie = (cookieId: string, value = ''): void => {
  if (cookieId && value) {
    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    document['cookie'] = `${sanitizeCookie(cookieId)}=${sanitizeCookie(value)}${';max-age=0'}`;
  }
};

/**
 * Checks if a cookie is set.
 *
 * @param cookieId the cookie's key
 * @returns true if the cookie has a truthy value. False otherwise.
 */
export const isCookieSet = (cookieId: string): boolean => {
  const val = getCookie(cookieId);
  if (val) {
    return true;
  }
  return false;
};
