import esbuild from "esbuild";
import {Compress} from "gzipper";
import sharp from "sharp";
import fg from "fast-glob";
import path from "path";
import {sassPlugin} from "esbuild-sass-plugin";
import autoprefixer from "autoprefixer";
import postcss from "postcss";
import tailwindcss from "tailwindcss";

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

const assetsFolder = "src/main/resources/public";
const nodeModulesFolder = "node_modules";
const assetsBuildFolder = "build/resources/main/public";

const compressPlugin = {
    name: "compress",
    setup(build) {
        build.onEnd(() => {
            const outputPath = build.initialOptions.outdir;
            const verbose = build.initialOptions.logLevel === "verbose" || build.initialOptions.logLevel === "debug";

            const compressOptions = {
                brotli: true,
                deflate: true,
                deflateLevel: 9,
                gzip: true,
                gzipLevel: 9,
                exclude: ["jpeg", "jpg", "png", "webp", "avif"],
                verbose: verbose,
            };

            new Compress(outputPath, outputPath, compressOptions).run();
        });
    },
};

const webpPlugin = {
    name: "webp",
    setup(build) {
        build.onEnd(() => {
            fg.async([`${build.initialOptions.outdir}/**/*.{png,jpg}`], {caseSensitiveMatch: false, dot: true})
                .then(images =>
                    images.forEach(image => {
                        const imagePath = path.parse(image);
                        const webpOutputImage = `${imagePath.dir}/${imagePath.name}.webp`;
                        const avifOutputImage = `${imagePath.dir}/${imagePath.name}.avif`;

                        sharp(image).toFormat("webp").toFile(webpOutputImage);
                        sharp(image).toFormat("avif").toFile(avifOutputImage);
                    })
                );
        });
    }
};

await esbuild.build({
    entryPoints: [
        `${assetsFolder}/stylesheets/main.scss`,
        `${assetsFolder}/javascripts/main.js`,
        `${assetsFolder}/images/**/*.*`,
    ],
    bundle: true,
    minify: true,
    allowOverwrite: true,
    metafile: true,
    legalComments: "none",
    entryNames: "[dir]/[name].[hash]",
    outdir: assetsBuildFolder,
    logLevel: "info",
    loader: {
        ".webp": "copy",
        ".jpg": "copy",
        ".png": "copy",
        ".ico": "copy",
    },
    plugins: [
        sassPlugin({
            async transform(source) {
                const {css} = await postcss([tailwindcss, autoprefixer])
                    .process(source, {from: undefined, map: false});
                return css;
            }
        }),
        compressPlugin,
        webpPlugin,
    ],
});

await esbuild.build({
    entryPoints: [`${nodeModulesFolder}/htmx.org/dist/htmx.js`,],
    bundle: true,
    minify: true,
    allowOverwrite: true,
    legalComments: "none",
    entryNames: "[name].[hash]",
    logLevel: "info",
    outdir: `${assetsBuildFolder}/javascripts`,
    plugins: [compressPlugin]
})