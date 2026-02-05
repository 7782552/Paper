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
            System.out.println("🦁 [Phase: OpenClaw] 正在部署自动化增强套件...");

            // 1. 启动 n8n (后台运行)
            if (new File(n8nBin).exists()) {
                ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", n8nBin, "start");
                Map<String, String> n8nEnv = n8nPb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("N8N_PROTOCOL", "https");
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_SECURE_COOKIE", "false"); 
                n8nEnv.put("N8N_PROXY_HOPS", "1");
                n8nPb.inheritIO().start();
                System.out.println("✅ n8n 引擎已在后台就位。");
            }

            // 2. 安装 OpenClaw (如果还没安装)
            // 我们直接安装 'openclaw' 到 node_modules，避免 GitHub 账号报错
            File ocBin = new File(baseDir + "/node_modules/.bin/openclaw");
            if (!ocBin.exists()) {
                System.out.println("📦 正在下载 OpenClaw 组件 (4G内存极速模式)...");
                execute(npmBin + " install openclaw --no-audit", nodeBinDir);
            }

            // 3. 启动 OpenClaw
            System.out.println("🚀 正在拉起 OpenClaw 服务...");
            // 通常 OpenClaw 安装后可以通过 node 运行其入口文件
            // 如果是 npm 安装的，可以直接调用它的可执行脚本
            ProcessBuilder ocPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/openclaw/dist/index.js");
            Map<String, String> ocEnv = ocPb.environment();
            ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            ocEnv.put("PORT", "18789"); // 设置 OpenClaw 的默认端口
            ocPb.inheritIO().start();

            System.out.println("🎉 [部署成功] n8n (https://8.8855.cc.cd) 和 OpenClaw 已同步运行！");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            System.out.println("❌ 运行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void execute(String cmd, String nodeBinDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", cmd);
        pb.environment().put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
        pb.inheritIO().start().waitFor();
    }
}
