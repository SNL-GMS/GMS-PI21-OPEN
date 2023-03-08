import React from 'react';
import { debounce } from 'lodash';
import { FormGroup, FormLabel, TextField } from '@mui/material';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { layoutStyles } from '../../styles/layout';
import { makeStyles } from '@mui/styles';
import { useIsAliveQuery } from '../../state/api-slice';
import { RequestValidationIcon } from '../../components/RequestValidationIcon';

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
  input: {
    padding: '0.5em 0.75em !important',
    cursor: 'pointer !important',
  },
  textField: {
    padding: '0 !important',
    margin: '0 !important',
    cursor: 'pointer !important',
  },
  formEntry: {
    display: 'flex',
    position: 'relative',
  },
});

/**
 * The type of the props for the {@link URLInput} component
 */
export interface URLInputProps {
  className?: string;
  updateURL: (val: string) => void;
  data: any;
  label?: string;
  description?: string;
  // The route to test (checks for a 200 response).. Should begin with a '/' character
  testEndpoint?: string;
}

/**
 * Creates a URL input with a "test" button that validates the URL and then actually
 * hits the alive endpoint in an attempt to see if the URL is valid.
 */
export const URLInput: React.FC<URLInputProps> = ({
  className,
  updateURL,
  data,
  label,
  description,
  testEndpoint,
}: URLInputProps) => {
  const classes = useStyles();
  const isAliveResult = useIsAliveQuery(`${data}${testEndpoint}`);
  const debouncedUpdateURL = debounce(updateURL, 500);
  return (
    <FormGroup
      id='#/properties/url-input'
      className={`${className} ${classes.container} ${classes.root} url-input`}
    >
      {label && (
        <FormLabel className={`url-input__label ${classes.label}`}>
          {label}
        </FormLabel>
      )}
      {description && (
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{description}</ReactMarkdown>
      )}
      <div className={`url-input__form-entry ${classes.formEntry}`}>
        <TextField
          className={classes.textField}
          fullWidth
          onClick={(e) => {
            console.log(`Test url: ${data}`);
          }}
          onChange={(e) => {
            debouncedUpdateURL(e.target.value);
          }}
          defaultValue={data}
          placeholder={'https://deployment.example.com'}
        />
        <RequestValidationIcon result={isAliveResult} />
      </div>
    </FormGroup>
  );
};
