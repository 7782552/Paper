package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚨 [OpenClaw] 开始执行物理级重装流程...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 彻底清空所有残留
            System.out.println("🧹 正在清理旧环境...");
            deleteDirectory(new File(baseDir, ".openclaw"));
            // 如果你想重装代码，取消下面这行的注释（前提是你已经上传了 openclaw 的压缩包或能重新克隆）
            // deleteDirectory(new File(openclawDir)); 

            // 2. 建立纯净配置文件夹
            new File(baseDir, ".openclaw").mkdirs();

            // 3. 写入“黄金标准”配置文件 (经过 2026.2.1 版本验证)
            // 注意：不再使用 agents.main，改用 agents.default
            String goldConfig = "{\n" +
                "  \"gateway\": { \"port\": 18789, \"auth\": { \"mode\": \"token\", \"token\": \"mytoken123\" } },\n" +
                "  \"agents\": {\n" +
                "    \"default\": {\n" +
                "      \"model\": \"google/gemini-2.0-flash\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"accounts\": {\n" +
                "        \"default\": {\n" +
                "          \"enabled\": true,\n" +
                "          \"botToken\": \"" + botToken + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            Files.write(Paths.get(baseDir, ".openclaw/openclaw.json"), goldConfig.getBytes());
            System.out.println("✨ 黄金标准配置已注入。");

            // 4. 强力启动：跳过所有检查，直接拉起
            System.out.println("🚀 正在拉起全新引擎...");
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("CI", "true");
            env.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            // 环境变量强制指定频道
            env.put("OPENCLAW_CHANNELS", "telegram");

            pb.inheritIO();
            Process p = pb.start();
            
            // 额外监控：给 Telegram 模块一点启动缓冲时间
            p.waitFor();

        } catch (Exception e) {
            System.err.println("❌ 重装失败: " + e.getMessage());
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
