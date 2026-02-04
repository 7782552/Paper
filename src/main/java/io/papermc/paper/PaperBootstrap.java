package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 核心配置区 ---
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        
        String publicIp = "103.213.254.12"; // 你的服务器真实IP
        int publicPort = 30196;            // 面板分配的公网端口
        int internalPort = 18789;          // 内部通讯端口（避开冲突）

        try {
            System.out.println("🔥 [最终冲刺] 正在建立 30196 -> 18789 物理隧道...");

            // 1. 清理环境
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 物理隧道线程 (处理流量搬运)
            new Thread(() -> {
                try (ServerSocket ss = new ServerSocket(publicPort, 128, InetAddress.getByName("0.0.0.0"))) {
                    while (true) {
                        Socket client = ss.accept();
                        new Thread(() -> {
                            try (Socket node = new Socket("127.0.0.1", internalPort)) {
                                Thread t1 = new Thread(() -> pipe(client, node));
                                Thread t2 = new Thread(() -> pipe(node, client));
                                t1.start(); t2.start();
                                t1.join(); t2.join();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("❌ 端口 30196 被占用或权限不足");
                }
            }).start();

            // 3. 启动 OpenClaw 后端
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(internalPort),
                "--token", gatewayToken,
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            
            // 🚨 修正路径映射，解决 503 和 Bridge 丢失
            env.put("OPENCLAW_WS_URL", "ws://" + publicIp + ":" + publicPort + "/__openclaw__/ws");
            env.put("OPENCLAW_PUBLIC_URL", "http://" + publicIp + ":" + publicPort + "/__openclaw__/canvas/");

            pb.inheritIO();
            Process p = pb.start();

            // 4. 自动审批 pairing
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

            p.waitFor();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void pipe(Socket f, Socket t) {
        try (InputStream is = f.getInputStream(); OutputStream os = t.getOutputStream()) {
            byte[] b = new byte[32768];
            int l;
            while ((l = is.read(b)) != -1) { os.write(b, 0, l); os.flush(); }
        } catch (Exception ignored) {}
    }
}
