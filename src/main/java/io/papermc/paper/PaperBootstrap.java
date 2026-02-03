package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String configDir = baseDir + "/.openclaw";
        String jsonPath = configDir + "/openclaw.json";
        
        // --- 核心配置：根据你的面板截图修正 ---
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        String serverPort = "30196"; // 对应你截图中的 PORT 30196

        try {
            System.out.println("🩺 [端口对齐版] 正在将网关绑定至面板分配端口: " + serverPort);

            // 1. 清理环境
            Files.deleteIfExists(Paths.get(configDir + "/state.db"));
            Files.deleteIfExists(Paths.get(jsonPath));
            new File(configDir).mkdirs();

            // 2. 注入 2026.2.1 插件化配置
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + serverPort + "," // 必须是 30196
                    + "\"mode\":\"local\","
                    + "\"bind\":\"custom\"," // 必须是 custom 才能配合 0.0.0.0
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{"
                    + "\"entries\":{"
                        + "\"telegram\":{"
                            + "\"enabled\":true,"
                            + "\"botToken\":\"" + botToken + "\","
                            + "\"dmPolicy\":\"open\","
                            + "\"allowFrom\":[\"*\"],"
                            + "\"session\":{\"active\":true}"
                        + "}"
                    + "}"
                + "}"
            + "}";
            
            Files.write(Paths.get(jsonPath), configJson.getBytes());

            // 3. 设置权限
            new ProcessBuilder("chmod", "700", configDir).start().waitFor();
            new ProcessBuilder("chmod", "600", jsonPath).start().waitFor();

            // 4. 正式点火：强制 0.0.0.0 穿透
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--port", serverPort, "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_HOST", "0.0.0.0"); // 极其重要：强制监听所有接口
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("NODE_ENV", "production");

            System.out.println("🚀 启动成功后，请访问: ws://node.zenix.sg:" + serverPort);
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
