package de.denkunddachte.b2biutil.loader;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.denkunddachte.b2biutil.loader.model.AbstractLoadRecord;

public class LoaderResult {
  public enum Artifact {
    FGPartner, UserAccount, RoutingRule, LdapUser, FetchRule
  }

  public enum Outcome {
    ADDED, DELETED, MODIFIED, SKIPPED, FAILED
  }

  private boolean                               loadResult;
  private final EnumMap<Artifact, Set<Integer>> added    = new EnumMap<>(Artifact.class);
  private final EnumMap<Artifact, Set<Integer>> deleted  = new EnumMap<>(Artifact.class);
  private final EnumMap<Artifact, Set<Integer>> modified = new EnumMap<>(Artifact.class);
  private final EnumMap<Artifact, Set<Integer>> skipped  = new EnumMap<>(Artifact.class);
  private final EnumMap<Artifact, Set<Integer>> failed   = new EnumMap<>(Artifact.class);
  private LoaderInput                           input;
  private final List<String>                    messages = new ArrayList<>();

  public LoaderResult(LoaderInput input) {
    this.input = input;
    for (Artifact a : Artifact.values()) {
      added.put(a, new HashSet<>());
      deleted.put(a, new HashSet<>());
      modified.put(a, new HashSet<>());
      skipped.put(a, new HashSet<>());
      failed.put(a, new HashSet<>());
    }
    this.setResult(true);
  }

  public boolean getResult() {
    return loadResult;
  }

  public void setResult(boolean loadResult) {
    this.loadResult = loadResult;
  }

  public void addResult(AbstractLoadRecord record, Outcome outcome) {
    addResult(record.getLine(), record.getArtifact(), outcome, null);
  }

  public void addResult(AbstractLoadRecord record, Outcome outcome, String msg) {
    addResult(record.getLine(), record.getArtifact(), outcome, msg);
  }

  public void addResult(int id, Artifact artifact, Outcome outcome, String msg) {
    if (msg != null)
      messages.add(msg);

    switch (outcome) {
    case ADDED:
      added.get(artifact).add(id);
      break;
    case DELETED:
      deleted.get(artifact).add(id);
      break;
    case MODIFIED:
      modified.get(artifact).add(id);
      break;
    case SKIPPED:
      skipped.get(artifact).add(id);
      break;
    case FAILED:
    default:
      loadResult = false;
      failed.get(artifact).add(id);
      break;
    }
  }

  public void addMessage(String msg) {
    messages.add(msg);
  }

  public List<String> getMessages() {
    return Collections.unmodifiableList(messages);
  }

  public void printResult(PrintStream out) {
    out.println("Load result:");
    out.println("----------------------------------------------------------------------");
    out.format("%-16s: %s%n", "Input", input.getInputfile());
    out.format("%-16s: %s%n", "Result", loadResult ? "Success" : "Failed");
    for (Artifact a : Artifact.values()) {
      out.format("  %-14s: added=%d, modified=%d, deleted=%d, failed=%d, skipped=%d%n", a.name(), added.get(a).size(), modified.get(a).size(),
          deleted.get(a).size(), failed.get(a).size(), skipped.get(a).size());
    }
    out.format("%-16s: added=%d, modified=%d, deleted=%d, failed=%d, skipped=%d%n", "Total", added.values().stream().map(Set::size).reduce(0, Integer::sum),
        modified.values().stream().map(Set::size).reduce(0, Integer::sum), deleted.values().stream().map(Set::size).reduce(0, Integer::sum),
        failed.values().stream().map(Set::size).reduce(0, Integer::sum), skipped.values().stream().map(Set::size).reduce(0, Integer::sum));
    if (!messages.isEmpty()) {
      out.println();
      out.println("Log messages:");
      for (String msg : messages) {
        out.println(msg);
      }
    }
  }

  @Override
  public String toString() {
    return "LoaderResult [loadResult=" + loadResult + ", added=" + added.values().stream().map(Set::size).reduce(0, Integer::sum) + ", deleted="
        + deleted.values().stream().map(Set::size).reduce(0, Integer::sum) + ", modified=" + modified.values().stream().map(Set::size).reduce(0, Integer::sum)
        + ", skipped=" + skipped.values().stream().map(Set::size).reduce(0, Integer::sum) + ", failed="
        + failed.values().stream().map(Set::size).reduce(0, Integer::sum) + "]";
  }
}
