package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import gms.shared.frameworks.osd.dto.soh.DoubleOrInteger.DoubleValue;
import gms.shared.frameworks.osd.dto.soh.DoubleOrInteger.IntegerValue;

import java.io.IOException;

public class HistoricalAcquiredChannelEnvironmentalIssuesSerializer extends
  StdSerializer<HistoricalAcquiredChannelEnvironmentalIssues> {

  public HistoricalAcquiredChannelEnvironmentalIssuesSerializer() {
    this(null);
  }

  protected HistoricalAcquiredChannelEnvironmentalIssuesSerializer(
    Class<HistoricalAcquiredChannelEnvironmentalIssues> t) {
    super(t);
  }

  @Override
  public void serialize(HistoricalAcquiredChannelEnvironmentalIssues issues, JsonGenerator jsonGenerator,
    SerializerProvider provider)
    throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("channelName", issues.getChannelName());
    jsonGenerator.writeStringField("monitorType", issues.getMonitorType());
    jsonGenerator.writeArrayFieldStart("issues");
    for (LineSegment lineSegment : issues.getTrendLine()) {
      jsonGenerator.writeStartArray();
      for (DataPoint dataPoint : lineSegment.getDataPoints()) {
        var status = dataPoint.getStatus();
        jsonGenerator.writeStartArray();
        jsonGenerator.writeNumber(dataPoint.getTimeStamp());
        if (status instanceof IntegerValue) {
          jsonGenerator.writeNumber(((IntegerValue) status).getValue());
        } else {
          jsonGenerator.writeNumber(((DoubleValue) status).getValue());
        }
        jsonGenerator.writeEndArray();
      }
      jsonGenerator.writeEndArray();
    }
    jsonGenerator.writeEndArray();
    jsonGenerator.writeEndObject();
  }

}
