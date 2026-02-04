package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";

        try {
            System.out.println("⚠️ [Zenix-Emergency-Fix] 正在强制重置端口并拉起 API...");

            // 1. 暴力清理，确保 18789 端口彻底释放
            try { new ProcessBuilder("pkill", "-9", "node").start().waitFor(); } catch (Exception ignored) {}
            Thread.sleep(3000);

            // 2. 启动 n8n
            new ProcessBuilder(nodeBinDir + "/node", baseDir + "/node_modules/.bin/n8n", "start")
                .directory(new File(baseDir))
                .inheritIO().start();

            // 3. 启动 OpenClaw (终极组合参数)
            System.out.println("🧠 启动 OpenClaw (API 激活模式)...");
            // 🚨 核心改动：不再使用 gateway api，直接用 gateway 配合环境变量强制开启适配器
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", 
                "dist/index.js", 
                "gateway", 
                "--force", 
                "--port", "18789"
            );
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("OPENCLAW_AI_API_KEY", "你的_GEMINI_API_KEY"); 
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            
            // --- 🚨 核心 API 修复环境变量 ---
            cEnv.put("OPENCLAW_ENABLE_OPENAI_ADAPTER", "true"); // 必须开启
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");             // 必须固定前缀
            cEnv.put("OPENCLAW_EXPERIMENTAL_HTTP_API", "true"); 
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            // -----------------------------

            clawPb.inheritIO().start();
            System.out.println("✅ 启动指令已发出，请去 n8n 关闭 Stream 选项后测试！");
            
            while(true) { Thread.sleep(60000); }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
