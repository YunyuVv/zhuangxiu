package com.biliww.zhuangxiu.config;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

/**
 * GraalVM Native Image 兼容配置。
 *
 * <p>问题：Flyway 内部通过 {@code e.getClass().getMethod("isFlywaySpecificVersionOf", SQLException.class)}
 * 反射判断异常类型（{@code FlywaySqlException#throwFlywayExceptionIfPossible}）。
 * Native 镜像默认不为这些内部异常类生成反射元数据，导致 {@code NoSuchMethodException}，
 * 并掩盖真实的数据库连接错误（如 TLS 证书校验失败）。
 *
 * <p>解决：在 AOT 阶段扫描 {@code org.flywaydb.core.internal.exception.sqlExceptions} 包下所有类，
 * 为它们的声明方法注册反射调用权限，使上述反射调用在 Native 运行时可用。
 */
@Configuration
@ImportRuntimeHints(FlywayNativeConfig.FlywayHints.class)
public class FlywayNativeConfig {

    static class FlywayHints implements RuntimeHintsRegistrar {

        private static final String PKG = "org.flywaydb.core.internal.exception.sqlExceptions";

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
            SimpleMetadataReaderFactory readerFactory = new SimpleMetadataReaderFactory(classLoader);
            try {
                Resource[] resources = resolver.getResources("classpath*:" + PKG.replace('.', '/') + "/*.class");
                Set<String> seen = new LinkedHashSet<>();
                for (Resource resource : resources) {
                    String className = readerFactory.getMetadataReader(resource).getClassMetadata().getClassName();
                    if (className.contains("$") || !seen.add(className)) {
                        continue;
                    }
                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        hints.reflection().registerType(clazz,
                                MemberCategory.INVOKE_DECLARED_METHODS,
                                MemberCategory.INVOKE_PUBLIC_METHODS);
                    } catch (Throwable ignored) {
                        // 非 Native 构建或类不可见时忽略
                    }
                }
            } catch (IOException ignored) {
                // 扫描失败（如资源协议不支持）时忽略，JVM 模式不受影响
            }
        }
    }
}
