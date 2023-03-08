import React from 'react';

import { InteractionConsumer } from './interaction-consumer';
import { InteractionProvider } from './interaction-provider';

/**
 * Wrap the component with interaction handling
 */
export const InteractionWrapper = (
  Component: React.ComponentClass | React.FunctionComponent
): React.ComponentClass =>
  class InteractionWrapperComponent extends React.PureComponent {
    /**
     * Wrap the component in a redux providers
     */
    public render(): JSX.Element {
      return (
        <>
          {/* eslint-disable-next-line react/jsx-props-no-spreading */}
          <Component {...this.props} />
          <InteractionProvider>
            <InteractionConsumer />
          </InteractionProvider>
        </>
      );
    }
  };
