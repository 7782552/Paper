package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        String myTelegramId = "660059245"; // 你的 ID
        int publicPort = 30196;   

        try {
            System.out.println("🔥 [直接公网模式] 正在彻底重写配置...");

            // 1. 清理进程与旧配置
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            File configDir = new File(baseDir + "/.openclaw");
            if (!configDir.exists()) configDir.mkdirs();

            // 2. 写入 0.0.0.0 绑定配置，直接让 Node 暴露在公网
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + publicPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"0.0.0.0\","
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{\"enabled\":true}"
                + "}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 3. 启动进程：注入所有能跳过配对的环境变量
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");
            
            // 核心环境变量：试图直接锁死所有者
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_OWNER_ID", myTelegramId); 
            env.put("OPENCLAW_ADMINS", myTelegramId);

            pb.inheritIO();
            Process p = pb.start();

            // 4. 暴力自动审批流
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            new Thread(() -> {
                try {
                    while (p.isAlive()) {
                        Thread.sleep(15000); 
                        // 不管三七二十一，每15秒往控制台捅一次“同意全部”
                        writer.write("pairing approve telegram all\n");
                        writer.flush();
                        System.out.println("🤖 已自动发送全量审批指令...");
                    }
                } catch (Exception e) {}
            }).start();

            p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
