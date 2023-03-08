import { Typography } from '@mui/material';
import { makeStyles } from '@mui/styles';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { layoutStyles } from '../../styles/layout';

const useStyles = makeStyles({
  root: {
    '& input': {
      cursor: 'pointer',
    },
  },
  ...layoutStyles,
  label: {
    fontSize: '1.25em',
    fontWeight: 'bold',
  },
});

/**
 * The type of the props for the {@link MarkdownRenderer} component
 */
export interface MarkdownRendererProps {
  label: string; // a plain text string
  description: string; // a markdown string
}

/**
 * Renders markdown from a ui config file. Intended as a "help" or "about" type field, which contains no inputs.
 * The markdown Control should have 'renderer': 'markdown' set in the json for the ui config.
 */
export const MarkdownRenderer: React.FC<MarkdownRendererProps> = ({
  label,
  description,
}: MarkdownRendererProps) => {
  const classes = useStyles();
  return (
    <>
      {label && (
        <Typography variant={'h4'} className={classes.label}>
          {label}
        </Typography>
      )}
      {description && (
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{description}</ReactMarkdown>
      )}
    </>
  );
};
