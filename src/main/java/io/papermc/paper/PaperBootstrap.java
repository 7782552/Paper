package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        int PORT = 30194;
        String PASSWORD = "zenix2024";
        
        try {
            System.out.println("🚀 部署 Hysteria2 极速版（2人专用）...");
            System.out.println("");
            
            String serverIP = "node.zenix.sg";
            try {
                URL ipv4 = new URL("https://api.ipify.org");
                HttpURLConnection conn = (HttpURLConnection) ipv4.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                BufferedReader r4 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                System.out.println("📍 IPv4: " + r4.readLine());
                r4.close();
            } catch (Exception e) {
                System.out.println("📍 IPv4: 检测失败");
            }
            System.out.println("");
            
            File hysteria = new File(baseDir + "/hysteria");
            if (!hysteria.exists()) {
                System.out.println("📦 [1/3] 下载 Hysteria2...");
                downloadFile(
                    "https://github.com/apernet/hysteria/releases/download/app%2Fv2.6.1/hysteria-linux-amd64",
                    baseDir + "/hysteria"
                );
                runCmd(baseDir, "chmod", "+x", "hysteria");
            } else {
                System.out.println("📦 [1/3] Hysteria2 已存在 ✓");
            }
            
            File cert = new File(baseDir + "/server.crt");
            if (!cert.exists()) {
                System.out.println("📦 [2/3] 生成证书...");
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                        "openssl", "req", "-x509", "-nodes", "-newkey", "rsa:2048",
                        "-keyout", baseDir + "/server.key",
                        "-out", baseDir + "/server.crt",
                        "-days", "3650",
                        "-subj", "/CN=" + serverIP
                    );
                    pb.directory(new File(baseDir));
                    pb.inheritIO();
                    pb.start().waitFor();
                } catch (Exception e) {
                    generateCertWithKeytool(baseDir, serverIP);
                }
            } else {
                System.out.println("📦 [2/3] 证书已存在 ✓");
            }
            
            System.out.println("📦 [3/3] 创建极速配置...");
            String config = 
                "listen: :" + PORT + "\n" +
                "\n" +
                "tls:\n" +
                "  cert: /home/container/server.crt\n" +
                "  key: /home/container/server.key\n" +
                "\n" +
                "auth:\n" +
                "  type: password\n" +
                "  password: " + PASSWORD + "\n" +
                "\n" +
                "# 极速带宽（不限制）\n" +
                "# 不设置 bandwidth，让客户端决定速度\n" +
                "\n" +
                "# 2人专用极速配置（512MB内存优化）\n" +
                "quic:\n" +
                "  initStreamReceiveWindow: 2097152\n" +    // 2MB
                "  maxStreamReceiveWindow: 4194304\n" +     // 4MB
                "  initConnReceiveWindow: 4194304\n" +      // 4MB
                "  maxConnReceiveWindow: 8388608\n" +       // 8MB（2人够用）
                "  maxIdleTimeout: 90s\n" +
                "  maxIncomingStreams: 256\n" +             // 2人足够
                "  disablePathMTUDiscovery: false\n" +      // 开启探测提速
                "\n" +
                "masquerade:\n" +
                "  type: proxy\n" +
                "  proxy:\n" +
                "    url: https://www.bing.com\n" +
                "    rewriteHost: true\n";
            
            writeFile(baseDir + "/config.yaml", config);
            
            System.out.println("");
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║     ⚡ Hysteria2 极速版就绪！                        ║");
            System.out.println("╠══════════════════════════════════════════════════════╣");
            System.out.println("║  📍 地址: node.zenix.sg:" + PORT + "                       ║");
            System.out.println("║  🔑 密码: " + PASSWORD + "                               ║");
            System.out.println("║  🚄 带宽: 无限制（由客户端决定）                     ║");
            System.out.println("║  👥 用户: 2人专用                                    ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            System.out.println("");
            System.out.println("⚠️  重要：客户端必须设置带宽！建议 200-500 Mbps");
            System.out.println("");
            System.out.println("=== 📱 v2rayN 导入 ===");
            System.out.println("hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + "?insecure=1#Zenix-Fast");
            System.out.println("");
            System.out.println("=== 📱 Clash Meta 极速配置 ===");
            System.out.println("proxies:");
            System.out.println("  - name: Zenix-Fast");
            System.out.println("    type: hysteria2");
            System.out.println("    server: node.zenix.sg");
            System.out.println("    port: " + PORT);
            System.out.println("    password: " + PASSWORD);
            System.out.println("    skip-cert-verify: true");
            System.out.println("    up: \"200 Mbps\"     # 根据你的宽带调整");
            System.out.println("    down: \"500 Mbps\"   # 根据你的宽带调整");
            System.out.println("");
            System.out.println("=== 📱 NekoBox 极速链接 ===");
            System.out.println("hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + "?insecure=1&up=200&down=500#Zenix-Fast");
            System.out.println("");
            System.out.println("🔄 启动服务...");
            System.out.println("");
            
            ProcessBuilder pb = new ProcessBuilder(baseDir + "/hysteria", "server", "-c", baseDir + "/config.yaml");
            pb.directory(new File(baseDir));
            pb.inheritIO();
            Process process = pb.start();
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n⏹️ 关闭中...");
                process.destroy();
            }));
            
            process.waitFor();
            
        } catch (Exception e) {
            System.out.println("❌ 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static void generateCertWithKeytool(String baseDir, String cn) throws Exception {
        new File(baseDir + "/keystore.p12").delete();
        runCmd(baseDir, "keytool", "-genkeypair", "-alias", "hysteria", "-keyalg", "RSA", 
            "-keysize", "2048", "-validity", "3650", "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12", "-storepass", "changeit", "-keypass", "changeit", "-dname", "CN=" + cn);
        runCmd(baseDir, "keytool", "-exportcert", "-alias", "hysteria", "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12", "-storepass", "changeit", "-rfc", "-file", baseDir + "/server.crt");
        runCmd(baseDir, "openssl", "pkcs12", "-in", baseDir + "/keystore.p12", "-nocerts", "-nodes",
            "-out", baseDir + "/server.key", "-passin", "pass:changeit");
    }
    
    static void downloadFile(String urlStr, String dest) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setInstanceFollowRedirects(true);
        int status = conn.getResponseCode();
        if (status == 302 || status == 301) {
            conn = (HttpURLConnection) new URL(conn.getHeaderField("Location")).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        }
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
        }
        System.out.println("   下载完成 ✓");
    }
    
    static void writeFile(String path, String content) throws Exception {
        try (FileWriter w = new FileWriter(path)) { w.write(content); }
    }
    
    static void runCmd(String dir, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(dir));
        pb.inheritIO();
        pb.start().waitFor();
    }
}
