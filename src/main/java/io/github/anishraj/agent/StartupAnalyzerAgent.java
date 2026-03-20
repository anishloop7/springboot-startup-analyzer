package io.github.anishraj.agent;

import java.lang.instrument.Instrumentation;

/**
 * Optional Java Agent for deeper startup instrumentation.
 *
 * <p>The library works without this agent via Spring's {@code BeanPostProcessor}
 * hooks. The agent provides <em>additional</em> capabilities:
 * <ul>
 *   <li>Capture bean instantiation time (constructor call), not just init phase</li>
 *   <li>Detect {@code Thread.sleep()} calls during startup (test env anti-pattern)</li>
 *   <li>Measure class-loading time for heavy dependencies</li>
 * </ul>
 *
 * <h3>Usage (optional)</h3>
 * Add to your JVM startup args:
 * <pre>
 *   java -javaagent:springboot-startup-analyzer-1.0.0.jar -jar your-app.jar
 * </pre>
 *
 * <h3>Without the agent</h3>
 * Simply adding the Maven dependency is sufficient for full report generation.
 * The agent is purely additive — it extends but does not replace the BeanPostProcessor.
 */
public class StartupAnalyzerAgent {

    private static volatile Instrumentation instrumentation;

    /**
     * Called by the JVM before the main class runs (premain).
     * Registers the class file transformer for bytecode-level instrumentation.
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
        System.out.println("[StartupAnalyzerAgent] Agent attached — enhanced startup instrumentation active");
        inst.addTransformer(new StartupClassFileTransformer(), true);
    }

    /**
     * Called when the agent is attached to a running JVM (agentmain).
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static boolean isAttached() {
        return instrumentation != null;
    }
}
