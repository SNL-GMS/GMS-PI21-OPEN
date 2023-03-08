import { Classes, Colors } from '@blueprintjs/core';
import React from 'react';

export const WeavessGenericContainerWrapper: React.FunctionComponent<React.PropsWithChildren<
  unknown
  // eslint-disable-next-line react/function-component-definition
>> = props => {
  const { children } = props;

  return (
    <div
      className={Classes.DARK}
      style={{
        height: '80%',
        width: '100%',
        padding: '0.5rem',
        color: Colors.GRAY4,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
      }}
    >
      <div
        style={{
          height: '100%',
          width: '100%',
          display: 'flex',
          flexDirection: 'column'
        }}
      >
        <div
          style={{
            flex: '1 1 auto',
            position: 'relative'
          }}
        >
          <div
            style={{
              position: 'absolute',
              top: '0px',
              bottom: '0px',
              left: '0px',
              right: '0px'
            }}
          >
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};
