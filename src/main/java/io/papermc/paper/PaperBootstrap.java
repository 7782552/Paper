package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class PaperBootstrap {
    // 使用用户家目录，确保非 root 也有写权限
    private static final String APP_DIR = System.getProperty("user.home") + "/.cache_node";
    private static String uuid;
    private static Process singboxProcess;

    public static void main(String[] args) {
        try {
            System.out.println("🚀 正在以非 Root 模式启动高速节点程序...");
            Files.createDirectories(Paths.get(APP_DIR));
            Files.createDirectories(Paths.get("data"));

            // 1. 获取 UUID
            uuid = getOrGenerateUUID();

            // 2. 检测架构并下载 sing-box (纯 Java 处理)
            Path bin = Paths.get(APP_DIR, "sing-box");
            downloadSingBox(bin);

            // 3. 生成 Reality 密钥对
            Map<String, String> keys = generateRealityKeys(bin);

            // 4. 生成自签证书 (用于 TUIC/Hy2)
            generateCerts();

            // 5. 写入高速配置文件
            writeConfig(bin, keys.get("private"));

            // 6. 获取公网 IP
            String ip = getPublicIP();

            // 7. 启动引擎
            startEngine(bin);

            // 8. 打印节点链接
            printLinks(ip, uuid, keys.get("public"));

            // 保持进程运行
            System.out.println("\n[保持运行中] 请勿关闭此窗口...");
            Thread.currentThread().join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getOrGenerateUUID() throws IOException {
        Path p = Paths.get("data/uuid.txt");
        if (Files.exists(p)) return Files.readString(p).trim();
        String u = UUID.randomUUID().toString();
        Files.writeString(p, u);
        return u;
    }

    private static void downloadSingBox(Path bin) throws Exception {
        if (Files.exists(bin)) return;
        String arch = System.getProperty("os.arch").contains("aarch") ? "arm64" : "amd64";
        String v = "1.12.12";
        String urlStr = String.format("https://github.com/SagerNet/sing-box/releases/download/v%s/sing-box-%s-linux-%s.tar.gz", v, v, arch);
        System.out.println("⬇️ 正在下载引擎: " + urlStr);
        
        // 使用 curl 下载 (大多数非 root 环境都有 curl)
        new ProcessBuilder("curl", "-L", "-o", APP_DIR + "/sb.tar.gz", urlStr).start().waitFor();
        // 解压
        new ProcessBuilder("bash", "-c", "cd " + APP_DIR + " && tar -xzf sb.tar.gz --strip-components=1").start().waitFor();
        new ProcessBuilder("chmod", "+x", bin.toString()).start().waitFor();
    }

    private static Map<String, String> generateRealityKeys(Path bin) throws Exception {
        Process p = new ProcessBuilder(bin.toString(), "generate", "reality-keypair").start();
        String out = new String(p.getInputStream().readAllBytes());
        Matcher m = Pattern.compile("PrivateKey: (\\S+)\\s+PublicKey: (\\S+)").matcher(out);
        if (m.find()) return Map.of("private", m.group(1), "public", m.group(2));
        return Map.of("private", "mE96_A9m-uE6t-N_V4S-dO4_u6Q1U8Q3", "public", "dummy");
    }

    private static void generateCerts() throws Exception {
        // 如果系统没有 openssl，Hy2 和 TUIC 会报错，这里尝试生成
        String key = APP_DIR + "/priv.key";
        String cert = APP_DIR + "/cert.pem";
        new ProcessBuilder("bash", "-c", "openssl ecparam -genkey -name prime256v1 -out " + key + 
            " && openssl req -new -x509 -days 3650 -key " + key + " -out " + cert + " -subj '/CN=www.bing.com'").start().waitFor();
    }

    private static void writeConfig(Path bin, String privKey) throws IOException {
        String certPath = APP_DIR + "/cert.pem";
        String keyPath = APP_DIR + "/priv.key";
        // 核心高速配置：VLESS-Reality + Hy2 + TUIC
        String config = """
        {
          "log": {"level": "error"},
          "inbounds": [
            {
              "type": "vless",
              "listen": "::", "listen_port": 8443,
              "users": [{"uuid": "%s", "flow": "xtls-rprx-vision"}],
              "tls": {
                "enabled": true, "server_name": "www.microsoft.com",
                "reality": { "enabled": true, "handshake": {"server": "www.microsoft.com", "server_port": 443}, "private_key": "%s", "short_id": ["6ba8505e"]}
              }
            },
            {
              "type": "hysteria2",
              "listen": "::", "listen_port": 20002,
              "users": [{"password": "%s"}],
              "ignore_client_bandwidth": true,
              "tls": {"enabled": true, "certificate_path": "%s", "key_path": "%s"}
            },
            {
              "type": "tuic",
              "listen": "::", "listen_port": 20001,
              "users": [{"uuid": "%s", "password": "%s"}],
              "congestion_control": "bbr",
              "tls": {"enabled": true, "alpn": ["h3"], "certificate_path": "%s", "key_path": "%s"}
            }
          ],
          "outbounds": [{"type": "direct"}]
        }
        """.formatted(uuid, privKey, uuid, certPath, keyPath, uuid, uuid, certPath, keyPath);
        Files.writeString(Paths.get(APP_DIR, "config.json"), config);
    }

    private static void startEngine(Path bin) throws IOException {
        singboxProcess = new ProcessBuilder(bin.toString(), "run", "-c", APP_DIR + "/config.json").start();
    }

    private static String getPublicIP() {
        try { return new Scanner(new URL("https://api.ipify.org").openStream()).next(); }
        catch (Exception e) { return "你的服务器IP"; }
    }

    private static void printLinks(String ip, String id, String pub) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ 非 Root 高速节点部署完成！");
        System.out.printf("\n1. 高速 VLESS (Reality):\nvless://%s@%s:8443?encryption=none&flow=xtls-rprx-vision&security=reality&sni=www.microsoft.com&fp=chrome&pbk=%s&sid=6ba8505e#高速VLESS\n", id, ip, pub);
        System.out.printf("\n2. 高速 Hy2:\nhysteria2://%s@%s:20002?insecure=1#高速Hy2\n", id, ip);
        System.out.printf("\n3. 高速 TUIC:\ntuic://%s:%s@%s:20001?alpn=h3&congestion_control=bbr&allowInsecure=1#高速TUIC\n", id, id, ip);
        System.out.println("=".repeat(60));
    }
}
