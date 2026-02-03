package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛠️ [OpenClaw] 切换策略：使用 config set 注入凭据...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 注入环境变量 (确保基础认证通过)
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway"); 
            pb.directory(new File(openclawDir));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");

            // 2. 关键：通过 CLI 强行设置配置项 (参考 schema.js 的层级)
            System.out.println("💾 正在写入 Telegram 凭据到本地数据库...");
            String[] configCmds = {
                "channels.telegram.enabled:true",
                "channels.telegram.botToken:" + botToken,
                "channels.telegram.dmPolicy:open"
            };

            for (String cmd : configCmds) {
                String[] parts = cmd.split(":");
                new ProcessBuilder(nodePath, "dist/index.js", "config", "set", parts[0], parts[1])
                    .directory(new File(openclawDir))
                    .environment().put("HOME", baseDir) // 必须带上 HOME 否则找不到路径
                    .inheritIO()
                    .start()
                    .waitFor();
            }

            // 3. 启动网关
            System.out.println("🚀 凭据已注入，网关正在起飞...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
