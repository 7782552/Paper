package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String geminiApiKey = "AIzaSyCpolv3ZpSbdc9cTHlCqbURbdDhppxQ_90";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

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

            // 1. 检查是否需要初始化
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            if (!configFile.exists()) {
                System.out.println("📝 首次运行 onboard...");
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
                onboardPb.directory(new File(baseDir));
                onboardPb.inheritIO();
                onboardPb.start().waitFor();
                Thread.sleep(2000);
            }

            // 2. 使用 CLI 设置配置
            System.out.println("📝 配置 Telegram Bot Token...");
            runCommand(env, baseDir, nodeBin, ocBin, "config", "set", 
                "channels.telegram.botToken", telegramToken);
            
            System.out.println("📝 配置 dmPolicy...");
            runCommand(env, baseDir, nodeBin, ocBin, "config", "set", 
                "channels.telegram.dmPolicy", "open");
            
            System.out.println("📝 配置模型...");
            runCommand(env, baseDir, nodeBin, ocBin, "config", "set", 
                "agents.defaults.model.primary", "google/gemini-2.0-flash");

            // 3. 启动 n8n
            System.out.println("🚀 启动 n8n...");
            File n8nDir = new File(baseDir + "/.n8n");
            if (!n8nDir.exists()) n8nDir.mkdirs();

            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, "--max-old-space-size=2048",
                baseDir + "/node_modules/.bin/n8n", "start"
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
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();
            Thread.sleep(8000);

            // 4. 启动 Gateway
            System.out.println("🚀 启动 Gateway...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--bind", "lan",
                "--token", "admin123",
                "--verbose"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.directory(new File(baseDir));
            gatewayPb.inheritIO();
            gatewayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void runCommand(Map<String, String> env, String workDir, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().putAll(env);
        pb.directory(new File(workDir));
        pb.inheritIO();
        pb.start().waitFor();
    }
}
