import java.io.File;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = System.getProperty("user.dir");
        String nodeBinDir = baseDir + "/node/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Emergency] 正在强制拉起系统...");

            // 1. 暴力清理，归还端口
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(1000);
            } catch (Exception ignored) {}

            // 2. 启动 n8n
            System.out.println("🚀 启动 n8n (Port: 30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            n8nPb.environment().put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (核心修复点)
            // 🚨 绝对不向 ProcessBuilder 传递任何 args[]，防止面板参数注入
            System.out.println("🧠 启动 OpenClaw Gateway (Port: 18789)...");
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // 🚨 遵循 OpenClaw 官方环境变量规范
            cEnv.put("PORT", "18789"); 
            cEnv.put("OPENCLAW_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");

            clawPb.inheritIO().start();

            System.out.println("✅ 系统运行中。n8n: https://8.8855.cc.cd");

            while(true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
