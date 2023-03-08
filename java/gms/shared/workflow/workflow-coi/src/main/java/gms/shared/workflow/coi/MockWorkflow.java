package gms.shared.workflow.coi;

import gms.shared.stationdefinition.coi.station.StationGroup;

import java.time.Duration;
import java.util.List;

public class MockWorkflow {

  private MockWorkflow() {
  }

  public static Workflow get() {

    var autoNetworkProcessingStage = getAutoNetworkStage();

    var al1ProcessingStage = getInteractiveAnalysisStage("AL1");

    var autoPostAl1ProcessingStage = getAutoPostAl1Stage();

    var al2ProcessingStage = getInteractiveAnalysisStage("AL2");

    var workflowStages = List.of(
      autoNetworkProcessingStage,
      al1ProcessingStage,
      autoPostAl1ProcessingStage,
      al2ProcessingStage
    );

    return Workflow.from(
      "Mock Workflow",
      workflowStages
    );
  }

  private static InteractiveAnalysisStage getInteractiveAnalysisStage(String stageName) {

    var stationGroup = StationGroup.createEntityReference("Station Group 1");

    var eventReviewActivity = Activity.from(
      "Event Review",
      stationGroup,
      AnalysisMode.EVENT_REVIEW
    );

    var scanActivity = Activity.from(
      "Scan",
      stationGroup,
      AnalysisMode.SCAN
    );

    var activities = List.of(
      eventReviewActivity,
      scanActivity
    );

    return InteractiveAnalysisStage.from(
      stageName,
      Duration.ofHours(1),
      activities
    );
  }

  private static AutomaticProcessingStage getAutoNetworkStage() {

    var originBeamSpStep = ProcessingStep.from("Origin Beam SP");
    var originBeamLpStep = ProcessingStep.from("Origin Beam LP");
    var recallStep = ProcessingStep.from("Recall");
    var arrivalBeamSpStep = ProcessingStep.from("Arrival Beam SP");
    var detectionLpStep = ProcessingStep.from("Detection LP");
    var recallLpStep = ProcessingStep.from("Recall LP");
    var magnitudeStep = ProcessingStep.from("Magnitude");
    var hydroEdpStep = ProcessingStep.from("Hydro EDP");
    var haeStep = ProcessingStep.from("HAE");

    var autoNetworkSteps = List.of(
      originBeamSpStep,
      originBeamLpStep,
      recallStep,
      arrivalBeamSpStep,
      detectionLpStep,
      recallLpStep,
      magnitudeStep,
      hydroEdpStep,
      haeStep
    );

    var processingSequence = ProcessingSequence.from(
      "Auto Network Seq",
      autoNetworkSteps
    );

    return AutomaticProcessingStage.from(
      "Auto Network",
      Duration.ofHours(1),
      List.of(processingSequence)
    );
  }

  private static AutomaticProcessingStage getAutoPostAl1Stage() {

    var partialProcessingStep = ProcessingStep.from("Partial Processing");
    var associationStep = ProcessingStep.from("Association");
    var conflictResolutionStep = ProcessingStep.from("Conflict Resolution");
    var originBeamSpStep = ProcessingStep.from("Origin Beam SP");
    var arrivalBeamSpStep = ProcessingStep.from("Arrival Beam SP");

    var sequenceSteps = List.of(
      partialProcessingStep,
      associationStep,
      conflictResolutionStep,
      originBeamSpStep,
      arrivalBeamSpStep
    );

    var processingSequence = ProcessingSequence.from(
      "Auto Post-AL1 Seq",
      sequenceSteps
    );

    return AutomaticProcessingStage.from(
      "Auto Post-AL1",
      Duration.ofHours(1),
      List.of(processingSequence)
    );
  }
}
