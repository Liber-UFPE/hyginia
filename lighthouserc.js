// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.
const runningOnCi = process.env.CI === "true" ? "off" : "on";

module.exports = {
    ci: {
        collect: {
            startServerCommand: "MICRONAUT_ENVIRONMENTS=baremetal ./gradlew run",
            startServerReadyPattern: "Startup completed",
            startServerReadyTimeout: 90000, // 90 seconds. More than enough for CI.
            settings: {
                maxWaitForLoad: 10000, // 10 seconds
                chromeFlags: "--headless=new"
            },
            url: [
                "http://localhost:8080/",
            ]
        },
        assert: {
            preset: "lighthouse:no-pwa",
            assertions: {
                "offscreen-images": "off",
                "identical-links-same-purpose": "off",
                "heading-order": "off",
                "uses-responsive-images": "off",
                "total-byte-weight": "off",
                // Only failing at CI:
                "tap-targets": runningOnCi,
                "target-size": runningOnCi,
            }
        },
        upload: {
            target: "lhci",
            serverBaseUrl: "https://liber-lighthouse-server.up.railway.app",
            token: process.env.PROJECT_TOKEN,
            githubToken: process.env.GITHUB_TOKEN,
            basicAuth: {
                username: process.env.BASIC_AUTH_USERNAME,
                password: process.env.BASIC_AUTH_PASSWORD,
            }
        }
    },
};