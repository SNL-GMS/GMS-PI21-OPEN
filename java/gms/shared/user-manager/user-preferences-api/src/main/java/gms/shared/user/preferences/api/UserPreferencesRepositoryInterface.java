package gms.shared.user.preferences.api;

import gms.shared.user.preferences.coi.UserPreferences;

import java.util.Optional;

public interface UserPreferencesRepositoryInterface {

  /**
   * Returns an optional {@link UserPreferences} object for the given userId
   *
   * @param userId user id string to query
   * @return {@link UserPreferences} object
   */
  Optional<UserPreferences> getUserPreferencesByUserId(String userId);

  /**
   * Stores {@link UserPreferences} in the database
   *
   * @param userPreferences {@link UserPreferences} input object
   */
  void setUserPreferences(UserPreferences userPreferences);
}
