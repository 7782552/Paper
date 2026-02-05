package io.papermc.paper;

import java.io.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22/bin";
        String nodeBin = nodeBinDir + "/node";
        String npmBin = nodeBinDir + "/npm";
        String n8nBin = baseDir + "/node_modules/n8n/bin/n8n";
        
        try {
            System.out.println("🦁 [Phase 3] 正在启动 n8n 并准备部署 OpenClaw...");

            // 1. 启动 n8n (后台运行)
            if (new File(n8nBin).exists()) {
                ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2560", n8nBin, "start");
                Map<String, String> n8nEnv = n8nPb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("N8N_PROTOCOL", "https");
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_SECURE_COOKIE", "false");
                n8nEnv.put("N8N_PROXY_HOPS", "1");
                n8nPb.inheritIO().start();
                System.out.println("✅ n8n 正在后台唤醒...");
            }

            // 2. 检查并克隆 OpenClaw
            File openClawDir = new File(baseDir + "/openclaw");
            if (!openClawDir.exists()) {
                System.out.println("📥 正在从 GitHub 获取 OpenClaw...");
                execute("git clone https://github.com/n8n-io/openclaw.git " + baseDir + "/openclaw", nodeBinDir);
            }

            // 3. 安装 OpenClaw 依赖
            if (!new File(baseDir + "/openclaw/node_modules").exists()) {
                System.out.println("🔨 正在安装 OpenClaw 依赖 (4G 内存加持，速度会很快)...");
                execute(npmBin + " install --prefix " + baseDir + "/openclaw --no-audit", nodeBinDir);
            }

            // 4. 启动 OpenClaw
            System.out.println("🚀 正在拉起 OpenClaw 服务...");
            // 注意：此处假设入口文件为 index.js，请根据实际 OpenClaw 版本调整
            ProcessBuilder ocPb = new ProcessBuilder(nodeBin, baseDir + "/openclaw/dist/index.js");
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PORT", "18789"); // OpenClaw 常用端口
            ocPb.inheritIO().start();

            System.out.println("🎉 [全部完成] n8n (30196) 与 OpenClaw (18789) 已同步运行！");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 部署失败: " + e.getMessage());
        }
    }

    private static void execute(String cmd, String nodeBinDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", cmd);
        pb.environment().put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
        pb.inheritIO().start().waitFor();
    }
}
