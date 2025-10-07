package swe4.sos.gui.model;

import swe4.sos.gui.infrastructure.SurveyRepository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class AppointmentSurvey implements Serializable {
  private int id, createdByUserId;
  private String label, description;
  private LocalDateTime createdAt;
  private String joinKey;
  private boolean open = true;

  private Set<Integer> invitedUserIds = new HashSet<>();
  private Set<Integer> participantUserIds = new HashSet<>();
  private SortedSet<SurveyOption> options = new TreeSet<>();

  public AppointmentSurvey(int id, int createdByUserId,
                           String label, String description,
                           LocalDateTime createdAt, String joinKey) {
    this.id = id;
    this.createdByUserId = createdByUserId;
    this.label = label;
    this.description = description;
    this.createdAt = createdAt;
    this.joinKey = joinKey;
    participantUserIds.add(createdByUserId);
  }

  public AppointmentSurvey(int id, int createdByUserId,
                           String label, String description,
                           LocalDateTime createdAt, String joinKey, List<SurveyOption> options) {
    this(id, createdByUserId, label, description, createdAt, joinKey);
    this.options.addAll(options);
  }

  public boolean isOpen() { return open; }

  public void close() {
    if (open == false) System.out.println("Appointment survey already closed");

    open = false;
    System.out.printf("Appointment survey was closed with name %s\n", label); // debug

  }

  public int getId() {
    return id;
  }

  public int getCreatedByUserId() {
    return createdByUserId;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Set<Integer> getInvitedUserIds() {
    return invitedUserIds;
  }

  public Set<Integer> getParticipantUserIds() {
    return participantUserIds;
  }

  public SortedSet<SurveyOption> getOptions() {
    return options;
  }

  public void addOption(SurveyOption option) {
    options.add(option);
  }

  public String getJoinKey() {
    return joinKey;
  }

  public void inviteUser(int userId) {
    invitedUserIds.add(userId);
  }

  public void join(int userId) {
    participantUserIds.add(userId);
  }
}
