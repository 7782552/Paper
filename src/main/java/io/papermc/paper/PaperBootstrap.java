package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        int port = 30196; 

        try {
            System.out.println("🚀 [暴力公网模式] 正在物理强制 Node 占领端口 " + port + "...");

            // 1. 杀掉所有残留，确保端口干净
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 直接启动 Node，不要 Java 隧道了
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
            
            // 🚨 核心：直接监听所有网卡 (0.0.0.0)，不写 127.0.0.1
            env.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");
            env.put("OPENCLAW_PUBLIC_URL", "http://103.213.254.12:30196/__openclaw__/canvas/");

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
