package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        // 机器人身份证
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        // 你的公网域名
        String myDomain = "8.8855.cc.cd"; 
        // 🚨 爹！在这里填入你的 Gemini API Key
        String geminiKey = "AIzaSyCoDq2AD78bdWzOWP67zauQB2urVxiqH3c"; 

        try {
            System.out.println("🚀 [Zenix-Turbo] 正在启动 OpenClaw 直连模式...");

            // 1. 清理旧残留
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 2. 启动 OpenClaw (作为唯一核心)
            // 我们直接让 OC 监听 30196，对接 CF 的 Origin Rule
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", "dist/index.js", "gateway", 
                "--port", "30196", 
                "--token", "mytoken123", 
                "--force",
                "--ai-provider", "google",
                "--ai-api-key", geminiKey,
                "--ai-model", "gemini-1.5-flash",
                "--webhook-path", "/webhook/openclaw"
            );

            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            
            clawPb.inheritIO();
            Process pClaw = clawPb.start();

            // 3. 自动审批逻辑 (保持静默运行)
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(pClaw.getOutputStream()));
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(10000);
                        writer.write("pairing approve telegram all\n");
                        writer.flush();
                    }
                } catch (Exception ignored) {}
            }).start();

            System.out.println("✅ 系统已就绪！请确保 CF 的 SSL 模式为 'Flexible' (灵活)。");
            pClaw.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
