config.plugins.push(
    new (require('webpack').NormalModuleReplacementPlugin)(
        /^node:/,
        (resource) => { resource.request = resource.request.replace(/^node:/, ''); }
    )
);
config.resolve = config.resolve || {};
config.resolve.fallback = Object.assign(config.resolve.fallback || {}, {
    fs: false,
    path: false,
    crypto: false,
    net: false,
});
