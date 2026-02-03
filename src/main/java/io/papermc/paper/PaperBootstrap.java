package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛸 [OpenClaw] 切换至 'Doctor' 自动唤醒模式...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            Map<String, String> envVars = new HashMap<>();
            envVars.put("HOME", baseDir);
            envVars.put("OPENCLAW_GATEWAY_TOKEN", "123456789");

            // 1. 启动网关核心
            System.out.println("🛰️ 正在启动网关...");
            ProcessBuilder gatewayPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            gatewayPb.directory(new File(openclawDir));
            gatewayPb.environment().putAll(envVars);
            gatewayPb.inheritIO();
            Process gatewayProcess = gatewayPb.start();

            // 2. 关键：使用 doctor --fix 强行同步 Telegram 状态
            // 这是 LilysAI 指南中推荐的“非交互式环境”救命稻草
            Thread.sleep(8000); // 给网关多一点初始化时间
            System.out.println("🩺 正在运行 config doctor 自动修复连接...");
            ProcessBuilder doctorPb = new ProcessBuilder(nodePath, "dist/index.js", "doctor", "--fix");
            doctorPb.directory(new File(openclawDir));
            doctorPb.environment().putAll(envVars);
            doctorPb.inheritIO();
            doctorPb.start().waitFor();

            // 3. 额外保险：尝试用 message 模块发送一个系统探测
            System.out.println("🧪 正在尝试发送系统心跳探测...");
            ProcessBuilder statusPb = new ProcessBuilder(nodePath, "dist/index.js", "status");
            statusPb.directory(new File(openclawDir));
            statusPb.environment().putAll(envVars);
            statusPb.inheritIO();
            statusPb.start().waitFor();

            gatewayProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
