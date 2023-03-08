import {
  deleteCookie,
  getCookie,
  isCookieSet,
  sanitizeCookie,
  setCookie
} from '../../src/ts/ui-util/cookie-util';

const testCookieId = 'chocolate-chip';
const cookieValue = 'delicious';
const sanitizedValue = sanitizeCookie(cookieValue);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
Object.defineProperty(global, 'document', { writable: true, value: { cookie: '' } } as any);

describe('cookie-util', () => {
  it('can try to get cookie with nothing exists', () => {
    // eslint-disable-next-line @typescript-eslint/dot-notation
    document.cookie = `bad-cookie`;
    expect(document.cookie).toContain(sanitizeCookie('bad-cookie'));
    const foundCookieValue = getCookie(testCookieId);
    expect(foundCookieValue).toEqual('');
  });

  it('can set a cookie', () => {
    setCookie(undefined, undefined);
    setCookie(testCookieId, cookieValue);
    expect(document.cookie).toContain(sanitizedValue);
  });

  it('can get a cookie', () => {
    setCookie('myCookieId', 'my cookie value');
    let foundCookieValue = getCookie('myCookieId');
    expect(foundCookieValue).toEqual(sanitizeCookie('my cookie value'));

    setCookie('myOtherCookieId', 'my other cookie value');
    foundCookieValue = getCookie(testCookieId);
    expect(foundCookieValue).toEqual('');

    setCookie(testCookieId, cookieValue);
    foundCookieValue = getCookie(testCookieId);
    expect(foundCookieValue).toEqual(sanitizedValue);
  });

  it('can mark a cookie for deletion', () => {
    setCookie(testCookieId, cookieValue);
    deleteCookie(undefined, undefined);
    deleteCookie(testCookieId, cookieValue);
    expect(document.cookie).toContain('max-age=0');
  });

  it('can tell if a cookie is set', () => {
    setCookie(testCookieId, cookieValue);
    expect(isCookieSet(testCookieId)).toEqual(true);
  });

  it('can tell if a cookie is not set', () => {
    document.cookie = '';
    expect(isCookieSet(testCookieId)).toEqual(false);
  });
});
