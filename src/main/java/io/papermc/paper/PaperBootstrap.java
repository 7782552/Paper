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
        String gatewayToken = "secure_token_2026";

        try {
            System.out.println("🩺 [物理注入] 2026.2.1 最后的逻辑闭环：强制启动...");

            // 1. 物理粉碎
            Files.deleteIfExists(Paths.get(configDir + "/state.db"));
            Files.deleteIfExists(Paths.get(jsonPath));
            new File(configDir).mkdirs();

            // 2. 构造无菌 JSON (满足 Doctor 的洁癖)
            String configJson = "{\"gateway\":{\"auth\":{\"token\":\"" + gatewayToken + "\"}},"
                + "\"channels\":{\"telegram\":{\"enabled\":true,\"botToken\":\"" + botToken + "\","
                + "\"dmPolicy\":\"open\",\"allowFrom\":[\"*\"]}}}";
            
            Files.write(Paths.get(jsonPath), configJson.getBytes());

            // 3. 强锁权限
            new ProcessBuilder("chmod", "700", configDir).start().waitFor();
            new ProcessBuilder("chmod", "600", jsonPath).start().waitFor();

            // 4. 【核心改动】使用 Shell 包装启动，防止参数丢失
            // 我们直接调用 node 并把 gateway 当作第一个参数
            String[] command = {
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js",
                "gateway",
                "--port", "18789",
                "--force"
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("HOST", "0.0.0.0");
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_AUTH_TOKEN", gatewayToken);
            env.put("NODE_ENV", "production");

            System.out.println("🚀 执行指令: " + String.join(" ", command));
            
            pb.inheritIO();
            Process process = pb.start();
            
            // 5. 守护逻辑：如果 Exit Code 是 0 (即误触发了 help)，强制重试一次
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("⚠️ 检测到程序误入 Help 模式并退出，尝试备选路径启动...");
                // 备选路径：尝试直接运行 daemon
                pb.command(baseDir + "/node-v22.12.0-linux-x64/bin/node", "dist/index.js", "daemon");
                pb.start().waitFor();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
