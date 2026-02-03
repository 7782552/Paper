package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🩺 [OpenClaw] 正在执行系统诊断与自动修复...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 物理删除损坏的配置文件，让它重置
            Path configPath = Paths.get(baseDir, ".openclaw/openclaw.json");
            Files.deleteIfExists(configPath);
            System.out.println("🗑️ 已清理旧配置，准备纯净启动。");

            // 2. 执行官方建议的修复命令 (静默执行)
            System.out.println("🔧 正在通过 Doctor 修复环境...");
            runCmd(nodePath, openclawDir, "doctor", "--fix");

            // 3. 使用官方命令设置 Telegram 频道 (这将自动生成正确的配置文件结构)
            System.out.println("🤖 正在注册 Telegram 机器人...");
            runCmd(nodePath, openclawDir, "channels", "add", "telegram", "--token", botToken);

            // 4. 设置 Gemini 模型 (使用官方命令而非改文件)
            System.out.println("🧠 正在配置 Gemini 2.0 Flash 模型...");
            // 2026 版命令：config set <key> <value>
            runCmd(nodePath, openclawDir, "config", "set", "agents.default.model", "google/gemini-2.0-flash");

            // 5. 正式拉起网关
            System.out.println("🚀 引擎启动！请去 Telegram 测试...");
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway", "--token", "mytoken123");
            pb.directory(new File(openclawDir));
            pb.environment().put("HOME", baseDir);
            pb.environment().put("OPENCLAW_CHANNELS", "telegram");
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runCmd(String node, String dir, String... args) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add(node);
        cmd.add("dist/index.js");
        cmd.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(dir));
        pb.environment().put("HOME", "/home/container");
        pb.start().waitFor();
    }
}
