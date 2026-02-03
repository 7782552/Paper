package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 核心配置参数 ---
        String baseDir = "/home/container";
        String configDir = baseDir + "/.openclaw";
        String jsonPath = configDir + "/openclaw.json";
        String dbPath = configDir + "/state.db";
        
        // 建议从面板变量获取，或者在此硬编码
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM"; 
        String gatewayToken = "secure_token_2026";

        try {
            System.out.println("🩺 收到 Doctor 遗嘱，正在进行最后的逻辑闭环手术...");

            // 1. 物理清理：粉碎旧世界 (state.db 是 2026 版启动失败的头号元凶)
            Files.deleteIfExists(Paths.get(dbPath));
            Files.deleteIfExists(Paths.get(jsonPath));
            
            File dir = new File(configDir);
            if (!dir.exists()) dir.mkdirs();

            // 2. 逻辑闭环 JSON (物理阉割 gateway.auth.method)
            // 满足 dmPolicy="open" 必须配合 allowFrom=["*"] 的死逻辑
            String configJson = "{"
                + "\"gateway\":{\"auth\":{\"token\":\"" + gatewayToken + "\"}},"
                + "\"channels\":{"
                    + "\"telegram\":{"
                        + "\"enabled\":true,"
                        + "\"botToken\":\"" + botToken + "\","
                        + "\"dmPolicy\":\"open\","
                        + "\"allowFrom\":[\"*\"]"
                    + "}"
                + "}"
            + "}";
            
            Files.write(Paths.get(jsonPath), configJson.getBytes());
            System.out.println("🚀 逻辑已对齐，包含 '*': true，点火！");

            // 3. 强制权限锁死 (在 Pterodactyl 修正权限前抢跑)
            // 文件夹 700, JSON 600
            runCommand("chmod", "700", configDir);
            runCommand("chmod", "600", jsonPath);

            // 4. 启动执行流
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node", 
                "dist/index.js", 
                "gateway"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            
            // 注入环境变量：这是 2026 版最稳的鉴权方式
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("NODE_ENV", "production");

            pb.inheritIO();
            Process process = pb.start();
            
            // 守护进程
            process.waitFor();

        } catch (Exception e) {
            System.err.println("❌ 部署崩溃: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCommand(String... args) throws Exception {
        new ProcessBuilder(args).start().waitFor();
    }
}
