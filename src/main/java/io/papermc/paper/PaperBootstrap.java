package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        
        // 🚨 既然 30196 是 N8N 的领地，OpenClaw 绝对不准碰这个端口
        // 我们给 OpenClaw 分配一个容器内部的随机端口（比如 18789）
        int clawInternalPort = 18789; 

        try {
            System.out.println("♻️ [回归原始模式] 正在把 30196 还给 N8N...");
            
            // 1. 杀掉所有抢占 30196 的 Node 残留，让 N8N 重新呼吸
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 启动 OpenClaw：只听本地，不占公网
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(clawInternalPort),
                "--token", "mytoken123",
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            
            // 🚨 核心：OpenClaw 只待在 127.0.0.1，不准去 0.0.0.0 抢风头
            env.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            
            // 🚨 N8N 的地址：既然你习惯用公网 IP，我们就填公网 IP
            // 但如果报错，我会教你改成 127.0.0.1
            env.put("OPENCLAW_N8N_URL", "http://42.119.166.155:30196/webhook/openclaw");

            pb.inheritIO();
            Process p = pb.start();

            // 3. 自动审批 (免去访问网页的麻烦)
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
        } catch (Exception e) { e.printStackTrace(); }
    }
}
