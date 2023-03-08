package converter;

import gms.shared.user.preferences.coi.UserInterfaceMode;

import javax.persistence.Converter;

@Converter
public class UserInterfaceModeConverter extends EnumToStringConverter<UserInterfaceMode> {
  public UserInterfaceModeConverter() {
    super(UserInterfaceMode.class);
  }
}
