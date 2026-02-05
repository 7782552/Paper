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
            System.out.println("🦞 [System-Fusion] 正在执行 2026.2.3 零参数启动逻辑...");

            // --- 0. 环境彻底净化 ---
            File stateDir = new File(ocStateDir);
            if (!stateDir.exists()) stateDir.mkdirs();
            
            // 物理删除所有可能触发校验的配置文件
            Files.deleteIfExists(Paths.get(ocStateDir, "openclaw.json"));
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes(StandardCharsets.UTF_8));

            // --- 1. 启动 n8n ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // --- 2. 启动 OpenClaw (核心：彻底移除所有会引发 unknown option 的参数) ---
            // 仅保留必须要有的 gateway 和 --force。其余全部交给环境变量。
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", "--force"
            );
            
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
            ocEnv.put("CI", "true");

            // --- 关键：2026.2.3 内部逻辑对应的环境变量 ---
            // 绑定地址与端口：不再通过命令行传，而是通过环境变量注入
            ocEnv.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");
            ocEnv.put("OPENCLAW_GATEWAY_PORT", "18789");
            ocEnv.put("OPENCLAW_GATEWAY_AUTH", "false"); 

            // Telegram 修复：使用新版标准环境变量
            ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "true");
            ocEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            
            // AI 修复：Gemini 专用变量
            ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
            ocEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");

            ocPb.inheritIO().start();
            System.out.println("✅ OpenClaw 已进入纯变量环境，正在静默激活 Telegram...");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
