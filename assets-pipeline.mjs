import esbuild from "esbuild";
import sharp from "sharp";
import fg from "fast-glob";
import {sassPlugin} from "esbuild-sass-plugin";
import autoprefixer from "autoprefixer";
import postcss from "postcss";
import tailwindcss from "tailwindcss";

import path from "node:path";
import fs from "node:fs";
import {createGzip, createBrotliCompress, createDeflate} from "node:zlib";
import pipe from "node:stream/promises";

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

const assetsFolder = "src/main/resources/public";
const nodeModulesFolder = "node_modules";
const assetsBuildFolder = "build/resources/main/public";

const compressPlugin = {
    name: "compress",
    setup(build) {
        build.onEnd(() => {
            fg.async([`${build.initialOptions.outdir}/**/*.{css,js,html,svg,txt,json,ico}`], {
                caseSensitiveMatch: false,
                dot: true
            }).then(files =>
                files.forEach(file => {
                    const gzipPipe = pipe.pipeline(
                        fs.createReadStream(file),
                        createGzip(),
                        fs.createWriteStream(`${file}.gz`)
                    );
                    const brotliPipe = pipe.pipeline(
                        fs.createReadStream(file),
                        createBrotliCompress(),
                        fs.createWriteStream(`${file}.br`)
                    );
                    const deflatePipe = pipe.pipeline(
                        fs.createReadStream(file),
                        createDeflate(),
                        fs.createWriteStream(`${file}.zz`)
                    );

                    // will go as slow as the slowest compression
                    return Promise.all([gzipPipe, brotliPipe, deflatePipe]);
                })
            );
        });
    },
};

const modernImages = {
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
        modernImages,
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