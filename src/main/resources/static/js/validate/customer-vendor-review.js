document.addEventListener('DOMContentLoaded', () => {
    // Select the vendor review form
    const validator = new FormValidator('#vendorReviewForm');

    // Rating validation (1-5 stars)
    validator.addRule('rating', 'required', 'Vui l�ng ch�n s� sao �nh gi�.');
    validator.addRule('rating', 'minValue:1', '�nh gi� ph�i t� 1 �n 5 sao.');
    validator.addRule('rating', 'maxValue:5', '�nh gi� ph�i t� 1 �n 5 sao.');

    // Review content validation
    validator.addRule('content', 'required', 'Vui l�ng nh�p n�i dung �nh gi�.');
    validator.addRule('content', 'minLength:10', 'N�i dung �nh gi� ph�i c� �t nh�t 10 k� t�.');
    validator.addRule('content', 'maxLength:1000', 'N�i dung �nh gi� kh�ng ��c qu� 1000 k� t�.');

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
