$(function() {
      "use strict";
      //Datepicker embedded
      var picker = $('#date_booking').daterangepicker({
        parentEl: '#daterangepicker-embedded-container',
        autoUpdateInput: false,
        autoApply :true,
        alwaysShowCalendars:true
      });
      // range update listener
      picker.on('apply.daterangepicker', function(ev, picker) {
        $(this).val(picker.startDate.format('MM-DD-YY') + ' to ' + picker.endDate.format('MM-DD-YY'));
      });
      // prevent hide after range selection
      picker.data('daterangepicker').hide = function () {};
      // show picker on load
      picker.data('daterangepicker').show();
  });