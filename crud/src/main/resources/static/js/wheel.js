document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll(".user-wheel-container").forEach(wheelContainer => {
        const wheel = wheelContainer.querySelector(".user-wheel");
        if (!wheel) throw new Error("No wheel found");
        
        const winner = wheel.getAttribute("data-winner");
        if (typeof winner === "string") {
            spinWheel(wheelContainer, wheel, winner);
        }
    });
});

/**
 * Spins the wheel to land on the winner element
 * @param {Element} wheelContainer
 * @param {Element} wheelElement
 * @param {string} winner
 * @param {number} fullRotations - Number of full rotations before stopping (default: 3)
 */
function spinWheel(wheelContainer, wheelElement, winner, fullRotations = 4) {
    // Find the winner's index
    const liElements = wheelElement.querySelectorAll('li');
    let winnerIndex = -1;
    
    liElements.forEach((item, index) => {
        const span = item.querySelector('span');
        if (span && span.textContent.trim() === winner.trim()) {
            winnerIndex = index;
        }
    });

    if (winnerIndex === -1) {
        console.error('Winner not found:', winner);
        return;
    }

    // Calculate total items
    const totalItems = liElements.length;
    
    // Calculate the rotation to bring the winner to the top (0 degrees)
    // Each item is positioned at: (index * 360 / totalItems) degrees
    // We need to rotate the wheel so the winner ends up at 0 degrees (top)
    const winnerAngle = (winnerIndex * 360) / totalItems;
    
    // Add full rotations and position the winner at the top
    // The wheel rotates clockwise, so we need a negative rotation to bring winner to top
    const targetRotation = -(fullRotations * 360 + /*To make wheel winner stop on right side of wheel*/ 180 + winnerAngle);
    
    // Spin the wheel
    const animation = wheelElement.animate([
        { transform: `rotate(0deg)` },
        { transform: `rotate(${targetRotation}deg)` }
    ], {
        duration: 5000,
        direction: 'normal',
        easing: 'cubic-bezier(0.25, 0.1, 0.25, 1)',
        fill: 'forwards',
        iterations: 1
    });

    // Update flips during animation using CSS custom properties
    const updateFlips = () => {
        // Get current rotation from computed style
        const computedStyle = window.getComputedStyle(wheelElement);
        const transform = computedStyle.transform;
        
        if (transform && transform !== 'none') {
            // Extract rotation value from matrix
            const values = transform.split('(')[1].split(')')[0].split(',');
            const a = parseFloat(values[0]);
            const b = parseFloat(values[1]);
            const wheelRotation = Math.atan2(b, a) * (180 / Math.PI);
            
            // Calculate which items should be flipped based on true rotation
            liElements.forEach((item, index) => {
                // Calculate the slice's own rotation based on its index
                const sliceRotation = (index * 360) / totalItems;
                
                // Calculate true rotation: wheel rotation + slice rotation
                const trueRotation = wheelRotation + sliceRotation;
                
                // Normalize to 0-360 range
                const normalizedRotation = ((trueRotation % 360) + 360) % 360;
                
                // Determine if slice is on the right side (top) or left side (bottom)
                // Right side: 270-90 degrees (wrapping around)
                // Left side: 90-270 degrees
                const isOnRightSide = normalizedRotation >= 270 || normalizedRotation <= 90;
                
                // Apply flip class based on position
                if (isOnRightSide) {
                    item.classList.remove('flip');
                } else {
                    item.classList.add('flip');
                }
            });
        }
        
        // Continue updating during animation
        if (animation.playState === 'running') {
            requestAnimationFrame(updateFlips);
        }
    };
    
    // query for all elements besides wheel in #content
    const contentElementsNotWheel = document.querySelectorAll("main > *:not(.user-wheel-container)");
    contentElementsNotWheel.forEach(element => {element.classList.add('hide');});
    updateFlips();

    animation.addEventListener('finish', () => {
        // Add a new event listener to make the winner li blink
        const blinkDuration = 3000; // 3 seconds
        const winnerLi = liElements[winnerIndex];
        const textElement = winnerLi.querySelector("span");
        if (!textElement) return;
        textElement.classList.add('wheel-blink');
        setTimeout(() => {
            textElement.classList.remove('wheel-blink');
            contentElementsNotWheel.forEach(element => {element.classList.remove('hide');});
            wheelContainer.classList.add('hide');
        }, blinkDuration);
    });
}

/**
 * Determines if a slice is on the right side of the wheel based on its true rotation angle
 * @param {number} trueRotation - The true rotation angle in degrees (0-360)
 * @returns {boolean} - True if slice is on right side (should face up), false for left side (should face down)
 */
function isSliceOnRightSide(trueRotation) {
    // The right side of the wheel covers angles from 270 to 90 degrees (wrapping around)
    // This includes:
    // - 270 to 360 degrees (bottom-right to top)
    // - 0 to 90 degrees (top to top-right)
    
    // The left side covers angles from 90 to 270 degrees
    // This includes:
    // - 90 to 180 degrees (top-right to bottom-left)
    // - 180 to 270 degrees (bottom-left to bottom-right)
    
    if (trueRotation >= 270 || trueRotation <= 90) {
        // On right side (should face up)
        return true;
    } else {
        // On left side (should face down)
        return false;
    }
}
