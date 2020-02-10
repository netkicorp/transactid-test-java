$(document).ready(function () {
    $("#alert-error-configuration").hide();
    $("#alert-success-configuration").hide();
    $("#form-invoice-request").hide();
    $("#alert-error-invoice-request").hide();
    $("#form-send-invoice-request").hide();
    $("#alert-error-send-invoice-request").hide();
    $("#form-create-payment").hide();
    $("#alert-error-create-payment").hide();
    $("#form-send-payment").hide();
    $("#alert-error-send-payment").hide();
    $("#alert-success-process").hide();

    $("#form-configuration").submit(function (event) {
        event.preventDefault();
        sendConfiguration();
    });

    $("#form-invoice-request").submit(function (event) {
        event.preventDefault();
        createInvoice();
    });

    $("#form-send-invoice-request").submit(function (event) {
        event.preventDefault();
        sendInvoice();
    });

    $("#form-create-payment").submit(function (event) {
        event.preventDefault();
        createPayment();
    });

    $("#form-send-payment").submit(function (event) {
        event.preventDefault();
        sendPayment();
    });

});

function sendConfiguration() {
    $("#alert-error-configuration").hide();

    var configuration = {}

    configuration["walletAddress"] = $("#wallet-address").val();
    configuration["privateKey"] = $("#private-key").val();
    configuration["certificate"] = $("#certificate").val();

    if (configuration["walletAddress"].length == 0 ||
        configuration["privateKey"].length == 0 ||
        configuration["certificate"].length == 0) {
            $("#alert-error-configuration").show();
            return;
    }

    $("#btn-configure").prop("disabled", true);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/configure",
        data: JSON.stringify(configuration),
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {
            $("#alert-success-configuration").show();
            $("#form-invoice-request").show();
        },
        error: function (e) {
            $("#btn-configure").prop("disabled", false);
            $("#alert-error-configuration").show();
            $("#alert-error-configuration").text("Error:" + JSON.stringify(e, null, 4));
        }
    });
}

function createInvoice() {
    $("#alert-error-invoice-request").hide();

    var invoiceRequestParameters = {}
    invoiceRequestParameters["amount"] = $("#amount").val();
    invoiceRequestParameters["memo"] = $("#memo").val();
    invoiceRequestParameters["notificationUrl"] = $("#notification-url").val();

    if (invoiceRequestParameters["amount"].length == 0 ||
        invoiceRequestParameters["memo"].length == 0 ||
        invoiceRequestParameters["notificationUrl"].length == 0) {
        $("#alert-error-invoice-request").show();
        return;
    }

    $("#btn-create-invoice").prop("disabled", true);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/create_invoice",
        data: JSON.stringify(invoiceRequestParameters),
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {
            var json = "<h4>Invoice Request created</h4><pre>" + JSON.stringify(data, null, 4) + "</pre>";
            $('#alert-invoice-request-content').html(json);
            $("#form-send-invoice-request").show();
        },
        error: function (e) {
            $("#btn-create-invoice").prop("disabled", false);
            $("#alert-error-invoice-request").show();
            $("#alert-error-invoice-request").text("Error:" + JSON.stringify(e, null, 4));
        }
    });

}

function sendInvoice() {
    $("#btn-send-invoice-request").prop("disabled", true);
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/send_invoice",
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {
            var json = "<h4>Payment Request received</h4><pre>" + JSON.stringify(data, null, 4) + "</pre>";
            $('#alert-payment-request-content').html(json);
            $("#form-create-payment").show();
        },
        error: function (e) {
            $("#btn-send-invoice-request").prop("disabled", false);
            $("#alert-error-send-invoice-request").show();
            $("#alert-error-send-invoice-request").text("Error:" + JSON.stringify(e, null, 4));
        }
    });
}

function createPayment() {
    $("#btn-create-payment").prop("disabled", true);
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/create_payment",
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {
            var json = "<h4>Payment created</h4><pre>" + JSON.stringify(data, null, 4) + "</pre>";
            $('#alert-payment-content').html(json);
            $("#form-send-payment").show();
        },
        error: function (e) {
            $("#btn-create-payment").prop("disabled", false);
            $("#alert-error-create-payment").show();
            $("#alert-error-create-payment").text("Error:" + JSON.stringify(e, null, 4));
        }
    });
}

function sendPayment() {
    $("#btn-send-payment").prop("disabled", true);
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/send_payment",
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {
            var json = "<h4>PaymentAck received</h4><pre>" + JSON.stringify(data, null, 4) + "</pre>";
            $('#alert-payment-ack-content').html(json);
            $("#alert-success-process").show();
        },
        error: function (e) {
            $("#btn-send-payment").prop("disabled", false);
            $("#alert-error-send-payment").show();
            $("#alert-error-send-payment").text("Error:" + JSON.stringify(e, null, 4));
        }
    });
}
