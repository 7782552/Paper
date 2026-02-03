package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("⚡ [OpenClaw] 正在执行全模块强制唤醒...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 启动网关进程
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway", "--token", "123456789");
            pb.directory(new File(openclawDir));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_CHANNELS", "telegram");
            
            pb.inheritIO();
            Process gatewayProcess = pb.start();

            // 2. 【核心唤醒】等待 5 秒网关稳定后，强制推送连接指令
            new Thread(() -> {
                try {
                    Thread.sleep(5000); 
                    System.out.println("🔔 正在发送强制连接指令到 Telegram...");
                    ProcessBuilder wakePb = new ProcessBuilder(nodePath, "dist/index.js", "channels", "connect", "telegram", "--token", botToken);
                    wakePb.directory(new File(openclawDir));
                    wakePb.environment().put("HOME", baseDir);
                    wakePb.start().waitFor();
                    System.out.println("✅ 唤醒指令已发出，请检查 Telegram！");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            gatewayProcess.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
