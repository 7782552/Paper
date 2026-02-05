import java.io.File;
import java.util.Map;

public class ZenixEmergencyLauncher {
    public static void main(String[] args) {
        // --- 路径配置 ---
        String baseDir = "/your/absolute/path"; // 请确保这是绝对路径
        String nodeBinDir = baseDir + "/node/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("⚠️ [Zenix-Emergency] 启动全量环境注入...");

            // 1. 暴力清理残留，归还 30196 和 18789 端口
            System.out.println("🔄 正在清理 Node 进程...");
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(2000); 
            } catch (Exception ignored) {}

            // 2. 启动 n8n (自动化中心)
            System.out.println("🚀 启动 n8n (Port: 30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBinDir + "/node", n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            
            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw (AI 大脑)
            // 核心策略：改用 "gateway" 指令并完全通过环境变量配置
            System.out.println("🧠 启动 OpenClaw (Port: 18789)...");
            ProcessBuilder clawPb = new ProcessBuilder(nodeBinDir + "/node", "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));

            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 🚨 2026 版核心环境变量注入 ---
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            cEnv.put("OPENCLAW_TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            
            // 安全补丁：允许非加密流量
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            cEnv.put("OPENCLAW_API_PREFIX", "/v1");
            
            clawPb.inheritIO().start();

            // 4. 最终确认
            System.out.println("\n✅ [胜利时刻] 系统已就绪！");
            System.out.println("🌍 n8n 控制台: https://8.8855.cc.cd");
            System.out.println("🔗 OpenClaw 接口: http://localhost:18789/v1");

            // 保持存活
            while(true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.err.println("❌ 严重错误：");
            e.printStackTrace();
        }
    }
}
