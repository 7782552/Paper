package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 [OpenClaw] 根据 2026.2.1 报错实据，执行结构重组启动...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 严格遵循报错提示的结构：agents.defaults
            // 并且将工具类设置移出，确保不再触发 Legacy 警告
            String finalJson = "{\n" +
                "  \"gateway\": { \"port\": 18789 },\n" +
                "  \"agents\": {\n" +
                "    \"defaults\": {\n" + // 👈 报错里明确要求的 Key
                "      \"model\": \"google/gemini-2.0-flash\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"" + botToken + "\",\n" +
                "      \"allowFrom\": [\"*\"],\n" +
                "      \"config\": { \"polling\": true }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            File configDir = new File(baseDir, ".openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            Files.write(Paths.get(baseDir, ".openclaw/openclaw.json"), finalJson.getBytes());
            System.out.println("✅ 配置文件已根据架构要求更新为 [agents.defaults] 模式。");

            // 2. 启动
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");

            System.out.println("🛰️ 正在拉起网关...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
