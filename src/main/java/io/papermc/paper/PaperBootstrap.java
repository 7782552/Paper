package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        
        // 🚨 锁定你刚才确认的公网 IP
        String realIp = "42.119.166.155"; 
        int port = 30196; 
        
        // 🚨 N8N 的地址（如果你的 N8N 端口不是 5678，请修改这里）
        String n8nWebhook = "http://" + realIp + ":5678/webhook/openclaw";

        try {
            System.out.println("🔥 [公网绝杀] 宿主机IP: " + realIp);
            System.out.println("📡 信号出口 (N8N): " + n8nWebhook);

            // 1. 物理清理残留
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 启动 Node：禁止 127.0.0.1，强制公网 0.0.0.0
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(port),
                "--token", "mytoken123",
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            
            // 核心环境变量
            env.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0"); // 监听公网
            env.put("OPENCLAW_WS_URL", "ws://" + realIp + ":" + port + "/__openclaw__/ws");
            env.put("OPENCLAW_PUBLIC_URL", "http://" + realIp + ":" + port + "/__openclaw__/canvas/");
            
            // 告诉 OpenClaw 往哪发信号给 N8N
            env.put("OPENCLAW_N8N_URL", n8nWebhook);

            pb.inheritIO();
            Process p = pb.start();

            // 3. 自动审批 (每 10 秒通过一次所有 TG 配对请求)
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
