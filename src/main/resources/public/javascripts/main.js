import {initFlowbite} from "flowbite";

initFlowbite();

function delayHidingHtmxIndicator() {
    const indicator = document.getElementById("brand-button");
    if (indicator) {
        indicator.classList.add("htmx-request");

        // If the request completes too fast, this avoids the indicator to just flicking.
        setTimeout(() => indicator.classList.remove("htmx-request"), 300);
    }
}

// https://htmx.org/events/#htmx:afterRequest
document.documentElement.addEventListener("htmx:afterRequest", delayHidingHtmxIndicator);