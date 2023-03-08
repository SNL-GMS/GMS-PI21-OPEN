package gms.shared.user.manager;

import gms.shared.user.preferences.api.UserPreferencesRepositoryInterface;
import gms.shared.user.preferences.coi.UserPreferences;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static gms.shared.frameworks.common.ContentType.MSGPACK_NAME;

@RestController
@RequestMapping(value = "/user-preferences")
public class UserManager {

  private final UserPreferencesRepositoryInterface userPreferencesRepositoryImpl;

  @Autowired
  public UserManager(UserPreferencesRepositoryInterface userPreferencesRepositoryImpl) {
    this.userPreferencesRepositoryImpl = userPreferencesRepositoryImpl;
  }

  /**
   * Returns an optional {@link UserPreferences} object for the given userId
   *
   * @param userId user id string to query
   * @return {@link UserPreferences} object
   */
  @PostMapping(value = "", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Returns the user preferences specified by the provided user id")
  public Optional<UserPreferences> getUserPreferencesByUserId(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User id to retrieve preferences for")
    @RequestBody String userId) {
    //TODO: when switching to Spring we noticed the ui was sending quotes as in userId string ex: "userId" we should fix the UI or accept json
    userId = userId.replace("\"", "");
    return userPreferencesRepositoryImpl.getUserPreferencesByUserId(userId);
  }

  @PostMapping(value = "/store", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MSGPACK_NAME})
  @Operation(summary = "Stores the provided user preferences")
  public Optional<Object> setUserPreferences(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User preferences to store")
    @RequestBody UserPreferences userPreferences) {
    userPreferencesRepositoryImpl.setUserPreferences(userPreferences);
    return Optional.empty();
  }
}
