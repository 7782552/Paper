package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        int HY_PORT = 30194;      // Hysteria2 用主端口 (UDP)
        int SS_PORT = 30194;      // Shadowsocks 也用同端口 (TCP) - 实际不冲突
        String PASSWORD = "zenix2024";
        
        try {
            System.out.println("🚀 部署双协议高速节点...");
            System.out.println("");
            
            // 检测服务器 IP
            System.out.println("🔍 检测服务器网络...");
            String serverIP = "node.zenix.sg";
            try {
                URL ipv4 = new URL("https://api.ipify.org");
                BufferedReader r4 = new BufferedReader(new InputStreamReader(ipv4.openStream()));
                String ip = r4.readLine();
                System.out.println("📍 IPv4: " + ip);
            } catch (Exception e) {
                System.out.println("📍 IPv4: 使用域名");
            }
            System.out.println("");
            
            // ==================== 节点1: Hysteria2 ====================
            File hysteria = new File(baseDir + "/hysteria");
            if (!hysteria.exists()) {
                System.out.println("📦 [1/4] 下载 Hysteria2...");
                downloadFile(
                    "https://github.com/apernet/hysteria/releases/download/app%2Fv2.6.1/hysteria-linux-amd64",
                    baseDir + "/hysteria"
                );
                runCmd(baseDir, "chmod", "+x", "hysteria");
            } else {
                System.out.println("📦 [1/4] Hysteria2 已存在 ✓");
            }
            
            // ==================== 节点2: Shadowsocks ====================
            File ss = new File(baseDir + "/shadowsocks-server");
            if (!ss.exists()) {
                System.out.println("📦 [2/4] 下载 Shadowsocks...");
                downloadFile(
                    "https://github.com/shadowsocks/go-shadowsocks2/releases/download/v0.1.5/shadowsocks2-linux.gz",
                    baseDir + "/ss.gz"
                );
                runCmd(baseDir, "gzip", "-d", "ss.gz");
                runCmd(baseDir, "mv", "ss", "shadowsocks-server");
                runCmd(baseDir, "chmod", "+x", "shadowsocks-server");
            } else {
                System.out.println("📦 [2/4] Shadowsocks 已存在 ✓");
            }
            
            // 生成证书
            File cert = new File(baseDir + "/server.crt");
            if (!cert.exists()) {
                System.out.println("📦 [3/4] 生成证书...");
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
                System.out.println("📦 [3/4] 证书已存在 ✓");
            }
            
            // 创建 Hysteria2 配置（只用 UDP，不开 TCP）
            System.out.println("📦 [4/4] 创建配置文件...");
            String hyConfig = 
                "listen: :" + HY_PORT + "\n" +
                "\n" +
                "tls:\n" +
                "  cert: /home/container/server.crt\n" +
                "  key: /home/container/server.key\n" +
                "\n" +
                "auth:\n" +
                "  type: password\n" +
                "  password: " + PASSWORD + "\n" +
                "\n" +
                "bandwidth:\n" +
                "  up: 200 mbps\n" +
                "  down: 200 mbps\n" +
                "\n" +
                "quic:\n" +
                "  initStreamReceiveWindow: 8388608\n" +
                "  maxStreamReceiveWindow: 8388608\n" +
                "  initConnReceiveWindow: 20971520\n" +
                "  maxConnReceiveWindow: 20971520\n" +
                "  maxIdleTimeout: 60s\n" +
                "  maxIncomingStreams: 1024\n" +
                "  disablePathMTUDiscovery: false\n" +
                "\n" +
                "masquerade:\n" +
                "  type: proxy\n" +
                "  proxy:\n" +
                "    url: https://www.bing.com\n" +
                "    rewriteHost: true\n";
            writeFile(baseDir + "/config.yaml", hyConfig);
            
            // 显示配置
            System.out.println("");
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║         ✅ 双协议高速节点部署完成！                     ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.println("║  📍 地址: node.zenix.sg                                  ║");
            System.out.println("║  📍 端口: " + HY_PORT + "                                         ║");
            System.out.println("║  🔑 密码: " + PASSWORD + "                                   ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.println("");
            System.out.println("┌──────────────────────────────────────────────────────────┐");
            System.out.println("│  🖥️  节点1: Hysteria2（电脑/安卓，速度最快）             │");
            System.out.println("├──────────────────────────────────────────────────────────┤");
            System.out.println("│  协议: Hysteria2 (UDP)                                   │");
            System.out.println("│  端口: " + HY_PORT + "                                            │");
            System.out.println("│                                                          │");
            System.out.println("│  v2rayN 导入:                                            │");
            System.out.println("│  hysteria2://" + PASSWORD + "@node.zenix.sg:" + HY_PORT + "?insecure=1#Zenix-Hy2");
            System.out.println("└──────────────────────────────────────────────────────────┘");
            System.out.println("");
            System.out.println("┌──────────────────────────────────────────────────────────┐");
            System.out.println("│  📱 节点2: Shadowsocks（苹果手机）                       │");
            System.out.println("├──────────────────────────────────────────────────────────┤");
            System.out.println("│  协议: Shadowsocks (TCP)                                 │");
            System.out.println("│  端口: " + SS_PORT + "                                            │");
            System.out.println("│  密码: " + PASSWORD + "                                      │");
            System.out.println("│  加密: chacha20-ietf-poly1305                            │");
            System.out.println("│                                                          │");
            System.out.println("│  Shadowrocket 配置:                                      │");
            System.out.println("│    类型: Shadowsocks                                     │");
            System.out.println("│    地址: node.zenix.sg                                   │");
            System.out.println("│    端口: " + SS_PORT + "                                          │");
            System.out.println("│    密码: " + PASSWORD + "                                    │");
            System.out.println("│    加密: chacha20-ietf-poly1305                          │");
            System.out.println("└──────────────────────────────────────────────────────────┘");
            System.out.println("");
            System.out.println("══════════════════════════════════════════════════════════");
            System.out.println("🔄 启动服务...");
            System.out.println("══════════════════════════════════════════════════════════");
            
            // 先启动 Hysteria2（后台运行）
            ProcessBuilder hyPb = new ProcessBuilder(
                baseDir + "/hysteria", "server", "-c", baseDir + "/config.yaml"
            );
            hyPb.directory(new File(baseDir));
            hyPb.redirectErrorStream(true);
            Process hyProcess = hyPb.start();
            
            // 读取 Hysteria2 输出
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(hyProcess.getInputStream())
                    );
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Hy2] " + line);
                    }
                } catch (Exception e) {}
            }).start();
            
            Thread.sleep(2000);
            System.out.println("✅ Hysteria2 已启动 (UDP:" + HY_PORT + ")");
            
            // 启动 Shadowsocks（前台运行）
            System.out.println("✅ Shadowsocks 启动中 (TCP:" + SS_PORT + ")...");
            System.out.println("");
            
            ProcessBuilder ssPb = new ProcessBuilder(
                baseDir + "/shadowsocks-server",
                "-s", "ss://AEAD_CHACHA20_POLY1305:" + PASSWORD + "@:" + SS_PORT,
                "-udp",
                "-verbose"
            );
            ssPb.directory(new File(baseDir));
            ssPb.inheritIO();
            ssPb.start().waitFor();
            
        } catch (Exception e) {
            System.out.println("❌ 部署失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static void generateCertWithKeytool(String baseDir, String cn) throws Exception {
        runCmd(baseDir, "keytool", "-genkeypair",
            "-alias", "hysteria",
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "3650",
            "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12",
            "-storepass", "changeit",
            "-keypass", "changeit",
            "-dname", "CN=" + cn
        );
        
        runCmd(baseDir, "keytool", "-exportcert",
            "-alias", "hysteria",
            "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12",
            "-storepass", "changeit",
            "-rfc",
            "-file", baseDir + "/server.crt"
        );
        
        runCmd(baseDir, "openssl", "pkcs12",
            "-in", baseDir + "/keystore.p12",
            "-nocerts", "-nodes",
            "-out", baseDir + "/server.key",
            "-passin", "pass:changeit"
        );
    }
    
    static void downloadFile(String urlStr, String dest) throws Exception {
        System.out.println("   下载: " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setInstanceFollowRedirects(true);
        
        int status = conn.getResponseCode();
        if (status == 302 || status == 301) {
            String newUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        }
        
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int len;
            long total = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                total += len;
                System.out.print("\r   已下载: " + (total / 1024) + " KB");
            }
            System.out.println(" ✓");
        }
    }
    
    static void writeFile(String path, String content) throws Exception {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
        }
    }
    
    static void runCmd(String dir, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(dir));
        pb.inheritIO();
        pb.start().waitFor();
    }
}
