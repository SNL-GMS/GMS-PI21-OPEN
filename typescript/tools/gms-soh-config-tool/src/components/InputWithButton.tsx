import {
  FilledInput,
  Button,
  FormGroup,
  FormLabel,
  TextField,
} from '@mui/material';
import { makeStyles } from '@mui/styles';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { layoutStyles } from '../styles/layout';

interface InputWithButtonProps {
  buttonText: string;
  data: any;
  onClick: React.MouseEventHandler<unknown>;
  onInputClick?: React.MouseEventHandler<unknown>;
  onChange?: (event: React.ChangeEvent<HTMLInputElement>) => void;
  defaultValue?: string;
  className?: string;
  id?: string;
  label?: string;
  startIcon?: JSX.Element;
  description?: string;
  placeholder?: string;
  isEditable?: boolean;
}

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
    display: 'grid',
    gridTemplateColumns: 'auto var(--file-input-button)',
  },
});

export const InputWithButton: React.FC<InputWithButtonProps> = ({
  className,
  id,
  onClick,
  onInputClick,
  onChange,
  label,
  data,
  buttonText,
  defaultValue,
  startIcon,
  description,
  placeholder,
  isEditable = true,
}: InputWithButtonProps) => {
  const classes = useStyles();
  return (
    <FormGroup
      id={id ?? ''}
      className={`${className} ${classes.container} ${classes.root} input-with-button`}
    >
      {label && (
        <FormLabel className={`input-with-button__label ${classes.label}`}>
          {label}
        </FormLabel>
      )}
      {description && (
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{description}</ReactMarkdown>
      )}
      <div className={`input-with-button__form-entry ${classes.formEntry}`}>
        {isEditable ? (
          <TextField
            className={classes.textField}
            fullWidth
            onClick={onInputClick}
            onChange={onChange}
            defaultValue={data}
            placeholder={placeholder}
          />
        ) : (
          <FilledInput
            className={classes.input}
            value={data ?? defaultValue ?? ''}
            fullWidth
            disabled={!isEditable}
            disableUnderline
            onClick={onInputClick}
            placeholder={placeholder}
          />
        )}
        <Button
          variant='contained'
          component='span'
          startIcon={startIcon}
          onClick={onClick}
        >
          {buttonText}
        </Button>
      </div>
    </FormGroup>
  );
};
