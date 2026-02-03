package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String configDir = baseDir + "/.openclaw";
        String jsonPath = configDir + "/openclaw.json";
        String dbPath = configDir + "/state.db";
        
        // 核心凭据
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "secure_token_2026";

        try {
            System.out.println("🩺 收到 Doctor 遗嘱：启动 [极简无菌模式] 手术...");

            // 1. 彻底粉碎旧世界
            Files.deleteIfExists(Paths.get(dbPath));
            Files.deleteIfExists(Paths.get(jsonPath));
            new File(configDir).mkdirs();

            // 2. 逻辑闭环 JSON (根据报错：剔除 host, 剔除 polling)
            // 严格遵循 2026.2.1 的 Schema：只允许存在的键
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
            System.out.println("🚀 极简配置已注入，剔除所有非法 Key，点火！");

            // 3. 强制权限锁死 (Pterodactyl 环境生存必备)
            runCommand("chmod", "700", configDir);
            runCommand("chmod", "600", jsonPath);

            // 4. 启动进程：将无法在 JSON 中配置的参数全部转入环境变量
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node", 
                "dist/index.js", 
                "gateway"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            
            // 环境变量注入核心参数 (避开 JSON Schema 校验)
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_GATEWAY_KEY", gatewayToken);
            env.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0"); // 尝试通过环境变量强制监听
            env.put("OPENCLAW_GATEWAY_PORT", "18789");
            env.put("NODE_ENV", "production");

            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();

        } catch (Exception e) {
            System.err.println("❌ 严重崩溃: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCommand(String... args) throws Exception {
        Process p = new ProcessBuilder(args).start();
        p.waitFor();
    }
}
