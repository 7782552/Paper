package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class PaperBootstrap {
    static final int PORT = 30194;
    static final String USERNAME = "user";
    static final String PASSWORD = "zenix2024";
    
    public static void main(String[] args) {
        System.out.println("🚀 正在启动代理节点...");
        System.out.println("📍 地址: node.zenix.sg:" + PORT);
        System.out.println("🔑 用户名: " + USERNAME);
        System.out.println("🔑 密码: " + PASSWORD);
        System.out.println("");
        System.out.println("=== Clash 配置 ===");
        System.out.println("- name: Zenix-Node");
        System.out.println("  type: http");
        System.out.println("  server: node.zenix.sg");
        System.out.println("  port: " + PORT);
        System.out.println("  username: " + USERNAME);
        System.out.println("  password: " + PASSWORD);
        System.out.println("");
        
        ExecutorService pool = Executors.newCachedThreadPool();
        
        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("✅ 代理服务器已启动，监听端口 " + PORT);
            
            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handleClient(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static void handleClient(Socket client) {
        try {
            client.setSoTimeout(30000);
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            
            // 读取请求头
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String firstLine = reader.readLine();
            if (firstLine == null) {
                client.close();
                return;
            }
            
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                int idx = line.indexOf(':');
                if (idx > 0) {
                    headers.put(line.substring(0, idx).trim().toLowerCase(), 
                               line.substring(idx + 1).trim());
                }
            }
            
            // 验证密码
            String auth = headers.get("proxy-authorization");
            if (!checkAuth(auth)) {
                String response = "HTTP/1.1 407 Proxy Authentication Required\r\n" +
                                  "Proxy-Authenticate: Basic realm=\"Proxy\"\r\n" +
                                  "Content-Length: 0\r\n\r\n";
                out.write(response.getBytes());
                out.flush();
                client.close();
                return;
            }
            
            String[] parts = firstLine.split(" ");
            String method = parts[0];
            String target = parts[1];
            
            if ("CONNECT".equalsIgnoreCase(method)) {
                // HTTPS 隧道
                handleConnect(client, target, out);
            } else {
                // HTTP 代理
                handleHttp(client, method, target, headers, in, out);
            }
            
        } catch (Exception e) {
            // 静默处理
        } finally {
            try { client.close(); } catch (Exception e) {}
        }
    }
    
    static boolean checkAuth(String auth) {
        if (auth == null) return false;
        try {
            String encoded = auth.replace("Basic ", "").trim();
            String decoded = new String(Base64.getDecoder().decode(encoded));
            return decoded.equals(USERNAME + ":" + PASSWORD);
        } catch (Exception e) {
            return false;
        }
    }
    
    static void handleConnect(Socket client, String target, OutputStream clientOut) {
        try {
            String[] hp = target.split(":");
            String host = hp[0];
            int port = hp.length > 1 ? Integer.parseInt(hp[1]) : 443;
            
            Socket remote = new Socket();
            remote.connect(new InetSocketAddress(host, port), 10000);
            remote.setSoTimeout(30000);
            
            // 发送连接成功
            String response = "HTTP/1.1 200 Connection Established\r\n\r\n";
            clientOut.write(response.getBytes());
            clientOut.flush();
            
            // 双向转发
            ExecutorService executor = Executors.newFixedThreadPool(2);
            
            executor.submit(() -> {
                try {
                    pipe(client.getInputStream(), remote.getOutputStream());
                } catch (Exception e) {}
            });
            
            executor.submit(() -> {
                try {
                    pipe(remote.getInputStream(), client.getOutputStream());
                } catch (Exception e) {}
            });
            
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
            
            remote.close();
            
        } catch (Exception e) {
            try {
                String response = "HTTP/1.1 502 Bad Gateway\r\n\r\n";
                clientOut.write(response.getBytes());
            } catch (Exception ex) {}
        }
    }
    
    static void handleHttp(Socket client, String method, String target, 
                           Map<String, String> headers, InputStream clientIn, 
                           OutputStream clientOut) {
        try {
            URL url = new URL(target);
            String host = url.getHost();
            int port = url.getPort() > 0 ? url.getPort() : 80;
            
            Socket remote = new Socket();
            remote.connect(new InetSocketAddress(host, port), 10000);
            remote.setSoTimeout(30000);
            
            OutputStream remoteOut = remote.getOutputStream();
            InputStream remoteIn = remote.getInputStream();
            
            // 构建请求
            String path = url.getPath();
            if (url.getQuery() != null) path += "?" + url.getQuery();
            if (path.isEmpty()) path = "/";
            
            StringBuilder req = new StringBuilder();
            req.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");
            req.append("Host: ").append(host).append("\r\n");
            
            for (Map.Entry<String, String> h : headers.entrySet()) {
                if (!h.getKey().equals("proxy-authorization") && 
                    !h.getKey().equals("proxy-connection")) {
                    req.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
                }
            }
            req.append("\r\n");
            
            remoteOut.write(req.toString().getBytes());
            remoteOut.flush();
            
            // 转发响应
            pipe(remoteIn, clientOut);
            
            remote.close();
            
        } catch (Exception e) {
            try {
                String response = "HTTP/1.1 502 Bad Gateway\r\n\r\n";
                clientOut.write(response.getBytes());
            } catch (Exception ex) {}
        }
    }
    
    static void pipe(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
            out.flush();
        }
    }
}
