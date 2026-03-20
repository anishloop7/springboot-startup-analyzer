package io.github.anishraj.agent;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Set;

/**
 * Bytecode transformer that intercepts class loading to detect
 * potentially blocking patterns at startup (Thread.sleep, blocking I/O).
 *
 * <p>In its current form this is a pass-through transformer (returns null = no change).
 * Full ASM/Javassist bytecode manipulation can be added here for production
 * instrumentation of {@code Thread.sleep()} calls and blocking socket operations.
 *
 * <p>This class demonstrates the agent architecture; the BeanPostProcessor in
 * {@link io.github.anishraj.processor.BeanTimingPostProcessor} handles
 * all timing without bytecode manipulation.
 */
public class StartupClassFileTransformer implements ClassFileTransformer {

    private static final Set<String> MONITORED_CLASSES = Set.of(
            "java/lang/Thread",
            "java/net/Socket",
            "java/net/HttpURLConnection",
            "java/io/FileInputStream"
    );

    /**
     * Called for every class loaded by the JVM.
     * Returns null to indicate no transformation (pass-through).
     * Override with ASM transformation logic to inject timing probes.
     */
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        // Pass-through — real implementation would use ASM to inject timing probes
        // into Thread.sleep(), Socket.connect(), FileInputStream.read() calls
        // when they occur during beans' @PostConstruct lifecycle phase
        return null;
    }
}
