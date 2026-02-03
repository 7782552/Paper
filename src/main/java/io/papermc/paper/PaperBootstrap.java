package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 核心配置区 ---
        String baseDir = "/home/container";
        String configDir = baseDir + "/.openclaw";
        String jsonPath = configDir + "/openclaw.json";
        String dbPath = configDir + "/state.db";
        
        // 请确保以下 Token 正确
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "secure_token_2026";

        try {
            System.out.println("🩺 收到 Doctor 遗嘱，正在进行最后的逻辑闭环手术...");

            // 1. 物理粉碎：删除 state.db (2026.2.1 启动崩溃头号杀手)
            Files.deleteIfExists(Paths.get(dbPath));
            Files.deleteIfExists(Paths.get(jsonPath));
            
            File dir = new File(configDir);
            if (!dir.exists()) dir.mkdirs();

            // 2. 构造符合 2026.2.1 规范的无菌 JSON
            // 注意：gateway 必须监听 0.0.0.0；allowFrom 必须包含 '*'
            String configJson = "{"
                + "\"gateway\":{"
                    + "\"host\":\"0.0.0.0\","
                    + "\"port\":18789,"
                    + "\"auth\":{\"token\":\"" + gatewayToken + "\"},"
                    + "\"controlUi\":{\"allowInsecureAuth\":true}"
                + "},"
                + "\"channels\":{"
                    + "\"telegram\":{"
                        + "\"enabled\":true,"
                        + "\"botToken\":\"" + botToken + "\","
                        + "\"dmPolicy\":\"open\","
                        + "\"allowFrom\":[\"*\"],"
                        + "\"polling\":{\"enabled\":true}"
                    + "}"
                + "}"
            + "}";
            
            Files.write(Paths.get(jsonPath), configJson.getBytes());
            System.out.println("🚀 逻辑已对齐 [Host: 0.0.0.0, allowFrom: '*']，点火！");

            // 3. 物理权限强锁 (700/600)
            runCommand("chmod", "700", configDir);
            runCommand("chmod", "600", jsonPath);

            // 4. 构建进程：注入环境变量
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node", 
                "dist/index.js", 
                "gateway"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            // 2026.2.1 鉴权全家桶，确保 CLI 和 Service 都能识别
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_GATEWAY_KEY", gatewayToken); 
            env.put("NODE_ENV", "production");

            pb.inheritIO();
            Process process = pb.start();
            
            // 存活监控
            process.waitFor();

        } catch (Exception e) {
            System.err.println("❌ 严重错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCommand(String... args) throws Exception {
        Process p = new ProcessBuilder(args).start();
        p.waitFor();
    }
}
