package gms.shared.frameworks.osd.dao.stationgroupsoh.converter;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.dao.util.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class AcquiredChannelEnvironmentIssueTypeConverter extends EnumToStringConverter<AcquiredChannelEnvironmentIssueType> {
  public AcquiredChannelEnvironmentIssueTypeConverter() {
    super(AcquiredChannelEnvironmentIssueType.class);
  }
}
