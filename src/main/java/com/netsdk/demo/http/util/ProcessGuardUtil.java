package com.netsdk.demo.http.util;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/*
 * 工具类主要功能：
 *   - 不依赖外部进程，实现在Java进程内部自动退出后自动重启。
 *   - 适用于JNA/JNI DLL崩溃后自我恢复，解决服务假死、线程挂起等问题。
 *   - 通过守护进程模式监控worker进程，worker异常退出时自动重启。
 *   - 全局异常捕获：
 *       - 兼容Web线程（@ControllerAdvice + @ExceptionHandler），优先检测native崩溃，自动自愈。
 *       - 兼容非Web线程（Thread.setDefaultUncaughtExceptionHandler），如异步任务、主线程等，统一自愈机制。
 *   - 统一配置关键字、退出码，支持yml、JVM参数、环境变量多种方式。
 *
 * 解决方案：
 *   单jar包双模式启动，必须要为fat jar，否则无法自动重启。
 *     1. 守护进程模式：java -jar xxx.jar guard
 *     2. 业务进程模式：java -jar xxx.jar
 *
 *   守护进程模式下，会自动启动worker进程，并监控worker进程的退出状态。
 *   如果worker进程退出，且为配置的特定退出码，则守护进程会自动重启worker进程。
 *
 * Docker部署最佳实践：
 *   - Dockerfile中的ENTRYPOINT或CMD应为：
 *       ENTRYPOINT ["java", "-jar", "xxx.jar", "guard"]
 *   - 不要直接用 java -jar ... 启动worker，否则守护机制无效。
 *   - 这样worker崩溃时容器不会退出，守护进程会自动重启worker。
 *
 * 使用方式：
 *   在Spring Boot主类main方法中，替换原有SpringApplication.run(xxx, args)
 *   public static void main(String[] args) throws Exception {
 *       ProcessGuardUtil.registerGlobalExceptionHandler(); // 注册全局异常捕获
 *       ProcessGuardUtil.launchWithGuard(MySpringBootApp.class, args);
 *   }
 *
 *   捕获全局异常后，判断为特定异常（如native崩溃），再使用特定退出码（如System.exit(99)），触发守护进程自动重启worker。
 */
@Slf4j
@ControllerAdvice
public class ProcessGuardUtil {

    // 配置参数key
    public static final String GUARD_PREFIX = "guard.";
    public static final String KEY_GUARD_WORKER_CMD = "guard.worker-cmd";
    public static final String KEY_GUARD_NATIVE_CRASH_EXIT_CODE = "guard.native-crash-exit-code";
    public static final String KEY_GUARD_NATIVE_CRASH_KEYWORDS = "guard.native-crash-keywords";

    /**
     * 一步式启动守护进程或Spring Boot主程序
     * @param springBootMainClass Spring Boot主启动类
     * @param args main方法参数
     * @return ApplicationContext（仅worker模式下非null）
     */
    public static ApplicationContext launchWithGuard(Class<?> springBootMainClass, String[] args) throws Exception {
        // 获取启动参数（当前用yml方式）
        Map<String, String> guardParams = getGuardParamsFromYaml();
        String guardCmd = guardParams.getOrDefault(KEY_GUARD_WORKER_CMD, "java -jar dh-netsdk-http.jar worker");
        int nativeCrashExitCode = Integer.parseInt(guardParams.getOrDefault(KEY_GUARD_NATIVE_CRASH_EXIT_CODE, "99"));
        if (args.length > 0 && "guard".equalsIgnoreCase(args[0])) {
            // 守护进程模式
            String[] cmdArray = guardCmd.trim().split("\\s+");
            while (true) {
                log.info("启动worker进程命令: {}", String.join(" ", cmdArray));
                ProcessBuilder pb = new ProcessBuilder(cmdArray);
                pb.inheritIO();
                Process p = pb.start();
                int exitCode = p.waitFor();
                log.warn("worker进程退出，exitCode={}", exitCode);
                if (exitCode == 0) {
                    log.info("worker正常退出，守护进程也退出。");
                    break;
                } else if (exitCode == nativeCrashExitCode) {
                    log.warn("worker检测到native崩溃，守护进程自动重启worker...");
                } else {
                    log.warn("worker异常退出(exitCode={})，守护进程自动重启worker...", exitCode);
                }
                Thread.sleep(2000);
            }
            return null;
        } else {
            // 业务进程模式，正常Spring Boot启动
            return SpringApplication.run(springBootMainClass, args);
        }
    }

    /**
     * 非web线程全局异常捕获，支持native崩溃自动退出，触发守护进程重启。
     * 建议在main方法最前调用一次。
     */
    public static void registerGlobalExceptionHandler() {
        // 1. 关键字、退出码优先从yml，其次JVM参数、环境变量
        Map<String, String> guardParams = getGuardParamsFromYaml();
        guardParams.putAll(getGuardParamsFromEnv());
        String keywordsStr = guardParams.getOrDefault(KEY_GUARD_NATIVE_CRASH_KEYWORDS, "invalid memory access,segmentation fault,core dumped");
        int exitCode = Integer.parseInt(guardParams.getOrDefault(KEY_GUARD_NATIVE_CRASH_EXIT_CODE, "99"));
        java.util.List<String> keywords = java.util.Arrays.stream(keywordsStr.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
        // 保存原有handler，实现链式处理。优先捕获native崩溃，未匹配时交由原有handler（如Spring Web的@ControllerAdvice等）处理。
        Thread.UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            String msg = ex.toString();
            if (keywords.stream().anyMatch(k -> msg.toLowerCase().contains(k.toLowerCase()))) {
                log.error("全局异常捕获: 线程={} 异常={}", t.getName(), ex);
                // 试过了，软重启无效
                // log.error("检测到native崩溃，尝试SDK软重启...");
                // LoginExtModule.resetSdk();
                log.error("检测到native崩溃，即将退出，将会触发守护进程重启。退出码{}", exitCode);
                try {
                    // 等待2秒，让错误日志完成刷盘 
                    Thread.sleep(2000); 
                } catch (InterruptedException ignored) {}
                // 自定义退出码，触发守护进程重启
                System.exit(exitCode);
            } else {
                // 未匹配native关键字，继续抛出，交由上层业务处理
                // 如果有Spring Web环境的@ControllerAdvice等全局异常处理器，依然会被调用
                if (previousHandler != null) {
                    previousHandler.uncaughtException(t, ex);
                } else if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                } else if (ex instanceof Error) {
                    throw (Error) ex;
                } else {
                    throw new RuntimeException(ex);
                }
            }
        });
        // Spring Web异常兼容说明：
        // 本处理器不会覆盖Spring Web的@ControllerAdvice、@ExceptionHandler等全局异常机制。
        // 若在Web环境下，未被本处理器System.exit的异常，依然会被Spring的全局异常处理器捕获。
    }

    /**
     * Spring Web全局异常处理，优先检测native崩溃，触发守护进程自愈。
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleAllThrowable(Throwable ex) {
        checkAndExitOnNativeCrash(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("server error");
    }

    /**
     * web全局异常处理，检测native崩溃关键字，命中则System.exit，触发守护进程重启。
     */
    public static void checkAndExitOnNativeCrash(Throwable ex) {
        Map<String, String> guardParams = getGuardParamsFromYaml();
        guardParams.putAll(getGuardParamsFromEnv());
        String keywordsStr = guardParams.getOrDefault(KEY_GUARD_NATIVE_CRASH_KEYWORDS, "invalid memory access,segmentation fault,core dumped");
        int exitCode = Integer.parseInt(guardParams.getOrDefault(KEY_GUARD_NATIVE_CRASH_EXIT_CODE, "99"));
        java.util.List<String> keywords = java.util.Arrays.stream(keywordsStr.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
        String msg = ex.toString();
        log.error("全局异常捕获", ex);
        if (keywords.stream().anyMatch(k -> msg.toLowerCase().contains(k.toLowerCase()))) {
            log.error("检测到native崩溃，即将退出，将会触发守护进程重启。退出码{}", exitCode);
            try {
                // 等待2秒，让错误日志完成刷盘 
                Thread.sleep(2000); 
            } catch (InterruptedException ignored) {}
            System.exit(exitCode);
        }
    }

    /**
     * 从JVM启动参数（System properties）获取守护进程参数
     * <p>
     * 示例：
     *   -Dguard.worker-cmd="java -jar dh-netsdk-http.jar worker" -Dguard.native-crash-exit-code=99 -Dguard.native-crash-keywords="invalid memory access,segmentation fault,core dumped"
     * <p>
     * 返回map，key为KEY_GUARD_WORKER_CMD、KEY_GUARD_NATIVE_CRASH_EXIT_CODE、KEY_GUARD_NATIVE_CRASH_KEYWORDS
     */
    public static java.util.Map<String, String> getGuardParamsFromEnv() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        org.springframework.core.env.StandardEnvironment env = new org.springframework.core.env.StandardEnvironment();
        String cmd = env.getProperty(KEY_GUARD_WORKER_CMD, "");
        String exitCode = env.getProperty(KEY_GUARD_NATIVE_CRASH_EXIT_CODE, "99");
        String keywords = env.getProperty(KEY_GUARD_NATIVE_CRASH_KEYWORDS, "");
        if (!cmd.isEmpty()) map.put(KEY_GUARD_WORKER_CMD, cmd);
        if (!exitCode.isEmpty()) map.put(KEY_GUARD_NATIVE_CRASH_EXIT_CODE, exitCode);
        if (!keywords.isEmpty()) map.put(KEY_GUARD_NATIVE_CRASH_KEYWORDS, keywords);
        return map;
    }

    /**
     * 从yml配置获取守护进程参数
     * <p>
     * yml配置示例：
     * guard:
     *   worker-cmd: java -jar dh-netsdk-http.jar worker
     *   native-crash-exit-code: 99
     *   native-crash-keywords: invalid memory access,segmentation fault,core dumped
     * <p>
     * 返回map，key为KEY_GUARD_WORKER_CMD、KEY_GUARD_NATIVE_CRASH_EXIT_CODE、KEY_GUARD_NATIVE_CRASH_KEYWORDS
     */
    public static Map<String, String> getGuardParamsFromYaml() {
        Map<String, String> map = new HashMap<>();
        Map<String, Object> config = loadYamlConfig("application.yml");
        if (config != null && config.containsKey(GUARD_PREFIX.substring(0, GUARD_PREFIX.length() - 1))) {
            Map<String, Object> guard = (Map<String, Object>) config.get(GUARD_PREFIX.substring(0, GUARD_PREFIX.length() - 1));
            if (guard.get(KEY_GUARD_WORKER_CMD.substring(GUARD_PREFIX.length())) != null) map.put(KEY_GUARD_WORKER_CMD, guard.get(KEY_GUARD_WORKER_CMD.substring(GUARD_PREFIX.length())).toString());
            if (guard.get(KEY_GUARD_NATIVE_CRASH_EXIT_CODE.substring(GUARD_PREFIX.length())) != null) map.put(KEY_GUARD_NATIVE_CRASH_EXIT_CODE, guard.get(KEY_GUARD_NATIVE_CRASH_EXIT_CODE.substring(GUARD_PREFIX.length())).toString());
            if (guard.get(KEY_GUARD_NATIVE_CRASH_KEYWORDS.substring(GUARD_PREFIX.length())) != null) map.put(KEY_GUARD_NATIVE_CRASH_KEYWORDS, guard.get(KEY_GUARD_NATIVE_CRASH_KEYWORDS.substring(GUARD_PREFIX.length())).toString());
        }
        return map;
    }

    private static Map<String, Object> loadYamlConfig(String fileName) {
        Yaml yaml = new Yaml();
        try (InputStream in = ProcessGuardUtil.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in != null) {
                return yaml.load(in);
            }
        } catch (Exception e) {
            log.warn("读取配置文件{}失败: {}", fileName, e.getMessage());
        }
        return null;
    }
} 