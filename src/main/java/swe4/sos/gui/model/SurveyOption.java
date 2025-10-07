package swe4.sos.gui.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class SurveyOption implements Comparable<SurveyOption>, Serializable {
  private int id; // optional for DB use
  private LocalDateTime timeOption;
  private Set<Integer> votedUserIds = new HashSet<>(), preferredUserIds = new HashSet<>();

  @Override
  public int compareTo(SurveyOption o) {
    int voteDiff = Integer.compare(o.getWeight(), this.getWeight()); // descending
    if (voteDiff != 0) return voteDiff;
    else {
      voteDiff = Integer.compare(o.getPreferredWeight(), this.getPreferredWeight());
      if (voteDiff != 0) return voteDiff;
      return this.timeOption.compareTo(o.timeOption); // ascending by time
    }
  }

  public SurveyOption(int id, LocalDateTime timeOption) {
    this.id = id;
    this.timeOption = timeOption;
  }

  public LocalDateTime getTimeOption() {
    return timeOption;
  }

  public Set<Integer> getVotedUserIds() {
    return votedUserIds;
  }

  public int getWeight() {
    return votedUserIds.size();
  }

  public void vote(int userId) {
    votedUserIds.add(userId);
  }

  public int getPreferredWeight() {
    return preferredUserIds.size();
  }

  public void prefer(int userId) {
    preferredUserIds.add(userId);
  }

  public void revokePreferVote(int userId) {
    preferredUserIds.remove(userId);
  }

  public void revokeVote(int userId) {
    votedUserIds.remove(userId);
  }

  public int getVoteCount() {
    return votedUserIds.size();
  }

  public boolean hasVoted(int userId) {
    return votedUserIds.contains(userId);
  }

  public boolean hasPreferred(int userId) {
    return preferredUserIds.contains(userId);
  }

}
