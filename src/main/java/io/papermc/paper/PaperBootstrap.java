package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        int PORT = 30194;
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
            File ss = new File(baseDir + "/ssserver");
            if (!ss.exists()) {
                System.out.println("📦 [2/4] 下载 Shadowsocks...");
                downloadFile(
                    "https://github.com/shadowsocks/shadowsocks-rust/releases/download/v1.18.2/shadowsocks-v1.18.2.x86_64-unknown-linux-gnu.tar.xz",
                    baseDir + "/ss.tar.xz"
                );
                runCmd(baseDir, "tar", "-xf", "ss.tar.xz");
                runCmd(baseDir, "chmod", "+x", "ssserver");
                new File(baseDir + "/ss.tar.xz").delete();
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
                    System.out.println("   证书生成成功 ✓");
                } catch (Exception e) {
                    System.out.println("   使用 keytool 生成证书...");
                    generateCertWithKeytool(baseDir, serverIP);
                }
            } else {
                System.out.println("📦 [3/4] 证书已存在 ✓");
            }
            
            // 创建配置文件
            System.out.println("📦 [4/4] 创建配置文件...");
            
            // Hysteria2 配置
            String hyConfig = 
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
                "# 速度优化\n" +
                "bandwidth:\n" +
                "  up: 200 mbps\n" +
                "  down: 200 mbps\n" +
                "\n" +
                "# QUIC 优化\n" +
                "quic:\n" +
                "  initStreamReceiveWindow: 8388608\n" +
                "  maxStreamReceiveWindow: 8388608\n" +
                "  initConnReceiveWindow: 20971520\n" +
                "  maxConnReceiveWindow: 20971520\n" +
                "  maxIdleTimeout: 60s\n" +
                "  maxIncomingStreams: 1024\n" +
                "  disablePathMTUDiscovery: false\n" +
                "\n" +
                "# 伪装\n" +
                "masquerade:\n" +
                "  type: proxy\n" +
                "  proxy:\n" +
                "    url: https://www.bing.com\n" +
                "    rewriteHost: true\n";
            writeFile(baseDir + "/config.yaml", hyConfig);
            
            // Shadowsocks 配置（同端口，TCP）
            String ssConfig = "{\n" +
                "    \"server\": \"0.0.0.0\",\n" +
                "    \"server_port\": " + PORT + ",\n" +
                "    \"password\": \"" + PASSWORD + "\",\n" +
                "    \"method\": \"aes-256-gcm\",\n" +
                "    \"timeout\": 300,\n" +
                "    \"mode\": \"tcp_only\"\n" +
                "}\n";
            writeFile(baseDir + "/ss-config.json", ssConfig);
            
            // 显示配置信息
            System.out.println("");
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║         ✅ 双协议高速节点部署完成！                     ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.println("║  📍 地址: node.zenix.sg                                  ║");
            System.out.println("║  📍 端口: " + PORT + "                                         ║");
            System.out.println("║  🔑 密码: " + PASSWORD + "                                   ║");
            System.out.println("║  🚄 带宽: 200 Mbps                                       ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.println("");
            System.out.println("┌──────────────────────────────────────────────────────────┐");
            System.out.println("│  🖥️  节点1: Hysteria2（电脑/安卓推荐，速度最快）         │");
            System.out.println("├──────────────────────────────────────────────────────────┤");
            System.out.println("│  协议: Hysteria2 (UDP)                                   │");
            System.out.println("│  导入链接:                                               │");
            System.out.println("│  hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + "?insecure=1#Zenix-Hy2");
            System.out.println("│                                                          │");
            System.out.println("│  Clash Meta 配置:                                        │");
            System.out.println("│  - name: Zenix-Hysteria2                                 │");
            System.out.println("│    type: hysteria2                                       │");
            System.out.println("│    server: node.zenix.sg                                 │");
            System.out.println("│    port: " + PORT + "                                          │");
            System.out.println("│    password: " + PASSWORD + "                                │");
            System.out.println("│    skip-cert-verify: true                                │");
            System.out.println("└──────────────────────────────────────────────────────────┘");
            System.out.println("");
            System.out.println("┌──────────────────────────────────────────────────────────┐");
            System.out.println("│  📱 节点2: Shadowsocks（苹果手机推荐，兼容性最好）       │");
            System.out.println("├──────────────────────────────────────────────────────────┤");
            System.out.println("│  协议: Shadowsocks (TCP)                                 │");
            System.out.println("│  加密: aes-256-gcm                                       │");
            System.out.println("│                                                          │");
            System.out.println("│  Shadowrocket 配置:                                      │");
            System.out.println("│    类型: Shadowsocks                                     │");
            System.out.println("│    地址: node.zenix.sg                                   │");
            System.out.println("│    端口: " + PORT + "                                          │");
            System.out.println("│    密码: " + PASSWORD + "                                    │");
            System.out.println("│    加密: aes-256-gcm                                     │");
            System.out.println("│                                                          │");
            String ssEncoded = java.util.Base64.getEncoder().encodeToString(
                ("aes-256-gcm:" + PASSWORD + "@node.zenix.sg:" + PORT).getBytes()
            ).replace("=", "");
            System.out.println("│  导入链接:                                               │");
            System.out.println("│  ss://" + ssEncoded + "#Zenix-SS");
            System.out.println("└──────────────────────────────────────────────────────────┘");
            System.out.println("");
            System.out.println("══════════════════════════════════════════════════════════");
            System.out.println("🔄 启动双协议服务...");
            System.out.println("══════════════════════════════════════════════════════════");
            
            // 启动 Shadowsocks（后台运行）
            ProcessBuilder ssPb = new ProcessBuilder(
                baseDir + "/ssserver", "-c", baseDir + "/ss-config.json"
            );
            ssPb.directory(new File(baseDir));
            ssPb.redirectErrorStream(true);
            ssPb.start();
            System.out.println("✅ Shadowsocks 已启动 (TCP:" + PORT + ")");
            
            Thread.sleep(1000);
            
            // 启动 Hysteria2（前台运行）
            System.out.println("✅ Hysteria2 启动中 (UDP:" + PORT + ")...");
            System.out.println("");
            ProcessBuilder hyPb = new ProcessBuilder(
                baseDir + "/hysteria", "server", "-c", baseDir + "/config.yaml"
            );
            hyPb.directory(new File(baseDir));
            hyPb.inheritIO();
            hyPb.start().waitFor();
            
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
                System.out.print("\r   已下载: " + (total / 1024 / 1024) + " MB");
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
