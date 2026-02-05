import java.io.File;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        // 保持你原本能启动的硬编码路径
        String nodeBinDir = baseDir + "/node-v22.12.0-linux-x64/bin";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";
        String nodePath = nodeBinDir + "/node";

        try {
            System.out.println("⚠️ [Zenix-Fix] 正在启动 n8n 并修复 521 访问错误...");

            // 1. 强力清理旧进程（确保端口 30196 完全释放）
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(1000L);
            } catch (Exception ignored) {}

            // 2. 配置 n8n 启动环境
            ProcessBuilder n8nPb = new ProcessBuilder(nodePath, n8nBin, "start");
            n8nPb.directory(new File(baseDir));
            
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            
            // --- 【关键修复点：解决 521 错误】 ---
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("N8N_HOST", "0.0.0.0");               // 修正1：允许所有外部连接
            nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");     // 修正2：强制监听所有网卡
            nEnv.put("N8N_PROTOCOL", "https");             // 修正3：匹配你的 https 域名
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            
            // 修正4：指定数据目录，防止权限导致的启动挂起
            nEnv.put("N8N_USER_FOLDER", baseDir + "/.n8n"); 
            // -------------------------------------

            n8nPb.inheritIO().start();

            // 3. 启动 OpenClaw
            System.out.println("🧠 正在启动 OpenClaw Gateway...");
            ProcessBuilder clawPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_AI_PROVIDER", "google");
            cEnv.put("OPENCLAW_AI_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            clawPb.inheritIO().start();

            System.out.println("✅ 服务已完全拉起，请尝试刷新页面。");

            while (true) {
                Thread.sleep(60000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
