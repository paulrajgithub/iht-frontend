$(document).ready(function() {

// =====================================================
// Initialise show-hide-content
// Toggles additional content based on radio/checkbox input state
// =====================================================
var showHideContent = new GOVUK.ShowHideContent()
showHideContent.init()


// =====================================================
// Combine two values to display a third
// e.g. used on Gifts details
// =====================================================
  combinedValue();


// =====================================================
// Check for hashed url and jump to input if needed
// The non-error-list focus will not focus on the input for iOS due to security restrictions
// The in-page link-click focus setting is not affected
// =====================================================
    var hash = window.location.hash.substring(1);
    if(hash > ""){
        hashJump(hash);
    }

    // error summary jump links
    $('.error-list a').on('click', function(e){
        var linkhash = $(this).attr("href").replace('#','');
        hashJump(linkhash);
    });


// =====================================================
// Declaration - needs refactoring
// =====================================================
  $(".toDisableButton").prop( "disabled", true ).attr('id', 'accept-button');
  $("#isDeclared").attr('aria-controls', 'accept-button');
  $("#isDeclared").click(function(){
      if($(this).is(":checked")){
          $(".toDisableButton").prop( "disabled", false );
      }else{
          $(".toDisableButton").prop( "disabled", true );
          $(this).parent().focusout(function(){
              $(this).removeClass("checkbox-checked")
          });
      }
  });


// =====================================================
// reset an input when a radio button changes value
// =====================================================
  $("[data-clear-target]").click(function(){
      clearInput($(this));
  });


// =====================================================
// kick off Jquery UI component
// =====================================================
if($('.to-combobox').length > 0){
  countryCodeAutoComplete();
}


// =====================================================
// Submit trigger
// Used on Gifts section
// Should be replaced by links without the POST through
// =======================================================
  $(".js-submitButtonAction").click(function(){
      var submitValue = $(this).attr("data-submitValue");
      $("form").append("<input type='hidden' name='action' value='"+submitValue+"'>");
      $("form").trigger('submit');
  });

  // end of on doc ready
});


/// =====================================================
/// Hash jump
/// Takes user to location of input which needs error fixing
/// Also looks for a parent label or fieldset and ensures the page is scrolled to leave that visible
/// =====================================================
function hashJump(hash){
    hash = hash.replace('.','\\\.');
    if (hash.length != 0) {
        if($('#' + hash).length > 0){
            var el = $('#' + hash);
            if(el[0].tagName == "INPUT"){
                // set fallback scroll of the input
                var scrollPos = el.offset().top;
                if (el[0].type == "radio" || el[0].type == "checkbox"){
                    // focus on the group legend if present, else on the label
                    if(el.parents('fieldset').length > 0){
                        scrollPos = el.parents('fieldset').first().find('legend').offset().top;
                    }
                } else {
                    if($('label[for="' + hash + '"]').length > 0){
                        scrollPos = $('label[for="' + hash + '"]').offset().top;
                        setFocus($('#' + hash));
                    }
                }
                setTimeout(function(){
                    $(window).scrollTop(scrollPos);
                }, 50);

            } else {
                 // scroll and set focus on inputs when anchor is on container
                 setInputFocus(hash);
            }
        }
    }
}


/// =====================================================
/// Set Focus
/// cross-device focus set
/// We set via a timeout for desktop browsers
/// We also set immediately as iOS will not support a timeout set due to security rules
/// Both are needed as support is not cross-device for a single implementation
/// =====================================================
function setFocus(el){
    el.focus();
    setTimeout(function(){
        el.focus();
    }, 10);
}



/// =====================================================
/// Set Input Focus
/// finds an input in a given element with id of elid and sets focus
/// Will focus on either:
/// 1. An input *outside* of a fieldset which is related to the given label id
/// This captures standard label + input[text] combinations
/// 2. An input of type[text||tel||number] which is the first visible input inside a fieldset
/// This captures patterns such as date-of-birth where it is desirable to have focus on the first input
/// 3. An already selected option in a group of checkboxes/radio-buttons
/// We can safely target this as it will playback the selected option
///
/// In all other instances focus will not be set
/// =====================================================
function setInputFocus(elid) {
    var labelTarget = $('label[id="' + elid + '"]');
    var fieldsetTarget = $('[id="' + elid + '"] fieldset, fieldset[id="' + elid + '"]').first();

    if(labelTarget.length > 0 && fieldsetTarget.length == 0){
       setFocus(labelTarget.find('input').first());
    }

    if(fieldsetTarget.length > 0){
        // focus on first text field if it is the first visible input in a fieldset
        var firstInput = fieldsetTarget.find('input:visible').first();
        if(firstInput.attr('type') == "text" || firstInput.attr('type') == "tel" || firstInput.attr('type') == "number"){
            // text input inside a fieldset
            setFocus(firstInput);
        } else {
            // non-focus-able inputs
            // but we can focus on a checked radio button or checkbox
            if(fieldsetTarget.find(':checked').length > 0){
                 setFocus(fieldsetTarget.find(':checked').first());
            } else {
                fieldsetTarget.first().click();
            }
        }
    }
}

// =====================================================
// Clear inputs
// Used to reset an input when a radio button is changed
// eg clear a value text input when a Yes is changed to a No
// =======================================================
function clearInput(el){
  var target = el.attr("data-clear-target");
  if($("#"+target+"").length){
      $('#'+target).val("");
  }
}



// =====================================================
// Takes an element and subtracts one input from another to show a combined message
// e.g. used on gifts details
// =====================================================
  function checkforCombinedValue(el){
    // input we are taking as a start value
    var addValue = $('#' + el.attr('data-combine-add'));
    var addValueVal = 0;
    if(isNumeric(addValue.val())){
      addValueVal = addValue.val();
    }
    // input we are going to subtract from the start value
    var subtractValue = $('#' + el.attr('data-combine-subtract'));
    var subtractValueVal = 0;
    if(isNumeric(subtractValue.val())){
      subtractValueVal = subtractValue.val();
    }
    // calc
    var total = addValueVal - subtractValueVal;

    // conditionally show the message if we have a valid number
    if(total >= 0){
     if( (addValueVal.toString().indexOf('.')==-1) && (subtractValueVal.toString().indexOf('.')==-1) ) {
      el.html(el.attr('data-combine-copy') + total);
      } else {
      el.html(el.attr('data-combine-copy') + total.toFixed(2));}
    } else {
      // maybe need to show a message here if the calc is invalid to update screen readers
      el.html(el.attr('data-combine-copy') + 0);
    }
  }



// =====================================================
// Fires checkforCombinedValue on page load and on input change
// =====================================================
  function combinedValue(){
    $('.js-combined-value').each(function(){
      var el = $(this);
      var timer;
      function trackKey(){
        // gives a small delay after key input before updating value
        timer = setInterval(function(){
          clearInterval(timer);
          checkforCombinedValue(el);
        }, 500);
      }


      // check on page load
      checkforCombinedValue(el);

      // check when either input changes
      $('[aria-controls="' + el.attr('id') + '"]').each(function(){
        $(this).on('keyup blur', function(){
          if(timer != undefined){
            clearInterval(timer);
          }
          trackKey();
        });
      });

    });
  }



// ======================================================
// checks if a value is numeric
// ======================================================
function isNumeric(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}


// ======================================================
// Country select combo box
// ======================================================
function countryCodeAutoComplete() {

  (function( $ ) {
      $.widget( "custom.combobox", {
        _create: function() {
          this.wrapper = $( "<span>" )
            .insertAfter( this.element );

          this.element.hide();
          this._createAutocomplete();
          this.element.attr("id", this.element.attr("id")+"_");
        },

        _createAutocomplete: function() {
          var selected = this.element.children( ":selected" ),
            value = selected.val() ? selected.text() : "";

          this.input = $( "<input>" )
            .appendTo( this.wrapper )
            .val( value )
            .attr( "title", "" )
            .attr( "id", this.element.attr("id") )
            .addClass( "custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left form-control" )
            .autocomplete({
              delay: 0,
              minLength: 2,
              source: $.proxy( this, "_source" )
            });

          this._on( this.input, {
            autocompleteselect: function( event, ui ) {
              ui.item.option.selected = true;
              this._trigger( "select", event, {
                item: ui.item.option
              });
            },

            autocompletechange: "_removeIfInvalid"
          });
        },

        _source: function( request, response ) {
          var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
          response( this.element.children( "option" ).map(function() {
            var text = $( this ).text();
            if ( this.value && ( !request.term || matcher.test(text) ) )
              return {
                label: text,
                value: text,
                option: this
              };
          }) );
        },

        _removeIfInvalid: function( event, ui ) {

          // Selected an item, nothing to do
          if ( ui.item ) {
            return;
          }

          // Search for a match (case-insensitive)
          var value = this.input.val(),
            valueLowerCase = value.toLowerCase(),
            valid = false;
          this.element.children( "option" ).each(function() {
            if ( $( this ).text().toLowerCase() === valueLowerCase ) {
              this.selected = valid = true;
              return false;
            }
          });

          // Found a match, nothing to do
          if ( valid ) {
            return;
          }

          // Remove invalid value
          this.input
            .val( "" )
            .attr( "title", value + " didn't match any item" );
          this.element.val( "" );
          this.input.autocomplete( "instance" ).term = "";
        },

        _destroy: function() {
          this.wrapper.remove();
          this.element.show();
        }
      });
    })( jQuery );

  $(function() {
      $(".to-combobox").combobox();
  });

}