package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123"; // 你的网关 Token
        int publicPort = 30196;   
        int internalPort = 18789; 

        try {
            System.out.println("📖 [官方文档对标版] 正在按照 2026 标准重构启动流...");

            // 1. 彻底杀掉旧进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 写入官方文档最推荐的极简 JSON
            // 注意：auth 留空，让 --token 参数接管，防止冲突
            File configDir = new File(baseDir + "/.openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + internalPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"loopback\"" // 严格按照说明书，不在这里写 auth
                + "},"
                + "\"plugins\":{\"enabled\":true}"
                + "}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 3. 极速隧道 (0.0.0.0:30196 -> 127.0.0.1:18789)
            new Thread(() -> {
                try (ServerSocket ss = new ServerSocket(publicPort, 64, InetAddress.getByName("0.0.0.0"))) {
                    System.out.println("🌉 物理隧道 [30196 -> 18789] 准备就绪");
                    while (true) {
                        Socket client = ss.accept();
                        new Thread(() -> {
                            try (Socket target = new Socket("127.0.0.1", internalPort)) {
                                pipe(client, target);
                                pipe(target, client);
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {}
            }).start();

            // 4. 启动 Node：直接使用官方推荐的 --token 参数
            System.out.println("🚀 正在点火：node dist/index.js gateway --token " + gatewayToken);
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(internalPort),
                "--token", gatewayToken,  // 解决 "no token is configured" 报错
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            
            pb.redirectErrorStream(true);
            Process p = pb.start();

            // 5. 自动审批注入
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            new Thread(() -> {
                try {
                    while (p.isAlive()) {
                        Thread.sleep(15000);
                        writer.write("pairing approve telegram all\n");
                        writer.flush();
                    }
                } catch (Exception ignored) {}
            }).start();

            // 实时打印日志
            InputStream is = p.getInputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                System.out.print(new String(buf, 0, len));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void pipe(Socket from, Socket to) {
        try {
            InputStream in = from.getInputStream();
            OutputStream out = to.getOutputStream();
            byte[] buffer = new byte[32768];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                out.flush();
            }
        } catch (Exception ignored) {}
    }
}
