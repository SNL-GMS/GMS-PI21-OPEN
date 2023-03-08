import { IS_NODE_ENV_DEVELOPMENT } from '@gms/common-util';

/**
 * Configures the React Performance Dev Tool.
 * ONLY ENABLED FOR NODE_ENV === 'development'
 */
export const configureReactPerformanceDevTool = (): void => {
  // REACT Performance DEV Tool, enabled only in develop
  if (IS_NODE_ENV_DEVELOPMENT) {
    // eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports, global-require
    const { registerObserver } = require('react-perf-devtool');
    const options = {};

    const callback = () => {
      /* no-op */
    };

    // assign the observer to the global scope, as the GC will delete it otherwise
    if (window) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (window as any).observer = registerObserver(options, callback);
    }
  }
};
