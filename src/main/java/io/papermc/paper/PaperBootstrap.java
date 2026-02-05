package io.papermc.paper;

import java.io.*;
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

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GOOGLE_API_KEY", geminiKey);

            // 1. 配置 Telegram Bot Token
            System.out.println("📝 配置 Telegram Bot...");
            ProcessBuilder configPb = new ProcessBuilder(
                nodeBin, ocBin, "config", "set", 
                "channels.telegram.botToken", telegramToken
            );
            configPb.environment().putAll(env);
            configPb.inheritIO();
            configPb.start().waitFor();

            // 2. Onboard (不跳过 channels)
            System.out.println("📝 运行 OpenClaw onboard...");
            ProcessBuilder onboardPb = new ProcessBuilder(
                nodeBin, ocBin, "onboard",
                "--non-interactive",
                "--accept-risk",
                "--mode", "local",
                "--auth-choice", "gemini-api-key",
                "--gemini-api-key", geminiKey,
                "--gateway-port", "18789",
                "--gateway-bind", "lan",
                "--gateway-auth", "token",
                "--gateway-token", "admin123",
                "--skip-daemon",
                "--skip-skills",
                "--skip-health",
                "--skip-ui"
            );
            onboardPb.environment().putAll(env);
            onboardPb.inheritIO();
            onboardPb.start().waitFor();

            // 3. 启动 n8n
            System.out.println("🚀 启动 n8n (端口 30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, baseDir + "/node_modules/.bin/n8n", "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.inheritIO();
            n8nPb.start();

            Thread.sleep(3000);

            // 4. 启动 Gateway (会自动连接 Telegram)
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
}
