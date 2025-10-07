package swe4.sos.gui.infrastructure;

import swe4.sos.gui.model.AppointmentSurvey;
import swe4.sos.gui.model.SurveyOption;
import swe4.sos.gui.model.User;
import swe4.sos.server.client.ClientCallback;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeRepository implements Repository {
  private static FakeRepository instance;
  private final List<ClientCallback> observers = new ArrayList<>();

  private int userId = 1;
  private int surveyId = 1;
  private final List<UserData> users = new ArrayList<>();
  private final Map<Integer, AppointmentSurvey> surveysById = new HashMap<>();
  private final Map<String, AppointmentSurvey>  surveysByJoinKey = new HashMap<>();

  private FakeRepository() {
    // mock users
    try {
      addUser("admin", "1234", "admin@example.com");
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    try {
      addUser("user", "user123", "user@example.com");
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    try {
      addUser("john", "john123", "john@example.com");
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }

    // Mock surveysById
    initializeMockSurveys();
  }

  public static FakeRepository getInstance() {
    if (instance == null) {
      instance = new FakeRepository();
    }
    return instance;
  }

  private void initializeMockSurveys() {
    // Survey 1 - Created by admin (id=1)
    try {
      addSurvey(1, "Team Meeting", "Weekly sync");
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    AppointmentSurvey teamMeeting = null;
    try {
      teamMeeting = getSurvey(1);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }

    // Clear auto-invites and add original invites
    teamMeeting.getInvitedUserIds().clear();
    teamMeeting.inviteUser(3);  // Invite john
    teamMeeting.inviteUser(2);  // Invite user
    teamMeeting.join(1);        // Admin participates

    // Add options
    SurveyOption option1 = new SurveyOption(1, LocalDateTime.now().plusDays(1).withHour(14));
    option1.vote(1);
    teamMeeting.addOption(option1);

    SurveyOption option2 = new SurveyOption(2, LocalDateTime.now().plusDays(2).withHour(10));
    option2.vote(1);
    option2.prefer(1);
    option2.vote(2);
    teamMeeting.addOption(option2);

    // Survey 2 - Created by user (id=2)
    try {
      addSurvey(2, "Project Review", "Q2 Planning");
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    AppointmentSurvey projectReview = null;
    try {
      projectReview = getSurvey(2);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    projectReview.join(2);  // Creator participates

    // Add options
    SurveyOption option3 = new SurveyOption(3, LocalDateTime.now().plusDays(3).withHour(15));
    option3.vote(2);
    projectReview.addOption(option3);

    SurveyOption option4 = new SurveyOption(4, LocalDateTime.now().plusDays(2).withHour(15));
    option4.vote(2);
    projectReview.addOption(option4);
  }

  @Override
  public synchronized void addObserver(ClientCallback observer) throws RemoteException {
    observers.add(observer);
  }

  @Override
  public synchronized void removeObserver(ClientCallback observer) throws RemoteException {
    observers.remove(observer);
  }

  protected synchronized void notifyObservers() {
    observers.forEach(observer -> {
      try {
        observer.notifyChange();
      } catch (RemoteException e) {
        // Remove stale observers (e.g., disconnected clients)
        observers.remove(observer);
      }
    });
  }

  @Override
  public synchronized User getUser(int id) throws RemoteException {
    for (UserData user : users) {
      if (user.id == id) return new User(user.id, user.username, user.password);
    }
    return null;
  }

  @Override
  public synchronized User getUserByUsername(String username) throws RemoteException {
    for (UserData user : users) {
      if (user.username.equals(username)) return new User(user.id, user.username, user.password);
    }
    return null;
  }

  @Override
  public synchronized User getUserByEmail(String email) throws RemoteException {
    for (UserData user : users) {
      if (user.email.equals(email)) return new User(user.id, user.username, user.password);
    }
    return null;
  }

  @Override
  public synchronized void addUser(String username, String password, String email) throws RemoteException {
    users.add(new UserData(userId++, username, password, email));
    notifyObservers();
  }

  @Override
  public synchronized List<AppointmentSurvey> getParticipatedSurveys(int userId) throws RemoteException {
    return surveysById.values().stream()
      .filter(s -> s.getParticipantUserIds().contains(userId) || s.getCreatedByUserId() == userId)
      .toList();
  }

  @Override
  public synchronized List<AppointmentSurvey> getInvitedSurveys(int userId) throws RemoteException {
    return surveysById.values().stream()
      .filter(s -> s.getInvitedUserIds().contains(userId))
      .filter(s -> !s.getParticipantUserIds().contains(userId))
      .toList();
  }

  @Override
  public synchronized void addSurvey(int userId, String label, String description) throws RemoteException {
    String joinKey;

    AppointmentSurvey survey = new AppointmentSurvey(surveyId++, userId,
      label, description, LocalDateTime.now(), joinKey = JoinKeyService.generateJoinKey());

    System.out.printf("JoinKey for %s is %s\n", label, joinKey);

    surveysById.put(survey.getId(), survey);
    surveysByJoinKey.put(joinKey, survey);
    notifyObservers();
  }

  @Override
  public synchronized void addSurvey(int userId, String label, String description, List<SurveyOption> options) throws RemoteException {
    String joinKey;

    AppointmentSurvey survey = new AppointmentSurvey(surveyId++, userId,
      label, description, LocalDateTime.now(), joinKey = JoinKeyService.generateJoinKey(), options);

    System.out.printf("JoinKey for %s is %s\n", label, joinKey);

    surveysById.put(survey.getId(), survey);
    surveysByJoinKey.put(joinKey, survey);
    notifyObservers();
  }

  @Override
  public synchronized AppointmentSurvey getSurvey(int surveyId) throws RemoteException {
    return surveysById.get(surveyId);
  }

  @Override
  public synchronized void removeSurvey(int surveyId) throws RemoteException {
    surveysById.remove(surveyId);
    notifyObservers();
  }

  @Override
  public boolean participateInSurvey(String keyJoin, int userId) throws RemoteException {
    AppointmentSurvey survey = surveysByJoinKey.get(keyJoin);
    try {
      if (survey != null && !getParticipatedSurveys(userId).contains(survey)) {
        survey.join(userId);
        notifyObservers();
        return true;
      }
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  @Override
  public boolean declineSurvey(String keyJoin, int userId) throws RemoteException {
    AppointmentSurvey survey = surveysByJoinKey.get(keyJoin);
    if (survey == null) return false;

    if (survey.getInvitedUserIds().contains(userId) &&
      !survey.getParticipantUserIds().contains(userId)) {

      survey.getInvitedUserIds().remove(userId);
      notifyObservers();

      return true;
    }

    return false;
  }

  @Override
  public void updateSurvey(AppointmentSurvey currentSurvey) throws RemoteException {
    surveysById.put(currentSurvey.getId(), currentSurvey);

    surveysByJoinKey.values().removeIf(s -> s.getId() == currentSurvey.getId());
    surveysByJoinKey.put(currentSurvey.getJoinKey(), currentSurvey);

    notifyObservers(); 
  }
}
