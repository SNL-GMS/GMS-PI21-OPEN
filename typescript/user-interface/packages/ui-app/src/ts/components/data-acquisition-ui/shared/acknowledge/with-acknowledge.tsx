import * as React from 'react';

import { ReduxAcknowledgeContainer } from './acknowledge-container';

/**
 * Adds the acknowledgeSohStatus function to the component provided
 *
 * @param WrappedComponent the component to which we should add the acknowledgeSohStatus function
 */
export function WithAcknowledge<T>(
  WrappedComponent: React.FunctionComponent | React.ComponentClass
): React.FunctionComponent<T> {
  function WithAcknowledgeComponent(props) {
    return (
      <ReduxAcknowledgeContainer>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <WrappedComponent {...props} />
      </ReduxAcknowledgeContainer>
    );
  }
  return WithAcknowledgeComponent;
}
