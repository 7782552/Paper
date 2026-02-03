package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw Official Headless] 正在拉取官方容器自动化指令...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 设置基础环境
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(openclawDir));
            pb.environment().put("HOME", baseDir);
            pb.inheritIO();

            // 2. 官方隐藏的“一键点火”指令：system sync
            // 2026版中，这个命令专门用于从环境变量强制同步配置到数据库并解决权限问题
            System.out.println("🛰️ 正在执行系统同步 (system sync)...");
            pb.command(nodePath, "dist/index.js", "system", "sync", 
                       "--channel", "telegram", 
                       "--token", botToken,
                       "--yes"); // 自动确认所有审计修复
            pb.start().waitFor();

            // 3. 官方 Headless 启动指令
            // 加上 --onboard 参数会让网关在启动时自动尝试连接所有已激活频道
            System.out.println("🚀 正在以官方自动驾驶模式启动网关...");
            pb.command(nodePath, "dist/index.js", "gateway", "--onboard");
            
            // 注入必要的网关验证令牌
            pb.environment().put("OPENCLAW_GATEWAY_TOKEN", "secure_long_token_2026_final");
            
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
