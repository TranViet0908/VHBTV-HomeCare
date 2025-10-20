document.addEventListener('DOMContentLoaded', () => {
    // Select the vendor review form
    const validator = new FormValidator('#vendorReviewForm');

    // Rating validation (1-5 stars)
    validator.addRule('rating', 'required', 'Vui lòng chÍn sÑ sao ánh giá.');
    validator.addRule('rating', 'minValue:1', 'ánh giá ph£i të 1 ¿n 5 sao.');
    validator.addRule('rating', 'maxValue:5', 'ánh giá ph£i të 1 ¿n 5 sao.');

    // Review content validation
    validator.addRule('content', 'required', 'Vui lòng nh­p nÙi dung ánh giá.');
    validator.addRule('content', 'minLength:10', 'NÙi dung ánh giá ph£i có ít nh¥t 10 ký tñ.');
    validator.addRule('content', 'maxLength:1000', 'NÙi dung ánh giá không °ãc quá 1000 ký tñ.');

    // Star rating interactive UI handler
    const starBox = document.getElementById('venStarBox');
    const ratingInput = document.getElementById('venRating');

    if (starBox && ratingInput) {
        const stars = starBox.querySelectorAll('i[data-rate]');

        // Click handler for each star
        stars.forEach(star => {
            star.addEventListener('click', function() {
                const rating = parseInt(this.getAttribute('data-rate'));
                ratingInput.value = rating;

                // Update star display
                updateStars(rating);
            });

            // Hover effect
            star.addEventListener('mouseenter', function() {
                const rating = parseInt(this.getAttribute('data-rate'));
                updateStars(rating);
            });
        });

        // Reset to selected rating on mouse leave
        starBox.addEventListener('mouseleave', function() {
            const currentRating = parseInt(ratingInput.value) || 0;
            updateStars(currentRating);
        });

        // Function to update star display
        function updateStars(rating) {
            stars.forEach((star, index) => {
                if (index < rating) {
                    star.classList.remove('fa-regular', 'text-gray-300');
                    star.classList.add('fas', 'text-yellow-400');
                } else {
                    star.classList.remove('fas', 'text-yellow-400');
                    star.classList.add('fa-regular', 'text-gray-300');
                }
            });
        }
    }
});
