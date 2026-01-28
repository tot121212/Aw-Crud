document.addEventListener('DOMContentLoaded', () => {
    var prevScrollPos = window.scrollY;
    const headerElement = document.querySelector("header");
    if (!headerElement) return;

    window.addEventListener('scroll', function () {
        var currentScrollPos = window.scrollY;

        headerElement.style.transform =
            prevScrollPos > currentScrollPos
                ? 'translateY(0)'
                : 'translateY(-85%)';

        prevScrollPos = currentScrollPos;
    });
});