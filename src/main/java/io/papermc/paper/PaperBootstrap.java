package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 增强版容器网络环境深度测试...\n");

        // 1. 测试外部连通性 (使用 curl)
        String[] curlTests = {
            "curl -s -m 5 https://www.google.com -o /dev/null -w '%{http_code}'",
            "curl -s -m 5 https://api.ipify.org",
            "curl -s -m 5 https://www.youtube.com -o /dev/null -w '%{http_code}'"
        };
        String[] names = {"Google", "获取出口IP", "YouTube"};

        for (int i = 0; i < curlTests.length; i++) {
            System.out.print("🔍 测试 " + names[i] + ": ");
            executeCmd(curlTests[i]);
        }

        // 2. 使用 Java 原生方法测试 DNS 和 Ping (解决 ping: not found 问题)
        System.out.println("\n📡 测试 Java 原生网络连接 (不依赖系统 ping)...");
        try {
            String host = "8.8.8.8";
            InetAddress address = InetAddress.getByName(host);
            System.out.println("✅ DNS 解析成功: " + address.getHostAddress());
            boolean reachable = address.isReachable(3000); // 尝试原生检测连通性
            System.out.println("📶 " + host + " 可达性测试: " + (reachable ? "成功" : "失败 (受限)"));
        } catch (Exception e) {
            System.out.println("❌ 原生网络测试失败: " + e.getMessage());
        }

        // 3. 【最重要】测试本地端口监听 (检查 30194 端口是否已启动)
        System.out.println("\n🏠 测试本地节点端口监听 (30194)...");
        testLocalPort(30194);

        System.out.println("\n✅ 测试流程结束，容器将保持运行 5 分钟以供观察日志...");
        try { Thread.sleep(300000); } catch (Exception e) {}
    }

    private static void executeCmd(String cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.print(line + " ");
            }
            int code = p.waitFor();
            System.out.println("[退出码: " + code + "]");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testLocalPort(int port) {
        try (Socket socket = new Socket()) {
            // 尝试连接本地端口，看是否有服务在监听
            socket.connect(new InetSocketAddress("127.0.0.1", port), 1000);
            System.out.println("✅ 端口 " + port + " 状态: 【监听中】 (服务已就绪)");
        } catch (IOException e) {
            System.out.println("❌ 端口 " + port + " 状态: 【未开放】 (sing-box 可能未启动或配置错误)");
        }
    }
}
