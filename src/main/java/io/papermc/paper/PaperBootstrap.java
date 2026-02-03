package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🕵️ [OpenClaw] 正在启动 ESM 兼容探测器...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 1. 编写探测脚本，保存为 .cjs 以支持 require，或者直接用 ESM 语法
            // 我们尝试直接读取构建后的 schema 配置文件
            String probeScript = 
                "import fs from 'fs';\n" +
                "import path from 'path';\n" +
                "import { fileURLToPath } from 'url';\n" +
                "const __dirname = path.dirname(fileURLToPath(import.meta.url));\n" +
                "async function probe() {\n" +
                "  console.log('--- START STRUCTURE PROBE ---');\n" +
                "  try {\n" +
                "    const schemaPath = 'file://' + path.join(process.cwd(), 'dist/config/schema.js');\n" +
                "    const schema = await import(schemaPath);\n" +
                "    // 打印所有的配置键位定义\n" +
                "    console.log(JSON.stringify(schema.configSchema || schema.default || schema, (key, value) => {\n" +
                "      return (typeof value === 'function') ? '[Function]' : value;\n" +
                "    }, 2));\n" +
                "  } catch (e) {\n" +
                "    console.log('Schema probe failed, trying raw config keys...');\n" +
                "    try {\n" +
                "      const configPath = 'file://' + path.join(process.cwd(), 'dist/config/config.js');\n" +
                "      const config = await import(configPath);\n" +
                "      console.log('Root Keys:', Object.keys(config.default || config));\n" +
                "    } catch (e2) { console.error('All probes failed: ' + e2.message); }\n" +
                "  }\n" +
                "  console.log('--- END STRUCTURE PROBE ---');\n" +
                "}\n" +
                "probe();";

            Files.write(Paths.get(openclawDir, "probe.js"), probeScript.getBytes());

            // 2. 执行探测
            ProcessBuilder pb = new ProcessBuilder(nodePath, "probe.js");
            pb.directory(new File(openclawDir));
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
