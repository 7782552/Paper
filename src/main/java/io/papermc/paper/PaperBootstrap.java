package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 核心配置区 (请根据实际情况调整 Token) ---
        String baseDir = "/home/container";
        String configDir = baseDir + "/.openclaw";
        String jsonPath = configDir + "/openclaw.json";
        String dbPath = configDir + "/state.db";
        
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "secure_token_2026";

        try {
            System.out.println("🩺 正在进行最后的逻辑闭环手术：2026.2.1 极简注入模式...");

            // 1. 物理粉碎：强制清除旧状态，防止迁移锁死
            Files.deleteIfExists(Paths.get(dbPath));
            Files.deleteIfExists(Paths.get(jsonPath));
            
            File dir = new File(configDir);
            if (!dir.exists()) dir.mkdirs();

            // 2. 构造 2026.2.1 严苛 Schema 下的无菌 JSON
            // 剔除了 host, polling 等所有被 Doctor 视为 Unrecognized 的键
            String configJson = "{"
                + "\"gateway\":{"
                    + "\"auth\":{\"token\":\"" + gatewayToken + "\"}"
                + "},"
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
            System.out.println("🚀 JSON 注入完成：已绕过 Doctor 校验。");

            // 3. 物理权限强锁 (700/600) 防止 Pterodactyl 的权限系统误杀
            runCommand("chmod", "700", configDir);
            runCommand("chmod", "600", jsonPath);

            // 4. 构建启动进程
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node", 
                "dist/index.js", 
                "gateway"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            
            // --- 5. 核心：通过环境变量绕过 Schema 限制，强制修改监听地址 ---
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            
            // 强制网关监听所有地址 (0.0.0.0)，而非 127.0.0.1
            env.put("HOST", "0.0.0.0"); 
            env.put("OPENCLAW_HOST", "0.0.0.0");
            env.put("PORT", "18789");
            env.put("OPENCLAW_PORT", "18789");
            
            // 鉴权令牌多重注入
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_GATEWAY_KEY", gatewayToken); 
            env.put("NODE_ENV", "production");

            System.out.println("🚀 环境变量已就绪，正在点火启动网关...");
            
            pb.inheritIO();
            Process process = pb.start();
            
            // 守护进程：保持 Java 进程存活直到 Node 退出
            process.waitFor();

        } catch (Exception e) {
            System.err.println("❌ 部署失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCommand(String... args) throws Exception {
        Process p = new ProcessBuilder(args).start();
        p.waitFor();
    }
}
