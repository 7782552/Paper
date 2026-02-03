package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔨 [OpenClaw] 正在执行针对性的网关修复...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            // 你的 Telegram Token 是对的，直接用
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 清理并初始化
            deleteDirectory(new File(baseDir, ".openclaw"));
            System.out.println("🧹 已清空配置，重新构建环境...");

            // 2. 核心：通过环境变量直接喂给它网关 Token
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            
            // --- 解决报错的关键：设置网关本身的密码 ---
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789"); 
            // --------------------------------------

            // 3. 同时把 Telegram 的信息也通过环境变量塞进去，防止 JSON 解析失败
            env.put("TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_CHANNELS", "telegram");
            env.put("AGENT_MODEL", "google/gemini-2.0-flash");

            System.out.println("🚀 正在强行挂载环境变量并启动...");
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
