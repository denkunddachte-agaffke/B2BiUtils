package de.denkunddachte.b2biutil.workflow;

import java.util.stream.StreamSupport;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultComparisonFormatter;
import org.xmlunit.diff.Diff;

public class XmlDiff {

  private boolean ignoreComments   = true;
  private boolean ignoreWhitespace = true;
  Diff            lastDiff;

  public XmlDiff() {
    super();
  }

  public boolean compare(Object src1, Object src2) {
    lastDiff = null;
    DiffBuilder db = DiffBuilder.compare(src1).withTest(src2);
    if (ignoreComments)
      db.ignoreComments();
    if (ignoreWhitespace)
      db.ignoreWhitespace();
    // TODO: implement custom formatter:
    lastDiff = db.withComparisonFormatter(new DefaultComparisonFormatter()).build();
    return lastDiff.hasDifferences();
  }

  public Diff get() {
    if (lastDiff == null) {
      throw new IllegalStateException("No diff performed yet!");
    }
    return this.lastDiff;
  }

  public long getCount() {
    return StreamSupport.stream(get().getDifferences().spliterator(), false).count();
  }

  public boolean differs() {
    return get().hasDifferences();
  }

  public boolean isIgnoreComments() {
    return ignoreComments;
  }

  public void setIgnoreComments(boolean ignoreComments) {
    this.ignoreComments = ignoreComments;
  }

  public boolean isIgnoreWhitespace() {
    return ignoreWhitespace;
  }

  public void setIgnoreWhitespace(boolean ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
  }
}
