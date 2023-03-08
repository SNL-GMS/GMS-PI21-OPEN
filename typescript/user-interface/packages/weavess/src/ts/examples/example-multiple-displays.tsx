import React from 'react';

import { WeavessExample } from './example-weavess';

// eslint-disable-next-line react/prefer-stateless-function
export class MultipleDisplaysExample extends React.Component<unknown, unknown> {
  public render(): JSX.Element {
    return (
      <div
        style={{
          height: '80%',
          display: 'flex',
          justifyContent: 'space-around',
          flexDirection: 'column',
          alignItems: 'center'
        }}
      >
        <WeavessExample key={1} showExampleControls={false} />
        <WeavessExample key={2} showExampleControls={false} />
      </div>
    );
  }
}
