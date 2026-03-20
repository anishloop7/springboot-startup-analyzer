package io.github.anishraj.model;

/**
 * Represents a detected (or suspected) blocking call that occurred
 * during application startup — e.g. DB connections, HTTP calls, file reads.
 */
public class BlockingCallWarning {

    public enum Type {
        DATABASE_CONNECTION,
        HTTP_CALL,
        FILE_IO,
        SLOW_INITIALIZATION,
        THREAD_SLEEP
    }

    private final String beanName;
    private final String className;
    private final Type   type;
    private final String description;
    private final String recommendation;
    private final long   durationMs;

    public BlockingCallWarning(String beanName, String className,
                                Type type, String description,
                                String recommendation, long durationMs) {
        this.beanName       = beanName;
        this.className      = className;
        this.type           = type;
        this.description    = description;
        this.recommendation = recommendation;
        this.durationMs     = durationMs;
    }

    public String getBeanName()       { return beanName; }
    public String getClassName()      { return className; }
    public Type   getType()           { return type; }
    public String getDescription()    { return description; }
    public String getRecommendation() { return recommendation; }
    public long   getDurationMs()     { return durationMs; }

    public String getTypeLabel() {
        return switch (type) {
            case DATABASE_CONNECTION -> "🗄️ DB Connection";
            case HTTP_CALL           -> "🌐 HTTP Call";
            case FILE_IO             -> "📁 File I/O";
            case SLOW_INITIALIZATION -> "🐢 Slow Init";
            case THREAD_SLEEP        -> "😴 Thread Sleep";
        };
    }
}
