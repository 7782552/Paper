package io.papermc.paper;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class PaperBootstrap {
    public static void main(String[] args) {
        String n8nUrl = "http://你的N8N地址:5678/healthz"; // 替换成你N8N的实际地址
        int targetPort = 30196;

        System.out.println("🔍 [系统体检] 正在检查公网环境...");

        // 1. 检查 30196 端口是否被占用 (看看是不是旧进程没杀干净)
        try (ServerSocket socket = new ServerSocket(targetPort, 0, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("✅ 端口 " + targetPort + " 处于空闲状态，可以绑定。");
        } catch (IOException e) {
            System.err.println("❌ 端口 " + targetPort + " 仍被占用！请确认已执行 pkill -9 node。");
        }

        // 2. 检查 N8N 是否在线
        System.out.println("📡 正在尝试连接 N8N...");
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(n8nUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            int code = connection.getResponseCode();
            System.out.println("✅ N8N 响应正常，状态码: " + code);
        } catch (Exception e) {
            System.err.println("❌ 无法连接到 N8N: " + e.getMessage());
        }

        // 3. 检查 DNS 解析
        try {
            InetAddress address = InetAddress.getByName("node.zenix.sg");
            System.out.println("🌍 域名 node.zenix.sg 解析结果: " + address.getHostAddress());
        } catch (UnknownHostException e) {
            System.err.println("❌ 域名解析失败。");
        }

        System.out.println("\n💡 [诊断建议]: 如果端口空闲但你依然打不开网页，说明翼龙面板所在服务器的防火墙拦截了 " + targetPort + "。");
    }
}
