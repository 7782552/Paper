package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("✅ [OpenClaw] 源码解析完成，正在注入 2026 标准配置...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 根据源码 FIELD_LABELS 精确匹配键名
            String finalJson = "{\n" +
                "  \"gateway\": {\n" +
                "    \"auth\": { \"token\": \"123456789\" },\n" +
                "    \"port\": 18789\n" +
                "  },\n" +
                "  \"agents\": {\n" +
                "    \"defaults\": {\n" +
                "      \"model\": {\n" + 
                "        \"primary\": \"google/gemini-2.0-flash\"\n" + // 👈 源码显示有 .model.primary 路径
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"" + botToken + "\",\n" + // 👈 源码确认键名为 botToken
                "      \"dmPolicy\": \"all\"\n" +             // 👈 源码确认有 dmPolicy
                "    }\n" +
                "  }\n" +
                "}";

            File configDir = new File(baseDir, ".openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            Files.write(Paths.get(baseDir, ".openclaw/openclaw.json"), finalJson.getBytes());

            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            pb.environment().put("HOME", baseDir);
            pb.environment().put("OPENCLAW_GATEWAY_TOKEN", "123456789");

            System.out.println("🚀 源码级适配完成，启动网关并拉起 Telegram...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
