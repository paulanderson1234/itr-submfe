$("#ninetyPercentErrorSection").hide();



$(document).ready($(function() {

  $(".removeLink").each(function()  {

      $( this ).on( 'focus', function() {
              	$( this ).addClass( 'in-focusd' );
              });
              $( this ).on( 'focusout', function(){
              	$( this ).removeClass( 'in-focusd' );
              });
        });


    $('*[data-hidden]').each(function() {

        var $self = $(this);
        var $hidden = $('#hidden')
        var $hiddenNo = $('#hidden-no')
        var $input = $self.find('input');

        if ($input.val() === 'Yes' && $input.prop('checked')) {
            $hidden.show();
            $hiddenNo.hide();
        } else {
            $hidden.hide();
            $hiddenNo.hide();
        }

        $input.change(function() {

            var $this = $(this);

            if ($this.val() === 'Yes') {
                $hidden.show();
                $hiddenNo.hide();

            } else if($this.val() === 'No') {
               $hidden.hide();
               $hiddenNo.show();
               $("#error-summary-display").hide();
               $(".form-field--error").removeClass("form-field--error");
               $(".error-summary-show").removeClass("error-summary-show");
               ClearRevealingContentInputs();
            }
        });


    });

    if ($('#schemeTypeDesc-another_scheme').is(":checked")) {
        $('#hidden-other-scheme').show();

    } else if ($('#schemeTypeDesc-another_scheme').is(":not(:checked)")) {
        $('#hidden-other-scheme').hide();
    }

    if ($('#schemeTypeDesc-seed_enterprise_investment_scheme').is(":checked")) {
            $($('#hidden-investment-spent')).show();

    } else if ($('#schemeTypeDesc-seed_enterprise_investment_scheme').is(":not(:checked)")) {
            $('#hidden-investment-spent').hide();
    }



    // commercial-sale page handling
//    $("input[name='hasCommercialSale']").each(function()  {
//
//        var $checkbox = $(this)
//
//        var $hiddenOtherScheme = $('#hidden-other-scheme')
//        var $hiddenInvestmentSpent = $('#hidden-investment-spent')
//
//        $checkbox.change(function() {
//
//            ClearRevealingContentInputs();
//            if ($checkbox.val() === 'Another scheme') {
//                 //alert($checkbox.val());
//                $hiddenOtherScheme.show();
//                $hiddenInvestmentSpent.hide();
//                ClearPageErrors();
//;
//            } else if($checkbox.val() === 'Seed Enterprise Investment Scheme') {
//                //alert($checkbox.val());
//               $hiddenOtherScheme.hide();
//               $hiddenInvestmentSpent.show();
//               ClearPageErrors();
//
//            }
//            else
//            {
//                 $hiddenOtherScheme.hide();
//                 $hiddenInvestmentSpent.hide();
//            }
//        });
//    });

    var $hiddenOtherScheme = $('#hidden-other-scheme')
    var $hiddenInvestmentSpent = $('#hidden-investment-spent')

    $hiddenInvestmentSpent.hide();
    $hiddenOtherScheme.hide();

    $("input[name='schemeTypeDesc']").each(function()  {

        var $checkbox = $(this)

        if ($checkbox.val() === 'Other' && $checkbox.prop('checked')) {
            $hiddenOtherScheme.show();
        }

        if ($checkbox.val() === 'SEIS' && $checkbox.prop('checked')) {
            $hiddenInvestmentSpent.show();
        }

        $checkbox.change(function() {

            ClearRevealingContentInputs();
            if ($checkbox.val() === 'Other') {
                 //alert($checkbox.val());
                $hiddenOtherScheme.show();
                $hiddenInvestmentSpent.hide();
                ClearPageErrors();
;
            } else if($checkbox.val() === 'SEIS') {
                //alert($checkbox.val());
               $hiddenOtherScheme.hide();
               $hiddenInvestmentSpent.show();
               ClearPageErrors();

            }
            else
            {
                 $hiddenOtherScheme.hide();
                 $hiddenInvestmentSpent.hide();
            }
        });
    });


    var radioOptions = $('input[type="radio"]');

    radioOptions.each(function() {
        var o = $(this).parent().next('.additional-option-block');
        if ($(this).prop('checked')) {
            o.show();
        } else {
            o.hide();
        }
    });

    radioOptions.on('click', function(e){
        var o = $(this).parent().next('.additional-option-block');
        if(o.index() == 1){
            $('.additional-option-block').hide();
            o.show();
        }
    });

    $('[data-metrics]').each(function() {
        var metrics = $(this).attr('data-metrics');
        var parts = metrics.split(':');
        ga('send', 'event', parts[0], parts[1], parts[2]);
        console.log("sending: " + parts[0] + " - " + parts[1] + " - " + parts[2])
    });

    function ClearRevealingContentInputs()
    {
         // if you have a form with revealing controls.
         // clear them here by using the appropriate selectors and clearing the inputs.

         // clear any revealing date controls by selecting by class
         //$(".form-group-day").children("input").val("");
         //$(".form-group-month").children("input").val("");
         //$(".form-group-year").children("input").val("");

         $("#commercialSaleDay").val("");
         $("#commercialSaleMonth").val("");
         $("#commercialSaleYear").val("");
         $("#otherSchemeName").val("");
         $("#investmentSpent").val("");
         $("#tradeStartDay").val("");
         $("#tradeStartMonth").val("");
         $("#tradeStartYear").val("");
    }

    function ClearPageErrors()
    {
        $("#error-summary-display").hide();
        $(".form-field--error").removeClass("form-field--error");
        $(".error-summary-show").removeClass("error-summary-show");
    }

    $("#ownNinetyPercent-no").on("click", function () {
          if ($(this).is(":checked")) {
              EnableNinetyPercentError();
        }
    });

    $("#ownNinetyPercent-yes").on("click", function () {
          if ($(this).is(":checked")) {
            DisableNinetyPercentError();
        }
    });$("#ownNinetyPercent-yes").on("click", function () {
          if ($(this).is(":checked")) {
            DisableNinetyPercentError();
        }
    });

}));

function EnableNinetyPercentError()
{
    $("#radioNinetyPercentDiv").addClass("error");
    $("#ownNinetyPercent-noLabel").addClass("error-border");
    $("#ninetyPercentErrorSection").show();
    $("#ninetyPercentButtonDiv").hide();

}

function DisableNinetyPercentError()
{
    $("#radioNinetyPercentDiv").removeClass("error");
    $("#ownNinetyPercent-noLabel").removeClass("error-border");
    $("#ninetyPercentErrorSection").hide();
    $("#ninetyPercentButtonDiv").show();

}



// <details> polyfill
// http://caniuse.com/#feat=details

// FF Support for HTML5's <details> and <summary>
// https://bugzilla.mozilla.org/show_bug.cgi?id=591737

// http://www.sitepoint.com/fixing-the-details-element/

(function () {
  'use strict';

  var NATIVE_DETAILS = typeof document.createElement('details').open === 'boolean';

  // Add event construct for modern browsers or IE
  // which fires the callback with a pre-converted target reference
  function addEvent(node, type, callback) {
    if (node.addEventListener) {
      node.addEventListener(type, function (e) {
        callback(e, e.target);
      }, false);
    } else if (node.attachEvent) {
      node.attachEvent('on' + type, function (e) {
        callback(e, e.srcElement);
      });
    }
  }

  // Handle cross-modal click events
  function addClickEvent(node, callback) {
    // Prevent space(32) from scrolling the page
    addEvent(node, 'keypress', function (e, target) {
      if (target.nodeName === 'SUMMARY') {
        if (e.keyCode === 32) {
          if (e.preventDefault) {
            e.preventDefault();
          } else {
            e.returnValue = false;
          }
        }
      }
    });
    // When the key comes up - check if it is enter(13) or space(32)
    addEvent(node, 'keyup', function (e, target) {
      if (e.keyCode === 13 || e.keyCode === 32) { callback(e, target); }
    });
    addEvent(node, 'mouseup', function (e, target) {
      callback(e, target);
    });
  }

  // Get the nearest ancestor element of a node that matches a given tag name
  function getAncestor(node, match) {
    do {
      if (!node || node.nodeName.toLowerCase() === match) {
        break;
      }
    } while (node = node.parentNode);

    return node;
  }

  // Create a started flag so we can prevent the initialisation
  // function firing from both DOMContentLoaded and window.onload
  var started = false;

  // Initialisation function
  function addDetailsPolyfill(list) {

    // If this has already happened, just return
    // else set the flag so it doesn't happen again
    if (started) {
      return;
    }
    started = true;

    // Get the collection of details elements, but if that's empty
    // then we don't need to bother with the rest of the scripting
    if ((list = document.getElementsByTagName('details')).length === 0) {
      return;
    }

    // else iterate through them to apply their initial state
    var n = list.length, i = 0;
    for (i; i < n; i++) {
      var details = list[i];

      // Save shortcuts to the inner summary and content elements
      details.__summary = details.getElementsByTagName('summary').item(0);
      details.__content = details.getElementsByTagName('div').item(0);

      // If the content doesn't have an ID, assign it one now
      // which we'll need for the summary's aria-controls assignment
      if (!details.__content.id) {
        details.__content.id = 'details-content-' + i;
      }

      // Add ARIA role="group" to details
      details.setAttribute('role', 'group');

      // Add role=button to summary
      details.__summary.setAttribute('role', 'button');

      // Add aria-controls
      details.__summary.setAttribute('aria-controls', details.__content.id);

      // Set tabIndex so the summary is keyboard accessible for non-native elements
      // http://www.saliences.com/browserBugs/tabIndex.html
      if (!NATIVE_DETAILS) {
        details.__summary.tabIndex = 0;
      }

      // Detect initial open state
      var openAttr = details.getAttribute('open') !== null;
      if (openAttr === true) {
        details.__summary.setAttribute('aria-expanded', 'true');
        details.__content.setAttribute('aria-hidden', 'false');
      } else {
        details.__summary.setAttribute('aria-expanded', 'false');
        details.__content.setAttribute('aria-hidden', 'true');
        if (!NATIVE_DETAILS) {
          details.__content.style.display = 'none';
        }
      }

      // Create a circular reference from the summary back to its
      // parent details element, for convenience in the click handler
      details.__summary.__details = details;

      // If this is not a native implementation, create an arrow
      // inside the summary
      if (!NATIVE_DETAILS) {

        var twisty = document.createElement('i');
        details.__summary.__twisty = details.__summary.insertBefore(twisty, details.__summary.firstChild);
        details.__summary.__twisty.setAttribute('aria-hidden', 'true');

      }
    }

    // Define a statechange function that updates aria-expanded and style.display
    // Also update the arrow position
    function statechange(summary) {

      var expanded = summary.__details.__summary.getAttribute('aria-expanded') === 'true';
      var hidden = summary.__details.__content.getAttribute('aria-hidden') === 'true';

      summary.__details.__summary.setAttribute('aria-expanded', (expanded ? 'false' : 'true'));
      summary.__details.__content.setAttribute('aria-hidden', (hidden ? 'false' : 'true'));

      if (!NATIVE_DETAILS) {
        summary.__details.__content.style.display = (expanded ? 'none' : '');

        var hasOpenAttr = summary.__details.getAttribute('open') !== null;
        if (!hasOpenAttr) {
          summary.__details.setAttribute('open', 'open');
        } else {
          summary.__details.removeAttribute('open');
        }
      }

      if (summary.__twisty) {
        summary.__twisty.firstChild.nodeValue = (expanded ? '\u25ba' : '\u25bc');
        summary.__twisty.setAttribute('class', (expanded ? 'arrow arrow-closed' : 'arrow arrow-open'));
      }

      return true;
    }

    // Bind a click event to handle summary elements
    addClickEvent(document, function(e, summary) {
      if (!(summary = getAncestor(summary, 'summary'))) {
        return true;
      }
      return statechange(summary);
    });
  }

  // Bind two load events for modern and older browsers
  // If the first one fires it will set a flag to block the second one
  // but if it's not supported then the second one will fire
  addEvent(document, 'DOMContentLoaded', addDetailsPolyfill);
  addEvent(window, 'load', addDetailsPolyfill);

})();