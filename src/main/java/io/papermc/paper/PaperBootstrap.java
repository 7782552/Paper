package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("⚡ [OpenClaw] 正在执行最终合并启动：注入环境变量 + 启动网关...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // ⚠️ 关键修正：重新带上 gateway 参数，让它保持运行
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway"); 
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            
            // 注入核心变量
            env.put("TELEGRAM_BOT_TOKEN", botToken); 
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");
            env.put("OPENCLAW_TELEGRAM_ENABLED", "true");
            env.put("OPENCLAW_TELEGRAM_DM_POLICY", "open");
            // 2026版新环境变量：显式允许所有来源
            env.put("OPENCLAW_TELEGRAM_ALLOW_FROM", "*");

            System.out.println("🚀 网关点火中... 只要看到 [gateway] listening，就去 Telegram 发消息！");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
