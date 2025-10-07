package swe4.sos.gui.infrastructure;

public class UserData {
  public int id;
  public String username, password, email;
  public UserData(int id, String username, String password, String email) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
  }
}