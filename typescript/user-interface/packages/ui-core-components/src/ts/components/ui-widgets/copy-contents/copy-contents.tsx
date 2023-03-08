import { H5 } from '@blueprintjs/core';
import { Tooltip2 } from '@blueprintjs/popover2';
import classNames from 'classnames';
import React from 'react';
import { toast } from 'react-toastify';

/**
 * The type of the props for the {@link CopyContents} component
 */
export interface CopyContentsProps {
  children: React.ReactNode;
  className?: string;
  clipboardText?: string;
  tooltipClassName?: string;
  tooltipLabel?: string;
}

/**
 * Makes the children so that, when clicked, it copies a provided value to the user's clipboard
 */
export function CopyContents({
  children,
  className,
  clipboardText,
  tooltipClassName,
  tooltipLabel
}: CopyContentsProps) {
  const copyText = clipboardText ?? (typeof children === 'string' ? children : children.toString());

  const tooltipLabelText = tooltipLabel ?? 'Copy';

  const copy = React.useCallback(async () => {
    await navigator.clipboard.writeText(copyText);
    toast.info(`Copied to clipboard`, { toastId: `copied-to-clipboard` });
  }, [copyText]);

  const handleKeyDown = React.useCallback(
    async e => {
      if (e.key === 'Enter') {
        await copy();
      }
    },
    [copy]
  );
  return (
    <Tooltip2
      content={
        <div className={classNames('copy-contents__tooltip_contents', tooltipClassName)}>
          <H5>{tooltipLabelText}</H5>
          <p className="monospace">{copyText}</p>
        </div>
      }
    >
      <div
        className={classNames('copy-contents', className)}
        role="button"
        tabIndex={-1}
        onKeyDown={handleKeyDown}
        onClick={copy}
      >
        {children}
      </div>
    </Tooltip2>
  );
}
