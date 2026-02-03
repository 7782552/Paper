package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw Official Sync] 正在执行官方容器化无头部署方案...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 1. 物理修复权限 (官方要求的安全前置条件)
            System.out.println("🔐 执行安全审计合规修复 (chmod 700)...");
            new ProcessBuilder("chmod", "-R", "700", baseDir + "/.openclaw").start().waitFor();

            // 2. 使用环境变量强行激活 (官方推荐的容器环境绕过方案)
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            // 核心环境变量注入：直接跳过配置文件，强行加载插件
            env.put("OPENCLAW_CHANNELS_TELEGRAM_ENABLED", "true");
            env.put("OPENCLAW_CHANNELS_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            env.put("OPENCLAW_CHANNELS_TELEGRAM_DM_POLICY", "open");
            env.put("OPENCLAW_CHANNELS_TELEGRAM_ALLOW_FROM", "[\"*\"]");
            env.put("OPENCLAW_GATEWAY_TOKEN", "secure_long_token_for_2026_gateway");
            
            // 加入官方针对 Node 22+ 的网络优化参数 (解决 Issue #4622 的 DNS 崩溃)
            env.put("NODE_OPTIONS", "--dns-result-order=ipv4first");

            System.out.println("🚀 环境变量已就绪，正在以无头模式启动网关...");
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
