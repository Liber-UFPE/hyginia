import {initFlowbite, initPopovers, Collapse} from "flowbite";

initFlowbite();

function setupFootnotesAnimationEvent() {
    const pageContents = document.querySelector(".day-contents");
    if (pageContents) {
        pageContents
            .querySelectorAll(".footnote-ref")
            .forEach(note => {
                note.addEventListener("click", () => {
                    const noteId = note.href.split("#")[1];
                    const noteElement = document.getElementById(noteId);
                    if (noteElement) {
                        noteElement.classList.add("highlight-footnote");
                        // Remove the class after sometime so that the same footnote
                        // will blink when user clicks it a second time.
                        setTimeout(() => {noteElement.classList.remove("highlight-footnote");}, 3000);
                    }
                });
            });
    }
}

function showRequestProgress() {
    const indicator = document.getElementById("brand-button");
    indicator.classList.add("htmx-request");

    // If the request completes too fast, this avoids the indicator to just flicking.
    setTimeout(() => indicator.classList.remove("htmx-request"), 1300);
}

function collapseNavigation() {
    const $targetEl = document.getElementById("navbar-search");

    const instanceOptions = {
        id: "navbar",
        override: true
    };

    const collapse = new Collapse($targetEl, null, {}, instanceOptions);
    collapse.collapse();
}

window.onload = () => {
    const navbarSearchButton = document.getElementById("navbar-search-button");
    navbarSearchButton.onclick = () => {
        const queryInput = document.getElementById("sm-query-input");
        queryInput.focus();
    };
};

window.addEventListener("load", setupFootnotesAnimationEvent);

// https://htmx.org/events/#htmx:afterRequest
document.documentElement.addEventListener("htmx:afterRequest", setupFootnotesAnimationEvent);
document.documentElement.addEventListener("htmx:afterRequest", initPopovers);
document.documentElement.addEventListener("htmx:afterRequest", showRequestProgress);
document.documentElement.addEventListener("htmx:afterRequest", collapseNavigation);