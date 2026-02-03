package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔧 [OpenClaw] 启动官方环境自适应修复流程...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 彻底清理环境
            deleteDirectory(new File(baseDir, ".openclaw"));
            new File(baseDir, ".openclaw").mkdirs();

            // 2. 核心：运行官方 onboard 命令生成“绝对正确”的 JSON 结构
            System.out.println("🔨 正在运行官方 Onboarding 自动构建配置...");
            runCmd(nodePath, openclawDir, "onboard", "--skip-skills", "--skip-health", "--skip-ui", "--confirm");

            // 3. 环境变量注入：这种方式不会导致 Config Invalid 报错
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("CI", "true");
            
            // 解决 Gateway 验证问题
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");
            
            // 强制覆盖模型和 Token (环境变量优先级最高)
            env.put("AGENT_MODEL", "google/gemini-2.0-flash");
            env.put("TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_CHANNELS", "telegram");
            
            // 🔓 解决权限问题的关键环境变量
            env.put("TELEGRAM_ALLOW_ALL", "true");
            env.put("OPENCLAW_ADMIN_ID", "660059245"); 

            System.out.println("🚀 引擎点火！请在 Telegram 疯狂发送消息测试...");
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
