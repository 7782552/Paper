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
            System.out.println("💎 [Ultimate-Fusion] 正在拉起 n8n + OpenClaw 自动化集群...");

            // 1. 启动 n8n 引擎 (2GB 内存分配)
            if (new File(n8nBin).exists()) {
                ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", n8nBin, "start");
                n8nPb.directory(new File(baseDir));
                
                Map<String, String> n8nEnv = n8nPb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // HTTPS 域名适配
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("N8N_PROTOCOL", "https");
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_EDITOR_BASE_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_SECURE_COOKIE", "false"); 
                n8nEnv.put("N8N_PROXY_HOPS", "1");
                
                n8nPb.inheritIO().start();
                System.out.println("✅ n8n 引擎已启动: https://8.8855.cc.cd");
            }

            // 2. 启动 OpenClaw 网关 (加入免配置启动参数)
            if (new File(ocBin).exists()) {
                System.out.println("🚀 正在激活 OpenClaw 网关 (强制模式)...");
                // --allow-unconfigured 解决 Missing config 报错
                // --force 解决端口占用
                ProcessBuilder ocPb = new ProcessBuilder(
                    nodeBin, ocBin, "gateway", 
                    "--port", "18789", 
                    "--force", 
                    "--allow-unconfigured"
                );
                
                Map<String, String> ocEnv = ocPb.environment();
                ocEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                ocEnv.put("OPENCLAW_STATE_DIR", baseDir + "/.openclaw");
                
                ocPb.inheritIO().start();
                System.out.println("✅ OpenClaw 服务已在本地 18789 端口待命");
            }

            System.out.println("🎊 联动环境已就绪！");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
