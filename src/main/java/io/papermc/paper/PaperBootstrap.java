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

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GOOGLE_API_KEY", geminiKey);

            // 1. Onboard
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
            onboardPb.start().waitFor();
            System.out.println("✅ Onboard 完成");

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

            // 3. 启动 Gateway（后台）
            System.out.println("🚀 启动 OpenClaw Gateway (端口 18789)...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--bind", "lan",
                "--token", "admin123",
                "--verbose"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.inheritIO();
            gatewayPb.start();

            // 等待 Gateway 启动
            Thread.sleep(8000);

            // 4. 测试 Gateway health
            System.out.println("\n🧪 测试 Gateway health...");
            ProcessBuilder healthPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", "health"
            );
            healthPb.environment().putAll(env);
            healthPb.inheritIO();
            healthPb.start().waitFor();

            // 5. 测试 Gateway probe
            System.out.println("\n🧪 测试 Gateway probe...");
            ProcessBuilder probePb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", "probe"
            );
            probePb.environment().putAll(env);
            probePb.inheritIO();
            probePb.start().waitFor();

            // 保持运行
            System.out.println("\n✅ 服务已启动！");
            System.out.println("📍 n8n: http://node.zenix.sg:30196");
            System.out.println("📍 OpenClaw Gateway: ws://node.zenix.sg:18789");
            System.out.println("📍 Token: admin123");
            
            Thread.currentThread().join();

        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}
