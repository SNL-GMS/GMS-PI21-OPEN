import type { DataPayload } from '../cache/types';
import type { TimeRange } from '../common/types';

// ***************************************
// Mutations
// ***************************************

export interface QcMaskInput {
  timeRange: TimeRange;
  category: string;
  type: string;
  rationale: string;
}

export interface CreateQcMaskMutationArgs {
  channelNames: string[];
  input: QcMaskInput;
}

export interface CreateQcMaskMutationResult {
  data: {
    createQcMask: DataPayload;
  };
}

export interface RejectQcMaskMutationArgs {
  maskId: string;
  inputRationale: string;
}

export interface RejectQcMaskMutationResult {
  data: {
    rejectQcMask: DataPayload;
  };
}

export interface UpdateQcMaskMutationArgs {
  maskId: string;
  input: QcMaskInput;
}

export interface UpdateQcMaskMutationResult {
  data: {
    updateQcMask: DataPayload;
  };
}
