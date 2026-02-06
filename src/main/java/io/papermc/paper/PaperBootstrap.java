package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class PaperBootstrap {
    // ================= 配置参数 =================
    private static final String PORT = "30194"; 
    private static final String UUID = "16202dac-ec89-49bd-92aa-0b537d9ac66c";
    private static final String DEST = "www.microsoft.com:443"; // 落地伪装域名
    private static final String SNI = "www.microsoft.com";
    // Reality 密钥对 (可以使用你日志里固定的，这里示例一对)
    private static final String PRIVATE_KEY = "uOf7O0z3...你的私钥..."; 
    private static final String PUBLIC_KEY = "Hnx5iiA5nEykaXEwBZZLuH7fQC7ydz2fRztLwGrM3F0";
    // ============================================

    public static void main(String[] args) {
        System.out.println("🛠️ 正在初始化 VLESS Reality 高速节点...");

        try {
            // 1. 下载 sing-box 二进制文件 (如果不存在)
            File exe = new File("sing-box");
            if (!exe.exists()) {
                System.out.println("⬇️ 正在下载 sing-box 内核...");
                // 这里建议预先手动上传 sing-box 文件到根目录，或者使用 Java 下载代码
            }

            // 2. 动态生成 config.json
            generateConfig();

            // 3. 启动节点进程
            System.out.println("🚀 正在启动 sing-box 核心进程...");
            ProcessBuilder pb = new ProcessBuilder("./sing-box", "run", "-c", "config.json");
            pb.inheritIO();
            Process process = pb.start();

            // 4. 防止 Java 退出导致容器关闭
            System.out.println("\n✅ 节点已启动！端口: " + PORT);
            System.out.println("🔗 链接: vless://" + UUID + "@113.22.166.76:" + PORT + "?encryption=none&flow=xtls-rprx-vision&security=reality&sni=" + SNI + "&fp=chrome&pbk=" + PUBLIC_KEY + "#Zenix-HighSpeed");
            
            process.waitFor(); // 只要 sing-box 不挂，Java 就一直运行
        } catch (Exception e) {
            System.err.println("❌ 启动失败: " + e.getMessage());
        }
    }

    private static void generateConfig() throws IOException {
        String config = "{\n" +
                "  \"inbounds\": [{\n" +
                "    \"type\": \"vless\",\n" +
                "    \"tag\": \"vless-in\",\n" +
                "    \"listen\": \"::\",\n" +
                "    \"listen_port\": " + PORT + ",\n" +
                "    \"users\": [{\"uuid\": \"" + UUID + "\", \"flow\": \"xtls-rprx-vision\"}],\n" +
                "    \"tls\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"server_name\": \"" + SNI + "\",\n" +
                "      \"reality\": {\n" +
                "        \"enabled\": true,\n" +
                "        \"handshake\": {\"server\": \"" + SNI + "\", \"server_port\": 443},\n" +
                "        \"private_key\": \"" + PRIVATE_KEY + "\",\n" +
                "        \"short_id\": [\"16\", \"a1b2c3d4\"]\n" +
                "      }\n" +
                "    }\n" +
                "  }],\n" +
                "  \"outbounds\": [{\"type\": \"direct\", \"tag\": \"direct\"}]\n" +
                "}";
        Files.write(Paths.get("config.json"), config.getBytes());
        System.out.println("📝 config.json 已成功生成。");
    }
}
