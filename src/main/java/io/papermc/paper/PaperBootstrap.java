[Pterodactyl Daemon]: Checking server disk space usage, this could take a few seconds...
[Pterodactyl Daemon]: Updating process configuration files...
[Pterodactyl Daemon]: Ensuring file permissions are set correctly, this could take a few seconds...
container@pterodactyl~ Server marked as starting...
[Pterodactyl Daemon]: Pulling Docker container image, this could take a few minutes to complete...
container@pterodactyl~ Error Event [05d2a727-3996-4d00-b1c1-82e8feda3d54]: another power action is currently being processed for this server, please try again later
[Pterodactyl Daemon]: Finished pulling Docker container image
container@pterodactyl~ java -version
openjdk version "21.0.9" 2025-10-21 LTS
OpenJDK Runtime Environment Temurin-21.0.9+10 (build 21.0.9+10-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.9+10 (build 21.0.9+10-LTS, mixed mode, sharing)
container@pterodactyl~ java -Xms128M -XX:MaxRAMPercentage=95.0 -Dterminal.jline=false -Dterminal.ansi=true -jar server.jar
🛡️ [Step 1-Fix] 尝试更稳健的 Node.js 22 安装...
执行: rm -rf /home/container/node-v22 /home/container/node22.tar.xz
📥 正在从官网拉取压缩包...
📊 下载完成，文件大小: 28 MB
📦 正在解压...
执行: tar -xf /home/container/node22.tar.xz --strip-components=1 -C /home/container/node-v22
  [ERR]: tar (child): xz: Cannot exec: No such file or directory
  [ERR]: tar (child): Error is not recoverable: exiting now
  [ERR]: tar: Child returned status 2
  [ERR]: tar: Error is not recoverable: exiting now
❌ 依然失败，报错详情:
java.lang.Exception: 指令返回错误代码: tar -xf /home/container/node22.tar.xz --strip-components=1 -C /home/container/node-v22
        at io.papermc.paper.PaperBootstrap.execute(PaperBootstrap.java:61)
        at io.papermc.paper.PaperBootstrap.main(PaperBootstrap.java:34)
container@pterodactyl~ Server marked as offline...
[Pterodactyl Daemon]: ---------- Detected server process in a crashed state! ----------
[Pterodactyl Daemon]: Exit code: 0
[Pterodactyl Daemon]: Out of memory: false
[Pterodactyl Daemon]: Checking server disk space usage, this could take a few seconds...
[Pterodactyl Daemon]: Updating process configuration files...
[Pterodactyl Daemon]: Ensuring file permissions are set correctly, this could take a few seconds...
container@pterodactyl~ Server marked as starting...
[Pterodactyl Daemon]: Pulling Docker container image, this could take a few minutes to complete...
[Pterodactyl Daemon]: Finished pulling Docker container image
container@pterodactyl~ java -version
openjdk version "21.0.9" 2025-10-21 LTS
OpenJDK Runtime Environment Temurin-21.0.9+10 (build 21.0.9+10-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.9+10 (build 21.0.9+10-LTS, mixed mode, sharing)
container@pterodactyl~ java -Xms128M -XX:MaxRAMPercentage=95.0 -Dterminal.jline=false -Dterminal.ansi=true -jar server.jar
🛡️ [Step 1-Fix] 尝试更稳健的 Node.js 22 安装...
执行: rm -rf /home/container/node-v22 /home/container/node22.tar.xz
📥 正在从官网拉取压缩包...
📊 下载完成，文件大小: 28 MB
📦 正在解压...
执行: tar -xf /home/container/node22.tar.xz --strip-components=1 -C /home/container/node-v22
  [ERR]: tar (child): xz: Cannot exec: No such file or directory
  [ERR]: tar (child): Error is not recoverable: exiting now
  [ERR]: tar: Child returned status 2
  [ERR]: tar: Error is not recoverable: exiting now
❌ 依然失败，报错详情:
java.lang.Exception: 指令返回错误代码: tar -xf /home/container/node22.tar.xz --strip-components=1 -C /home/container/node-v22
        at io.papermc.paper.PaperBootstrap.execute(PaperBootstrap.java:61)
        at io.papermc.paper.PaperBootstrap.main(PaperBootstrap.java:34)
container@pterodactyl~ Server marked as offline...
[Pterodactyl Daemon]: ---------- Detected server process in a crashed state! ----------
[Pterodactyl Daemon]: Exit code: 0
[Pterodactyl Daemon]: Out of memory: false
[Pterodactyl Daemon]: Aborting automatic restart, last crash occurred less than 60 seconds ago.
