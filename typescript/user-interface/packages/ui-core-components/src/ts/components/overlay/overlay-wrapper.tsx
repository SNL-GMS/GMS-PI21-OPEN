/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Overlay } from '@blueprintjs/core';
import { classList } from '@gms/ui-util';
import * as React from 'react';

export interface OverlayWrapperProps {
  /**
   * True to show the overlay contents. False to hide them.
   */
  isOpen: boolean;

  /**
   * A string containing one or more optional classnames to add to the overlay.
   */
  className?: string;

  /**
   * A string containing one or more class names for the div that wraps the contents of the overlay.
   */
  contentClassName?: string;

  /**
   * Called whenever the overlay is closed for any reason.
   */
  onClose(): void;
}

/**
 * The OverlayWrapper creates an overlay that contains the OverlayWrapper's children
 * and sets common props. Note, handles hide/show using classes that apply/remove display: none.
 */
export const OverlayWrapper: React.FunctionComponent<React.PropsWithChildren<
  OverlayWrapperProps
  // eslint-disable-next-line react/function-component-definition
>> = props => (
  <Overlay
    className={classList(
      {
        overlay: true,
        'overlay--hidden': !props.isOpen
      },
      props.className
    )}
    backdropClassName={classList({
      'overlay__backdrop--hidden': !props.isOpen
    })}
    autoFocus={false}
    canEscapeKeyClose
    canOutsideClickClose
    enforceFocus={false}
    hasBackdrop
    isOpen={props.isOpen}
    onClose={() => props.onClose()}
    usePortal
  >
    {props.isOpen && (
      <div className={`overlay__contents ${props.contentClassName}`}>{props.children}</div>
    )}
  </Overlay>
);
