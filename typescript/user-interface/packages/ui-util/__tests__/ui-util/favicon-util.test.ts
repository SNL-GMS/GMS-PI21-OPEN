import * as FaviconUtils from '../../src/ts/ui-util/favicon-util';

const faviconUrl = 'http://example.com/my.ico';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let result: any;
Object.defineProperty(global, 'document', {
  writable: true,
  value: {
    querySelector: () => ({}),
    getElementsByTagName: () => [
      {
        appendChild: link => {
          result = link;
        }
      }
    ]
  }
});
Object.defineProperty(global, 'window', {
  writable: true,
  value: {
    matchMedia: query => ({ matches: query === '(prefers-color-scheme: dark)' })
  }
});

describe('Favicon Util', () => {
  // These are dumb tests, but they prevent some regressions and cover the code
  it('should populate the favicon object', () => {
    FaviconUtils.replaceFavIcon(faviconUrl);
    expect(result).toEqual({
      type: 'image/x-icon',
      rel: 'shortcut icon',
      href: faviconUrl
    });
  });

  it('detects dark mode', () => {
    expect(FaviconUtils.isDarkMode()).toBe(true);
  });
});
