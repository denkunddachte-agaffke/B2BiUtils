package de.denkunddachte.b2biutil.loader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventListener;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;

public class LoggingSessionEventListener implements SessionEventListener {
  private static final Logger LOGGER = Logger.getLogger(LoggingSessionEventListener.class.getName());

  @Override
  public void missingDescriptor(SessionEvent event) {
    LOGGER.log(Level.FINEST, "missingDescriptor : {0}", event);
  }

  @Override
  public void moreRowsDetected(SessionEvent event) {
    LOGGER.log(Level.FINEST, "moreRowsDetected : {0}", event);
  }

  @Override
  public void noRowsModified(SessionEvent event) {
    LOGGER.log(Level.FINEST, "noRowsModified : {0}", event);
  }

  @Override
  public void outputParametersDetected(SessionEvent event) {
    LOGGER.log(Level.FINEST, "outputParametersDetected : {0}", event);
  }

  @Override
  public void postAcquireClientSession(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postAcquireClientSession : {0}", event);
  }

  @Override
  public void postAcquireConnection(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postAcquireConnection : {0}", event);
  }

  @Override
  public void postAcquireExclusiveConnection(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postAcquireExclusiveConnection : {0}", event);
  }

  @Override
  public void postAcquireUnitOfWork(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postAcquireUnitOfWork : {0}", event);
  }

  @Override
  public void postBeginTransaction(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postBeginTransaction : {0}", event);
  }

  @Override
  public void preCalculateUnitOfWorkChangeSet(SessionEvent event) {
    LOGGER.log(Level.FINE, "preCalculateUnitOfWorkChangeSet : {0}", event);
  }

  @Override
  public void postCalculateUnitOfWorkChangeSet(SessionEvent event) {
    LOGGER.log(Level.FINE, "postCalculateUnitOfWorkChangeSet : {0}", event);
  }

  @Override
  public void postCommitTransaction(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postCommitTransaction : {0}", event);
  }

  @Override
  public void postCommitUnitOfWork(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postCommitUnitOfWork : {0}", event);
  }

  @Override
  public void postConnect(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postConnect : {0}", event);
  }

  @Override
  public void postExecuteQuery(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postExecuteQuery : {0}", event);
  }

  @Override
  public void postReleaseClientSession(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postReleaseClientSession : {0}", event);
  }

  @Override
  public void postReleaseUnitOfWork(SessionEvent event) {
    LOGGER.log(Level.FINE, "postReleaseUnitOfWork : {0}", event);
  }

  @Override
  public void postResumeUnitOfWork(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postResumeUnitOfWork : {0}", event);
  }

  @Override
  public void postRollbackTransaction(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postRollbackTransaction : {0}", event);
  }

  @Override
  public void postDistributedMergeUnitOfWorkChangeSet(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postDistributedMergeUnitOfWorkChangeSet : {0}", event);
  }

  @Override
  public void postMergeUnitOfWorkChangeSet(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postMergeUnitOfWorkChangeSet : {0}", event);
  }

  @Override
  public void preBeginTransaction(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preBeginTransaction : {0}", event);
  }

  @Override
  public void preCommitTransaction(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preCommitTransaction : {0}", event);
  }

  @Override
  public void preCommitUnitOfWork(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preCommitUnitOfWork : {0}", event);
  }

  @Override
  public void preExecuteQuery(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preExecuteQuery : {0}", event);
  }

  @Override
  public void prepareUnitOfWork(SessionEvent event) {
    LOGGER.log(Level.FINEST, "prepareUnitOfWork : {0}", event);
  }

  @Override
  public void preReleaseClientSession(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preReleaseClientSession : {0}", event);
  }

  @Override
  public void preReleaseConnection(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preReleaseConnection : {0}", event);
  }

  @Override
  public void preReleaseExclusiveConnection(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preReleaseExclusiveConnection : {0}", event);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void preReleaseUnitOfWork(SessionEvent event) {
    LOGGER.log(Level.FINE, "preReleaseUnitOfWork : {0}", event);
    event.getQuery();
    UnitOfWork u= (UnitOfWork) event.getSource();
    org.eclipse.persistence.sessions.changesets.UnitOfWorkChangeSet uowcs =  u.getUnitOfWorkChangeSet();
    uowcs.getDeletedObjects();
    ObjectChangeSet ocs = uowcs.getObjectChangeSetForClone(this);
    if (ocs == null) {
      ocs = new org.eclipse.persistence.internal.sessions.ObjectChangeSet();
    }
    try {
    for (ChangeRecord cr: ocs.getChanges()) {
      Field field = this.getClass().getDeclaredField(cr.getAttribute());
      field.setAccessible(true);
      Object newVal = field.get(this);
      if (newVal instanceof Collection<?>) {
        List<Object> added = new ArrayList<>();
        List<Object> removed = new ArrayList<>();
        calculateCollectionDiff((Collection<Object>) cr.getOldValue(), (Collection<Object>) newVal, added, removed);
        if (!added.isEmpty()) {
          LOGGER.info(() -> cr.getAttribute() + ": ADDED " + added);
        }
        if (!added.isEmpty()) {
          LOGGER.info(() -> cr.getAttribute() + ": REMOVED " + removed);
        }
      } else {
        LOGGER.info(() -> cr.getAttribute() + ": [" + cr.getOldValue() + "] --> [" + newVal + "]");
      }
    }
    } catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException se) {
      se.printStackTrace();
    }
      //LOGGER.log(Level.FINE, "{0} -> {1}", new Object[] {cr.getAttribute(), cr.getOldValue());
    
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

  @Override
  public void preRollbackTransaction(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preRollbackTransaction : {0}", event);
  }

  @Override
  public void preDistributedMergeUnitOfWorkChangeSet(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preDistributedMergeUnitOfWorkChangeSet : {0}", event);
  }

  @Override
  public void preMergeUnitOfWorkChangeSet(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preMergeUnitOfWorkChangeSet : {0}", event);
  }

  @Override
  public void preLogin(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preLogin : {0}", event);
  }

  @Override
  public void postLogin(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postLogin : {0}", event);
  }

  @Override
  public void preLogout(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preLogout : {0}", event);
  }

  @Override
  public void postLogout(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postLogout : {0}", event);
  }

  @Override
  public void postExecuteCall(SessionEvent event) {
    LOGGER.log(Level.FINEST, "postExecuteCall : {0}", event);
  }

  @Override
  public void preExecuteCall(SessionEvent event) {
    LOGGER.log(Level.FINEST, "preExecuteCall : {0}", event);
  }

}
