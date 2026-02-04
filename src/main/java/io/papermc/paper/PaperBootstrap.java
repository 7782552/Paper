package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        // 🚨 爹！在这里填入你的 Gemini API Key
        String geminiKey = "AIzaSyCoDq2AD78bdWzOWP67zauQB2urVxiqH3c"; 

        try {
            System.out.println("🚀 [Zenix-Turbo-V2] 正在切换至环境变量注入模式...");

            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Thread.sleep(1000);

            // 1. 启动 OpenClaw (把参数塞进 Environment)
            // 只传最基础的 gateway 指令，防止参数不匹配报错
            ProcessBuilder clawPb = new ProcessBuilder(
                nodeBinDir + "/node", "dist/index.js", "gateway", 
                "--port", "30196", 
                "--token", "mytoken123", 
                "--force"
            );

            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // 🚨 核心修改：通过环境变量传参，解决 unknown option 问题
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", geminiKey);
            cEnv.put("OPENCLAW_AI_MODEL", "gemini-1.5-flash");
            cEnv.put("OPENCLAW_WEBHOOK_PATH", "/webhook/openclaw");

            clawPb.inheritIO();
            Process pClaw = clawPb.start();

            // 2. 自动审批
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

            System.out.println("✅ 系统已就绪！这次绝对稳。");
            pClaw.waitFor();

        } catch (Exception e) { e.printStackTrace(); }
    }
}
