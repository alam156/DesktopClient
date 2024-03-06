module com.example.demodesktopcertificateextractor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires java.net.http;
    requires jpms_dss_xades;
    requires  jpms_dss_pades;
    requires jpms_dss_cades;
    requires jpms_dss_document;
    requires jpms_dss_service;
    requires jpms_dss_token;
    requires jpms_dss_tsl_validation;
    requires jpms_dss_validation_policy;
    requires jpms_dss_utils_apache_commons;
    requires jpms_dss_utils;
    requires jpms_dss_spi;
    requires jpms_dss_enumerations;
    requires jpms_dss_model;
    requires jpms_dss_pades_openpdf;
    requires jpms_dss_asic_cades;
    requires jpms_dss_asic_common;
    requires org.json;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;




    opens com.example.demodesktopcertificateextractor to javafx.fxml;

    exports com.example.demodesktopcertificateextractor;
}