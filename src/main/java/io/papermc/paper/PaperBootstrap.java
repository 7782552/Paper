package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔥 [OpenClaw] 正在执行终极文件注入启动...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 彻底删除旧数据，防止 Database Locked 或配置冲突
            File stateDir = new File(baseDir, ".openclaw");
            deleteDirectory(stateDir);
            stateDir.mkdirs();
            System.out.println("🧹 清理完成，环境已纯净。");

            // 2. 写入 2026 版最严格格式的配置文件
            // 注意：2026版必须把模型放在 agents.default 下，频道放在 channels.telegram 下
            String configJson = "{\n" +
                "  \"gateway\": { \"port\": 18789, \"auth\": { \"mode\": \"token\", \"token\": \"mytoken123\" } },\n" +
                "  \"agents\": {\n" +
                "    \"default\": {\n" +
                "      \"model\": \"google/gemini-2.0-flash\",\n" +
                "      \"preamble\": \"You are a helpful AI assistant.\"\n" +
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

            Files.write(Paths.get(baseDir, ".openclaw/openclaw.json"), configJson.getBytes());
            System.out.println("📝 配置文件已精准注入。");

            // 3. 极简启动：不再带任何不支持的 --channel 或 --config 参数
            // 只设置环境变量告知 HOME 路径
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("CI", "true");
            env.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            
            System.out.println("🚀 引擎点火...");
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) deleteDirectory(child);
        }
        dir.delete();
    }
}
