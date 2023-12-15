(function ($) {

	"use strict";

	$(window).on('load', function(){
			'use strict';
			$('#carousel_slider').flexslider({
				animation: "slide",
				controlNav: false,
				animationLoop: false,
				slideshow: true,
				itemWidth: 280,
				itemMargin: 40,
				asNavFor: '#slider'
			});
			$('#slider').flexslider({
				animation: "fade",
				controlNav: false,
				animationLoop: false,
				slideshow: true,
				sync: "#carousel_slider",
				start: function(slider) {
					$('body').removeClass('loading');
				}
			});
	});

})(window.jQuery); 