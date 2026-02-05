import java.io.File;
import java.util.Map;

public class ZenixEmergencyLauncher {
    public static void main(String[] args) {
        // 自动获取当前 jar 包运行的目录作为 baseDir
        String baseDir = System.getProperty("user.dir");
        String nodeBinDir = baseDir + "/node/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Emergency] 2026 启动程序初始化...");

            // 1. 暴力清理
            System.out.println("🔄 正在清理冲突进程...");
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(1500); 
            } catch (Exception ignored) {}

            // 2. 启动 n8n
            System.out.println("🚀 启动 n8n (Port: 30196)...");
            File n8nFile = new File(n8nBin);
            if (!n8nFile.exists()) {
                System.err.println("❌ 错误：找不到 n8n 路径: " + n8nBin);
            }
            
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw
            System.out.println("🧠 启动 OpenClaw (Port: 18789)...");
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");
            clawPb.inheritIO().start();

            System.out.println("\n✅ [胜利时刻] 系统已就绪！不再使用 Tunnels。");

            // 保持存活
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.err.println("❌ 编译或运行期严重错误：");
            e.printStackTrace();
        }
    }
}
