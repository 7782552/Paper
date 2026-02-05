package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = "/home/container/node-v22/bin/node";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        
        try {
            System.out.println("🔧 [System-Fusion] 正在执行精准配置修正...");

            // --- 步骤 1: 精准修改 defaults.js (根据 OpenClaw 目录结构) ---
            String[] possiblePaths = {
                baseDir + "/node_modules/openclaw/dist/agents/defaults.js",
                baseDir + "/node_modules/openclaw/dist/defaults.js",
                baseDir + "/node_modules/.pnpm/openclaw/node_modules/openclaw/dist/agents/defaults.js" // pnpm 路径
            };

            for (String path : possiblePaths) {
                File file = new File(path);
                if (file.exists()) {
                    System.out.println("🎯 锁定目标文件: " + path);
                    String content = new String(Files.readAllBytes(Paths.get(path)));
                    if (content.contains("claude-opus-4-5")) {
                        String updated = content
                            .replace("anthropic/claude-opus-4-5", "google/gemini-1.5-pro-latest")
                            .replace("provider: \"anthropic\"", "provider: \"google\"");
                        Files.write(Paths.get(path), updated.getBytes());
                        System.out.println("✅ 模型默认值已成功修改为 Gemini。");
                    }
                }
            }

            // --- 步骤 2: 启动 n8n (恢复原本正常工作的配置) ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            // 补全环境变量，防止 spawn node ENOENT
            n8nEnv.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();
            System.out.println("✅ n8n 已在 30196 端口恢复运行。");

            // --- 步骤 3: 启动 OpenClaw ---
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway", "--allow-unconfigured", "--port", "18789"
            );
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            
            // 即使 JS 改不成功，通过环境变量再次强制锁定 (2026版关键变量)
            ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
            ocEnv.put("OPENCLAW_AI_MODEL", "google/gemini-1.5-pro-latest");
            ocEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            ocEnv.put("OPENCLAW_GATEWAY_TOKEN", "admin123");
            ocEnv.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");
            ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "false"); 

            ocPb.inheritIO().start();
            System.out.println("🚀 OpenClaw 网关已启动。");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
