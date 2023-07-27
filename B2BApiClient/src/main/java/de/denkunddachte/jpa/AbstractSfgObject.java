/*
  Copyright 2018 - 2023 denk & dachte Software GmbH

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package de.denkunddachte.jpa;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;
import org.eclipse.persistence.sessions.changesets.UnitOfWorkChangeSet;
import org.json.JSONArray;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.Exportable;
import de.denkunddachte.ft.TrackableItem;
import de.denkunddachte.ft.TrackableItemClass;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class AbstractSfgObject implements TrackableItem, Exportable {
  @Transient
  protected final DateFormat fmtDate            = new SimpleDateFormat("yyyy-MM-dd");
  @Transient
  protected final DateFormat fmtDateTime        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  @Transient
  protected final DateFormat fmtTimestamp       = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  @Transient
  protected final DateFormat fmtOffsetTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  @Column(name = "CREATE_TIME", nullable = false, updatable = false, insertable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date               createTime;

  @Column(name = "CREATED_BY", nullable = false, updatable = false, insertable = true)
  private String             createdBy;

  @Column(name = "MODIFIED_BY", nullable = true, updatable = true, insertable = false)
  private String             modifiedBy;

  @Column(name = "MODIFY_TIME", nullable = true, updatable = true, insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date               modifyTime;

  private enum Action {
    LOAD, NEW, UPD, DEL, UNCHANGED
  }

  protected AbstractSfgObject() {
    super();
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Timestamp createTime) {
    this.createTime = createTime;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public Date getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(Timestamp modifyTime) {
    this.modifyTime = modifyTime;
  }

  public abstract Map<String, Object> getIdentityFields();

  public abstract String getShortId();

  public abstract Logger getLogger();

  public abstract boolean pointsToSame(AbstractSfgObject other);

  @Override
  public String toString() {
    return " [createTime=" + createTime + ", createdBy=" + createdBy + ", modifiedBy=" + modifiedBy + ", modifyTime=" + modifyTime + "]";
  }

  @PrePersist
  private void prePersist() {
    this.setCreatedBy(Defaults.getUserId());
    this.setCreateTime(new java.sql.Timestamp((new Date()).getTime()));
  }

  @PreUpdate
  private void preUpdate() {
    this.setModifiedBy(Defaults.getUserId());
    this.setModifyTime(new java.sql.Timestamp((new Date()).getTime()));
  }

  @PostPersist
  private void logPersist() {
    logAction(Action.NEW, Level.INFO, this.toString());
  }

  @SuppressWarnings("unchecked")
  @PostUpdate
  private void logUpdate() {
    List<ChangeRecord> changes;
    try {
      changes = getChangeSet(SfgEntityManager.instance().getEntityManager()).getChanges();
      if (!changes.isEmpty()) {
        logAction(Action.UPD, Level.INFO, this.getShortId());
        for (ChangeRecord cr : changes) {
          // ignore changes im modifiedBy and modifyTime fields
          if (!"modifiedBy".equals(cr.getAttribute()) && !"modifyTime".equals(cr.getAttribute())) {
            try {
              Object newVal = getValue(this.getClass().getDeclaredField(cr.getAttribute()), this);
              if (newVal instanceof Collection<?>) {
                List<Object> added   = new ArrayList<>();
                List<Object> removed = new ArrayList<>();
                calculateCollectionDiff((Collection<Object>) cr.getOldValue(), (Collection<Object>) newVal, added, removed);
                if (!added.isEmpty()) {
                  getLogger().info(() -> cr.getAttribute() + ": ADDED " + added);
                }
                if (!added.isEmpty()) {
                  getLogger().info(() -> cr.getAttribute() + ": REMOVED " + removed);
                }
              } else {
                getLogger().info(() -> cr.getAttribute() + ": [" + cr.getOldValue() + "] --> [" + newVal + "]");
              }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException e) {
              getLogger().warning("Error reading changes: " + e.getMessage());
            }
          }
        }
      } else {
        logAction(Action.UNCHANGED, Level.INFO, this.toString());

        getLogger().info(this.getShortId() + ": Unchanged.");
      }
    } catch (ApiException e) {
      getLogger().log(Level.SEVERE, "Could not read changes!", e);
    }
  }

  @PostRemove
  private void logRemove() {
    logAction(Action.DEL, Level.INFO, this.toString());
  }

  @PostLoad
  private void logLoad() {
    logAction(Action.LOAD, Level.FINER, this.toString());
  }

  private void logAction(Action action, Level level, String msg) {
    if (action != null) {
      final String  clazz = this.getClass().getSimpleName();
      StringBuilder sb    = new StringBuilder("[");
      sb.append(action).append(" ").append(clazz).append("]: ");
      sb.append((msg.startsWith(clazz + " [") ? msg.substring(clazz.length() + 2, msg.length() - 1) : msg));
      getLogger().log(level, sb.toString());
    } else {
      getLogger().log(level, msg);
    }
  }

  public ObjectChangeSet getChangeSet(EntityManager em) {
    final JpaEntityManager    jpaEm     = (org.eclipse.persistence.jpa.JpaEntityManager) em.getDelegate();
    final UnitOfWorkChangeSet changeSet = jpaEm.getUnitOfWork().getCurrentChanges();
    ObjectChangeSet           result    = changeSet.getObjectChangeSetForClone(this);
    if (result == null) {
      result = new org.eclipse.persistence.internal.sessions.ObjectChangeSet();
    }
    return result;
  }

  private static boolean calculateCollectionDiff(Collection<Object> firstList, Collection<Object> secondList, List<Object> added, List<Object> removed) {
    removed.clear();
    removed.addAll(firstList);
    removed.removeAll(secondList);
    added.clear();
    added.addAll(secondList);
    added.removeAll(firstList);
    return (removed.isEmpty() && added.isEmpty());
  }

  public AbstractSfgObject createCopy() throws ApiException {
    throw new ApiException("createCopy() not supported for type " + this.getClass().getName() + "!");
  }

  // Interface Exportable:
  @Override
  public void export(PrintWriter out) {
    export(out, true, true);
  }
  
  @Override
  public void export(PrintWriter out, boolean prettyPrint, boolean suppressNullValues) {
    out.append(toJSON(!suppressNullValues).toString(prettyPrint ? 2 : 0));
  }
  
  @Override
  public String getBasename() {
    return getShortId();
  }
  
  @Override
  public Mode getExportMode() {
    return Mode.JSON;
  }
  
  public JSONObject toJSON(boolean includeNull) {
    return toJSON(this, includeNull);
  }

  @SuppressWarnings("unchecked")
  private JSONObject toJSON(Object instance, boolean includeNull) {
    if (instance == null) {
      return null;
    }

    JSONObject              json  = new JSONObject();
    Class<? extends Object> clazz = instance.getClass();
    json.put("jpaClass", clazz.getName());
    for (Field f : clazz.getDeclaredFields()) {
      if (f.getAnnotations().length == 0 || f.isAnnotationPresent(Transient.class)) {
        continue;
      }
      Object v = getValue(f, instance);
      if (v == null) {
        if (includeNull)
          json.put(f.getName(), JSONObject.NULL);
        continue;
      }
      if (f.isAnnotationPresent(Column.class)) {
        if (v instanceof Timestamp || (f.isAnnotationPresent(Temporal.class) && f.getAnnotation(Temporal.class).value() == TemporalType.TIMESTAMP)) {
          json.put(f.getName(), fmtTimestamp.format((Date) v));
        } else if (v instanceof OffsetDateTime) {
          json.put(f.getName(), fmtOffsetTimestamp.format((OffsetDateTime) v));
        } else if (v instanceof Date) {
          json.put(f.getName(), fmtDateTime.format((Date) v));
        } else {
          json.put(f.getName(), v);
        }
      } else if (f.isAnnotationPresent(EmbeddedId.class) || f.isAnnotationPresent(Embedded.class)) {
        json.put(f.getName(), toJSON(v, includeNull));
      } else if (f.isAnnotationPresent(ManyToOne.class) || (f.isAnnotationPresent(OneToOne.class) && f.getAnnotation(OneToOne.class).mappedBy().isEmpty())) {
        // FK ref (this references other): put identity fields to enable lookup
        JSONObject fk = new JSONObject();
        fk.put("jpaClass", v.getClass().getName());
        fk.put("_id", ((AbstractSfgObject) v).getIdentityFields());
        json.put(f.getName(), fk);
      } else if (f.isAnnotationPresent(OneToMany.class) || (f.isAnnotationPresent(OneToOne.class) && !f.getAnnotation(OneToOne.class).mappedBy().isEmpty())) {
        // FK ref (this is referenced by other): put object
        if (f.isAnnotationPresent(OneToMany.class)) {
          JSONArray children = new JSONArray();
          for (AbstractSfgObject child : (Collection<? extends AbstractSfgObject>) v) {
            children.put(child.toJSON(includeNull));
          }
          json.put(f.getName(), children);
        } else if (f.isAnnotationPresent(OneToOne.class)) {
          json.put(f.getName(), ((AbstractSfgObject) v).toJSON(includeNull));
        }
      } else {
        getLogger().log(Level.FINER, "Ignore annotated field {0}={1} in class {2}.", new Object[] { f.getName(), v, instance.getClass().getName() });
      }
    }
    json.put("createdBy", createdBy == null && includeNull ? JSONObject.NULL : createdBy);
    if (createTime != null) {
      json.put("createTime", fmtTimestamp.format(createTime));
    } else if (includeNull) {
      json.put("createTime", JSONObject.NULL);
    }
    json.put("createTime", createTime == null && includeNull ? JSONObject.NULL : fmtTimestamp.format(createTime));
    json.put("modifiedBy", modifiedBy == null && includeNull ? JSONObject.NULL : modifiedBy);
    if (modifyTime != null) {
      json.put("modifyTime", fmtTimestamp.format(modifyTime));
    } else if (includeNull) {
      json.put("modifyTime", JSONObject.NULL);
    }
    return json;
  }

  // Interface TrackableItem:
  @Override
  public TrackableItemClass getItemClass() {
    return TrackableItemClass.JPA;
  }

  @Override
  public String getType() {
    Table t = this.getClass().getAnnotation(Table.class);
    return t == null ? String.join("_", this.getClass().getSimpleName().split("(?=\\p{Upper})")).toUpperCase() : t.name();
  }

  @Override
  public boolean isNew() {
    try {
      EntityManager em = SfgEntityManager.instance().getEntityManager();
      return !em.contains(this);
    } catch (ApiException e) {
      return false;
    }

  }

  @Override
  public Map<String, Object> getFields() {
    final Map<String, Object> fields = new HashMap<>();
    mapFields(this, fields);
    // columns in superclass:
    fields.put("CREATE_TIME", createTime);
    fields.put("CREATED_BY", createdBy);
    fields.put("MODIFY_TIME", modifyTime);
    fields.put("MODIFIED_BY", modifiedBy);
    return fields;
  }

  private void mapFields(Object instance, Map<String, Object> fields) {
    Class<? extends Object> clazz = instance.getClass();
    for (Field f : clazz.getDeclaredFields()) {
      if (f.getAnnotations().length == 0 || f.isAnnotationPresent(Transient.class)) {
        continue;
      }
      Object v = getValue(f, instance);
      if (f.isAnnotationPresent(Column.class)) {
        String name = f.getAnnotation(Column.class).name();
        fields.put((name == null || name.isEmpty() ? f.getName().toUpperCase() : name), v);
      } else if (f.isAnnotationPresent(EmbeddedId.class) || f.isAnnotationPresent(Embedded.class)) {
        mapFields(v, fields);
      } else if (f.isAnnotationPresent(ManyToOne.class) || (f.isAnnotationPresent(OneToOne.class) && f.getAnnotation(OneToOne.class).mappedBy().isEmpty())) {
        // FK ref (this references other)
        if (v instanceof AbstractSfgObject) {
          fields.put(f.getName(), "{" + ((AbstractSfgObject) v).getShortId() + "}");
        } else {
          fields.put(f.getName(), v);
        }
      } else if (f.isAnnotationPresent(OneToMany.class) || (f.isAnnotationPresent(OneToOne.class) && !f.getAnnotation(OneToOne.class).mappedBy().isEmpty())) {
        // FK ref (this is referenced by other)
        if (v instanceof Collection<?>) {
          List<String> rvals = new ArrayList<>();
          ((Collection<?>) v).forEach(o -> rvals.add(o instanceof AbstractSfgObject ? ((AbstractSfgObject) o).getKey() : o.toString()));
          fields.put(f.getName(), rvals);
        } else {
          fields.put(f.getName(), v);
        }
      }
    }
  }

  private Object getValue(Field f, Object instance) {
    final boolean accessible = f.isAccessible();
    Object        v          = null;
    try {
      if (!accessible)
        f.setAccessible(true);
      v = f.get(instance);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      getLogger().log(Level.WARNING, "Could not get value for field {0} in class {1}: {2}",
          new Object[] { f.getName(), instance.getClass().getName(), e.getMessage() });
    } finally {
      f.setAccessible(accessible);
    }
    return v;
  }

}
