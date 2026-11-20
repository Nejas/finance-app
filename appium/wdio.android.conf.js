const path = require("node:path");

const debugApkPath = path.resolve(
    __dirname,
    "../app/build/outputs/apk/debug/app-debug.apk"
);

exports.config = {
    runner: "local",
    specs: ["./specs/**/*.e2e.js"],
    maxInstances: 1,
    logLevel: "info",
    framework: "mocha",
    reporters: ["spec"],
    hostname: process.env.APPIUM_HOST ?? "127.0.0.1",
    port: Number(process.env.APPIUM_PORT ?? "4723"),
    connectionRetryTimeout: 120000,
    connectionRetryCount: 1,
    mochaOpts: {
        ui: "bdd",
        timeout: 120000
    },
    capabilities: [
        {
            platformName: "Android",
            "appium:automationName": "UiAutomator2",
            "appium:deviceName": process.env.ANDROID_DEVICE_NAME ?? "Android Emulator",
            "appium:app": process.env.FINANCE_APP_PATH ?? debugApkPath,
            "appium:autoGrantPermissions": true,
            "appium:noReset": false,
            "appium:fullReset": false,
            "appium:newCommandTimeout": 120
        }
    ]
};
