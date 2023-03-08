/**
 * Replaces favicon with dark mode icon if found.
 *
 * @param logo
 */
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any
export function replaceFavIcon(logo: any): void {
  const favLink: HTMLLinkElement =
    document.querySelector("link[rel*='icon']") || document.createElement('link');

  favLink.type = 'image/x-icon';
  favLink.rel = 'shortcut icon';
  favLink.href = logo || 'gms-logo-favicon.ico';

  document.getElementsByTagName('head')[0].appendChild(favLink);
}

/**
 * Return true if the browser knows to prefer dark mode, like Mojave's dark mode and some browser themes
 */
export function isDarkMode(): boolean {
  return window.matchMedia('(prefers-color-scheme: dark)').matches;
}
