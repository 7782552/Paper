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
            String geminiKey = "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String yourTelegramId = "660059245";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GOOGLE_API_KEY", geminiKey);

            // 0. 删除 Telegram Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // 1. 配置 Telegram Bot Token
            System.out.println("📝 配置 Telegram Bot...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "channels.telegram.botToken", telegramToken);

            // 2. 添加你的 Telegram ID 到白名单
            System.out.println("📝 添加用户到白名单...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "channels.telegram.allowlist", yourTelegramId);

            // 3. 禁用 pairing 模式（允许所有人使用）
            System.out.println("📝 配置访问权限...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "channels.telegram.security", "allowlist");

            // 4. 运行 doctor --fix
            System.out.println("🔧 运行 doctor --fix...");
            runCommand(env, nodeBin, ocBin, "doctor", "--fix");

            // 5. 启动 n8n
            System.out.println("🚀 启动 n8n (端口 30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, baseDir + "/node_modules/.bin/n8n", "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.inheritIO();
            n8nPb.start();

            Thread.sleep(3000);

            // 6. 启动 Gateway
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
}
