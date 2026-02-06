package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置 Telegram...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            String googleApiKey = "AIzaSyCpolv3ZpSbdc9cTHlCqbURbdDhppxQ_90";  // ← 替换这里
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String pairingCode = "L4BTFFMR";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GOOGLE_API_KEY", googleApiKey);

            // 0. 删除 Telegram Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // 1. 清理旧配置
            System.out.println("🧹 清理旧配置...");
            File configDir = new File(baseDir + "/.openclaw");
            if (configDir.exists()) {
                deleteDirectory(configDir);
            }

            // 2. 运行 onboard 配置 Google AI
            System.out.println("📝 运行 onboard 配置 Google AI...");
            ProcessBuilder onboardPb = new ProcessBuilder(
                nodeBin, ocBin, "onboard",
                "--non-interactive",
                "--accept-risk",
                "--mode", "local",
                "--auth-choice", "google-api-key",
                "--google-api-key", googleApiKey,
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

            // 3. 配置 Telegram Bot Token
            System.out.println("📝 配置 Telegram Bot...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "channels.telegram.botToken", telegramToken);

            // 4. 设置 Provider 为 Google
            System.out.println("📝 设置 Provider 为 Google...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "agents.defaults.model.provider", "google");

            // 5. 设置模型
            System.out.println("📝 设置模型 Gemini 2.0...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "agents.defaults.model.primary", "gemini-2.0-flash");

            // 6. 批准 Pairing Code
            System.out.println("✅ 批准 Pairing Code...");
            runCommand(env, nodeBin, ocBin, "pairing", "approve", "telegram", pairingCode);

            // 7. 运行 doctor --fix
            System.out.println("🔧 运行 doctor --fix...");
            runCommand(env, nodeBin, ocBin, "doctor", "--fix");

            // 8. 启动 n8n（修复 503 问题）
            System.out.println("🚀 启动 n8n (端口 30196)...");
            
            // 创建 n8n 数据目录
            new File(baseDir + "/.n8n").mkdirs();
            
            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, baseDir + "/node_modules/.bin/n8n", "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("N8N_HOST", "0.0.0.0");
            n8nPb.environment().put("N8N_SECURE_COOKIE", "false");
            n8nPb.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8nPb.environment().put("DB_TYPE", "sqlite");
            n8nPb.environment().put("DB_SQLITE_DATABASE", baseDir + "/.n8n/database.sqlite");
            n8nPb.environment().put("N8N_RUNNERS_DISABLED", "true");
            n8nPb.environment().put("EXECUTIONS_MODE", "regular");
            n8nPb.inheritIO();
            n8nPb.start();

            Thread.sleep(5000);

            // 9. 启动 Gateway
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
