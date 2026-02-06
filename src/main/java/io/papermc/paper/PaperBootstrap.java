package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            String geminiApiKey = "AIzaSyB_cCHb6nSws8C3UWaPI3Mg6M503kggX7Q";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String pairingCode = "NZHGKE5W";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GEMINI_API_KEY", geminiApiKey);

            // 0. 删除 Telegram Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // 1. 清理旧配置
            System.out.println("🧹 清理旧配置...");
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) {
                deleteDirectory(openclawDir);
            }
            Thread.sleep(500);

            // 2. 运行 onboard
            System.out.println("📝 运行 onboard 配置 Gemini...");
            ProcessBuilder onboardPb = new ProcessBuilder(
                nodeBin, ocBin, "onboard",
                "--non-interactive",
                "--accept-risk",
                "--mode", "local",
                "--auth-choice", "gemini-api-key",
                "--gemini-api-key", geminiApiKey,
                "--gateway-port", "18789",
                "--gateway-bind", "lan",
                "--gateway-auth", "token",
                "--gateway-token", "admin123",
                "--skip-daemon",
                "--skip-channels",
                "--skip-skills",
                "--skip-health",
                "--skip-ui"
            );
            onboardPb.environment().putAll(env);
            onboardPb.inheritIO();
            onboardPb.start().waitFor();

            // 3. 配置 Telegram
            System.out.println("📝 配置 Telegram Bot...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "channels.telegram.botToken", telegramToken);

            // 4. 【修复】设置模型（必须带 google/ 前缀）
            System.out.println("📝 设置模型 google/gemini-2.0-flash...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "agents.defaults.model.primary", "google/gemini-2.0-flash");

            // 5. 批准 Pairing Code
            System.out.println("✅ 批准 Pairing Code: " + pairingCode);
            runCommand(env, nodeBin, ocBin, "pairing", "approve", "telegram", pairingCode);

            // 6. 运行 doctor --fix
            System.out.println("🔧 运行 doctor --fix...");
            runCommand(env, nodeBin, ocBin, "doctor", "--fix");

            // 7. 启动 n8n
            System.out.println("🚀 启动 n8n (端口 30196)...");
            File n8nDir = new File(baseDir + "/.n8n");
            if (n8nDir.exists()) {
                deleteDirectory(n8nDir);
            }
            Thread.sleep(500);
            n8nDir.mkdirs();

            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin,
                "--max-old-space-size=2048",
                baseDir + "/node_modules/.bin/n8n",
                "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("N8N_HOST", "0.0.0.0");
            n8nPb.environment().put("N8N_SECURE_COOKIE", "false");
            n8nPb.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8nPb.environment().put("N8N_DIAGNOSTICS_ENABLED", "false");
            n8nPb.environment().put("N8N_VERSION_NOTIFICATIONS_ENABLED", "false");
            n8nPb.environment().put("N8N_HIRING_BANNER_ENABLED", "false");
            n8nPb.environment().put("N8N_PERSONALIZATION_ENABLED", "false");
            n8nPb.environment().put("N8N_TEMPLATES_ENABLED", "false");
            n8nPb.environment().put("N8N_LICENSE_AUTO_RENEW_ENABLED", "false");
            n8nPb.environment().put("N8N_PAYLOAD_SIZE_MAX", "64");
            n8nPb.environment().put("EXECUTIONS_DATA_SAVE_ON_ERROR", "none");
            n8nPb.environment().put("EXECUTIONS_DATA_SAVE_ON_SUCCESS", "none");
            n8nPb.inheritIO();
            n8nPb.start();

            Thread.sleep(5000);

            // 8. 启动 Gateway
            System.out.println("🚀 启动 OpenClaw Gateway + Telegram...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--bind", "lan",
                "--token", "admin123",
                "--verbose"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.inheritIO();
            gatewayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void runCommand(Map<String, String> env, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().putAll(env);
        pb.inheritIO();
        pb.start().waitFor();
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
