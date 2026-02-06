package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 正在启动纯 Java 环境网络连通性分析...\n");

        // 1. 获取公网 IP (不依赖 curl)
        System.out.print("🔍 [1/4] 正在获取公网出口 IP: ");
        String publicIp = fetchUrlContent("https://api.ipify.org");
        System.out.println(publicIp != null ? publicIp : "获取失败");

        // 2. 测试国外站点连通性 (不依赖 curl)
        testHttp("Google", "https://www.google.com");
        testHttp("YouTube", "https://www.youtube.com");

        // 3. 使用 Java 原生方法模拟 Ping (不依赖 ping 命令)
        System.out.print("📡 [3/4] 正在测试 8.8.8.8 的可达性 (Java Reachable): ");
        try {
            InetAddress address = InetAddress.getByName("8.8.8.8");
            boolean reachable = address.isReachable(3000); // 3秒超时
            System.out.println(reachable ? "✅ 成功" : "❌ 失败 (可能受 ICMP 限制)");
        } catch (Exception e) {
            System.out.println("❌ 错误: " + e.getMessage());
        }

        // 4. 【核心检测】检测节点端口 30194 是否已在本地开启
        System.out.println("\n🏠 [4/4] 正在检测本地节点监听状态 (Port 30194)...");
        checkLocalPort(30194);

        System.out.println("\n✅ 所有测试已完成，容器将保持运行 60 秒供查看日志。");
        try { Thread.sleep(60000); } catch (InterruptedException e) { }
    }

    private static void testHttp(String name, String urlStr) {
        System.out.print("🌐 [2/4] 测试 " + name + " 连通性: ");
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            System.out.println("✅ 成功 (HTTP " + code + ")");
        } catch (Exception e) {
            System.out.println("❌ 失败: " + e.getMessage());
        }
    }

    private static String fetchUrlContent(String urlStr) {
        try {
            URL url = new URL(urlStr);
            try (Scanner s = new Scanner(url.openStream())) {
                return s.useDelimiter("\\A").hasNext() ? s.next() : null;
            }
        } catch (Exception e) { return null; }
    }

    private static void checkLocalPort(int port) {
        // 尝试作为客户端连接该端口
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            System.out.println("   >>> 🟢 结果: 30194 端口【有服务正在监听】。");
            System.out.println("   >>> 建议: 如果此处显示正常但手机连不上，请检查翼龙面板 Network 里的外部端口映射。");
        } catch (IOException e) {
            System.out.println("   >>> 🔴 结果: 30194 端口【未检测到监听】。");
            System.out.println("   >>> 原因: 你的节点程序 (sing-box) 可能崩溃了，或者根本没启动。");
        }
    }
}
