package io.papermc.paper;

import java.io.*;
import java.util.Map;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22/bin";
        String nodeBin = nodeBinDir + "/node";
        String n8nBin = baseDir + "/node_modules/n8n/bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        String ocStateDir = baseDir + "/.openclaw";

        try {
            System.out.println("🦞 [System-Fusion] 正在注入 OpenClaw 2026 标准环境...");

            // --- 0. 环境预检与静默处理 ---
            File stateDir = new File(ocStateDir);
            if (!stateDir.exists()) stateDir.mkdirs();
            
            // 写入 2026 版强校验通过的 .onboarded
            Files.write(Paths.get(ocStateDir, ".onboarded"), "true".getBytes(StandardCharsets.UTF_8));

            // --- 重点：这是完全对齐 2026.2.3 版本的 JSON 结构 ---
            // 移除了所有 Unrecognized key (workspace, allowUnconfigured)
            // 修正了 gateway 下的字段名
            String configContent = "{\n" +
                "  \"gateway\": {\n" +
                "    \"host\": \"127.0.0.1\",\n" +
                "    \"port\": 18789,\n" +
                "    \"auth\": {\n" +
                "      \"enabled\": false\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            Files.write(Paths.get(ocStateDir, "openclaw.json"), configContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("✅ 配置文件校验对齐完成");

            // --- 1. 启动 n8n ---
            if (new File(n8nBin).exists()) {
                ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", n8nBin, "start");
                n8nPb.directory(new File(baseDir));
                Map<String, String> n8nEnv = n8nPb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // n8n 配置维持不变
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("N8N_PROTOCOL", "https");
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_EDITOR_BASE_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_SECURE_COOKIE", "false");
                
                n8nPb.inheritIO().start();
                System.out.println("✅ n8n 引擎已就绪");
            }

            // --- 2. 启动 OpenClaw Gateway ---
            if (new File(ocBin).exists()) {
                System.out.println("🚀 正在激活 OpenClaw Gateway...");
                
                // 注意：不再在命令行传递 --allow-unconfigured，因为这在 2026 版某些子命令中是非法的
                // 我们通过文件让它验证合法
                ProcessBuilder ocPb = new ProcessBuilder(
                    nodeBin, ocBin, "gateway", "--force"
                );
                
                Map<String, String> ocEnv = ocPb.environment();
                ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                ocEnv.put("OPENCLAW_STATE_DIR", ocStateDir);
                ocEnv.put("OPENCLAW_ONBOARDED", "true");

                ocPb.inheritIO().start();
                System.out.println("✅ OpenClaw 2026 网关服务已在 18789 端口挂载");
            }

            System.out.println("🎊 系统全量启动完毕，终端已接管日志。");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.err.println("致命错误: " + e.getMessage());
        }
    }
}
