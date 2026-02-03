package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 核心参数区 ---
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        int publicPort = 30196;   // 面板分配的外部端口
        int internalPort = 18789; // OpenClaw 实际监听的内部端口

        System.out.println("🩺 [全线贯通版] 正在执行最后一次总攻程序...");

        try {
            // 1. 网络自检：确认服务器能不能摸到 Telegram 服务器
            System.out.println("📡 阶段 1：正在探测 Telegram 通道...");
            try {
                URL url = new URL("https://api.telegram.org/bot" + botToken + "/getMe");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                System.out.println("📬 Telegram 响应码: " + code + (code == 200 ? " (网络正常)" : " (网络受限)"));
            } catch (Exception e) {
                System.out.println("❌ 网络警告：服务器无法直接访问 Telegram API: " + e.getMessage());
            }

            // 2. 清理环境：杀死幽灵进程并删除旧配置
            System.out.println("🧹 阶段 2：正在清理旧进程与配置...");
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Files.deleteIfExists(Paths.get(baseDir + "/.openclaw/openclaw.json"));
            Files.deleteIfExists(Paths.get(baseDir + "/.openclaw/state.db"));
            new File(baseDir + "/.openclaw").mkdirs();

            // 3. 强制注入 2026.2.1 规范的 JSON
            System.out.println("📝 阶段 3：正在写入强制引导配置...");
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + internalPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"loopback\","
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{"
                    + "\"entries\":{"
                        + "\"telegram\":{"
                            + "\"enabled\":true,"
                            + "\"botToken\":\"" + botToken + "\","
                            + "\"dmPolicy\":\"open\","
                            + "\"allowFrom\":[\"*\"],"
                            + "\"session\":{\"active\":true,\"status\":\"connected\"}"
                        + "}"
                    + "}"
                + "}"
            + "}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 4. 建立 Java 隧道：30196 (外) -> 18789 (内)
            new Thread(() -> {
                try {
                    System.out.println("🌉 [隧道] 监听开启: 0.0.0.0:" + publicPort + " -> 127.0.0.1:" + internalPort);
                    ServerSocket serverSocket = new ServerSocket(publicPort, 50, InetAddress.getByName("0.0.0.0"));
                    while (true) {
                        Socket client = serverSocket.accept();
                        new Thread(() -> {
                            try (Socket target = new Socket("127.0.0.1", internalPort)) {
                                Thread t1 = new Thread(() -> pipe(client, target));
                                Thread t2 = new Thread(() -> pipe(target, client));
                                t1.start(); t2.start();
                                t1.join(); t2.join();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("❌ 隧道错误: " + e.getMessage());
                }
            }).start();

            // 5. 启动 OpenClaw
            System.out.println("🚀 阶段 4：正在点火 OpenClaw...");
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--port", String.valueOf(internalPort), "--force"
            );
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("NODE_ENV", "production");

            pb.inheritIO();
            Process p = pb.start();
            System.out.println("✅ 程序已成功挂载。请观察下方输出。");
            p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void pipe(Socket in, Socket out) {
        try {
            InputStream is = in.getInputStream();
            OutputStream os = out.getOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
                os.flush();
            }
        } catch (Exception ignored) {}
    }
}
