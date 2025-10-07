package swe4.sos.db;

import swe4.sos.gui.infrastructure.JoinKeyService;
import swe4.sos.gui.infrastructure.Repository;
import swe4.sos.gui.model.AppointmentSurvey;
import swe4.sos.gui.model.SurveyOption;
import swe4.sos.gui.model.User;
import swe4.sos.server.client.ClientCallback;

import java.rmi.RemoteException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class DBRepository implements Repository {

  // --- Singleton Implementation ---
  private static DBRepository instance;

  private final List<ClientCallback> observers = new ArrayList<>();
  private final String connectionString;
  private final String userName;
  private final String password;

  private DBRepository(String connectionString, String userName, String password) {
    this.connectionString = connectionString;
    this.userName = userName;
    this.password = password;
  }

  public static synchronized DBRepository getInstance(String connectionString, String userName, String password) {
    if (instance == null) {
      instance = new DBRepository(connectionString, userName, password);
    }
    return instance;
  }

  public Connection getConnection() throws DataAccessException {
    try {
      // Always return a new connection. The try-with-resources block will handle closing it.
      return DriverManager.getConnection(connectionString, userName, password);
    } catch (SQLException ex) {
      throw new DataAccessException("Can't establish connection to database. SQLException: "
        + ex.getMessage());
    }
  }

  // --- Observer Pattern Implementation ---

  @Override
  public synchronized void addObserver(ClientCallback observer) throws RemoteException {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  @Override
  public synchronized void removeObserver(ClientCallback observer) throws RemoteException {
    observers.remove(observer);
  }

  protected synchronized void notifyObservers() {
    // Create a copy to iterate over, avoiding ConcurrentModificationException
    new ArrayList<>(observers).forEach(observer -> {
      try {
        observer.notifyChange();
      } catch (RemoteException e) {
        // Remove stale observers (e.g., disconnected clients)
        System.err.println("Removing stale observer: " + e.getMessage());
        observers.remove(observer);
      }
    });
  }

  // --- User Management Implementation (Updated for Email) ---

  @Override
  public User getUser(int id) throws RemoteException {
    String sql = "SELECT id, username, email, password FROM user WHERE id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new User(rs.getInt("id"), rs.getString("username"), rs.getString("email"), rs.getString("password"));
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error fetching user by ID: " + e.getMessage());
    }
    return null;
  }

  @Override
  public User getUserByUsername(String username) throws RemoteException {
    String sql = "SELECT id, username, email, password FROM user WHERE username = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, username);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new User(rs.getInt("id"), rs.getString("username"), rs.getString("email"), rs.getString("password"));
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error fetching user by username: " + e.getMessage());
    }
    return null;
  }

  @Override
  public User getUserByEmail(String email) throws RemoteException {
    String sql = "SELECT id, username, email, password FROM user WHERE email = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, email);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new User(rs.getInt("id"), rs.getString("username"), rs.getString("email"), rs.getString("password"));
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error fetching user by email: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void addUser(String username, String password, String email) throws RemoteException {
    String sql = "INSERT INTO user (username, email, password) VALUES (?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, username);
      pstmt.setString(2, email);
      pstmt.setString(3, password);
      pstmt.executeUpdate();
      notifyObservers();
    } catch (SQLException e) {
      throw new DataAccessException("Error adding user: " + e.getMessage());
    }
  }

  // --- Survey Management Implementation ---

  @Override
  public void addSurvey(int userId, String label, String description) throws RemoteException {
    addSurvey(userId, label, description, new ArrayList<>());
  }

  @Override
  public void addSurvey(int userId, String label, String description, List<SurveyOption> options) throws RemoteException {
    String surveySql = "INSERT INTO survey (created_by, label, description, created_at, joinkey, open) VALUES (?, ?, ?, ?, ?, ?)";
    String participationSql = "INSERT INTO participation (user_id, survey_id) VALUES (?, ?)";
    String insertOptionSql = "INSERT INTO survey_option (timeOption, survey_id) VALUES (?, ?)";

    Connection conn = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false); // Start transaction

      // === Step 1: Create the Survey and Get its new ID ===
      int surveyId;
      try (PreparedStatement surveyPstmt = conn.prepareStatement(surveySql, Statement.RETURN_GENERATED_KEYS)) {
        surveyPstmt.setInt(1, userId);
        surveyPstmt.setString(2, label);
        surveyPstmt.setString(3, description);
        surveyPstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        surveyPstmt.setString(5, JoinKeyService.generateJoinKey());
        surveyPstmt.setBoolean(6, true);
        surveyPstmt.executeUpdate();

        try (ResultSet generatedKeys = surveyPstmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            surveyId = generatedKeys.getInt(1);
          } else {
            throw new SQLException("Creating survey failed, no ID obtained.");
          }
        }
      }

      // === Step 2: Add the creator to the participation table ===
      try(PreparedStatement participationPstmt = conn.prepareStatement(participationSql)) {
        participationPstmt.setInt(1, userId);
        participationPstmt.setInt(2, surveyId);
        participationPstmt.executeUpdate();
      }

      // === Step 3: Add the provided options (if any) ===
      if (options != null && !options.isEmpty()) {
        try (PreparedStatement optionPstmt = conn.prepareStatement(insertOptionSql)) {
          for (SurveyOption option : options) {
            optionPstmt.setTimestamp(1, Timestamp.valueOf(option.getTimeOption()));
            optionPstmt.setInt(2, surveyId);
            optionPstmt.addBatch();
          }
          optionPstmt.executeBatch();
        }
      }

      conn.commit(); // Commit all changes at once
      notifyObservers();

    } catch (SQLException e) {
      try {
        if (conn != null) conn.rollback();
      } catch (SQLException ex) {
        throw new DataAccessException("Error rolling back transaction: " + ex.getMessage());
      }
      throw new DataAccessException("Error adding survey with options: " + e.getMessage());
    } finally {
      try {
        if (conn != null) {
          conn.setAutoCommit(true);
          conn.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public AppointmentSurvey getSurvey(int surveyId) throws RemoteException {
    String surveySql = "SELECT * FROM survey WHERE id = ?";
    AppointmentSurvey survey = null;

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(surveySql)) {
      pstmt.setInt(1, surveyId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          survey = new AppointmentSurvey(
            rs.getInt("id"),
            rs.getInt("created_by"),
            rs.getString("label"),
            rs.getString("description"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getString("joinkey")
          );
          if (!rs.getBoolean("open")) {
            survey.close();
          }
        } else {
          return null; // Survey not found
        }
      }

      loadSurveyParticipants(conn, survey);
      loadSurveyInvitations(conn, survey);
      loadSurveyOptionsAndVotes(conn, survey);

    } catch (SQLException e) {
      throw new DataAccessException("Error getting survey: " + e.getMessage());
    }
    return survey;
  }

  @Override
  public List<AppointmentSurvey> getParticipatedSurveys(int userId) throws RemoteException {
    String sql = "SELECT DISTINCT id FROM survey s " +
      "LEFT JOIN participation p ON s.id = p.survey_id " +
      "WHERE s.created_by = ? OR p.user_id = ?";
    List<AppointmentSurvey> surveys = new ArrayList<>();
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      pstmt.setInt(2, userId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          surveys.add(getSurvey(rs.getInt("id")));
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error getting participated surveys: " + e.getMessage());
    }
    return surveys;
  }

  @Override
  public List<AppointmentSurvey> getInvitedSurveys(int userId) throws RemoteException {
    String sql = "SELECT survey_id FROM invitation " +
      "WHERE user_id = ? AND survey_id NOT IN (SELECT survey_id FROM participation WHERE user_id = ?)";
    List<AppointmentSurvey> surveys = new ArrayList<>();
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      pstmt.setInt(2, userId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          surveys.add(getSurvey(rs.getInt("survey_id")));
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error getting invited surveys: " + e.getMessage());
    }
    return surveys;
  }

  @Override
  public void removeSurvey(int surveyId) throws RemoteException {
    String sql = "DELETE FROM survey WHERE id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, surveyId);
      int affectedRows = pstmt.executeUpdate();
      if (affectedRows > 0) {
        notifyObservers();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error removing survey: " + e.getMessage());
    }
  }

  @Override
  public boolean participateInSurvey(String keyJoin, int userId) throws RemoteException {
    String surveyIdSql = "SELECT id FROM survey WHERE joinkey = ?";
    String checkParticipationSql = "SELECT COUNT(*) FROM participation WHERE user_id = ? AND survey_id = ?";
    String insertParticipationSql = "INSERT INTO participation (user_id, survey_id) VALUES (?, ?)";
    String deleteInvitationSql = "DELETE FROM invitation WHERE user_id = ? AND survey_id = ?";

    try (Connection conn = getConnection()) {
      int surveyId;
      try (PreparedStatement pstmt = conn.prepareStatement(surveyIdSql)) {
        pstmt.setString(1, keyJoin);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            surveyId = rs.getInt("id");
          } else {
            return false;
          }
        }
      }

      try (PreparedStatement pstmt = conn.prepareStatement(checkParticipationSql)) {
        pstmt.setInt(1, userId);
        pstmt.setInt(2, surveyId);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next() && rs.getInt(1) > 0) {
            return false;
          }
        }
      }

      try (PreparedStatement pstmt = conn.prepareStatement(insertParticipationSql)) {
        pstmt.setInt(1, userId);
        pstmt.setInt(2, surveyId);
        pstmt.executeUpdate();
      }

      try (PreparedStatement pstmt = conn.prepareStatement(deleteInvitationSql)) {
        pstmt.setInt(1, userId);
        pstmt.setInt(2, surveyId);
        pstmt.executeUpdate();
      }

      notifyObservers();
      return true;

    } catch (SQLException e) {
      throw new DataAccessException("Error participating in survey: " + e.getMessage());
    }
  }

  @Override
  public boolean declineSurvey(String keyJoin, int userId) throws RemoteException {
    String surveyIdSql = "SELECT id FROM survey WHERE joinkey = ?";
    String deleteInvitationSql = "DELETE FROM invitation WHERE user_id = ? AND survey_id = ?";

    try (Connection conn = getConnection()) {
      int surveyId;
      try (PreparedStatement pstmt = conn.prepareStatement(surveyIdSql)) {
        pstmt.setString(1, keyJoin);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            surveyId = rs.getInt("id");
          } else {
            return false;
          }
        }
      }

      try(PreparedStatement pstmt = conn.prepareStatement(deleteInvitationSql)) {
        pstmt.setInt(1, userId);
        pstmt.setInt(2, surveyId);
        int affectedRows = pstmt.executeUpdate();
        if(affectedRows > 0) {
          notifyObservers();
          return true;
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error declining survey: " + e.getMessage());
    }
    return false;
  }

  @Override
  public void updateSurvey(AppointmentSurvey currentSurvey) throws RemoteException {
    String updateSurveySql = "UPDATE survey SET label = ?, description = ?, open = ? WHERE id = ?";

    Connection conn = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      // 1. Update basic survey details
      try (PreparedStatement pstmt = conn.prepareStatement(updateSurveySql)) {
        pstmt.setString(1, currentSurvey.getLabel());
        pstmt.setString(2, currentSurvey.getDescription());
        pstmt.setBoolean(3, currentSurvey.isOpen());
        pstmt.setInt(4, currentSurvey.getId());
        pstmt.executeUpdate();
      }

      // 2. Sync invitations
      try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM invitation WHERE survey_id = ?")) {
        pstmt.setInt(1, currentSurvey.getId());
        pstmt.executeUpdate();
      }
      try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO invitation (user_id, survey_id) VALUES (?, ?)")) {
        for (Integer userId : currentSurvey.getInvitedUserIds()) {
          pstmt.setInt(1, userId);
          pstmt.setInt(2, currentSurvey.getId());
          pstmt.addBatch();
        }
        pstmt.executeBatch();
      }

      // 3. Sync options and votes (delete then re-insert)
      try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM vote WHERE survey_option_id IN (SELECT id FROM survey_option WHERE survey_id = ?)")) {
        pstmt.setInt(1, currentSurvey.getId());
        pstmt.executeUpdate();
      }
      try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM survey_option WHERE survey_id = ?")) {
        pstmt.setInt(1, currentSurvey.getId());
        pstmt.executeUpdate();
      }

      String insertOptionSql = "INSERT INTO survey_option (timeOption, survey_id) VALUES (?, ?)";
      String insertVoteSql = "INSERT INTO vote (user_id, survey_option_id, is_preferred) VALUES (?, ?, ?)";

      for (SurveyOption option : currentSurvey.getOptions()) {
        int newOptionId;
        try (PreparedStatement optionPstmt = conn.prepareStatement(insertOptionSql, Statement.RETURN_GENERATED_KEYS)) {
          optionPstmt.setTimestamp(1, Timestamp.valueOf(option.getTimeOption()));
          optionPstmt.setInt(2, currentSurvey.getId());
          optionPstmt.executeUpdate();
          try (ResultSet rs = optionPstmt.getGeneratedKeys()) {
            if (rs.next()) {
              newOptionId = rs.getInt(1);
            } else {
              throw new SQLException("Failed to create survey option.");
            }
          }
        }

        if (!option.getVotedUserIds().isEmpty()) {
          try (PreparedStatement votePstmt = conn.prepareStatement(insertVoteSql)) {
            for (Integer voterId : option.getVotedUserIds()) {
              votePstmt.setInt(1, voterId);
              votePstmt.setInt(2, newOptionId);
              // Check if this voter also preferred the option
              boolean isPreferred = option.hasPreferred(voterId);
              votePstmt.setBoolean(3, isPreferred);
              votePstmt.addBatch();
            }
            votePstmt.executeBatch();
          }
        }
      }

      conn.commit();
      notifyObservers();

    } catch (SQLException e) {
      try {
        if (conn != null) conn.rollback();
      } catch (SQLException ex) {
        throw new DataAccessException("Error rolling back transaction: " + ex.getMessage());
      }
      throw new DataAccessException("Error updating survey: " + e.getMessage());
    } finally {
      try {
        if (conn != null) conn.setAutoCommit(true);
      } catch (SQLException e) { /* log error */ }
    }
  }

  // --- Helper methods for populating survey objects ---

  private void loadSurveyParticipants(Connection conn, AppointmentSurvey survey) throws SQLException {
    String sql = "SELECT user_id FROM participation WHERE survey_id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, survey.getId());
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          survey.getParticipantUserIds().add(rs.getInt("user_id"));
        }
      }
    }
  }

  private void loadSurveyInvitations(Connection conn, AppointmentSurvey survey) throws SQLException {
    String sql = "SELECT user_id FROM invitation WHERE survey_id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, survey.getId());
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          survey.getInvitedUserIds().add(rs.getInt("user_id"));
        }
      }
    }
  }

  private void loadSurveyOptionsAndVotes(Connection conn, AppointmentSurvey survey) throws SQLException {
    String optionsSql = "SELECT id, timeOption FROM survey_option WHERE survey_id = ?";
    String votesSql = "SELECT user_id, is_preferred FROM vote WHERE survey_option_id = ?";

    SortedSet<SurveyOption> options = new TreeSet<>();

    try (PreparedStatement optionsPstmt = conn.prepareStatement(optionsSql)) {
      optionsPstmt.setInt(1, survey.getId());
      try (ResultSet optionsRs = optionsPstmt.executeQuery()) {
        while (optionsRs.next()) {
          int optionId = optionsRs.getInt("id");
          LocalDateTime timeOption = optionsRs.getTimestamp("timeOption").toLocalDateTime();
          SurveyOption option = new SurveyOption(optionId, timeOption);

          try (PreparedStatement votesPstmt = conn.prepareStatement(votesSql)) {
            votesPstmt.setInt(1, optionId);
            try (ResultSet votesRs = votesPstmt.executeQuery()) {
              while(votesRs.next()) {
                int userId = votesRs.getInt("user_id");
                boolean isPreferred = votesRs.getBoolean("is_preferred");

                option.vote(userId); // Every record in the vote table is a vote
                if (isPreferred) {
                  option.prefer(userId); // If the flag is set, it's also a preference
                }
              }
            }
          }
          options.add(option);
        }
      }
    }
    survey.getOptions().addAll(options);
  }
}