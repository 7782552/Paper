package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class PaperBootstrap {
    private static final Path UUID_FILE = Paths.get("data/uuid.txt");
    private static String uuid;
    private static Process singboxProcess;

    public static void main(String[] args) {
        try {
            System.out.println("🚀 节点自启动环境部署中...");
            
            // 1. 加载配置
            Map<String, String> config = simpleYamlParser(Paths.get("config.yml"));
            uuid = generateOrLoadUUID(config.get("uuid"));
            String realityPort = config.getOrDefault("reality_port", "");
            String sni = config.getOrDefault("sni", "www.bing.com");

            if (realityPort.isEmpty()) {
                System.err.println("❌ 错误: 请在 config.yml 中设置 reality_port");
                return;
            }

            // 2. 准备工作目录
            Path baseDir = Paths.get("data/singbox_runtime");
            Files.createDirectories(baseDir);
            Path bin = baseDir.resolve("sing-box");
            Path configJson = baseDir.resolve("config.json");

            // 3. 下载内核 (amd64 架构)
            downloadSingBox("1.10.1", bin, baseDir);

            // 4. 生成 Reality 密钥对
            Map<String, String> keys = generateRealityKeypair(bin);
            String privateKey = keys.get("private_key");
            String publicKey = keys.get("public_key");

            // 5. 生成 sing-box 配置文件
            String jsonConfig = """
                {
                  "log": { "level": "info" },
                  "inbounds": [{
                    "type": "vless",
                    "listen": "::",
                    "listen_port": %s,
                    "users": [{"uuid": "%s", "flow": "xtls-rprx-vision"}],
                    "tls": {
                      "enabled": true,
                      "server_name": "%s",
                      "reality": {
                        "enabled": true,
                        "handshake": {"server": "%s", "server_port": 443},
                        "private_key": "%s",
                        "short_id": [""]
                      }
                    }
                  }],
                  "outbounds": [{"type": "direct"}]
                }
                """.formatted(realityPort, uuid, sni, sni, privateKey);
            Files.writeString(configJson, jsonConfig);

            // 6. 运行
            System.out.println("⚡ 正在启动 sing-box 内核...");
            singboxProcess = new ProcessBuilder(bin.toString(), "run", "-c", configJson.toString()).inheritIO().start();

            // 7. 输出节点链接
            String ip = detectPublicIP();
            System.out.println("\n=== ✅ 部署成功 ===");
            System.out.printf("VLESS Reality 链接:\nvless://%s@%s:%s?encryption=none&flow=xtls-rprx-vision&security=reality&sni=%s&fp=chrome&pbk=%s#Falix_Reality\n",
                    uuid, ip, realityPort, sni, publicKey);

            singboxProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadSingBox(String version, Path bin, Path dir) throws Exception {
        if (Files.exists(bin)) return;
        String url = "https://github.com/SagerNet/sing-box/releases/download/v" + version + "/sing-box-" + version + "-linux-amd64.tar.gz";
        System.out.println("⬇️ 下载内核中...");
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, dir.resolve("sb.tar.gz"), StandardCopyOption.REPLACE_EXISTING);
        }
        new ProcessBuilder("tar", "-xzf", dir.resolve("sb.tar.gz").toString(), "-C", dir.toString(), "--strip-components=1").start().waitFor();
        bin.toFile().setExecutable(true);
    }

    private static Map<String, String> generateRealityKeypair(Path bin) throws Exception {
        Process p = new ProcessBuilder(bin.toString(), "generate", "reality-keypair").start();
        String out = new String(p.getInputStream().readAllBytes());
        Map<String, String> map = new HashMap<>();
        Matcher m1 = Pattern.compile("PrivateKey: (\\S+)").matcher(out);
        Matcher m2 = Pattern.compile("PublicKey: (\\S+)").matcher(out);
        if (m1.find()) map.put("private_key", m1.group(1));
        if (m2.find()) map.put("public_key", m2.group(1));
        return map;
    }

    private static String detectPublicIP() {
        try (Scanner s = new Scanner(new URL("https://api.ipify.org").openStream())) { return s.next(); }
        catch (Exception e) { return "127.0.0.1"; }
    }

    private static Map<String, String> simpleYamlParser(Path path) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (!Files.exists(path)) return map;
        for (String line : Files.readAllLines(path)) {
            if (line.contains(":") && !line.trim().startsWith("#")) {
                String[] p = line.split(":", 2);
                map.put(p[0].trim(), p[1].trim().replace("\"", ""));
            }
        }
        return map;
    }

    private static String generateOrLoadUUID(String cfg) throws IOException {
        if (cfg != null && !cfg.isEmpty()) return cfg;
        if (Files.exists(UUID_FILE)) return Files.readString(UUID_FILE);
        String u = UUID.randomUUID().toString();
        Files.createDirectories(UUID_FILE.getParent());
        Files.writeString(UUID_FILE, u);
        return u;
    }
}
