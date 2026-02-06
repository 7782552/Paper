package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 正在启动代理节点...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            
            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);

            // 1. 创建代理服务器脚本
            System.out.println("📝 创建代理服务器...");
            String proxyScript = 
                "const http = require('http');\n" +
                "const https = require('https');\n" +
                "const net = require('net');\n" +
                "const url = require('url');\n" +
                "\n" +
                "const PORT = 30194;\n" +
                "const PASSWORD = 'zenix2024';\n" +
                "\n" +
                "const server = http.createServer((req, res) => {\n" +
                "  // 验证密码\n" +
                "  const auth = req.headers['proxy-authorization'];\n" +
                "  if (!auth || !auth.includes(Buffer.from('user:' + PASSWORD).toString('base64'))) {\n" +
                "    res.writeHead(407, { 'Proxy-Authenticate': 'Basic realm=\"Proxy\"' });\n" +
                "    res.end('Proxy Authentication Required');\n" +
                "    return;\n" +
                "  }\n" +
                "\n" +
                "  const targetUrl = url.parse(req.url);\n" +
                "  const options = {\n" +
                "    hostname: targetUrl.hostname,\n" +
                "    port: targetUrl.port || 80,\n" +
                "    path: targetUrl.path,\n" +
                "    method: req.method,\n" +
                "    headers: req.headers\n" +
                "  };\n" +
                "  delete options.headers['proxy-authorization'];\n" +
                "\n" +
                "  const proxyReq = http.request(options, (proxyRes) => {\n" +
                "    res.writeHead(proxyRes.statusCode, proxyRes.headers);\n" +
                "    proxyRes.pipe(res);\n" +
                "  });\n" +
                "\n" +
                "  proxyReq.on('error', (e) => {\n" +
                "    res.writeHead(500);\n" +
                "    res.end('Proxy Error: ' + e.message);\n" +
                "  });\n" +
                "\n" +
                "  req.pipe(proxyReq);\n" +
                "});\n" +
                "\n" +
                "// HTTPS CONNECT 隧道\n" +
                "server.on('connect', (req, clientSocket, head) => {\n" +
                "  const auth = req.headers['proxy-authorization'];\n" +
                "  if (!auth || !auth.includes(Buffer.from('user:' + PASSWORD).toString('base64'))) {\n" +
                "    clientSocket.write('HTTP/1.1 407 Proxy Authentication Required\\r\\n');\n" +
                "    clientSocket.write('Proxy-Authenticate: Basic realm=\"Proxy\"\\r\\n\\r\\n');\n" +
                "    clientSocket.end();\n" +
                "    return;\n" +
                "  }\n" +
                "\n" +
                "  const [hostname, port] = req.url.split(':');\n" +
                "  const serverSocket = net.connect(port || 443, hostname, () => {\n" +
                "    clientSocket.write('HTTP/1.1 200 Connection Established\\r\\n\\r\\n');\n" +
                "    serverSocket.write(head);\n" +
                "    serverSocket.pipe(clientSocket);\n" +
                "    clientSocket.pipe(serverSocket);\n" +
                "  });\n" +
                "\n" +
                "  serverSocket.on('error', (e) => {\n" +
                "    clientSocket.end();\n" +
                "  });\n" +
                "\n" +
                "  clientSocket.on('error', (e) => {\n" +
                "    serverSocket.end();\n" +
                "  });\n" +
                "});\n" +
                "\n" +
                "server.listen(PORT, '0.0.0.0', () => {\n" +
                "  console.log('✅ 代理节点已启动');\n" +
                "  console.log('📍 地址: node.zenix.sg:' + PORT);\n" +
                "  console.log('🔑 用户名: user');\n" +
                "  console.log('🔑 密码: ' + PASSWORD);\n" +
                "  console.log('');\n" +
                "  console.log('=== 使用方法 ===');\n" +
                "  console.log('HTTP代理: http://user:' + PASSWORD + '@node.zenix.sg:' + PORT);\n" +
                "  console.log('');\n" +
                "  console.log('=== Clash 配置 ===');\n" +
                "  console.log('- name: Zenix-Node');\n" +
                "  console.log('  type: http');\n" +
                "  console.log('  server: node.zenix.sg');\n" +
                "  console.log('  port: ' + PORT);\n" +
                "  console.log('  username: user');\n" +
                "  console.log('  password: ' + PASSWORD);\n" +
                "});\n";

            // 写入脚本文件
            File proxyFile = new File(baseDir + "/proxy.js");
            java.nio.file.Files.write(proxyFile.toPath(), proxyScript.getBytes());

            // 2. 启动代理服务器
            System.out.println("🚀 启动代理服务器...");
            ProcessBuilder proxyPb = new ProcessBuilder(nodeBin, proxyFile.getAbsolutePath());
            proxyPb.environment().putAll(env);
            proxyPb.inheritIO();
            proxyPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
