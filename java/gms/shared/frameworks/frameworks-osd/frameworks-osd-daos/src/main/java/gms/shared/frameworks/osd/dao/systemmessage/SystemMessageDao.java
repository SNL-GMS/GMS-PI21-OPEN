package gms.shared.frameworks.osd.dao.systemmessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSeverity;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSubCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "system_message")
@TypeDef(
  name = "jsonb",
  typeClass = JsonBinaryType.class
)
@TypeDef(
  name = "pgsql_enum",
  typeClass = PostgreSQLEnumType.class
)
public class SystemMessageDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(SystemMessageDao.class);

  private static final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "time", nullable = false)
  private Instant time;

  @Column(name = "message", nullable = false)
  private String message;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "system_message_type", nullable = false, columnDefinition = "public.system_message_type_enum")
  private SystemMessageType systemMessageType;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "system_message_severity", nullable = false, columnDefinition = "public.system_message_severity_enum")
  private SystemMessageSeverity systemMessageSeverity;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "system_message_category", nullable = false, columnDefinition = "public.system_message_category_enum")
  private SystemMessageCategory systemMessageCategory;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "system_message_sub_category", nullable = false, columnDefinition = "public.system_message_sub_category_enum")
  private SystemMessageSubCategory systemMessageSubCategory;

  @Type(type = "jsonb")
  @Column(name = "messageTags", columnDefinition = "jsonb")
  private String messageTags;

  public SystemMessageDao() {
    // empty JPA constructor
  }

  private SystemMessageDao(UUID id, Instant time, String message,
    SystemMessageType systemMessageType,
    SystemMessageSeverity systemMessageSeverity,
    SystemMessageCategory systemMessageCategory,
    SystemMessageSubCategory systemMessageSubCategory, String messageTags) {

    this.id = Objects.requireNonNull(id);
    this.time = Objects.requireNonNull(time);
    this.message = Objects.requireNonNull(message);
    this.systemMessageType = Objects.requireNonNull(systemMessageType);
    this.systemMessageSeverity = Objects.requireNonNull(systemMessageSeverity);
    this.systemMessageCategory = Objects.requireNonNull(systemMessageCategory);
    this.systemMessageSubCategory = Objects.requireNonNull(systemMessageSubCategory);
    this.messageTags = Objects.requireNonNull(messageTags);
  }

  public SystemMessage toCoi() {
    Map<String, Object> tags = null;

    try {
      tags = jsonObjectMapper.readValue(this.messageTags, Map.class);
    } catch (JsonProcessingException e) {
      LOGGER.error("Error creating COI object: ", e);
    }

    return SystemMessage
      .from(this.id, this.time, this.message, this.systemMessageType, this.systemMessageSeverity,
        this.systemMessageCategory, this.systemMessageSubCategory, tags);
  }

  public static SystemMessageDao from(SystemMessage systemMessage) {
    var messageTags = jsonObjectMapper.valueToTree(systemMessage.getMessageTags()).toString();

    return new SystemMessageDao(systemMessage.getId(), systemMessage.getTime(),
      systemMessage.getMessage(), systemMessage.getType(), systemMessage.getSeverity(),
      systemMessage.getCategory(), systemMessage.getSubCategory(),
      messageTags);
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public SystemMessageType getSystemMessageType() {
    return systemMessageType;
  }

  public void setSystemMessageType(
    SystemMessageType systemMessageType) {
    this.systemMessageType = systemMessageType;
  }

  public SystemMessageSeverity getSystemMessageSeverity() {
    return systemMessageSeverity;
  }

  public void setSystemMessageSeverity(
    SystemMessageSeverity systemMessageSeverity) {
    this.systemMessageSeverity = systemMessageSeverity;
  }

  public SystemMessageCategory getSystemMessageCategory() {
    return systemMessageCategory;
  }

  public void setSystemMessageCategory(
    SystemMessageCategory systemMessageCategory) {
    this.systemMessageCategory = systemMessageCategory;
  }

  public SystemMessageSubCategory getSystemMessageSubCategory() {
    return systemMessageSubCategory;
  }

  public void setSystemMessageSubCategory(
    SystemMessageSubCategory systemMessageSubCategory) {
    this.systemMessageSubCategory = systemMessageSubCategory;
  }

  public String getMessageTags() {
    return messageTags;
  }

  public void setMessageTags(String messageTags) {
    this.messageTags = messageTags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SystemMessageDao that = (SystemMessageDao) o;
    return id.equals(that.id) &&
      time.equals(that.time) &&
      message.equals(that.message) &&
      systemMessageType == that.systemMessageType &&
      systemMessageSeverity == that.systemMessageSeverity &&
      systemMessageCategory == that.systemMessageCategory &&
      systemMessageSubCategory == that.systemMessageSubCategory &&
      messageTags.equals(that.messageTags);
  }

  @Override
  public int hashCode() {
    return Objects
      .hash(id, time, message, systemMessageType, systemMessageSeverity, systemMessageCategory,
        systemMessageSubCategory, messageTags);
  }

  @Override
  public String toString() {
    return "SystemMessageDao{" +
      "id=" + id +
      ", time=" + time +
      ", message='" + message + '\'' +
      ", systemMessageType=" + systemMessageType +
      ", systemMessageSeverity=" + systemMessageSeverity +
      ", systemMessageCategory=" + systemMessageCategory +
      ", systemMessageSubCategory=" + systemMessageSubCategory +
      ", messageTags='" + messageTags + '\'' +
      '}';
  }
}
