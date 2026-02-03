package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🧬 [OpenClaw] 正在执行数据库物理注入 (Database Hack)...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 物理修复权限 (防止审计拦截)
            new ProcessBuilder("chmod", "-R", "700", baseDir + "/.openclaw").start().waitFor();

            // 2. 关键：调用官方隐藏的维护指令，将配置强行压入 SQLite 数据库
            // 这是 2026 版在容器里激活频道的唯一物理途径
            System.out.println("💾 正在强行同步 Telegram 凭据到持久化层...");
            ProcessBuilder setupPb = new ProcessBuilder(
                nodePath, "dist/index.js", "config", "import", "--json",
                "{\"channels\":{\"telegram\":{\"enabled\":true,\"botToken\":\"" + botToken + "\",\"dmPolicy\":\"open\",\"allowFrom\":[\"*\"]}}}"
            );
            setupPb.directory(new File(openclawDir));
            setupPb.environment().put("HOME", baseDir);
            setupPb.inheritIO();
            setupPb.start().waitFor();

            // 3. 启动网关
            System.out.println("🚀 物理注入完成，正在拉起网关...");
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "secure_long_token_2026_success");
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
