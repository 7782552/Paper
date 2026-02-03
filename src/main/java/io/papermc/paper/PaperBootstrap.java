package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔨 [OpenClaw] 启动官方原位重装修复程序...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 物理粉碎旧配置
            deleteDirectory(new File(baseDir, ".openclaw"));
            System.out.println("🧹 残留配置已物理粉碎。");

            // 2. 运行官方 Doctor 命令进行环境初始化 (不再手动写 JSON)
            System.out.println("🔧 正在通过 Doctor 初始化环境...");
            runCmd(nodePath, openclawDir, "doctor", "--fix");

            // 3. 官方 onboard 命令 (静默模式)，强制它生成结构
            System.out.println("📦 正在强制执行官方 Onboarding...");
            runCmd(nodePath, openclawDir, "onboard", "--skip-skills", "--skip-health", "--skip-ui", "--confirm");

            // 4. 使用官方 config 命令设置模型 (这能保证写在它认的那个 key 下)
            System.out.println("🧠 正在配置 Gemini 模型...");
            runCmd(nodePath, openclawDir, "config", "set", "model", "google/gemini-2.0-flash");

            // 5. 注册 Telegram
            System.out.println("🤖 正在激活 Telegram 频道...");
            runCmd(nodePath, openclawDir, "channels", "add", "telegram", "--token", botToken);

            // 6. 最终拉起网关
            System.out.println("🚀 尝试全功能点火...");
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            pb.environment().put("HOME", baseDir);
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
