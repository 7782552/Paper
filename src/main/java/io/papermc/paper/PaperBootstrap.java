package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 [OpenClaw/Clawdbot] 正在应用 2026.02.02 最新指南配置...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // ⚠️ 关键修正：参考 2026.02.02 指南和源码 FIELD_LABELS
            // 1. agents.defaults.model 必须是一个包含 primary 的对象
            // 2. channels.telegram 不再有 config 层级
            String finalJson = "{\n" +
                "  \"gateway\": {\n" +
                "    \"auth\": { \"token\": \"123456789\" },\n" +
                "    \"port\": 18789\n" +
                "  },\n" +
                "  \"agents\": {\n" +
                "    \"defaults\": {\n" +
                "      \"model\": {\n" + 
                "        \"primary\": \"google/gemini-2.0-flash\"\n" + // 👈 适配 'expected object' 报错
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"" + botToken + "\",\n" + // 👈 按照源码 FIELD_LABELS 编写
                "      \"dmPolicy\": \"all\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

            // 写入配置文件
            File configDir = new File(baseDir, ".openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            Files.write(Paths.get(baseDir, ".openclaw/openclaw.json"), finalJson.getBytes());

            // 启动命令
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            // 环境变量设置
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789"); // 对应 gateway.auth.token

            System.out.println("🛰️ 正在按照 2026 版规范拉起网关...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
