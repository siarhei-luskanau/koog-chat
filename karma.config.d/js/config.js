const path = require("node:path");

const basePath = config.basePath;
const rootPath = path.resolve(basePath, "..", "..", "..", "..");
const staticFilesDir = path.resolve(rootPath, "karma.config.d", "js", "static");

config.customContextFile = path.resolve(staticFilesDir, "compose_context.html");

config.client.mocha = config.client.mocha || {};
config.client.mocha.timeout = 10000;

config.files.push(
    {pattern: path.resolve(basePath, "kotlin", "skiko.wasm"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "kotlin", "skiko.mjs"), included: false, served: true, watched: false, type: "module"},
    {pattern: path.resolve(basePath, "kotlin", "js-skiko-reexport-symbols.mjs"), included: false, served: true, watched: false, type: "module"},
);

config.proxies = config.proxies || {};
config.proxies["/skiko.mjs"] = "/base/kotlin/skiko.mjs";
config.proxies["/skiko.wasm"] = "/base/kotlin/skiko.wasm";
config.proxies["/js-skiko-reexport-symbols.mjs"] = "/base/kotlin/js-skiko-reexport-symbols.mjs";
