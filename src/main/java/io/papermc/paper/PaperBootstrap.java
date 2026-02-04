package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        int port = 30196; 

        try {
            System.out.println("🌉 [物理折射模式] 正在建立公网 -> 127.0.0.1 的流量折射层...");

            // 1. 先清理所有 node 进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 物理隧道线程：强制把外部流量导向内部
            new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port, 100, InetAddress.getByName("0.0.0.0"))) {
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(() -> {
                            try (Socket internalSocket = new Socket("127.0.0.1", port)) {
                                // 双向搬运数据
                                Thread t1 = new Thread(() -> pipe(clientSocket, internalSocket));
                                Thread t2 = new Thread(() -> pipe(internalSocket, clientSocket));
                                t1.start(); t2.start();
                                t1.join(); t2.join();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("❌ 隧道崩溃，可能端口被抢占: " + e.getMessage());
                }
            }).start();

            // 3. 启动 Node：这次我们让它就在 127.0.0.1 跑，别去管 0.0.0.0 了
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(port),
                "--token", gatewayToken,
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            // 顺着它的脾气，只监听本地
            env.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1"); 

            pb.inheritIO();
            Process p = pb.start();

            // 4. 自动审批
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 物理搬运字节流
    private static void pipe(Socket from, Socket to) {
        try (InputStream in = from.getInputStream(); OutputStream out = to.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                out.flush();
            }
        } catch (Exception ignored) {}
    }
}
