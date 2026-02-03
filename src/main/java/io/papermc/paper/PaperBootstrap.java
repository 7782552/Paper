package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔥 [OpenClaw] 激活终极暴力连接模式...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 注入强制环境变量 (2026版关键：OPENCLAW_AUTO_CONNECT)
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway"); 
            pb.directory(new File(openclawDir));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken); // 双重备份
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");
            env.put("OPENCLAW_AUTO_CONNECT", "telegram"); // 👈 强制自连 Telegram

            // 2. 先执行一次显式的频道激活命令 (这一步是 LilysAI 指南里的灵魂)
            System.out.println("🛰️ 正在预激活 Telegram 频道...");
            new ProcessBuilder(nodePath, "dist/index.js", "channels", "login", "--channel", "telegram", "--token", botToken)
                .directory(new File(openclawDir))
                .inheritIO()
                .start()
                .waitFor();

            // 3. 正式启动网关
            System.out.println("🚀 网关启动中，请死盯着日志，寻找 [telegram] 关键字！");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
