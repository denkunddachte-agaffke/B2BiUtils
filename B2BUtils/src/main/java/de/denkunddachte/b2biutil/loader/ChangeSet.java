package de.denkunddachte.b2biutil.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.denkunddachte.exception.B2BLoadException;
import de.denkunddachte.ft.TrackableItem;
import de.denkunddachte.utils.StringUtils;

public class ChangeSet {
  private Map<TrackableItem, TrackedItem> items   = new HashMap<>();
  private boolean                         collect = false;
  private final StringBuilder             log     = new StringBuilder();

  public ChangeSet(boolean collect) {
    super();
    this.setCollect(collect);
  }

  public boolean isCollect() {
    return collect;
  }

  public void setCollect(boolean collect) {
    this.collect = collect;
  }

  public void add(TrackableItem item) throws B2BLoadException {
    if (!collect || item == null)
      return;
    if (items.containsKey(item)) {
      throw new B2BLoadException("TrackableItem " + item + " exists!");
    } else {
      items.put(item, new TrackedItem(item));
    }
  }

  public void remove(TrackableItem item) throws B2BLoadException {
    if (!collect)
      return;

    if (!items.containsKey(item)) {
      add(item);
    }
    switch (items.get(item).getStatus()) {
    // added+remove -> unmodified
    case ADDED:
      items.remove(item);
      break;
    default:
      items.get(item).delete();
      break;
    }
  }

  public void update(TrackableItem item) throws B2BLoadException {
    if (!collect || item == null)
      return;
    TrackedItem tracked = items.get(item);
    if (tracked == null)
      throw new B2BLoadException("TrackableItem " + item + " not registered in ChangeSet!");
    tracked.update(item);
    for (Entry<String, TrackedValue> e : tracked.getFields(true).entrySet()) {
      if (e.getValue().getValue() instanceof TrackableItem) {
        update((TrackableItem) e.getValue().getValue());
      }
    }
  }

  public Collection<TrackedItem> getTrackedItems() {
    return items.values();
  }

  public Collection<TrackedItem> getTrackedItemsByType(String type) {
    return items.values().stream().filter(ti -> ti.getType().equalsIgnoreCase(type)).collect(Collectors.toList());
  }

  public Collection<String> getTrackedItemsTypes() {
    return items.values().stream().map(TrackedItem::getType).distinct().collect(Collectors.toList());
  }

  public void log(String msg) {
    if (!StringUtils.isNullOrWhiteSpace(msg))
      log.append(msg).append(StringUtils.LF);
  }

  public String getLog() {
    return log.toString();
  }
}
