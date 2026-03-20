package io.github.anishraj.model;

/**
 * Represents a potentially unnecessary autoconfiguration class
 * that was loaded during application startup.
 */
public class AutoConfigWarning {

    private final String autoConfigClass;
    private final String reason;
    private final String suggestion;

    public AutoConfigWarning(String autoConfigClass, String reason, String suggestion) {
        this.autoConfigClass = autoConfigClass;
        this.reason          = reason;
        this.suggestion      = suggestion;
    }

    public String getAutoConfigClass() { return autoConfigClass; }
    public String getReason()          { return reason; }
    public String getSuggestion()      { return suggestion; }

    /** Returns short class name for display purposes. */
    public String getShortName() {
        int idx = autoConfigClass.lastIndexOf('.');
        return idx >= 0 ? autoConfigClass.substring(idx + 1) : autoConfigClass;
    }
}
