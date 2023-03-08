package gms.shared.signalenhancementconfiguration.coi.filter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gms.shared.signalenhancementconfiguration.coi.types.CascadeFilterName;
import gms.shared.signalenhancementconfiguration.coi.types.FilterDefinitionUsage;
import gms.shared.signalenhancementconfiguration.coi.types.FilterType;
import gms.shared.signalenhancementconfiguration.coi.types.PassBandType;
import gms.shared.signalenhancementconfiguration.coi.types.UnfilteredName;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Filter fixtures used for tests and signal enhancement configuration
 */
public class FilterFixtures {

  private FilterFixtures() {
  }

  private static final String FILTER_COMMENTS = "filter comments";
  public static final String SEISMIC = "SEISMIC";
  public static final String LONG_PERIOD = "LONG-PERIOD";
  public static final String HYDRO = "HYDRO";

  public static final String FILTER1_COMMENTS = "Filter 1 comments";
  public static final String FILTER2_COMMENTS = "Filter 2 comments";
  public static final String FILTER3_COMMENTS = "Filter 3 comments";
  public static final String FILTER4_COMMENTS = "Filter 4 comments";
  public static final String FILTER5_COMMENTS = "Filter 5 comments";
  public static final String FILTER6_COMMENTS = "Filter 6 comments";
  public static final String FILTER7_COMMENTS = "Filter 7 comments";
  public static final String FILTER8_COMMENTS = "Filter 8 comments";
  public static final String FILTER9_COMMENTS = "Filter 9 comments";
  public static final String FILTER10_COMMENTS = "Filter 10 comments";
  public static final String FILTER11_COMMENTS = "Filter 11 comments";
  public static final String FILTER12_COMMENTS = "Filter 12 comments";
  public static final String FILTER13_COMMENTS = "Filter 13 comments";
  public static final String FILTER14_COMMENTS = "Filter 14 comments";
  public static final String FILTER15_COMMENTS = "Filter 15 comments";

  public static final String CASCADE_FILTER_1 = "Cascade Filter 1";
  public static final String CASCADE_FILTER_2 = "Cascade Filter 2";
  public static final String CASCADE_FILTER_3 = "Cascade Filter 3";

  public static final String HAM_FIR_BP_0_70_2_00_HZ = "HAM FIR BP 0.70-2.00 Hz";
  public static final String HAM_FIR_BP_1_00_3_00_HZ = "HAM FIR BP 1.00-3.00 Hz";
  public static final String HAM_FIR_BP_4_00_8_00_HZ = "HAM FIR BP 4.00-8.00 Hz";
  public static final String HAM_FIR_BP_0_40_3_50_HZ = "HAM FIR BP 0.40-3.50 Hz";
  public static final String HAM_FIR_BP_0_50_4_00_HZ = "HAM FIR BP 0.50-4.00 Hz";
  public static final String HAM_FIR_BP_2_00_5_00_HZ = "HAM FIR BP 2.00-5.00 Hz";
  public static final String HAM_FIR_BP_1_50_3_00_HZ = "HAM FIR BP 1.50-3.00 Hz";
  public static final String HAM_FIR_BP_1_00_5_00_HZ = "HAM FIR BP 1.00-5.00 Hz";
  public static final String HAM_FIR_BP_0_50_2_50_HZ = "HAM FIR BP 0.50-2.50 Hz";
  public static final String HAM_FIR_BP_1_50_3_50_HZ = "HAM FIR BP 1.50-3.50 Hz";
  public static final String HAM_FIR_BP_1_70_3_20_HZ = "HAM FIR BP 1.70-3.20 Hz";
  public static final String HAM_FIR_LP_4_20_HZ = "HAM FIR LP 4.20 Hz";
  public static final String HAM_FIR_BP_0_50_1_50_HZ = "HAM FIR BP 0.50-1.50 Hz";
  public static final String HAM_FIR_BP_2_00_4_00_HZ = "HAM FIR BP 2.00-4.00 Hz";
  public static final String HAM_FIR_HP_0_30_HZ = "HAM FIR HP 0.30 Hz";
  public static final String HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL = "HAM FIR BP 0.50-4.00 HZ non causal";

  public static final String HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION = "HAM FIR BP 0.70-2.00 Hz Description";
  public static final String HAM_FIR_BP_1_00_3_00_HZ_DESCRIPTION = "HAM FIR BP 1.00-3.00 Hz Description";
  public static final String HAM_FIR_BP_4_00_8_00_HZ_DESCRIPTION = "HAM FIR BP 4.00-8.00 Hz Description";
  public static final String HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION = "HAM FIR BP 0.40-3.50 Hz Description";
  public static final String HAM_FIR_BP_0_50_4_00_HZ_DESCRIPTION = "HAM FIR BP 0.50-4.00 Hz Description";
  public static final String HAM_FIR_BP_2_00_5_00_HZ_DESCRIPTION = "HAM FIR BP 2.00-5.00 Hz Description";
  public static final String HAM_FIR_BP_1_50_3_00_HZ_DESCRIPTION = "HAM FIR BP 1.50-3.00 Hz Description";
  public static final String HAM_FIR_BP_1_00_5_00_HZ_DESCRIPTION = "HAM FIR BP 1.00-5.00 Hz Description";
  public static final String HAM_FIR_BP_0_50_2_50_HZ_DESCRIPTION = "HAM FIR BP 0.50-2.50 Hz Description";
  public static final String HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION = "HAM FIR BP 1.50-3.50 Hz Description";
  public static final String HAM_FIR_BP_1_70_3_20_HZ_DESCRIPTION = "HAM FIR BP 1.70-3.20 Hz Description";
  public static final String HAM_FIR_LP_4_20_HZ_DESCRIPTION = "HAM FIR LP 4.20 Hz Description";
  public static final String HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION = "HAM FIR BP 0.50-1.50 Hz Description";
  public static final String HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION = "HAM FIR BP 2.00-4.00 Hz Description";
  public static final String HAM_FIR_HP_0_30_HZ_DESCRIPTION = "HAM FIR HP 0.30 Hz Description";
  public static final String HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL_DESCRIPTION = "HAM FIR BP 0.50-4.00 HZ non causal Description";

  public static final ImmutableMap<String, Boolean> FILTER_ENTRY_NAME_TO_HOT_KEY_CYCLE_MAP =
    getFilterEntryNameToHotKeyCycleMap();

  public static final LinearFilterParameters LINEAR_FILTER_PARAMETERS = LinearFilterParameters.from(3.5,
    2.2, ImmutableList.copyOf(List.of(3.5)), ImmutableList.copyOf(List.of(3.5)),
    Duration.parse("PT1212.5273S"));
  public static final CascadeFilterParameters CASCADED_FILTERS_PARAMETERS = CascadeFilterParameters.from(
    3.4, 2, Optional.of(Duration.parse("PT1212.5273S")));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER1_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(0.7), Optional.of(2.0),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_1_00_3_00_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER2_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(1.0), Optional.of(3.0),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_4_00_8_00_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER3_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(4.0), Optional.of(8.0),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of("0.4 3.5 3 BP causal"), true, FilterType.FIR_HAMMING, Optional.of(0.4), Optional.of(3.5),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_0_50_4_00_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER5_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(0.5), Optional.of(4.0),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_2_00_5_00_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER6_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(2.0), Optional.of(5.0),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_1_50_3_00_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER7_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(1.5), Optional.of(3.0),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_1_00_5_00_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER8_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(1.0), Optional.of(5.0),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_0_50_2_50_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER9_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(0.5), Optional.of(2.5),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER10_COMMENTS), false, FilterType.FIR_HAMMING, Optional.of(1.5), Optional.of(3.5),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_1_70_3_20_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER11_COMMENTS), false, FilterType.FIR_HAMMING, Optional.of(1.7), Optional.of(3.2),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_LP_4_20_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER12_COMMENTS), false, FilterType.FIR_HAMMING, Optional.of(0.0), Optional.of(4.2),
    48,
    false, PassBandType.LOW_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER13_COMMENTS), false, FilterType.FIR_HAMMING, Optional.of(0.5), Optional.of(1.5),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER14_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(2.0), Optional.of(4.0),
    48,
    false, PassBandType.BAND_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_HP_0_30_HZ_DESCRIPTION = LinearFilterDescription.from(
    Optional.of(FILTER15_COMMENTS), true, FilterType.FIR_HAMMING, Optional.of(0.3), Optional.of(0.0),
    48,
    false, PassBandType.HIGH_PASS, Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final LinearFilterDescription LINEAR_HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL_DESCRIPTION =
    LinearFilterDescription.from(Optional.of(FILTER5_COMMENTS), false, FilterType.FIR_HAMMING,
      Optional.of(0.5), Optional.of(4.0), 48, false, PassBandType.HIGH_PASS,
      Optional.of(LINEAR_FILTER_PARAMETERS));
  public static final ImmutableList<FilterDescription> FILTER_DESCRIPTION_LIST_CAUSAL = ImmutableList
    .copyOf(Arrays.asList(LINEAR_HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION, LINEAR_HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION));

  public static final ImmutableList<FilterDescription> FILTER_DESCRIPTION_LIST_NON_CAUSAL = ImmutableList
    .copyOf(Arrays.asList(LINEAR_HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION, LINEAR_HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION));
  public static final CascadeFilterDescription CASCADED_FILTERS_1_DESCRIPTION = CascadeFilterDescription.from(
    Optional.of(CASCADE_FILTER_1), FILTER_DESCRIPTION_LIST_CAUSAL, Optional.of(CASCADED_FILTERS_PARAMETERS));
  public static final CascadeFilterDescription CASCADED_FILTERS_2_DESCRIPTION = CascadeFilterDescription.from(
    Optional.of(CASCADE_FILTER_2), FILTER_DESCRIPTION_LIST_NON_CAUSAL, Optional.of(CASCADED_FILTERS_PARAMETERS));
  public static final CascadeFilterDescription CASCADED_FILTERS_3_DESCRIPTION = CascadeFilterDescription.from(
    Optional.of(CASCADE_FILTER_3), FILTER_DESCRIPTION_LIST_CAUSAL, Optional.of(CASCADED_FILTERS_PARAMETERS));
  public static final FilterDefinition FILTER_DEFINITION_HAM_FIR_BP_0_70_2_00_HZ = FilterDefinition
    .from(HAM_FIR_BP_0_70_2_00_HZ,
    Optional.of(FILTER_COMMENTS), LINEAR_HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION);

  public static final FilterDefinition FILTER_DEFINITION_HAM_FIR_BP_0_40_3_50_HZ = FilterDefinition
    .from(HAM_FIR_BP_0_40_3_50_HZ,
      Optional.of(FILTER_COMMENTS), LINEAR_HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION);
  public static final FilterListEntry FILTER_LIST_ENTRY = FilterListEntry.from(true,
    Optional.empty(), Optional.empty(), Optional.of(FILTER_DEFINITION_HAM_FIR_BP_0_70_2_00_HZ));
  public static final List<FilterListEntry> FILTER_LIST_ENTRY_LIST = List.of(FILTER_LIST_ENTRY);

  // ------- Filter Coefficients -------

  private static final double[] FILTER1_B_COEFFS = new double[]{0.00154277211073, 0.00223135962309, 0.00273104312013,
    0.00280258383269, 0.00217656734384, 0.000812768009294, -0.000856234196934, -0.00192237976758, -0.0013754340351,
    0.00122672506506, 0.00510147945921, 0.0080189420631, 0.00682513728192, -0.00129622159881, -0.0172316219193,
    -0.0387105481955, -0.0601389046705, -0.0738477944677, -0.0725367436799, -0.0521167800143, -0.0138536966861,
    0.0351522813688, 0.0835493685776, 0.118991116265, 0.131989358502, 0.118991116265, 0.0835493685776, 0.0351522813688,
    -0.0138536966861, -0.0521167800143, -0.0725367436799, -0.0738477944677, -0.0601389046705, -0.0387105481955,
    -0.0172316219193, -0.00129622159881, 0.00682513728192, 0.0080189420631, 0.00510147945921, 0.00122672506506,
    -0.0013754340351, -0.00192237976758, -0.000856234196934, 0.000812768009294, 0.00217656734384, 0.00280258383269,
    0.00273104312013, 0.00223135962309, 0.00154277211073};

  private static final double[] FILTER2_B_COEFFS = new double[]{-0.00041931139298, -0.000674434004618, -0.00075295439347,
    -0.000564329029297, 6.00603432641e-19, 0.000949341935097, 0.00206546993035, 0.00282615645799, 0.0024372474717,
    -2.06057591484e-18, -0.00520585874328, -0.013391100708, -0.0239791261194, -0.0354872315641, -0.0456435786343,
    -0.051757642404, -0.0512886864221, -0.0424901510066, -0.0249687517076, 0, 0.0295102454011, 0.0594458360283,
    0.0852129861677, 0.102632086058, 0.108786936013, 0.102632086058, 0.0852129861677, 0.0594458360283,
    0.0295102454011, 0, -0.0249687517076, -0.0424901510066, -0.0512886864221, -0.051757642404, -0.0456435786343,
    -0.0354872315641, -0.0239791261194, -0.013391100708, -0.00520585874328, -2.06057591484e-18, 0.0024372474717,
    0.00282615645799, 0.00206546993035, 0.000949341935097, 6.00603432641e-19, -0.000564329029297, -0.00075295439347,
    -0.000674434004618, -0.00041931139298};

  private static final double[] FILTER3_B_COEFFS = new double[]{0.000384613747726, 0.00178371769276, -0.00212559624529,
    -0.000631950140381, -2.20361612866e-18, 0.00106309748039, 0.0058308380518, -0.00747451232012, -0.00223556740866,
    -5.66299554687e-18, 0.00346929764634, 0.0180454957668, -0.0219948747361, -0.00629412057373, -1.02543627331e-17,
    0.0091798888655, 0.0470445931855, -0.057258612032, -0.0166397199392, -1.40801363022e-17, 0.027068298811,
    0.157220111617, -0.240556938135, -0.114930045816, 0.399139654818, -0.114930045816, -0.240556938135, 0.157220111617,
    0.027068298811, -1.40801363022e-17, -0.0166397199392, -0.057258612032, 0.0470445931855, 0.0091798888655,
    -1.02543627331e-17, -0.00629412057373, -0.0219948747361, 0.0180454957668, 0.00346929764634, -5.66299554687e-18,
    -0.00223556740866, -0.00747451232012, 0.0058308380518, 0.00106309748039, -2.20361612866e-18, -0.000631950140381,
    -0.00212559624529, 0.00178371769276, 0.000384613747726};

  // ------- Workflow Definintion Ids and Pairs -------

  private static final WorkflowDefinitionId AL1_EVENT_REVIEW = WorkflowDefinitionId.from("AL1 Event Review");
  private static final WorkflowDefinitionId AL2_EVENT_REVIEW = WorkflowDefinitionId.from("AL2 Event Review");
  private static final WorkflowDefinitionId AL1_SCAN = WorkflowDefinitionId.from("AL1 Scan");
  private static final WorkflowDefinitionId AL2_SCAN = WorkflowDefinitionId.from("AL2 Scan");

  private static final WorkflowDefinitionIdStringPair SEISMIC_WORKFLOW_PAIR = WorkflowDefinitionIdStringPair.create(
    AL1_EVENT_REVIEW, SEISMIC);
  private static final WorkflowDefinitionIdStringPair LONG_PERIOD_WORKFLOW_PAIR = WorkflowDefinitionIdStringPair.create(
    AL2_EVENT_REVIEW, LONG_PERIOD);
  private static final WorkflowDefinitionIdStringPair HYDRO_WORKFLOW_PAIR = WorkflowDefinitionIdStringPair.create(
    AL1_SCAN, HYDRO);
  private static final WorkflowDefinitionIdStringPair SEISMIC_AL2_WORKFLOW_PAIR = WorkflowDefinitionIdStringPair.create(
    AL2_SCAN, SEISMIC);
  public static final List<WorkflowDefinitionIdStringPair> WORKFLOW_PAIR_LIST = List.of(SEISMIC_WORKFLOW_PAIR,
    LONG_PERIOD_WORKFLOW_PAIR, HYDRO_WORKFLOW_PAIR, SEISMIC_AL2_WORKFLOW_PAIR);

  // ------- Filter List Entry -------

  private static final FilterListEntry UNFILTERED_FILTER_LIST_ENTRY = FilterListEntry.from(true,
    Optional.of(true), Optional.empty(), Optional.empty());
  private static final FilterListEntry DETECTION_FILTER_LIST_ENTRY = FilterListEntry.from(false,
    Optional.empty(), Optional.of(FilterDefinitionUsage.DETECTION), Optional.empty());
  private static final FilterListEntry ONSET_FILTER_LIST_ENTRY = FilterListEntry.from(false,
    Optional.empty(), Optional.of(FilterDefinitionUsage.ONSET), Optional.empty());
  private static final FilterListEntry FK_FILTER_LIST_ENTRY = FilterListEntry.from(false,
    Optional.empty(), Optional.of(FilterDefinitionUsage.FK), Optional.empty());
  private static final FilterListEntry CASCADE_FILTER_1_LIST_ENTRY = FilterListEntry.from(true,
    Optional.empty(), Optional.empty(), Optional.of(FilterDefinition.from(CASCADE_FILTER_1,
      Optional.of(FILTER1_COMMENTS), CASCADED_FILTERS_1_DESCRIPTION)));
  private static final FilterListEntry CASCADE_FILTER_2_LIST_ENTRY = FilterListEntry.from(false,
    Optional.empty(), Optional.empty(), Optional.of(FilterDefinition.from(CASCADE_FILTER_2,
      Optional.of(FILTER2_COMMENTS), CASCADED_FILTERS_2_DESCRIPTION)));

  // custom filter list entries
  private static final FilterListEntry FIR1 = getFilter(true, HAM_FIR_BP_0_70_2_00_HZ, FILTER1_COMMENTS,
    0.7, 2.0, FILTER1_B_COEFFS);
  private static final FilterListEntry FIR2 = getFilter(true, HAM_FIR_BP_1_00_3_00_HZ, FILTER2_COMMENTS,
    1.0, 3.0, FILTER2_B_COEFFS);
  private static final FilterListEntry FIR22 = getFilter(false, HAM_FIR_BP_1_00_3_00_HZ, FILTER2_COMMENTS,
    1.0, 3.0, FILTER2_B_COEFFS);
  private static final FilterListEntry FIR3 = getFilter(true, HAM_FIR_BP_4_00_8_00_HZ, FILTER3_COMMENTS,
    4.0, 8.0, FILTER3_B_COEFFS);

  // lists of custom filter list entries
  private static final List<FilterListEntry> SEISMIC_FILTER_LIST_ENTRIES = List.of(UNFILTERED_FILTER_LIST_ENTRY,
    DETECTION_FILTER_LIST_ENTRY, ONSET_FILTER_LIST_ENTRY, FK_FILTER_LIST_ENTRY, FIR1, FIR2, CASCADE_FILTER_1_LIST_ENTRY);
  private static final List<FilterListEntry> LONG_PERIOD_FILTER_LIST_ENTRIES = List.of(UNFILTERED_FILTER_LIST_ENTRY,
    DETECTION_FILTER_LIST_ENTRY, ONSET_FILTER_LIST_ENTRY, FK_FILTER_LIST_ENTRY, FIR2, FIR3, CASCADE_FILTER_2_LIST_ENTRY);
  private static final List<FilterListEntry> HYDRO_FILTER_LIST_ENTRIES = List.of(UNFILTERED_FILTER_LIST_ENTRY,
    DETECTION_FILTER_LIST_ENTRY, ONSET_FILTER_LIST_ENTRY, FK_FILTER_LIST_ENTRY, FIR1, FIR22, CASCADE_FILTER_1_LIST_ENTRY);

  // ------- Filter List -------

  public static final FilterList SEISMIC_FILTER_LIST = FilterList.from(SEISMIC, 0,
    ImmutableList.copyOf(SEISMIC_FILTER_LIST_ENTRIES));
  public static final FilterList LONG_PERIOD_FILTER_LIST = FilterList.from(LONG_PERIOD, 1,
    ImmutableList.copyOf(LONG_PERIOD_FILTER_LIST_ENTRIES));
  public static final FilterList HYDRO_FILTER_LIST = FilterList.from(HYDRO, 2,
    ImmutableList.copyOf(HYDRO_FILTER_LIST_ENTRIES));

  public static final List<FilterList> DEFAULT_FILTER_LIST = List.of(SEISMIC_FILTER_LIST, LONG_PERIOD_FILTER_LIST,
    HYDRO_FILTER_LIST);

  /**
   * Create filter using specified parameters
   *
   * @param hotKey boolean hotkey
   * @param filterName filter name
   * @param comments filter comments
   * @param lowFreq low freq val
   * @param highFreq high freq val
   * @param bCoefficients b filter coefficients
   * @return {@link FilterListEntry}
   */
  private static FilterListEntry getFilter(boolean hotKey, String filterName, String comments, double lowFreq,
    double highFreq,
    double[] bCoefficients) {
    var linearFilterParameters = LinearFilterParameters.from(20.0,
      0.05, ImmutableList.copyOf(List.of(1.0)),
      ImmutableList.copyOf(DoubleStream.of(bCoefficients).boxed().collect(Collectors.toList())),
      Duration.parse("PT1.2S"));

    var filterDescription = LinearFilterDescription.from(Optional.of(comments), true,
      FilterType.FIR_HAMMING, Optional.of(lowFreq), Optional.of(highFreq), 48, false,
      PassBandType.BAND_PASS, Optional.of(linearFilterParameters));

    var filterDefinition = FilterDefinition.from(filterName, Optional.of(comments), filterDescription);

    return FilterListEntry.from(hotKey, Optional.empty(), Optional.empty(),
      Optional.of(filterDefinition));
  }

  private static ImmutableMap<String, Boolean> getFilterEntryNameToHotKeyCycleMap() {
    Map<String, Boolean> defaultFilterEntries = new LinkedHashMap<>();
    defaultFilterEntries.put(UnfilteredName.UNFILTERED.name(), true);
    defaultFilterEntries.put(HAM_FIR_BP_0_70_2_00_HZ, true);
    defaultFilterEntries.put(HAM_FIR_BP_1_00_3_00_HZ, true);
    defaultFilterEntries.put(HAM_FIR_BP_4_00_8_00_HZ, true);
    defaultFilterEntries.put(FilterDefinitionUsage.DETECTION.name(), false);
    defaultFilterEntries.put(FilterDefinitionUsage.FK.name(), false);
    defaultFilterEntries.put(FilterDefinitionUsage.ONSET.name(), false);
    defaultFilterEntries.put(CascadeFilterName.CASCADE_FILTER_1.getFilterName(), true);
    defaultFilterEntries.put(CascadeFilterName.CASCADE_FILTER_2.getFilterName(), false);
    defaultFilterEntries.put(CascadeFilterName.CASCADE_FILTER_3.getFilterName(), true);

    return ImmutableMap.copyOf(defaultFilterEntries);
  }
}
