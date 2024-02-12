// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.
/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/**/*.{kte,js}",
        "./node_modules/flowbite/**/*.js",
    ],
    safelist: [
        "footnotes",
        "footnote-ref",
        "htmx-request",
    ],
    theme: {
        fontFamily: {
            sans: ["system-ui", "sans-serif"],
            serif: ["Merriweather", "serif"],
        },
        extend: {},
    },
    plugins: [
        require("flowbite/plugin"),
    ],
};