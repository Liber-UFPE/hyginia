import {Collapse} from "bootstrap";
import * as Turbo from 'https://cdn.skypack.dev/pin/@hotwired/turbo@v7.3.0-44BiCcz1UaBhgMf1MCRj/mode=imports,min/optimized/@hotwired/turbo.js';

document.addEventListener("turbo:before-fetch-request", function (_e) {
    Turbo.navigator.delegate.adapter.showProgressBar();
});
document.addEventListener("turbo:frame-load", function (_e) {
    Turbo.navigator.delegate.adapter.progressBar.hide()
});

function setupCollapsableComponents() {
    const collapseElementList = document.querySelectorAll(".collapse");
    [...collapseElementList].forEach(collapseEl =>
        collapseEl.addEventListener("click", () => new Collapse(collapseEl))
    );
}

window.addEventListener("load", setupCollapsableComponents);

console.info("Ooops! Ainda tem algumas coisas para customizar no template");