package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🧹 [System-Fusion] 正在清空旧配置并注入 Gemini 引擎...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            String myKey = "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ";

            // 1. 清空旧配置
            File configDir = new File(baseDir + "/.openclaw");
            if (configDir.exists()) {
                deleteDirectory(configDir);
                System.out.println("🗑️ 旧配置已清除。");
            }

            // 2. 环境变量 (这是最可靠的方式)
            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("GOOGLE_API_KEY", myKey);
            
            // ⭐ 关键：通过环境变量强制指定模型
            env.put("OPENCLAW_MODEL", "google/gemini-2.0-flash");
            env.put("OPENCLAW_PROVIDER", "google");

            // 3. CLI 注入配置
            System.out.println("📝 正在注入新配置规则...");
            runCommand(env, nodeBin, ocBin, "config", "set", "agents.defaults.model.primary", "google/gemini-2.0-flash");
            runCommand(env, nodeBin, ocBin, "config", "set", "providers.google.apiKey", myKey);

            // 4. 启动 n8n (端口 30196)
            System.out.println("🚀 启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("N8N_SECURE_COOKIE", "false");  // 允许非HTTPS
            n8nPb.inheritIO().start();
            
            // 等待 n8n 启动
            Thread.sleep(3000);

            // 5. 启动 OpenClaw 网关 (移除 --model 参数!)
            System.out.println("🚀 启动 OpenClaw 网关...");
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", 
                "--allow-unconfigured", 
                "--port", "18789", 
                "--token", "admin123"
                // ❌ 删除了 "--model", "google/gemini-2.0-flash"
            );
            ocPb.environment().putAll(env);
            ocPb.inheritIO();
            
            ocPb.start().waitFor();
            
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }

    // 运行命令并等待完成
    private static void runCommand(Map<String, String> env, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().putAll(env);
        pb.inheritIO();
        int code = pb.start().waitFor();
        if (code != 0) {
            System.err.println("⚠️ 命令退出码: " + code);
        }
    }

    // 递归删除
    private static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }
}
