import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String workDir = "./.node_data";
        try {
            System.out.println("🚀 翼龙面板环境 - 高速节点自动部署中...");
            Files.createDirectories(Paths.get(workDir));

            // 1. 自动获取面板分配给你的端口
            String port = System.getenv("SERVER_PORT");
            if (port == null) port = "25565"; 

            // 2. 下载引擎 (sing-box)
            Path bin = Paths.get(workDir, "sing-box");
            if (!Files.exists(bin)) {
                String arch = System.getProperty("os.arch").contains("aarch") ? "arm64" : "amd64";
                System.out.println("⬇️ 正在下载引擎版本 1.12.12 (" + arch + ")...");
                String url = "https://github.com/SagerNet/sing-box/releases/download/v1.12.12/sing-box-1.12.12-linux-" + arch + ".tar.gz";
                new ProcessBuilder("bash", "-c", "curl -L " + url + " | tar -xzC " + workDir + " --strip-components=1").start().waitFor();
                new ProcessBuilder("chmod", "+x", bin.toString()).start().waitFor();
            }

            // 3. 生成 Reality 密钥对
            Process p = new ProcessBuilder(bin.toString(), "generate", "reality-keypair").start();
            String out = new String(p.getInputStream().readAllBytes());
            Matcher m = Pattern.compile("PrivateKey: (\\S+)\\s+PublicKey: (\\S+)").matcher(out);
            String priv = "", pub = "";
            if (m.find()) { priv = m.group(1); pub = m.group(2); }

            // 4. 写入高速 VLESS 配置文件
            String uuid = UUID.randomUUID().toString();
            String config = "{\"log\":{\"level\":\"error\"},\"inbounds\":[{\"type\":\"vless\",\"listen\":\"::\",\"listen_port\":" + port + ",\"users\":[{\"uuid\":\"" + uuid + "\",\"flow\":\"xtls-rprx-vision\"}],\"tls\":{\"enabled\":true,\"server_name\":\"www.microsoft.com\",\"reality\":{\"enabled\":true,\"handshake\":{\"server\":\"www.microsoft.com\",\"server_port\":443},\"private_key\":\"" + priv + "\",\"short_id\":[\"6ba8505e\"]}}}" + "],\"outbounds\":[{\"type\":\"direct\"}]}";
            Files.writeString(Paths.get(workDir, "config.json"), config);

            // 5. 启动引擎
            System.out.println("⚡ 引擎已启动，监听端口: " + port);
            Process engine = new ProcessBuilder(bin.toString(), "run", "-c", workDir + "/config.json").inheritIO().start();

            // 6. 打印连接信息
            String ip = new Scanner(new URL("https://api.ipify.org").openStream()).next();
            System.out.println("\n" + "=".repeat(50));
            System.out.println("✅ 高速 VLESS Reality 节点已就绪：");
            System.out.printf("\nvless://%s@%s:%s?encryption=none&flow=xtls-rprx-vision&security=reality&sni=www.microsoft.com&fp=chrome&pbk=%s&sid=6ba8505e#Ptero-HighSpeed\n", uuid, ip, port, pub);
            System.out.println("=".repeat(50));

            engine.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
