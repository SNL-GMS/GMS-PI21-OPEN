package gms.shared.stationdefinition.dao.css.converter;

import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class TagNameConverter extends EnumToStringConverter<TagName> {
  public TagNameConverter() {
    super(TagName.class, TagName::getName);
  }
}
