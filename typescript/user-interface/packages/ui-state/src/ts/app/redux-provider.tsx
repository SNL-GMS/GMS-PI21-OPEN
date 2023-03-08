import React from 'react';
import { Provider } from 'react-redux';

import { getStore } from './store';
/**
 * Wraps the provided component with Redux Provider.
 *
 * @param Component the component
 * @param store the redux store
 */
export const withReduxProvider = (
  Component: React.ComponentClass | React.FunctionComponent
): React.ComponentClass =>
  class WithReduxProvider extends React.PureComponent {
    /**
     * Wrap the component in a redux providers
     */
    public render(): JSX.Element {
      return (
        <Provider store={getStore()}>
          {/* eslint-disable-next-line react/jsx-props-no-spreading */}
          <Component {...this.props} />
        </Provider>
      );
    }
  };
