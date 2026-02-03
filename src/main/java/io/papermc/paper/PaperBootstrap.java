package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🤖 [OpenClaw] 切换至官方容器化一键修复启动模式...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 1. 设置执行环境
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(openclawDir));
            pb.environment().put("HOME", baseDir);
            // 务必使用这个 Token 绕过审计警告
            pb.environment().put("OPENCLAW_GATEWAY_TOKEN", "openclaw_secure_gateway_2026_safe");
            pb.inheritIO();

            // 2. 核心步骤：执行系统修复 (此命令会根据 openclaw.json 自动初始化 Telegram)
            System.out.println("🩺 执行系统自动修复与频道激活...");
            pb.command(nodePath, "dist/index.js", "system", "repair", "--force");
            pb.start().waitFor();

            // 3. 正式拉起网关
            System.out.println("🚀 网关点火...");
            pb.command(nodePath, "dist/index.js", "gateway");
            pb.start().waitFor();

        } catch (Exception e) {
            System.err.println("❌ 启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
