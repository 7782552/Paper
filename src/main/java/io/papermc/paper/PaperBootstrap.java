package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🧬 [OpenClaw] 启动官方环境自适应修复流程...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            Path configPath = Paths.get(baseDir, ".openclaw/openclaw.json");

            // 1. 彻底清理，强制重新初始化
            deleteDirectory(new File(baseDir, ".openclaw"));
            new File(baseDir, ".openclaw").mkdirs();

            // 2. 执行官方 setup，生成它“自认合法”的初始文件
            System.out.println("🔨 正在生成原生配置文件...");
            ProcessBuilder pbSetup = new ProcessBuilder(nodePath, "dist/index.js", "setup", "--confirm");
            pbSetup.directory(new File(openclawDir));
            pbSetup.environment().put("HOME", baseDir);
            pbSetup.start().waitFor();

            // 3. 【核心黑科技】不猜测结构，直接进行字符串级别注入
            if (Files.exists(configPath)) {
                String content = new String(Files.readAllBytes(configPath));
                System.out.println("💉 正在向原生文件注入凭据...");
                
                // 强制开启 Telegram 模块并注入 Token
                content = content.replace("\"channels\": {", 
                    "\"channels\": {\"telegram\": {\"enabled\": true, \"accounts\": {\"default\": {\"enabled\": true, \"botToken\": \"" + botToken + "\"}}},");
                
                // 强制注入模型配置
                content = content.replace("\"agents\": {", 
                    "\"agents\": {\"main\": {\"model\": \"google/gemini-2.0-flash\"},");

                Files.write(configPath, content.getBytes());
            }

            // 4. 纯净启动网关
            System.out.println("🚀 注入完成，尝试拉起网关...");
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            pb.environment().put("HOME", baseDir);
            pb.environment().put("CI", "true");
            pb.environment().put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            
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
