package com.biliww.zhuangxiu.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * GraalVM Native Image 兼容配置（springdoc / OpenAPI）。
 *
 * <p>问题：Spring Boot 4 + springdoc 3.x 在初始化 {@code swaggerWelcome} 等 Bean 时，
 * 会通过 {@code ContextAnnotationAutowireCandidateResolver} 对 {@code SpringWebProvider}
 * 等 Provider 创建 <b>CGLIB 惰性代理</b>。Native 镜像需要为这些运行时生成的代理子类
 * （如 {@code SpringWebProvider$$SpringCGLIB$$0}）登记反射元数据（尤其是
 * {@code CGLIB$FACTORY_DATA} 字段的写权限），否则访问该字段会抛
 * {@code MissingReflectionRegistrationError} -> {@code AopConfigException} -> 启动失败。
 *
 * <p>为什么用代码注册而非 reachability-metadata.json：
 * Spring 7 的 AOT 已经自动登记了这些代理类（但只给了构造器权限）。放在
 * {@code META-INF/native-image/.../reachability-metadata.json} 里的补充条目在
 * {@code add-reachability-metadata} 聚合时不会与已有条目合并字段权限，导致被"降配"丢失。
 * 因此这里在 AOT 阶段用 {@link TypeReference#of(String)} 按类名登记代理子类，
 * 并补全 DECLARED_FIELDS / 构造器 / 方法，确保字段写权限进入最终元数据。
 */
@Configuration
@ImportRuntimeHints(SpringDocNativeConfig.SpringDocHints.class)
public class SpringDocNativeConfig {

    static class SpringDocHints implements RuntimeHintsRegistrar {

        /** 兜底注册可能惰性代理的 springdoc Provider 目标类名（避免编译期强依赖）。 */
        private static final String[] TARGETS = {
                "org.springdoc.core.providers.SpringWebProvider",
                "org.springdoc.core.providers.SpringDocProviders",
                "org.springdoc.core.providers.ObjectMapperProvider",
                "org.springdoc.core.providers.SpringDocJsonMapperProvider",
        };

        /**
         * 运行期由 springdoc 经 CGLIB 惰性代理生成的子类。类名后缀在 Spring 7 下固定为
         * {@code $$SpringCGLIB$$0}（同一类首个代理）。若日志冒出其它 {@code $$SpringCGLIB$$N}，
         * 在此数组追加即可。
         */
        private static final String[] PROXY_CLASSES = {
                "org.springdoc.core.providers.SpringWebProvider$$SpringCGLIB$$0",
                "org.springdoc.core.providers.SpringDocProviders$$SpringCGLIB$$0",
                "org.springdoc.core.providers.ObjectMapperProvider$$SpringCGLIB$$0",
        };

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1) 目标类本身的反射（构造器/方法/字段），避免直接反射访问失败
            if (classLoader != null) {
                for (String name : TARGETS) {
                    try {
                        Class<?> clazz = Class.forName(name, false, classLoader);
                        hints.reflection().registerType(clazz,
                                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                                MemberCategory.INVOKE_DECLARED_METHODS,
                                MemberCategory.INVOKE_PUBLIC_METHODS,
                                MemberCategory.DECLARED_FIELDS,
                                MemberCategory.PUBLIC_FIELDS);
                    } catch (Throwable ignored) {
                        // 类不存在或当前模块未引入时忽略，不影响 JVM 模式与其他类
                    }
                }
            }

            // 2) 运行期生成的 CGLIB 代理子类：用 TypeReference（按名，无需类存在）
            //    补全字段/方法/构造器反射，修复 CGLIB$FACTORY_DATA 的反射写权限。
            for (String proxy : PROXY_CLASSES) {
                hints.reflection().registerType(TypeReference.of(proxy),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.DECLARED_FIELDS,
                        MemberCategory.PUBLIC_FIELDS);
            }
        }
    }
}
