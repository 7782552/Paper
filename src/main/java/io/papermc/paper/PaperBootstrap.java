package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        
        // 🚨 这里的 IP 必须用刚才体检出来的最新结果
        String realIp = "42.119.166.155"; 
        int port = 30196; 

        try {
            System.out.println("🚀 [绝对公网模式] 锁定新IP: " + realIp + " 端口: " + port);

            // 1. 物理清理进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 启动 Node：强制 0.0.0.0 公网监听
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
            
            // 🚨 禁止回环，拥抱公网
            env.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");
            env.put("OPENCLAW_WS_URL", "ws://" + realIp + ":" + port + "/__openclaw__/ws");
            env.put("OPENCLAW_PUBLIC_URL", "http://" + realIp + ":" + port + "/__openclaw__/canvas/");

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
