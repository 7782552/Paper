package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛠️ [OpenClaw] 正在修复 Token 冒号解析问题并注入凭据...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 设置 Gateway 启动器
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway"); 
            pb.directory(new File(openclawDir));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");

            // 2. 核心修正：使用明确的参数数组，避免 split(":") 破坏 Token
            String[][] configs = {
                {"channels.telegram.enabled", "true"},
                {"channels.telegram.botToken", botToken},
                {"channels.telegram.dmPolicy", "open"},
                {"channels.telegram.allowFrom", "*"} // 按照 LilysAI 指南补全
            };

            for (String[] config : configs) {
                System.out.println("💾 Setting " + config[0] + "...");
                Process p = new ProcessBuilder(nodePath, "dist/index.js", "config", "set", config[0], config[1])
                    .directory(new File(openclawDir))
                    .environment().put("HOME", baseDir)
                    .inheritIO()
                    .start();
                p.waitFor(); // 确保每个配置都写进去
            }

            // 3. 启动网关
            System.out.println("🚀 凭据已通过 config 指令物理注入，网关启动...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
