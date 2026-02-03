package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔥 [OpenClaw] 正在通过环境变量强行唤醒 Telegram...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 尝试使用 'all' 命令启动所有模块（gateway + channels）
            // 如果 'gateway' 参数只启动网关，那我们就换成不带参数或者 'start'
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js"); 
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            // ⚠️ 注入环境变量，这是 2026 版最无敌的启动方式
            env.put("TELEGRAM_BOT_TOKEN", botToken); 
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");
            env.put("OPENCLAW_TELEGRAM_ENABLED", "true");
            env.put("OPENCLAW_TELEGRAM_DM_POLICY", "open");

            System.out.println("🚀 强制注入完成。请观察日志中是否出现 [telegram] 字样...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
