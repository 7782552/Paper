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

            // 1. 强力清空旧的持久化配置文件夹 (核心操作)
            File configDir = new File(baseDir + "/.openclaw");
            if (configDir.exists()) {
                deleteDirectory(configDir);
                System.out.println("🗑️ 旧配置已清除。");
            }

            // 2. 环境变量准备
            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("GOOGLE_API_KEY", myKey);
            env.put("OPENCLAW_AI_GOOGLE_API_KEY", myKey);
            env.put("OPENCLAW_AI_PROVIDER", "google");
            env.put("OPENCLAW_AI_MODEL", "google/gemini-2.0-flash");

            // 3. 官方 CLI 注入 (确保数据库重新生成时就是 Gemini)
            System.out.println("📝 正在注入新配置规则...");
            ProcessBuilder configPb = new ProcessBuilder(
                nodeBin, ocBin, "config", "set", "agents.defaults.model.primary", "google/gemini-2.0-flash"
            );
            configPb.environment().putAll(env);
            configPb.inheritIO().start().waitFor();

            // 4. 启动 n8n (30196)
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.inheritIO().start();

            // 5. 启动 OpenClaw 网关 (使用强制参数)
            System.out.println("🚀 启动 OpenClaw 网关...");
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", 
                "--allow-unconfigured", 
                "--port", "18789", 
                "--token", "admin123",
                "--model", "google/gemini-2.0-flash"
            );
            ocPb.environment().putAll(env);
            ocPb.inheritIO();
            
            ocPb.start().waitFor();
            
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }

    // 递归删除工具
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
