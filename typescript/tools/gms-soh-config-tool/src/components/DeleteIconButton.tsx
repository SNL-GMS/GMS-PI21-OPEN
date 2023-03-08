import CloseIcon from '@mui/icons-material/Close';
import { Tooltip } from '@mui/material';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface DeleteIconButtonProps {
  containerId: string;
  helpText: string | undefined;
  className: string;
  onIconClick: (containerId: string) => void;
}

export const DeleteIconButton: React.FC<
  React.PropsWithChildren<DeleteIconButtonProps>
> = (props: React.PropsWithChildren<DeleteIconButtonProps>) => {
  const { helpText, containerId, className, onIconClick } = props;
  return (
    <Tooltip
      title={
        <ReactMarkdown remarkPlugins={[remarkGfm]}>
          {helpText ?? ''}
        </ReactMarkdown>
      }
    >
      <CloseIcon
        className={className}
        color={'inherit'}
        fontSize={'small'}
        onClick={() => onIconClick(containerId)}
      />
    </Tooltip>
  );
};
