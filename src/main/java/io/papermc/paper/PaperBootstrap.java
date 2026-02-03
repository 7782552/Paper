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
            System.out.println("🩺 [JSON 降维打击] 正在尝试通过物理修改 bind 属性解锁 0.0.0.0...");

            Files.deleteIfExists(Paths.get(configDir + "/state.db"));
            Files.deleteIfExists(Paths.get(jsonPath));
            new File(configDir).mkdirs();

            // 构造极其严格的 JSON
            // 重点：尝试将 bind 直接设为 "0.0.0.0"
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + serverPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"0.0.0.0\"," // 尝试直接注入 0.0.0.0
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{"
                    + "\"entries\":{"
                        + "\"telegram\":{\"enabled\":true}"
                    + "}"
                + "}"
            + "}";
            Files.write(Paths.get(jsonPath), configJson.getBytes());

            // 启动指令：去掉那个让它报错的 --host
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", 
                "gateway", 
                "--port", serverPort,
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");
            
            // 最后的挣扎：环境变量注入 Token
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);

            System.out.println("🚀 配置文件已就绪，正在点火...");
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
