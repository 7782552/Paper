package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        // 🚨 检查翼龙面板 Network 页面，确认这个端口是你主端口（Primary）吗？
        int port = 30196; 

        try {
            System.out.println("🔥 [死磕模式] 正在物理强制 Node 占领端口 " + port + "...");

            // 1. 彻底杀掉所有可能占用端口的进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 启动 Node：直接监听 0.0.0.0，不经过 Java 中转
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(port),
                "--host", "0.0.0.0", // 强制监听所有网卡
                "--token", gatewayToken,
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            
            pb.inheritIO();
            Process p = pb.start();

            // 3. 自动审批流
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            new Thread(() -> {
                try {
                    while (p.isAlive()) {
                        Thread.sleep(10000);
                        writer.write("pairing approve telegram all\n");
                        writer.flush();
                    }
                } catch (Exception ignored) {}
            }).start();

            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
