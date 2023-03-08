/**
 * OS type we are trying to detect for specific key commands
 */
export enum OSTypes {
  MAC = 'Mac',
  IOS = 'iOS',
  WINDOWS = 'Windows',
  ANDROID = 'Android',
  LINUX = 'Linux'
}

/**
 * @returns the os the browser is running on
 */
export const getOS = (): OSTypes => {
  const { userAgent } = window.navigator;
  const { platform } = window.navigator;
  const macosPlatforms = ['Macintosh', 'MacIntel', 'MacPPC', 'Mac68K'];
  const windowsPlatforms = ['Win32', 'Win64', 'Windows', 'WinCE'];
  const iosPlatforms = ['iPhone', 'iPad', 'iPod'];
  let os: OSTypes;

  if (macosPlatforms.indexOf(platform) !== -1) {
    os = OSTypes.MAC;
  } else if (iosPlatforms.indexOf(platform) !== -1) {
    os = OSTypes.IOS;
  } else if (windowsPlatforms.indexOf(platform) !== -1) {
    os = OSTypes.WINDOWS;
  } else if (/Android/.test(userAgent)) {
    os = OSTypes.ANDROID;
  } else if (/Linux/.test(platform)) {
    os = OSTypes.LINUX;
  } else {
    os = null;
  }

  return os;
};
