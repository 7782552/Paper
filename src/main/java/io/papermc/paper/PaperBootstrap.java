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
        
        // 你的核心信息
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123"; // 你在日志里被要求的 token
        String serverPort = "30196"; 

        try {
            System.out.println("🩺 [全量物理覆盖] 正在注入最后一套逻辑...");

            // 1. 继续执行外科手术，确保 host 永远是 0.0.0.0
            new ProcessBuilder("sed", "-i", "s/127.0.0.1/0.0.0.0/g", sourceFilePath).start().waitFor();

            // 2. 构造 100% 匹配 2026.2.1 要求的鉴权 JSON
            Files.deleteIfExists(Paths.get(configDir + "/state.db"));
            Files.deleteIfExists(Paths.get(jsonPath));
            new File(configDir).mkdirs();

            // 重点：将 token 直接写入 gateway.auth.token，这是它报错要的东西
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + serverPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"auto\"," 
                    + "\"auth\":{"
                        + "\"mode\":\"token\","
                        + "\"token\":\"" + gatewayToken + "\""
                    + "}"
                + "},"
                + "\"plugins\":{"
                    + "\"entries\":{"
                        + "\"telegram\":{\"enabled\":true}"
                    + "}"
                + "}"
            + "}";
            Files.write(Paths.get(jsonPath), configJson.getBytes());

            // 3. 启动进程，并使用 --token 参数做双重保险
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", 
                "gateway", 
                "--port", serverPort, 
                "--token", gatewayToken, // <--- 这里是重点，堵死它的嘴
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");
            
            // 环境变量也要给，防止插件读取不到
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);

            System.out.println("🚀 注入成功。如果看到 listening，请立刻发送 Telegram 消息！");
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
