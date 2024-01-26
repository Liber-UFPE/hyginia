import {initFlowbite, Collapse} from "flowbite";

initFlowbite();

window.onload = () => {
    const navbarSearchButton = document.getElementById("navbar-search-button");
    navbarSearchButton.onclick = () => {
        const queryInput = document.getElementById("sm-query-input");
        queryInput.focus();
    };
};

// https://htmx.org/events/#htmx:afterRequest
document.documentElement.addEventListener("htmx:afterRequest", () => {
    const indicator = document.getElementById("brand-button");
    indicator.classList.add("htmx-request");

    // If the request completes too fast, this avoids the indicator to just flicking.
    setTimeout(() => indicator.classList.remove("htmx-request"), 1300);
});

document.documentElement.addEventListener("htmx:afterRequest", () => {
    const $targetEl = document.getElementById("navbar-search");

    const instanceOptions = {
        id: "navbar",
        override: true
    };

    const collapse = new Collapse($targetEl, null, {}, instanceOptions);
    collapse.collapse();
});