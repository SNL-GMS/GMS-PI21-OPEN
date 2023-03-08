import { Classes, Colors, Intent, Spinner } from '@blueprintjs/core';
import * as React from 'react';

/**
 * Simple component to indicate loading status
 */
export class LoadingScreen extends React.PureComponent {
  /**
   * Display a loading spinner
   */
  public render(): JSX.Element {
    return (
      <div
        style={{
          display: 'flex',
          height: '100%',
          width: '100%',
          justifyContent: 'center',
          alignItems: 'center',
          backgroundColor: Colors.DARK_GRAY2
        }}
      >
        <Spinner className={Classes.LARGE} intent={Intent.PRIMARY} value={undefined} />
      </div>
    );
  }
}
