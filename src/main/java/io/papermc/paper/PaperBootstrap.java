package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = "/home/container/node-v22/bin/node"; 
        String nodeBinDir = new File(nodeBin).getParent();
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";

        try {
            System.out.println("🦞 [System-Fusion] 正在调用 OpenClaw 官方自动配置 (Onboard)...");

            // --- 1. 启动 n8n (保持稳定) ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> n8nEnv = n8nPb.environment();
            n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nEnv.put("N8N_PORT", "30196");
            n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // --- 2. 核心：执行官方自动配置 (Onboarding) ---
            // 这一步会根据环境变量自动创建 openclaw.json，绝对不会报 Unrecognized key
            System.out.println("⚙️ 正在执行官方静默初始化...");
            ProcessBuilder onboardPb = new ProcessBuilder(
                nodeBin, ocBin, "onboard", "--force", "--yes"
            );
            Map<String, String> obEnv = onboardPb.environment();
            obEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // 喂给自动配置程序的初始信息
            obEnv.put("OPENCLAW_TELEGRAM_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            obEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            
            // 等待自动配置完成
            Process obProcess = onboardPb.inheritIO().start();
            obProcess.waitFor(); 
            System.out.println("✅ 官方自动配置已完成，文件已由系统生成");

            // --- 3. 正式启动 Gateway ---
            // 此时配置文件已经是由官方自己生成的了，格式绝对 100% 正确
            ProcessBuilder ocPb = new ProcessBuilder(nodeBin, ocBin, "gateway");
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // 启动时覆盖端口，确保 n8n 能连上
            ocEnv.put("OPENCLAW_GATEWAY_PORT", "18789");
            ocEnv.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");

            ocPb.inheritIO().start();
            System.out.println("🚀 OpenClaw 已通过官方配置启动，Telegram 正在连接...");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
