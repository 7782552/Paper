package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        
        int publicPort = 30196;   // 外部进来的门
        int internalPort = 18789; // Node 躲在后面的门

        try {
            System.out.println("🌉 [物理折射 2.0] 修正端口冲突：30196 -> 18789");

            // 1. 物理清场
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 流量搬运线程 (把 30196 的流量转给 18789)
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
                    System.err.println("❌ 外部端口 30196 监听失败: " + e.getMessage());
                }
            }).start();

            // 3. 启动 Node：监听 internalPort (18789)
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
            env.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1"); // 锁死本地，不抢公网端口

            pb.inheritIO();
            Process p = pb.start();

            // 4. 暴力审批
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
