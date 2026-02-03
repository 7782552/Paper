package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🕵️ [OpenClaw] 正在启动内部结构探测器，请记录下方打印的内容...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 创建探测脚本：直接读取 OpenClaw 的配置文件定义
            String probeScript = 
                "const fs = require('fs');\n" +
                "const path = require('path');\n" +
                "try {\n" +
                "  // 尝试寻找配置文件校验定义文件\n" +
                "  const configPath = path.join(process.cwd(), 'dist/config/config.js');\n" +
                "  const schemaPath = path.join(process.cwd(), 'dist/config/schema.js');\n" +
                "  console.log('--- START STRUCTURE PROBE ---');\n" +
                "  if (fs.existsSync(schemaPath)) {\n" +
                "    const schema = require(schemaPath);\n" +
                "    console.log(JSON.stringify(schema, null, 2));\n" +
                "  } else {\n" +
                "    const config = require(configPath);\n" +
                "    console.log('Object Keys:', Object.keys(config));\n" +
                "  }\n" +
                "  console.log('--- END STRUCTURE PROBE ---');\n" +
                "} catch (e) {\n" +
                "  console.error('Probe failed: ' + e.message);\n" +
                "}";

            Files.write(Paths.get(openclawDir, "probe.js"), probeScript.getBytes());

            // 执行探测
            ProcessBuilder pb = new ProcessBuilder(nodePath, "probe.js");
            pb.directory(new File(openclawDir));
            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();

            System.out.println("\n💡 请根据上方打印的结构告诉我是什么，或者直接把那段输出发给我。");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
