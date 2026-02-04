import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class PaperBootstrap {
    // 翼龙面板工作目录
    private static final String APP_DIR = "./.node_cache";

    public static void main(String[] args) {
        try {
            System.out.println("🚀 [高速节点] 正在翼龙面板环境启动...");
            Files.createDirectories(Paths.get(APP_DIR));

            // 1. 获取面板分配的端口 (自动读取环境变量)
            String port = System.getenv("SERVER_PORT");
            if (port == null || port.isEmpty()) port = "25565"; // 备份端口
            
            // 2. 自动下载内核 (sing-box)
            Path bin = Paths.get(APP_DIR, "sing-box");
            if (!Files.exists(bin)) {
                String arch = System.getProperty("os.arch").contains("aarch") ? "arm64" : "amd64";
                String v = "1.12.12";
                String url = "https://github.com/SagerNet/sing-box/releases/download/v" + v + "/sing-box-" + v + "-linux-" + arch + ".tar.gz";
                System.out.println("⬇️ 正在下载引擎 (" + arch + ")...");
                new ProcessBuilder("bash", "-c", "curl -L " + url + " | tar -xzC " + APP_DIR + " --strip-components=1").start().waitFor();
                new ProcessBuilder("chmod", "+x", bin.toString()).start().waitFor();
            }

            // 3. 生成 Reality 密钥对
            Process p = new ProcessBuilder(bin.toString(), "generate", "reality-keypair").start();
            String out = new String(p.getInputStream().readAllBytes());
            Matcher m = Pattern.compile("PrivateKey: (\\S+)\\s+PublicKey: (\\S+)").matcher(out);
            String priv = "", pub = "";
            if (m.find()) { priv = m.group(1); pub = m.group(2); }

            // 4. 生成高速 VLESS 配置
            String config = "{\"log\":{\"level\":\"error\"},\"inbounds\":[{\"type\":\"vless\",\"listen\":\"::\",\"listen_port\":" + port + ",\"users\":[{\"uuid\":\"" + UUID.randomUUID() + "\",\"flow\":\"xtls-rprx-vision\"}],\"tls\":{\"enabled\":true,\"server_name\":\"www.microsoft.com\",\"reality\":{\"enabled\":true,\"handshake\":{\"server\":\"www.microsoft.com\",\"server_port\":443},\"private_key\":\"" + priv + "\",\"short_id\":[\"6ba8505e\"]}}}" + "],\"outbounds\":[{\"type\":\"direct\"}]}";
            Files.writeString(Paths.get(APP_DIR, "config.json"), config);

            // 5. 启动
            System.out.println("⚡ 引擎启动中，监听端口: " + port);
            Process engine = new ProcessBuilder(bin.toString(), "run", "-c", APP_DIR + "/config.json").inheritIO().start();

            // 6. 获取 IP 并生成链接
            String ip = new Scanner(new URL("https://api.ipify.org").openStream()).next();
            System.out.println("\n" + "=".repeat(50));
            System.out.println("✅ 高速 VLESS 节点部署成功！");
            System.out.printf("\nvless://%s@%s:%s?encryption=none&flow=xtls-rprx-vision&security=reality&sni=www.microsoft.com&fp=chrome&pbk=%s&sid=6ba8505e#高速VLESS\n", UUID.randomUUID(), ip, port, pub);
            System.out.println("\n" + "=".repeat(50));

            engine.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
