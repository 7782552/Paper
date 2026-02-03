package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String configDir = baseDir + "/.openclaw";
        String jsonPath = configDir + "/openclaw.json";
        
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        String serverPort = "30196"; 

        try {
            System.out.println("🩺 [物理参数强点火] 正在强制覆盖 Host 绑定...");

            // 1. 保持无菌 JSON
            Files.deleteIfExists(Paths.get(configDir + "/state.db"));
            Files.deleteIfExists(Paths.get(jsonPath));
            new File(configDir).mkdirs();

            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + serverPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"custom\"," // 必须是 custom
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{"
                    + "\"entries\":{"
                        + "\"telegram\":{\"enabled\":true}"
                    + "}"
                + "}"
            + "}";
            Files.write(Paths.get(jsonPath), configJson.getBytes());

            // 2. 【核心改动】直接在 CLI 参数里强插 --host
            // 2026.2.1 的 gateway 命令通常支持显式的 --host 参数
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", 
                "gateway", 
                "--port", serverPort, 
                "--host", "0.0.0.0", // <--- 物理强插
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");
            
            // 备选方案：尝试 2026 版可能采用的所有 Host 变量名
            env.put("HOST", "0.0.0.0");
            env.put("GATEWAY_HOST", "0.0.0.0");
            env.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");
            env.put("OPENCLAW_BIND", "0.0.0.0");
            
            // Telegram Token 继续走环境注入
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);

            System.out.println("🚀 执行指令: " + String.join(" ", pb.command()));
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
