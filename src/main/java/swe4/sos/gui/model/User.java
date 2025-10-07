package swe4.sos.gui.model;

import java.io.Serializable;

public class User implements Serializable {
  private int id;
  private String username;
  private String email;
  private String password; // In real apps, store hashes only

  public User(int id, String username, String password) {
    this.id = id;
    this.username = username;
    this.password = password;
  }

  public User(int id, String username, String email, String password) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
  }

  // Getters & setters
  public int getId() { return id; }
  public String getUsername() { return username; }
  public String getPassword() { return password; }
  public String getEmail() { return email; }
}
