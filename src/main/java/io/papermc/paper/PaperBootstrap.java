package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛠️ [OpenClaw] 正在写入 2026 标准版配置文件...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 确保配置目录存在
            File configDir = new File(baseDir, ".openclaw");
            if (!configDir.exists()) configDir.mkdirs();

            // 2. 写入最底层的 JSON (移除所有可能报错的 default 嵌套)
            // 采用 2026 版最核心的扁平化结构
            String pureJson = "{\n" +
                "  \"gateway\": { \"port\": 18789 },\n" +
                "  \"agents\": { \"main\": { \"model\": \"google/gemini-2.0-flash\" } },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"" + botToken + "\",\n" +
                "      \"allowFrom\": [\"*\"],\n" +
                "      \"config\": { \"polling\": true }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            Files.write(Paths.get(baseDir, ".openclaw/openclaw.json"), pureJson.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("✅ 配置文件已强行覆盖。");

            // 3. 极简启动 (不带任何报错参数)
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");

            System.out.println("🚀 引擎启动中...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
