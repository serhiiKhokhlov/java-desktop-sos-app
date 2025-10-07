package swe4.sos.gui.infrastructure;

import swe4.sos.gui.model.User;

public class AuthenticationService {
  private static User currentUser;

  public static void setCurrentUser(User user) {
    currentUser = user;
  }

  public static User getCurrentUser() {
    return currentUser;
  }

  public static int getCurrentUserId() {
    return currentUser != null ? currentUser.getId() : -1;
  }

  public static void clear() {
    currentUser = null;
  }
}
