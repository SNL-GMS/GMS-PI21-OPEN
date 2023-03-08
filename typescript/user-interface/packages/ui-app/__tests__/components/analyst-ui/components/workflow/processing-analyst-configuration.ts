import type { ProcessingAnalystConfigurationQuery } from '@gms/ui-state';
import { processingAnalystConfiguration } from '@gms/ui-state/__tests__/__data__/processing-analyst-configuration';

import { useQueryStateResult } from '../../../../__data__/test-util-data';

const query: ProcessingAnalystConfigurationQuery = useQueryStateResult;
query.data = processingAnalystConfiguration;

export const processingAnalystConfigurationQuery = query;
