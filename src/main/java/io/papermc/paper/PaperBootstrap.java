package io.papermc.paper;

import java.io.*;
import java.util.Map;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 路径配置 ---
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22/bin";
        String nodeBin = nodeBinDir + "/node";
        String n8nBin = baseDir + "/node_modules/n8n/bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        
        // OpenClaw 状态目录 (所有的配置、会话、凭据都存这)
        String ocStateDir = baseDir + "/.openclaw";

        try {
            System.out.println("🦞 [System-Fusion] 正在初始化 n8n + OpenClaw 联动环境...");

            // --- 0. 强行跳过 OpenClaw 初始化向导 ---
            File stateDirFile = new File(ocStateDir);
            if (!stateDirFile.exists()) stateDirFile.mkdirs();
            
            // 确保工作空间和凭据目录存在
            new File(ocStateDir + "/workspace").mkdirs();
            new File(ocStateDir + "/credentials").mkdirs();

            // 写入“已完成初始化”标记位 (核心：防止终端阻塞)
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes(StandardCharsets.UTF_8));

            // 写入预设配置文件 (JSON 格式)
            String configContent = "{\n" +
                "  \"gateway\": {\n" +
                "    \"port\": 18789,\n" +
                "    \"bind\": \"127.0.0.1\",\n" +
                "    \"auth\": { \"mode\": \"none\" },\n" +
                "    \"allowUnconfigured\": true\n" +
                "  },\n" +
                "  \"workspace\": { \"dir\": \"" + ocStateDir + "/workspace\" }\n" +
                "}";
            Files.write(Paths.get(ocStateDir, "openclaw.json"), configContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("✅ 已自动注入 OpenClaw 静默配置参数");

            // --- 1. 启动 n8n ---
            if (new File(n8nBin).exists()) {
                ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", n8nBin, "start");
                n8nPb.directory(new File(baseDir));
                Map<String, String> n8nEnv = n8nPb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // n8n 网络配置 (根据你要求的公网模式)
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("N8N_PROTOCOL", "https");
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_EDITOR_BASE_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_SECURE_COOKIE", "false");
                
                n8nPb.inheritIO().start();
                System.out.println("✅ n8n 引擎启动中: https://8.8855.cc.cd");
            }

            // --- 2. 启动 OpenClaw Gateway ---
            if (new File(ocBin).exists()) {
                System.out.println("🚀 正在激活 OpenClaw WebSocket 网关...");
                // 官方文档推荐：gateway 启动需带上 --force 确保清理旧进程
                ProcessBuilder ocPb = new ProcessBuilder(
                    nodeBin, ocBin, "gateway", 
                    "--port", "18789", 
                    "--force", 
                    "--allow-unconfigured"
                );
                
                Map<String, String> ocEnv = ocPb.environment();
                ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                // 核心环境变量：告诉 OpenClaw 别去 ~/.openclaw 找，去我们指定的 /home/container/.openclaw 找
                ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
                ocEnv.put("OPENCLAW_ONBOARDED", "true");

                ocPb.inheritIO().start();
                System.out.println("✅ OpenClaw 服务已挂载至本地 18789 端口 (WebSocket 模式)");
            }

            System.out.println("🎊 所有自动化组件已就绪，正在维持系统心跳...");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.err.println("❌ 启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
