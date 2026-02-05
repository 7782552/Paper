package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        // 自动探测 Node 路径列表
        String[] possibleNodePaths = {
            baseDir + "/node-v22.12.0-linux-x64/bin/node",
            baseDir + "/node-v22/bin/node",
            baseDir + "/node/bin/node",
            "/usr/bin/node" // 尝试系统全局路径
        };

        String nodeBin = null;
        for (String path : possibleNodePaths) {
            if (new File(path).exists()) {
                nodeBin = path;
                break;
            }
        }

        if (nodeBin == null) {
            System.err.println("❌ 致命错误：在所有预设路径中均未找到 node 程序！请检查 Files 中 node 文件夹的准确名称。");
            return;
        }

        String nodeBinDir = new File(nodeBin).getParent();
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        String ocStateDir = baseDir + "/.openclaw";

        try {
            System.out.println("💎 [System-Fusion] 正在初始化 2026 环境...");
            System.out.println("📍 已探测到 Node 路径: " + nodeBin);

            // 1. 强行修复配置与初始化标记
            File stateDir = new File(ocStateDir);
            if (!stateDir.exists()) stateDir.mkdirs();
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes(StandardCharsets.UTF_8));
            
            // 使用 2026.02.02 纯净配置
            String configContent = "{\"gateway\":{\"port\":18789,\"authEnabled\":false}}";
            Files.write(Paths.get(ocStateDir, "openclaw.json"), configContent.getBytes(StandardCharsets.UTF_8));

            // 2. 启动 n8n
            if (new File(n8nBin).exists()) {
                ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
                n8nPb.directory(new File(baseDir));
                Map<String, String> n8nEnv = n8nPb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nPb.inheritIO().start();
                System.out.println("✅ n8n 引擎启动指令已发出");
            }

            // 3. 启动 OpenClaw 并修复 Telegram
            if (new File(ocBin).exists()) {
                ProcessBuilder ocPb = new ProcessBuilder(nodeBin, ocBin, "gateway", "--force");
                Map<String, String> ocEnv = ocPb.environment();
                ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
                ocEnv.put("OPENCLAW_ONBOARDED", "true");

                // 2026 版 Telegram/AI 环境变量配置
                ocEnv.put("OPENCLAW_TELEGRAM_ENABLED", "true");
                ocEnv.put("OPENCLAW_TELEGRAM_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
                ocEnv.put("OPENCLAW_AI_ENABLED", "true");
                ocEnv.put("OPENCLAW_AI_PROVIDER", "google");
                ocEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");

                ocPb.inheritIO().start();
                System.out.println("🚀 OpenClaw 指令已发出，正尝试连接 Telegram...");
            }

            System.out.println("🎊 启动序列完成。");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
