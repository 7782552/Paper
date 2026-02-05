package io.papermc.paper;

import java.io.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22/bin";
        String nodeBin = nodeBinDir + "/node";
        String n8nBin = baseDir + "/node_modules/n8n/bin/n8n";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        
        try {
            System.out.println("💎 [System-Fusion] 正在初始化 n8n + OpenClaw 联动环境...");

            // --- 1. 启动 n8n 引擎 ---
            if (new File(n8nBin).exists()) {
                // 分配 2GB 内存给 n8n，留足空间给 OpenClaw
                ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", n8nBin, "start");
                n8nPb.directory(new File(baseDir));
                
                Map<String, String> n8nEnv = n8nPb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // 网络与 HTTPS 适配
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("N8N_PROTOCOL", "https");
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_EDITOR_BASE_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_SECURE_COOKIE", "false"); 
                n8nEnv.put("N8N_PROXY_HOPS", "1");
                
                n8nPb.inheritIO().start();
                System.out.println("✅ n8n 核心已在 https://8.8855.cc.cd 启动");
            }

            // --- 2. 启动 OpenClaw 网关 ---
            if (new File(ocBin).exists()) {
                System.out.println("🚀 正在激活 OpenClaw 网关服务 (Port: 18789)...");
                // 使用 gateway 命令启动监听，--force 确保端口清理
                ProcessBuilder ocPb = new ProcessBuilder(nodeBin, ocBin, "gateway", "--port", "18789", "--force");
                
                Map<String, String> ocEnv = ocPb.environment();
                ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                ocEnv.put("OPENCLAW_STATE_DIR", baseDir + "/.openclaw");
                
                ocPb.inheritIO().start();
                System.out.println("✅ OpenClaw 服务已挂载至本地 18789 端口");
            } else {
                System.out.println("⚠️ 未找到 OpenClaw 可执行文件，请检查 node_modules 安装情况。");
            }

            // 保持主线程存活
            System.out.println("🎊 所有自动化组件已就绪，等待任务执行...");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.err.println("❌ 启动序列发生崩溃: ");
            e.printStackTrace();
        }
    }
}
