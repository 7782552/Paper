package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛡️ [OpenClaw] 启动【网关+频道】强制模式...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 强制带参数启动，确保频道被激活
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway", "--channel", "telegram");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");
            env.put("TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            env.put("AGENT_MODEL", "google/gemini-2.0-flash");
            env.put("OPENCLAW_CHANNELS", "telegram");
            env.put("TELEGRAM_ALLOW_ALL", "true");
            env.put("OPENCLAW_ADMIN_ID", "660059245");

            System.out.println("🚀 正在拉起网关并强制挂载 Telegram 频道...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
