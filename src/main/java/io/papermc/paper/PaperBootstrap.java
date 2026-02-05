package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = baseDir + "/node-v22/bin/node"; // 你的 Node 路径
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        
        // 我们要动手术的三个核心混淆文件路径
        String[] surgeryTargets = {
            baseDir + "/node_modules/openclaw/dist/index.js",
            baseDir + "/node_modules/openclaw/dist/plugin-sdk/model-selection-Bo7pocNu.js",
            baseDir + "/node_modules/openclaw/dist/loader-BAZoAqqR.js",
            baseDir + "/node_modules/openclaw/dist/extensionAPI.js"
        };

        try {
            System.out.println("💉 [System-Fusion] 正在对 OpenClaw 核心混淆文件进行精准手术...");

            for (String path : surgeryTargets) {
                File target = new File(path);
                if (target.exists()) {
                    System.out.println("🎯 正在修正: " + path);
                    String content = new String(Files.readAllBytes(target.toPath()));
                    
                    // 执行三合一替换：模型名、Provider、以及默认 ID 逻辑
                    String updated = content
                        .replace("claude-opus-4-5", "gemini-2.0-flash")
                        .replace("anthropic/claude-opus-4-5", "google/gemini-2.0-flash")
                        .replace("provider === \"anthropic\" ? \"claude-opus-4-5\"", "provider === \"anthropic\" ? \"gemini-2.0-flash\"");
                    
                    Files.write(target.toPath(), updated.getBytes());
                    System.out.println("✅ 修正成功！");
                }
            }

            // --- 启动 n8n (保持稳定配置) ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // --- 启动 OpenClaw ---
            ProcessBuilder ocPb = new ProcessBuilder(nodeBin, ocBin, "gateway", "--allow-unconfigured", "--port", "18789");
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            ocEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            ocEnv.put("GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            ocEnv.put("OPENCLAW_GATEWAY_TOKEN", "admin123");
            ocEnv.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");
            
            ocPb.inheritIO().start();
            System.out.println("🚀 引擎已启动。现在 defaults 应指向 Gemini-2.0-Flash。");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
