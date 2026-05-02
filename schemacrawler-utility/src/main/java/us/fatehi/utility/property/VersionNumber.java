package us.fatehi.utility.property;

import java.io.Serializable;

public record VersionNumber(int major, int minor) implements Serializable {

  public VersionNumber {
    if (major < 0) {
      throw new IllegalArgumentException("Bad major version: %d".formatted(major));
    }
    if (minor < 0) {
      throw new IllegalArgumentException("Bad minor version: %d".formatted(minor));
    }
  }

  @Override
  public String toString() {
    return "%d.%d".formatted(major, minor);
  }
}
