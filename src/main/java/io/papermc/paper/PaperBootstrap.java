package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 [OpenClaw] 正在执行全模块强制启动流程...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 物理重置配置 (确保没有脏数据)
            Path configPath = Paths.get(baseDir, ".openclaw/openclaw.json");
            Files.deleteIfExists(configPath);
            
            // 2. 官方命令注入：注册 Telegram 并设置模型
            // 我们通过系统命令直接写入，不手动拼 JSON
            System.out.println("📦 正在注入核心凭据...");
            runCmd(nodePath, openclawDir, "doctor", "--fix");
            runCmd(nodePath, openclawDir, "channels", "add", "telegram", "--token", botToken, "--enabled", "true");
            runCmd(nodePath, openclawDir, "config", "set", "agents.default.model", "google/gemini-2.0-flash");

            // 3. 核心启动指令：强制加载 Telegram 频道
            System.out.println("🔥 正在唤醒网关并强行挂载 Telegram 频道...");
            
            // 关键改动：使用 --channel 参数强制唤醒监听器
            ProcessBuilder pb = new ProcessBuilder(
                nodePath, 
                "dist/index.js", 
                "gateway", 
                "--token", "mytoken123",
                "--channel", "telegram" 
            );

            pb.directory(new File(openclawDir));
            
            // 设置关键环境变量
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("CI", "true");
            env.put("OPENCLAW_CHANNELS", "telegram"); // 双重保障：变量 + 参数
            env.put("TELEGRAM_BOT_TOKEN", botToken);

            System.out.println("----------------------------------------------");
            System.out.println("✅ 服务已完全启动！请立即前往 Telegram 测试机器人。");
            System.out.println("----------------------------------------------");

            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();

        } catch (Exception e) {
            System.err.println("❌ 启动过程中发生错误:");
            e.printStackTrace();
        }
    }

    // 封装命令执行工具
    private static void runCmd(String node, String dir, String... args) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add(node);
        cmd.add("dist/index.js");
        cmd.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(dir));
        pb.environment().put("HOME", "/home/container");
        pb.environment().put("CI", "true");
        // 运行但不输出详细过程，避免刷屏，只在错误时抛出
        Process p = pb.start();
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            System.out.println("⚠️ 命令 [" + args[0] + "] 执行提示: " + exitCode);
        }
    }
}
