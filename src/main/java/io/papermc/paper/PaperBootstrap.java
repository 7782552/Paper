package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class PaperBootstrap {
    static final int PORT = 30194;
    static final String USERNAME = "user";
    static final String PASSWORD = "zenix2024";
    
    public static void main(String[] args) {
        System.out.println("🚀 正在启动 SOCKS5 代理节点...");
        System.out.println("📍 地址: node.zenix.sg:" + PORT);
        System.out.println("🔑 用户名: " + USERNAME);
        System.out.println("🔑 密码: " + PASSWORD);
        System.out.println("");
        System.out.println("=== v2rayN 配置 ===");
        System.out.println("协议: socks");
        System.out.println("地址: node.zenix.sg");
        System.out.println("端口: " + PORT);
        System.out.println("用户名: " + USERNAME);
        System.out.println("密码: " + PASSWORD);
        System.out.println("");
        
        ExecutorService pool = Executors.newCachedThreadPool();
        
        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("✅ SOCKS5 代理服务器已启动，监听端口 " + PORT);
            
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
            client.setSoTimeout(60000);
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            
            // SOCKS5 握手
            int version = in.read();
            if (version != 5) { client.close(); return; }
            
            int nmethods = in.read();
            byte[] methods = new byte[nmethods];
            in.read(methods);
            
            // 要求用户名密码认证
            out.write(new byte[]{0x05, 0x02});
            out.flush();
            
            // 认证
            int authVersion = in.read();
            if (authVersion != 1) { client.close(); return; }
            
            int ulen = in.read();
            byte[] uname = new byte[ulen];
            in.read(uname);
            
            int plen = in.read();
            byte[] passwd = new byte[plen];
            in.read(passwd);
            
            String u = new String(uname);
            String p = new String(passwd);
            
            if (!u.equals(USERNAME) || !p.equals(PASSWORD)) {
                out.write(new byte[]{0x01, 0x01}); // 认证失败
                out.flush();
                client.close();
                return;
            }
            
            out.write(new byte[]{0x01, 0x00}); // 认证成功
            out.flush();
            
            // 读取请求
            int ver = in.read();
            int cmd = in.read();
            int rsv = in.read();
            int atyp = in.read();
            
            String host;
            if (atyp == 1) { // IPv4
                byte[] addr = new byte[4];
                in.read(addr);
                host = InetAddress.getByAddress(addr).getHostAddress();
            } else if (atyp == 3) { // 域名
                int len = in.read();
                byte[] addr = new byte[len];
                in.read(addr);
                host = new String(addr);
            } else if (atyp == 4) { // IPv6
                byte[] addr = new byte[16];
                in.read(addr);
                host = InetAddress.getByAddress(addr).getHostAddress();
            } else {
                client.close();
                return;
            }
            
            int port = (in.read() << 8) | in.read();
            
            if (cmd != 1) { // 只支持 CONNECT
                out.write(new byte[]{0x05, 0x07, 0x00, 0x01, 0,0,0,0, 0,0});
                client.close();
                return;
            }
            
            // 连接目标
            Socket remote;
            try {
                remote = new Socket();
                remote.connect(new InetSocketAddress(host, port), 10000);
                remote.setSoTimeout(60000);
            } catch (Exception e) {
                out.write(new byte[]{0x05, 0x04, 0x00, 0x01, 0,0,0,0, 0,0});
                client.close();
                return;
            }
            
            // 发送成功响应
            byte[] response = new byte[]{0x05, 0x00, 0x00, 0x01, 0,0,0,0, 0,0};
            out.write(response);
            out.flush();
            
            // 双向转发
            Thread t1 = new Thread(() -> {
                try { pipe(client.getInputStream(), remote.getOutputStream()); } 
                catch (Exception e) {}
                try { remote.close(); client.close(); } catch (Exception e) {}
            });
            
            Thread t2 = new Thread(() -> {
                try { pipe(remote.getInputStream(), client.getOutputStream()); } 
                catch (Exception e) {}
                try { remote.close(); client.close(); } catch (Exception e) {}
            });
            
            t1.start();
            t2.start();
            t1.join(300000);
            
        } catch (Exception e) {
        } finally {
            try { client.close(); } catch (Exception e) {}
        }
    }
    
    static void pipe(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
            out.flush();
        }
    }
}
