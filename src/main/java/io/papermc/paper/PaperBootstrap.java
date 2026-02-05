package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = "/home/container/node-v22/bin/node"; 
        String nodeBinDir = new File(nodeBin).getParent();
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        String ocStateDir = baseDir + "/.openclaw";

        try {
            System.out.println("🦞 [System-Fusion] 正在应用 2026 容器部署最佳实践...");

            // --- 0. 环境准备：强制标记 ---
            File stateDir = new File(ocStateDir);
            if (!stateDir.exists()) stateDir.mkdirs();
            
            // 写入 2026 版必须的初始化绕过标记
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes(StandardCharsets.UTF_8));
            
            // 物理删除报错源：不生成任何 openclaw.json，全靠参数启动
            Files.deleteIfExists(Paths.get(ocStateDir, "openclaw.json"));

            // --- 1. 启动 n8n ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();
            System.out.println("✅ n8n 引擎已就绪");

            // --- 2. 启动 OpenClaw (全参数+全环境变量模式) ---
            // 根据社区资料：--address 和 --no-auth 是容器成功的关键
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", 
                "--address", "0.0.0.0", // 允许容器内通信
                "--port", "18789", 
                "--no-auth", 
                "--force"
            );
            
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
            ocEnv.put("CI", "true"); // 强制非交互模式

            // --- 2026.2.3 最新环境变量命名空间 ---
            // Telegram 修复：使用 BOT_TOKEN 并开启 ENABLED
            ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "true");
            ocEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            
            // AI 修复：使用专有的 GOOGLE 命名空间
            ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
            ocEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");

            ocPb.inheritIO().start();
            System.out.println("🚀 OpenClaw 已开启无配置模式，正在监听 Telegram...");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
