import java.io.File;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = System.getProperty("user.dir");
        String nodeBinDir = baseDir + "/node/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Emergency] 正在手动恢复系统...");

            // 1. 清理冲突进程
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(1000);
            } catch (Exception ignored) {}

            // 2. 启动 n8n (30196)
            System.out.println("🚀 启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (18789) - 核心修复
            System.out.println("🧠 启动 OpenClaw Gateway...");
            // 🚨 只传 gateway，绝不传 args[]
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PORT", "18789"); 
            cEnv.put("OPENCLAW_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");

            clawPb.inheritIO().start();

            System.out.println("✅ 系统已就绪。请刷新 https://8.8855.cc.cd");

            while(true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
