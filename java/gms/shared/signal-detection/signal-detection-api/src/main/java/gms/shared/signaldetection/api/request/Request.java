package gms.shared.signaldetection.api.request;

import gms.shared.workflow.coi.WorkflowDefinitionId;

public interface Request {
  WorkflowDefinitionId getStageId();
}
