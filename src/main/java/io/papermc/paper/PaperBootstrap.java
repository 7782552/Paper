import java.io.File;
import java.util.Map;

public class ZenixEmergencyLauncher {
    public static void main(String[] args) {
        // 自动定位当前目录（Pterodactyl 面板通常在 /home/container）
        String baseDir = System.getProperty("user.dir");
        String nodeBinDir = baseDir + "/node/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Emergency] 正在强行恢复全家桶系统...");

            // 1. 暴力清理旧进程
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(1000);
            } catch (Exception ignored) {}

            // 2. 启动 n8n (Port: 30196)
            System.out.println("🚀 正在启动 n8n (Port: 30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (AI 大脑)
            // 🚨 修正：严格只传 "gateway"，绝不携带任何可能导致报错的参数
            System.out.println("🧠 正在启动 OpenClaw (Port: 18789)...");
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 核心环境变量 (取代所有命令行参数) ---
            cEnv.put("PORT", "18789"); 
            cEnv.put("OPENCLAW_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");

            clawPb.inheritIO().start();

            System.out.println("✅ [胜利时刻] 系统已就绪！");
            System.out.println("🔗 n8n 管理页: https://8.8855.cc.cd");

            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
