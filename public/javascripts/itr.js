$(document).ready($(function() {

    var hidden = $("#hidden-field")
    var show = $("#show")
    var hide = $("#hide")

    if(show.hasClass("selected")){
        hidden.show();
    }

    show.on("change", function () {
        hidden.show();
    })

    hide.on("change", function () {
        hidden.hide();
        $("#day").val("");
        $("#month").val("");
        $("#year").val("");
        $("#error-summary-display").hide();
    })

}));