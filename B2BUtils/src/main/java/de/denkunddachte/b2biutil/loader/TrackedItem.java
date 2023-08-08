package de.denkunddachte.b2biutil.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import de.denkunddachte.exception.B2BLoadException;
import de.denkunddachte.ft.TrackableItem;
import de.denkunddachte.ft.TrackableItemClass;

public class TrackedItem {

  public enum Status {
    UNDEFINED, UNMODIFIED, ADDED, MODIFIED, DELETED
  }

  private TrackableItemClass  artifactApp;
  private String                    artifactType;
  private String                    key;
  private String                    shortId;
  private Status                    status = Status.UNDEFINED;

  private Map<String, TrackedValue> fields = new HashMap<>();

  public TrackedItem(TrackableItem item) {
    this.artifactApp = item.getItemClass();
    this.artifactType = item.getType();
    this.key = item.getKey();
    this.shortId = item.getShortId();
    this.status = item.isNew() ? Status.ADDED : Status.UNMODIFIED;
    for (Entry<String, Object> e : item.getFields().entrySet()) {
      fields.put(e.getKey(), new TrackedValue(e.getValue(), status == Status.ADDED));
    }
  }

  public TrackableItemClass getApplication() {
    return this.artifactApp;
  }

  public String getType() {
    return this.artifactType;
  }

  public String getKey() {
    return this.key;
  }

  public String getShortId() {
    return shortId;
  }

  public Status getStatus() {
    return status;
  }

  public void delete() {
    fields.values().forEach(v -> v.update(null));
    status = Status.DELETED;
  }

  public void update(TrackableItem item) throws B2BLoadException {
    if (item == null)
      throw new NullPointerException("Argument is null!");
    if (artifactApp != item.getItemClass() || !artifactType.equals(item.getType()) || (this.status != Status.ADDED && !key.equals(item.getKey()))) {
      throw new B2BLoadException(
          "Cannot update tracked item " + toString() + " with [" + item.getItemClass() + "." + item.getType() + "." + item.getShortId() + "]!");
    }
    if (this.status == Status.DELETED)
      throw new B2BLoadException("Cannot update deleted  item " + toString() + "!");

    for (Entry<String, TrackedValue> e : fields.entrySet()) {
      if (!item.getFields().containsKey(e.getKey())) {
        throw new B2BLoadException("TrackableItem " + item + " is missing field " + e.getKey() + "!");
      }
      e.getValue().update(item.getFields().get(e.getKey()));
      if (e.getValue().getStatus() == Status.MODIFIED) {
        this.status = this.status == Status.UNMODIFIED ? Status.MODIFIED : this.status;
      }
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactType, artifactApp, key);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof TrackedItem))
      return false;
    TrackedItem other = (TrackedItem) obj;
    return Objects.equals(artifactType, other.artifactType) && artifactApp == other.artifactApp && Objects.equals(key, other.key);
  }

  @Override
  public String toString() {
    return "[" + artifactApp + "." + artifactType + "." + shortId + "]";
  }

  public Map<String, TrackedValue> getFields(boolean includeUnmodified) {
    final Map<String, TrackedValue> result = new HashMap<>(fields.size());
    if (includeUnmodified) {
      fields.entrySet().stream().forEach(e -> result.put(e.getKey(), e.getValue()));
    } else {
      fields.entrySet().stream().filter(e -> e.getValue().getStatus() != Status.UNMODIFIED).forEach(e -> result.put(e.getKey(), e.getValue()));
    }
    return result;
  }

}
