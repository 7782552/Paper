package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String configDir = baseDir + "/.openclaw";
        String jsonPath = configDir + "/openclaw.json";
        String sourceFilePath = baseDir + "/openclaw/dist/config/config.js";
        
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        String serverPort = "30196"; 

        try {
            System.out.println("🩺 [外科手术模式] 正在物理修改 OpenClaw 源码以强制解锁 0.0.0.0...");

            // 1. 物理修改源码 (Sed 手术)
            // 这一步直接把 JS 代码里的默认 127.0.0.1 换成 0.0.0.0
            new ProcessBuilder("sed", "-i", "s/127.0.0.1/0.0.0.0/g", sourceFilePath).start().waitFor();
            System.out.println("✅ 源码硬编码已修改。");

            // 2. 准备一份它绝对挑不出刺的合法 JSON
            Files.deleteIfExists(Paths.get(configDir + "/state.db"));
            Files.deleteIfExists(Paths.get(jsonPath));
            new File(configDir).mkdirs();

            // 注意：这里 bind 使用 "auto"，这是它认可的合法字符串
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + serverPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"auto\"," 
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{"
                    + "\"entries\":{"
                        + "\"telegram\":{\"enabled\":true}"
                    + "}"
                + "}"
            + "}";
            Files.write(Paths.get(jsonPath), configJson.getBytes());

            // 3. 启动进程
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--port", serverPort, "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");
            
            // 注入 Telegram Token
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);

            System.out.println("🚀 源码与配置均已就绪，正在点火启动...");
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
