package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            String geminiKey = "AIzaSyDgHwoCORc_PZUKmvwMQayLwIdagcDP5go";

            // 环境变量
            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GOOGLE_API_KEY", geminiKey);

            // 1. 运行 onboard (非交互式，使用 Gemini)
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
                "--skip-channels",
                "--skip-skills",
                "--skip-health",
                "--skip-ui"
            );
            onboardPb.environment().putAll(env);
            onboardPb.inheritIO();
            int onboardResult = onboardPb.start().waitFor();
            System.out.println("✅ Onboard 完成，退出码: " + onboardResult);

            // 2. 启动 n8n
            System.out.println("🚀 启动 n8n (端口 30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, baseDir + "/node_modules/.bin/n8n", "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("N8N_SECURE_COOKIE", "false");
            n8nPb.inheritIO();
            n8nPb.start();
            
            Thread.sleep(5000);

            // 3. 启动 OpenClaw Gateway
            System.out.println("🚀 启动 OpenClaw Gateway (端口 18789)...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.inheritIO();
            gatewayPb.start().waitFor();

        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}
