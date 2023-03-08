import RemoveDoneIcon from '@mui/icons-material/RemoveDone';
import { Tooltip } from '@mui/material';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface DeselectAllIconButtonProps {
  containerId: string;
  helpText: string | undefined;
  className: string;
  onIconClick: (containerId: string) => void;
}

export const DeselectAllIconButton: React.FC<
  React.PropsWithChildren<DeselectAllIconButtonProps>
> = (props: React.PropsWithChildren<DeselectAllIconButtonProps>) => {
  const { helpText, containerId, className, onIconClick } = props;
  return (
    <Tooltip
      title={
        <ReactMarkdown remarkPlugins={[remarkGfm]}>
          {helpText ?? ''}
        </ReactMarkdown>
      }
    >
      <RemoveDoneIcon
        className={className}
        color={'inherit'}
        fontSize={'small'}
        onClick={() => onIconClick(containerId)}
      />
    </Tooltip>
  );
};
