# MpvService

MPV JSON-IPC 通信Java实现，基于 [MacFJA/MpvService](https://github.com/MacFJA/MpvService)。

本项目可以让你唤起外部的MPV播放器，并与之建立连接，调用播放器内置的API以控制播放器行为、获取播放器状态等。

详情请看MPV的官方文档：https://mpv.io/manual/master/#json-ipc

## 主要功能

请先查看原项目：[MacFJA/MpvService](https://github.com/MacFJA/MpvService)

相比于原项目，本项目做了如下改进：

- 支持Windows：与MVP通信时，原项目使用NCat监听Unix套接字，兼容性不好，且不支持Win32命名管道，本项目则使用了[sbt/ipcsocket](https://github.com/sbt/ipcsocket)作为通信方案，解决了该问题；

- 更新过时的依赖；将 [fastjson](https://github.com/alibaba/fastjson) 更换为 [gson](https://github.com/google/gson)；

- 修复过时的单元测试；

- 将项目依赖上传maven中央仓库，便于开发者导入使用；

- 少量其他优化。

## 使用

Gradle：

```groovy
implementation("io.github.kknifer7:mpv:0.1.0")
```

Maven：

```xml
<dependency>
    <groupId>io.github.kknifer7</groupId>
    <artifactId>mpv</artifactId>
    <version>0.1.0</version>
</dependency>
```

## License

The MIT License (MIT). Please see [License File](LICENSE.md) for more information.