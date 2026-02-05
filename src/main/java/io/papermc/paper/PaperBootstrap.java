package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 [System-Fusion] 开启全盘深度扫描与暴力重构模式...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            
            // --- 深度搜索并物理覆写 ---
            Files.walkFileTree(Paths.get(baseDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String path = file.toString();
                    // 重点寻找 openclaw 下的 defaults.js
                    if (path.contains("openclaw") && path.endsWith("defaults.js")) {
                        System.out.println("🎯 深度扫描命中目标: " + path);
                        String newContent = 
                            "export const DEFAULT_PROVIDER = \"google\";\n" +
                            "export const DEFAULT_MODEL = \"gemini-2.0-flash\";\n" +
                            "export const DEFAULT_CONTEXT_TOKENS = 1_000_000;\n";
                        Files.write(file, newContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.println("✅ 物理覆写完成！");
                        return FileVisitResult.TERMINATE; // 找到就停
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            // --- 环境变量准备 ---
            String myKey = "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ";
            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("GOOGLE_API_KEY", myKey);
            env.put("OPENCLAW_AI_GOOGLE_API_KEY", myKey);
            env.put("OPENCLAW_GATEWAY_TOKEN", "admin123");

            // --- 启动 n8n ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.inheritIO().start();

            // --- 启动 OpenClaw (核心：增加 --allow-unconfigured) ---
            System.out.println("🚀 尝试启动 OpenClaw 网关...");
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, 
                baseDir + "/node_modules/.bin/openclaw", 
                "gateway", 
                "--allow-unconfigured", 
                "--token", "admin123",
                "--port", "18789"
            );
            ocPb.environment().putAll(env);
            ocPb.inheritIO();
            
            Process p = ocPb.start();
            p.waitFor();
            
        } catch (Exception e) { 
            e.printStackTrace();
            try { Thread.sleep(60000); } catch (Exception ignored) {}
        }
    }
}
