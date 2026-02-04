package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        int port = 30196; 

        try {
            System.out.println("🔥 [死磕模式 2.0] 修正参数，环境变量强行绑定 0.0.0.0...");

            // 1. 杀掉残留进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 启动 Node：删掉了报错的 --host，改用环境变量控制
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(port),
                "--token", gatewayToken,
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            
            // --- 核心修正点 ---
            // 告诉 OpenClaw 别管 JSON，直接监听所有网卡
            env.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0"); 
            env.put("OPENCLAW_GATEWAY_PORT", String.valueOf(port));
            
            pb.inheritIO();
            Process p = pb.start();

            // 3. 自动审批
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
