# WifiPlug Java Wrapper

Inspired by [WifiPlug-api](https://github.com/EddM/wifiplug-api). Thanks!

### This is my first usable Java program, so do not be rude about my code!

We got device and wanted to manage it from web server. There was problem with jruby compilation, so I made java wrapper.
If someday I will have free time, than I will port this all thing to PHP (I doubt that :D)

Please read [notes about nice documentation they have](https://github.com/EddM/wifiplug-api/blob/master/README.md) to know all problems EddM worked around.

## What does it do now

With currently built .jar you can list all your devices and switch them on and off.

### I build this with [IntelliJ IDEA](https://www.jetbrains.com/idea/)

Requires java 1.8+

## Examples

1. `java -jar out/artifacts/WifiPlug_jar/WifiPlug.jar h` to get help
2. `java -jar wifiplug <userName> <password> list`  to connect and print your device list

Syntax!
```java -jar wifiplug userName password deviceId|list onOff verbose serverName serverPort```

Params:
```
    userName               - your user name (string)
    password               - your password (string)
    deviceId               - device to switch or keyword `list` (without `) for list (string)
    onOff                  - switch on or off target device (string - case sensitive - lowercase only)
    verbose                - (optional) dump irrelevant info (integer)
    serverName             - (optional) server domain or ip address (string)
    serverPort             - (optional) server port (integer)
```
