import esbuild from "esbuild";
import {sassPlugin} from "esbuild-sass-plugin";
import {compressPlugin} from "@liber-ufpe/esbuild-plugin-compress";
import {imagesPlugin} from "@liber-ufpe/esbuild-plugin-sharp";
import autoprefixer from "autoprefixer";
import postcss from "postcss";
import tailwindcss from "tailwindcss";

const assetsFolder = "src/main/resources/public";
const nodeModulesFolder = "node_modules";
const assetsBuildFolder = "build/resources/main/public";

const compress = compressPlugin({
    excludes: ["**/*.{jpg,jpeg,png,webp,avif}"]
});

const sass = sassPlugin({
    async transform(source) {
        const {css} = await postcss([tailwindcss, autoprefixer]).process(source, {
            from: undefined,
            map: false,
        });
        return css;
    },
});

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
    plugins: [sass, compress, imagesPlugin()],
});

await esbuild.build({
    entryPoints: [`${nodeModulesFolder}/htmx.org/dist/htmx.js`],
    bundle: true,
    minify: true,
    allowOverwrite: true,
    metafile: true,
    legalComments: "none",
    entryNames: "[name].[hash]",
    logLevel: "info",
    outdir: `${assetsBuildFolder}/javascripts`,
    plugins: [compress]
});
