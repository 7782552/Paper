package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 [System-Fusion] 正在应用经证实的暴力覆写方案...");
        try {
            String baseDir = "/home/container";
            // 自动匹配你的 node 路径
            String nodeBin = baseDir + "/node-v22/bin/node";
            String openclawDir = baseDir + "/openclaw";
            String defaultsPath = openclawDir + "/dist/agents/defaults.js";
            
            // --- 核心手术：修改 defaults.js ---
            String newContent = 
                "// Defaults for agent metadata when upstream does not supply them.\n" +
                "export const DEFAULT_PROVIDER = \"google\";\n" +
                "export const DEFAULT_MODEL = \"gemini-2.0-flash\";\n" +
                "export const DEFAULT_CONTEXT_TOKENS = 1_000_000;\n";
            
            try (FileWriter fw = new FileWriter(defaultsPath)) {
                fw.write(newContent);
            }
            System.out.println("✅ 已物理覆写 defaults.js 为 Gemini-2.0-Flash");

            // --- 环境变量准备 ---
            String myKey = "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ";
            Map<String, String> commonEnv = new HashMap<>();
            commonEnv.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            commonEnv.put("GOOGLE_API_KEY", myKey);
            commonEnv.put("OPENCLAW_AI_GOOGLE_API_KEY", myKey);
            commonEnv.put("OPENCLAW_GATEWAY_TOKEN", "admin123");

            // --- 启动 n8n (后台运行) ---
            System.out.println("🚀 启动 n8n 引擎...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(commonEnv);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // --- 启动 OpenClaw Gateway ---
            System.out.println("🚀 启动 OpenClaw Gateway...");
            ProcessBuilder ocPb = new ProcessBuilder(
                nodeBin, "dist/index.js", "gateway", "--token", "admin123"
            );
            ocPb.directory(new File(openclawDir));
            ocPb.environment().putAll(commonEnv);
            ocPb.inheritIO();
            
            // 启动并等待（防止 Java 进程直接退出）
            Process ocProcess = ocPb.start();
            ocProcess.waitFor();
            
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}
