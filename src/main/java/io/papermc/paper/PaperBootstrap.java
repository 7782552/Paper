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
            System.out.println("🚀 [Final-Dash] 正在全力拉起自动化套件...");

            // 1. 启动 n8n (这个已经稳了，保持住)
            if (new File(n8nBin).exists()) {
                ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", n8nBin, "start");
                Map<String, String> n8nEnv = n8nPb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("N8N_PROTOCOL", "https");
                env.put("N8N_SECURE_COOKIE", "false"); 
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nPb.inheritIO().start();
                System.out.println("✅ n8n 引擎已点火！");
            }

            // 2. 尝试用 NPM 安装 OpenClaw (避开 Git 弹出用户名输入)
            File ocBin = new File(baseDir + "/node_modules/.bin/openclaw");
            if (!ocBin.exists()) {
                System.out.println("📦 正在通过 NPM 安装 OpenClaw 运行环境...");
                // 这里我们直接安装到当前目录，不再去克隆仓库
                execute(npmBin + " install openclaw --no-audit", nodeBinDir);
            }

            System.out.println("🎉 环境准备就绪！");
            System.out.println("🔗 你的 n8n 访问地址: https://8.8855.cc.cd");
            System.out.println("⚠️ 如果 OpenClaw 需要额外配置，请在文件管理器中修改 config.json。");

            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 启动中遇到波折: " + e.getMessage());
        }
    }

    private static void execute(String cmd, String nodeBinDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", cmd);
        pb.environment().put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
        pb.inheritIO().start().waitFor();
    }
}
