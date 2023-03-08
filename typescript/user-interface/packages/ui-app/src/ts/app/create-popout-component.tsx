import { Colors } from '@blueprintjs/core';
import { GMS_UI_MODE } from '@gms/common-util';
import { getElectron } from '@gms/ui-util';
import React from 'react';

// electron instance; undefined if not running in electron
const electron = getElectron();

/**
 * Wrap the component with everything it needs to live standalone as a popout
 */
export const createPopoutComponent = (
  Component: any,
  props: any,
  suppressPopinIcon = false
): JSX.Element => {
  // eslint-disable-next-line react/display-name
  const PopoutComponent = class extends React.PureComponent {
    public render(): JSX.Element {
      return (
        <div
          style={{
            width: '100%',
            height: '100%',
            backgroundColor: Colors.DARK_GRAY2
          }}
          className="popout-component"
          data-gms-ui-mode={GMS_UI_MODE}
        >
          {/* eslint-disable-next-line react/jsx-props-no-spreading */}
          <Component {...this.props} />
          {
            // only show pop-in button if running in electron
            !suppressPopinIcon &&
            electron &&
            electron !== undefined &&
            electron.ipcRenderer !== undefined ? (
              // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
              <div
                className="lm_popin"
                title="pop-in"
                onClick={() => {
                  electron.ipcRenderer.send(
                    'popin-window',
                    electron.remote.getCurrentWebContents().popoutConfig
                  );
                  electron.remote.getCurrentWindow().close();
                }}
              >
                <div className="lm_icon" />
                <div className="lm_bg" />
              </div>
            ) : undefined
          }
        </div>
      );
    }
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  return <PopoutComponent {...props} />;
};
