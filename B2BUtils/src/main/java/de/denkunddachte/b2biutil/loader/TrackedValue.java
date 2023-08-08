package de.denkunddachte.b2biutil.loader;

import java.util.Collection;
import java.util.Objects;

import de.denkunddachte.b2biutil.loader.TrackedItem.Status;

public class TrackedValue {
  private Object             oldValue = null;
  private Object             newValue = null;
  private TrackedItem.Status status   = Status.UNDEFINED;

  public TrackedValue(Object value, boolean newField) {
    if (newField) {
      this.newValue = value;
      this.status = Status.ADDED;
    } else {
      this.status = Status.UNMODIFIED;
      this.oldValue = value;
    }
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  public Object getValue() {
    if (status == Status.UNMODIFIED) {
      return oldValue;
    } else {
      return newValue;
    }
  }

  public TrackedItem.Status getStatus() {
    return status;
  }

  public Object update(Object value) {
    this.newValue = value;
    if (this.status == Status.ADDED) {
      // noop
    } else if (!equals(newValue, oldValue)) {
      this.status = (newValue == null ? Status.DELETED : Status.MODIFIED);
    } else {
      this.status = Status.UNMODIFIED;
    }
    return oldValue;
  }

  private boolean equals(Object v1, Object v2) {
    if(v1 == null) {
      return v2 == null;
    }
    if (v2 == null)
      return false;
    
    if (v1 instanceof Collection<?> && v2 instanceof Collection<?>) {
      Collection<?> c1 = (Collection<?>)v1;
      Collection<?> c2 = (Collection<?>)v2;
      if(c1.size() != c2.size())
        return false;
      return c1.containsAll(c2);
    }
    return Objects.equals(v1, v2);
  }

  @Override
  public String toString() {
    return getValue() + " (" + status + ") [" + oldValue + "] -> [" + newValue + "]";
  }
  
}
