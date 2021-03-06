package edu.holycross.shot.hocuspocus


public enum DocumentFormat {



  XML("xml"),
  TWO_COL("2col"),
  O2XF("82xf"),
  MARKDOWN("markdown")

  private String label
  private DocumentFormat(String label) {
    this.label = label
  }

  /** Gets a human-readable label for this value. */
  public String getLabel() {
    return label
  }

  /** Looks up value by labelling String. */
  static getByLabel(String labelStr) {
    values().find { it.label == labelStr }
  }
}
