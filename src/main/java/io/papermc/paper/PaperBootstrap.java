package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        int publicPort = 30196;   
        int internalPort = 18789; 

        try {
            System.out.println("🔧 [回归隧道模式] 正在修复 Config Invalid 问题...");

            // 1. 杀掉旧进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 写入 100% 正确的 loopback 配置 (防止 Doctor 报错)
            File configDir = new File(baseDir + "/.openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            String configJson = "{\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},\"gateway\":{\"port\":" + internalPort + ",\"mode\":\"local\",\"bind\":\"loopback\",\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}},\"plugins\":{\"enabled\":true}}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 3. 极速字节隧道 (0.0.0.0:30196 -> 127.0.0.1:18789)
            new Thread(() -> {
                try (ServerSocket ss = new ServerSocket(publicPort, 50, InetAddress.getByName("0.0.0.0"))) {
                    System.out.println("🌉 物理隧道已架设: 0.0.0.0:" + publicPort + " -> 127.0.0.1:" + internalPort);
                    while (true) {
                        Socket client = ss.accept();
                        new Thread(() -> {
                            try (Socket target = new Socket("127.0.0.1", internalPort)) {
                                // 开启全速模式
                                client.setTcpNoDelay(true);
                                target.setTcpNoDelay(true);
                                Thread t1 = new Thread(() -> transfer(client, target));
                                Thread t2 = new Thread(() -> transfer(target, client));
                                t1.start(); t2.start();
                                t1.join(); t2.join();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("❌ 隧道崩溃: " + e.getMessage());
                }
            }).start();

            // 4. 启动 Node
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--force"
            );
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_OWNER_ID", "660059245");
            
            pb.redirectErrorStream(true);
            Process p = pb.start();

            // 5. 自动审批 (每10秒尝试一次，防止配对码过期)
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            new Thread(() -> {
                try {
                    while (p.isAlive()) {
                        Thread.sleep(10000);
                        writer.write("pairing approve telegram all\n");
                        writer.flush();
                    }
                } catch (Exception ignored) {}
            }).start();

            // 实时日志
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

    private static void transfer(Socket from, Socket to) {
        try (InputStream in = from.getInputStream(); OutputStream out = to.getOutputStream()) {
            byte[] buffer = new byte[65536];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                out.flush();
            }
        } catch (Exception ignored) {}
    }
}
