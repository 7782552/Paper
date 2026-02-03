package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔓 [OpenClaw] 正在执行权限解锁启动...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            // 你的 ID 是 660059245
            String myId = "660059245";

            // 1. 物理清空，防止旧的配对请求卡死进程
            deleteDirectory(new File(baseDir, ".openclaw"));
            new File(baseDir, ".openclaw").mkdirs();

            // 2. 注入“万能钥匙”配置
            // 直接把你的 ID 写进 allowFrom，并且关闭配对验证
            String configJson = "{\n" +
                "  \"gateway\": { \"port\": 18789, \"auth\": { \"mode\": \"token\", \"token\": \"123456789\" } },\n" +
                "  \"agents\": { \"default\": { \"model\": \"google/gemini-2.0-flash\" } },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"allowFrom\": [\"" + myId + "\", \"*\"],\n" + // 双重保险：指定 ID + 允许所有人
                "      \"accounts\": {\n" +
                "        \"default\": {\n" +
                "          \"enabled\": true,\n" +
                "          \"botToken\": \"" + botToken + "\",\n" +
                "          \"config\": { \"noPairing\": true }\n" + // 强制跳过配对逻辑
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            Files.write(Paths.get(baseDir, ".openclaw/openclaw.json"), configJson.getBytes());
            System.out.println("📝 权限白名单已硬编码注入。");

            // 3. 极简拉起
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");
            
            System.out.println("🚀 引擎起飞，请直接在 Telegram 给机器人发消息！");
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) deleteDirectory(f);
            }
            dir.delete();
        }
    }
}
