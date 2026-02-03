package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🎯 [OpenClaw] 发现 dmPolicy 校验细节，正在进行最后修正...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 修正：将 dmPolicy 改为 "open"
            String finalJson = "{\n" +
                "  \"gateway\": {\n" +
                "    \"auth\": { \"token\": \"123456789\" },\n" +
                "    \"port\": 18789\n" +
                "  },\n" +
                "  \"agents\": {\n" +
                "    \"defaults\": {\n" +
                "      \"model\": {\n" + 
                "        \"primary\": \"google/gemini-2.0-flash\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"" + botToken + "\",\n" +
                "      \"dmPolicy\": \"open\"\n" + // 👈 这里改成了 "open"
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

            System.out.println("✅ 校验规则已对齐。正在尝试启动...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
