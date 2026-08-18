package com.biliww.zhuangxiu.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * GraalVM Native Image 兼容配置（jjwt / JWT）。
 *
 * <p>问题：jjwt 0.12.x 在构建 JWT 解析器时会触发内部枚举 {@code Jwts$ENC} 的静态初始化，
 * 进而通过 {@code io.jsonwebtoken.lang.Classes.forName("io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms")}
 * 以<b>字符串名反射加载</b>加密算法聚合类。Native 镜像的静态可达性分析无法发现这种"运行期按需反射"，
 * 导致该类未被打进镜像，访问时抛 {@code UnknownClassException}
 * （根因）→ {@code NoClassDefFoundError: Could not initialize class io.jsonwebtoken.Jwts$ENC}
 * （后续请求复用失败状态），表现为任何带 token 的请求在 {@code JwtAuthenticationFilter} 解析 JWT 时 500。
 *
 * <p>解决：在 AOT 阶段把 jjwt 相关类登记进反射元数据，使其被包含在镜像且可反射。
 * 做法两层：
 * <ol>
 *   <li>显式兜底注册 5 个 {@code Standard*Algorithms} 聚合类（{@code forName} 的直接目标，
 *       必须进镜像）；</li>
 *   <li>扫描 {@code jjwt-impl}/{@code jjwt-jackson}/{@code jjwt-api} 三个 jar，
 *       把 {@code io.jsonwebtoken} 包下<b>所有</b>类登记反射，覆盖聚合类内部
 *       {@code new} 出来的具体算法实现类（如各种 AES/GCM/HMAC 算法），
 *       避免后续再因某个具体算法类反射失败而反复重编。</li>
 * </ol>
 *
 * <p>注意：扫描依赖 {@code java.class.path} 在 process-aot 阶段包含 jjwt 依赖 jar
 * （jjwt-impl/jjwt-jackson 为 compile scope，必在其中）。即便扫描未命中，
 * 第 1 层仍保证报错的 {@code StandardEncryptionAlgorithms} 被注册。
 */
@Configuration
@ImportRuntimeHints(JwtNativeConfig.JwtHints.class)
public class JwtNativeConfig {

    static class JwtHints implements RuntimeHintsRegistrar {

        /** jjwt 在 Native 下通过 Classes.forName 反射加载的"标准算法聚合类"。 */
        private static final String[] STANDARD_AGGREGATES = {
                "io.jsonwebtoken.impl.security.StandardSignatureAlgorithms",
                "io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms",
                "io.jsonwebtoken.impl.security.StandardDecryptionAlgorithms",
                "io.jsonwebtoken.impl.security.StandardKeyAlgorithms",
                "io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms",
        };

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1) 兜底注册 forName 的直接目标（必须进镜像，否则第一关就挂）
            for (String name : STANDARD_AGGREGATES) {
                hints.reflection().registerType(TypeReference.of(name), full());
            }

            // 2) 扫描 jjwt jar，注册 io.jsonwebtoken 下所有类（含具体算法实现）
            scanJjwtJars(hints);
        }

        /** 反射全权限组合，覆盖 jjwt 在运行期对字段/方法/构造器的反射访问。 */
        private static MemberCategory[] full() {
            // Spring 7 的 MemberCategory 已拆分为 INVOKE_/INTROSPECT_/ACCESS_ 前缀，
            // 直接用 values() 全量登记最稳妥，避免遗漏某个细分权限。
            return MemberCategory.values();
        }

        /** 遍历 classpath 上的 jjwt jar，把 io.jsonwebtoken 包下所有 .class 登记反射。 */
        private void scanJjwtJars(RuntimeHints hints) {
            String cp = System.getProperty("java.class.path", "");
            for (String entry : cp.split(File.pathSeparator)) {
                if (!entry.contains("jjwt") || !entry.endsWith(".jar")) {
                    continue;
                }
                File f = new File(entry);
                if (!f.exists()) {
                    continue;
                }
                try (JarFile jar = new JarFile(f)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry je = entries.nextElement();
                        String name = je.getName();
                        if (name.endsWith(".class") && name.startsWith("io/jsonwebtoken/")) {
                            String className = name.substring(0, name.length() - 6).replace('/', '.');
                            hints.reflection().registerType(TypeReference.of(className), full());
                        }
                    }
                } catch (Throwable ignored) {
                    // 某个 jar 扫描失败不影响其它 jar 与其余逻辑
                }
            }
        }
    }
}
