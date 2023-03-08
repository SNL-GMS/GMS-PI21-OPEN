export const layoutStyles = {
  container: {
    padding: '1em',
    width: '100%',
  },
  title: {
    textAlign: 'center',
    padding: '0.25em',
  },
} as const;

export const indentedContainer = {
  ...layoutStyles.container,
  position: 'relative',
  paddingLeft: '3rem',
  margin: 0,
  '&::before': {
    content: '""',
    position: 'absolute',
    display: 'block',
    backgroundColor: '#DDD',
    width: '0.125em',
    top: 0,
    bottom: 0,
    left: 0
  }
} as const;