package de.denkunddachte.b2biutil.loader;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import de.denkunddachte.b2biutil.loader.TrackedItem.Status;

public class ChangeReport {
  ChangeSet                       changeset;
  private final DateFormat fmtDate       = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ss\"");
  private final DateFormat fmtTimestamp  = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ss.SSS\"");
  private final DateFormat fmtOffsettime = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ssX\"");

  public ChangeReport(ChangeSet changeset) {
    this.changeset = changeset;
  }

  public void printReport(final PrintStream os) {
    Collection<String> types = changeset.getTrackedItemsTypes();

    if (!changeset.getLog().isEmpty()) {
      os.println(changeset.getLog());
      os.println();
    }

    for (String type : new String[] { "AZ_FG_CUSTOMER", "AZ_FG_FETCH_SFTP", "AZ_FG_FETCH_AWSS3", "AZ_FG_TRANSFER", "AZ_FG_DELIVERY" }) {
      if (types.contains(type)) {
        os.format("%s:%n", type);
        os.println(new String(new char[type.length() + 1]).replace('\0', '-'));
        changeset.getTrackedItemsByType(type).forEach(it -> printItem(os, it));
        os.println("");
      }
    }
    for (String type : types.stream().filter(s -> s.startsWith("AZ_FG_DELIVERY_")).collect(Collectors.toList())) {
      os.format("%s:%n", type);
      os.println(new String(new char[type.length() + 1]).replace('\0', '-'));
      changeset.getTrackedItemsByType(type).forEach(it -> printItem(os, it));
      os.println("");
    }
  }

  private void printItem(PrintStream os, TrackedItem item) {
    os.format("[%-10s]: %s%n", item.getStatus(), item.getShortId());
    if (item.getStatus() != Status.UNMODIFIED) {
      item.getFields(false).entrySet().stream()
          .forEach(e -> os.format("    %-20s: %s ==> %s%n", e.getKey(), val2str(e.getValue().getOldValue(), 30), val2str(e.getValue().getNewValue(), 0)));
    }
  }

  private String val2str(Object val, int len) {
    String str;
    if (val == null) {
      str = "";
    } else {
      if (val instanceof java.util.Date) {
        str = fmtDate.format(val);
      } else if (val instanceof Timestamp) {
        str = fmtTimestamp.format(val);
      } else if (val instanceof OffsetDateTime) {
        str = fmtOffsettime.format(val);
      } else if (val instanceof Collection<?>) {
        str = ((Collection<?>) val).size() + " items";
      } else {
        str = val.toString();
        if (!str.startsWith("{")) {
          str = "\"" + str + "\"";
        }
      }
    }
    if (len > 0) {
      return String.format("%-" + len + "s", str);
    } else {
      return str;
    }
  }
}
