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
        int publicPort = 30196;   
        int internalPort = 18789; 

        System.out.println("🩺 [最终审判版] 正在执行全量代码覆盖...");

        try {
            // 1. 网络探测
            try {
                URL url = new URL("https://api.telegram.org/bot" + botToken + "/getMe");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                System.out.println("📡 阶段 1 (网络检测): " + (code == 200 ? "OK" : "FAIL") + " Code: " + code);
            } catch (Exception e) {
                System.out.println("❌ 阶段 1 (网络检测): 失败 - " + e.getMessage());
            }

            // 2. 环境清理
            System.out.println("🧹 阶段 2 (清理进程)...");
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            Files.deleteIfExists(Paths.get(baseDir + "/.openclaw/openclaw.json"));
            Files.deleteIfExists(Paths.get(baseDir + "/.openclaw/state.db"));
            new File(baseDir + "/.openclaw").mkdirs();

            // 3. 极简 JSON (只写它认可的基础字段，防止 Doctor 报错)
            System.out.println("📝 阶段 3 (注入极简配置)...");
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + internalPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"loopback\","
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{\"entries\":{}}" // 保持插件入口为空，由环境变量强行激活
                + "}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 4. Java 端口转发隧道
            new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(publicPort, 50, InetAddress.getByName("0.0.0.0"));
                    System.out.println("🌉 [隧道] 0.0.0.0:" + publicPort + " -> 127.0.0.1:" + internalPort + " 已就绪");
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
                    System.err.println("❌ 隧道崩溃: " + e.getMessage());
                }
            }).start();

            // 5. 启动 Node 进程
            System.out.println("🚀 阶段 4 (启动主程序)...");
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--port", String.valueOf(internalPort), "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");

            // --- 环境变量暴力强灌 (覆盖所有可能的变量名) ---
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_PLUGINS_TELEGRAM_ENABLED", "true"); // 强制开启插件

            pb.inheritIO();
            System.out.println("✅ 总攻开始，盯着日志里的 Listening 字样！");
            pb.start().waitFor();

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
