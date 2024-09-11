package com.example.demodesktopcertificateextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.*;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.*;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.token.*;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.awt.Font;
import java.awt.Color;


public class HelloApplication extends Application {
    public String filePathFinal;
    private static final String JSON_LIST_URL = "http://localhost:8089/api/get-certificates-list";

    private static final String JSON_LIST_URL_PROMPT = "http://localhost:8089/api/show-certificates-list";
    private static final String JSON_XML_SIGN_URL = "http://localhost:8089/api/sign-hash";
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demo Signature App");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        Label label = new Label("Fetch Certificates List");


        Button submitButton = new Button("Fetch Certificates");
        Button selectFileButton = new Button("Select File");

        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");

            // Set initial directory
            // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            // Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pdf files (*.pdf)", "*.pdf","*.xml");
            fileChooser.getExtensionFilters().add(extFilter);

            // Show open file dialog
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                this.filePathFinal = selectedFile.getAbsolutePath();
                //System.out.println("Selected file: " + filePath);
                // You can use filePath as needed
            }
        });
        ListView<CertificateInfo>

                responseListView = new ListView<>();
        responseListView.setCellFactory(new Callback<ListView<CertificateInfo>, ListCell<CertificateInfo>>() {
            @Override
            public ListCell<CertificateInfo> call(ListView<CertificateInfo> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(CertificateInfo item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            //System.out.println(item.getName());
                            setText(item.getName().substring(3,item.getName().indexOf("2.5.4.5")-1));
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        responseListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // Handle single click
                CertificateInfo selectedObject = responseListView.getSelectionModel().getSelectedItem();
                List<CertificateToken> certTokens = new ArrayList<>();
               // CertificateToken baseCertificate = null;
                if (selectedObject != null) {
                    //signXmlDemo(filePathFinal,"aSdf1234**","3C70BBE13F880766");
                    //signPdfDemo(filePathFinal,"aSdf1234**","3C70BBE13F880766");
                    //signPdfDemoWithCertificate(filePathFinal,"aSdf1234**","3C70BBE13F880766");
                    File file = new File(filePathFinal);
                    DSSDocument toSignDocument = new FileDocument(file);
                    PAdESSignatureParameters parameters = new PAdESSignatureParameters();
                    Task<String> task = new Task<>() {

                        @Override
                        protected String call() {
                            try {
                                //List<CertificateToken> certTokens = new ArrayList<>();
                                for(String certData : selectedObject.certificateChain) {
                                    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(certData));
                                    ObjectInput in = new ObjectInputStream(bis);
                                    X509Certificate cert = (X509Certificate) in.readObject();
                                    bis.close();
                                    certTokens.add(new CertificateToken(cert));
                                }
                                URL url = new URL(JSON_XML_SIGN_URL);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                Map<String, String> requestBodyMap = new HashMap<>();
                                requestBodyMap.put("alias",selectedObject.alias );
                                requestBodyMap.put("type",selectedObject.type);

                                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
                                parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

                                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(selectedObject.getCertificate()));
                                ObjectInput in = new ObjectInputStream(bis);
                                X509Certificate cert = (X509Certificate) in.readObject();
                                bis.close();
                                CertificateToken baseCertificate = new CertificateToken(cert);
                                parameters.setSigningCertificate(baseCertificate);
                                parameters.setCertificateChain(certTokens);

                                SignatureImageParameters imageParameters = new SignatureImageParameters();
                                SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
                                DSSFont font = new DSSJavaFont(Font.SERIF);
                                font.setSize(8);
                                textParameters.setFont(font);
                                textParameters.setTextColor(Color.BLUE);
                                textParameters.setText(baseCertificate.getSubject().getPrincipal().getName().substring(3,24) + "\n" + "My Signature");
                                imageParameters.setTextParameters(textParameters);
                                SignatureFieldParameters fieldParameters = new SignatureFieldParameters();
                                imageParameters.setFieldParameters(fieldParameters);
                                fieldParameters.setOriginX(200);
                                fieldParameters.setOriginY(600);
                                parameters.setImageParameters(imageParameters);
                                CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
                                PAdESService service = new PAdESService(commonCertificateVerifier);
                                ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

                                requestBodyMap.put("hash", Base64.getEncoder().encodeToString(dataToSign.getBytes()));
                                ObjectMapper objectMapper = new ObjectMapper();
// Convert the map to JSON string
                                String requestBody = objectMapper.writeValueAsString(requestBodyMap);
                                connection.setDoOutput(true);
                                connection.getOutputStream().write(requestBody.getBytes());
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                reader.close();
                                System.out.println("response: " + response);
                                return response.toString();
                            } catch(Exception e) {
                                System.out.println("exception: " + e.getMessage());
                                return null;
                            }

                        }
                    };

                    task.setOnSucceeded(workerStateEvent -> {
                        System.out.println("taskValue: "+ task.getValue());
                        String response = task.getValue();
                        System.out.println("Full response string: " + response);
                        System.out.println("response substring: " + response.substring(85,response.length()-2));
                        byte[] bytes = Utils.fromBase64(escapeCharFromSignature(response.substring(85,response.length()-2)));

                        // this portion is for pdf signature

                       // PAdESSignatureParameters parameters = new PAdESSignatureParameters();
                        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
                        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
                        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
                        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(selectedObject.getCertificate()));
                        ObjectInput in = null;

                        try {
                            in = new ObjectInputStream(bis);
                            X509Certificate cert = (X509Certificate) in.readObject();
                            bis.close();
                            CertificateToken baseCertificate = new CertificateToken(cert);
                            parameters.setSigningCertificate(baseCertificate);
                            parameters.setCertificateChain(certTokens);
                            SignatureImageParameters imageParameters = new SignatureImageParameters();
                            SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
                            DSSFont font = new DSSJavaFont(Font.SERIF);
                            font.setSize(8);
                            textParameters.setFont(font);
                            textParameters.setTextColor(Color.BLUE);
                            textParameters.setText(baseCertificate.getSubject().getPrincipal().getName().substring(3,24) + "\n" + "My Signature");
                            imageParameters.setTextParameters(textParameters);
                            SignatureFieldParameters fieldParameters = new SignatureFieldParameters();
                            imageParameters.setFieldParameters(fieldParameters);
                            fieldParameters.setOriginX(200);
                            fieldParameters.setOriginY(600);
                            //fieldParameters.setFieldId("ExistingSignatureField");
                            parameters.setImageParameters(imageParameters);
                            CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
                            PAdESService service = new PAdESService(commonCertificateVerifier);
                            OnlineTSPSource tspSource = new OnlineTSPSource("http://tsa.belgium.be/connect");
                            service.setTspSource(tspSource);
                            SignatureValue signatureValue = new SignatureValue(parameters.getSignatureAlgorithm(), bytes);
                            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
                            signedDocument.save("/Users/bccca/Desktop/deliverables/signed-final.pdf");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println("This is Successful");
                    });
                    task.setOnFailed(workerStateEvent -> {
                        System.out.println("failedValue" + task.getValue());
                    });

                    new Thread(task).start();
                }
            }
        });

        submitButton.setOnAction(event -> {
            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    // Replace URL with your endpoint

                    Map<String, String> requestBodyMap = new HashMap<>();
                    requestBodyMap.put("type","Dongle");
                    ObjectMapper objectMapper = new ObjectMapper();
                    String requestBody = objectMapper.writeValueAsString(requestBodyMap);
                    //connection.setDoOutput(true);

                    /*BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();*/
                    URL url = new URL(JSON_LIST_URL_PROMPT);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.getOutputStream().write(requestBody.getBytes());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                }
            };

            task.setOnSucceeded(workerStateEvent -> {
                System.out.println("task value: " + task.getValue());
                String response = task.getValue();
                Gson gson = new Gson();
                CertificateFetchResponse fetchedList = gson.fromJson(response, CertificateFetchResponse.class);
                for(int i=0; i<fetchedList.certificateList.size(); i++) {
                    responseListView.getItems().add(fetchedList.certificateList.get(i));
                }

            });

            new Thread(task).start();
        });

        vbox.getChildren().addAll(label, submitButton, selectFileButton, responseListView);

        //vbox.getChildren().addAll(passwordField, submitButton, responseListView);

        primaryStage.setScene(new Scene(vbox, 300, 250));
        primaryStage.show();
    }

    public void signPdfDemoWithCertificate(String filePath, String password, String alias) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("password", password);
            StringEntity params = new StringEntity(jsonRequest.toString());

            HttpClient clientForRequest = HttpClient.newHttpClient();

            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("http://localhost:8089/api/get-certificates-list"))
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(EntityUtils.toString(params)))
                    .build();

            HttpResponse<String> httpResponse = clientForRequest.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = httpResponse.body();
            ObjectMapper objectMapper = new ObjectMapper();
            Response responseJSON = objectMapper.readValue(jsonResponse, Response.class);
            CertificateInfo info = responseJSON.certificateList.get(0);
            System.out.println(info.certificate);
            List<CertificateToken> certTokens = new ArrayList<>();
            for(String certData : info.certificateChain) {
                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(certData));
                ObjectInput in = new ObjectInputStream(bis);
                X509Certificate cert = (X509Certificate) in.readObject();
                bis.close();
                certTokens.add(new CertificateToken(cert));
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(info.certificate));
            ObjectInput in = new ObjectInputStream(bis);
            X509Certificate cert = (X509Certificate) in.readObject();
            bis.close();
            CertificateToken baseCertificate = new CertificateToken(cert);


            File file = new File(filePath);
            DSSDocument toSignDocument = new FileDocument(file);
            //String pkcs12TokenFile = "C:\\Users\\Ahad\\Downloads\\deliverables-V1\\SampleAhad.p12.pfx";
            //SignatureTokenConnection signingToken = new Pkcs12SignatureToken(pkcs12TokenFile, new KeyStore.PasswordProtection("bccca".toCharArray()));
            //DSSPrivateKeyEntry privateKey = signingToken.getKeys().get(0);
            PAdESSignatureParameters parameters = new PAdESSignatureParameters();
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setCertificateChain(certTokens);
            parameters.setSigningCertificate(baseCertificate);
            //parameters.setSigningCertificate(privateKey.getCertificate());
            //parameters.setCertificateChain(privateKey.getCertificateChain());


            SignatureImageParameters imageParameters = new SignatureImageParameters();
            SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
            DSSFont font = new DSSJavaFont(Font.SERIF);
            font.setSize(8); // Specifies the text size value (the default font size is 12pt)
            textParameters.setFont(font);
            textParameters.setTextColor(Color.BLUE);
            //textParameters.setText(privateKey.getCertificate().getCertificate().getSubjectX500Principal().getName().substring(3,24) + "\n" +
             //       "My signature");
            textParameters.setText(baseCertificate.getCertificate().getSubjectX500Principal().getName().substring(3,24) + "\n" +
                    "My signature");
            imageParameters.setTextParameters(textParameters);
            SignatureFieldParameters fieldParameters = new SignatureFieldParameters();
            imageParameters.setFieldParameters(fieldParameters);
            fieldParameters.setOriginX(200);
            fieldParameters.setOriginY(600);
            //fieldParameters.setFieldId("ExistingSignatureField");


            parameters.setImageParameters(imageParameters);
            CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
            PAdESService service = new PAdESService(commonCertificateVerifier);
            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

            DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
            //Digest digest = new Digest(digestAlgorithm, addPadding(DSSUtils.digest(digestAlgorithm, dataToSign.getBytes())));
            //Digest digest = new Digest(digestAlgorithm, addPadding(DSSUtils.digest(digestAlgorithm, dataToSign.getBytes())));
            //Digest digest = new Digest(digestAlgorithm,DSSUtils.digest(digestAlgorithm, dataToSign.getBytes()));
            System.out.println("raw hash: " + Base64.getEncoder().encodeToString(dataToSign.getBytes()));
            //Digest digest = new Digest(digestAlgorithm, addPadding(DSSUtils.digest(digestAlgorithm,dataToSign.getBytes())));
            Digest digest = new Digest(DigestAlgorithm.SHA256, addPadding(DSSUtils.digest(DigestAlgorithm.SHA256, dataToSign.getBytes())));
            JSONObject json = new JSONObject();
            json.put("password", password);
            json.put("pdfHash", Base64.getEncoder().encodeToString(digest.getValue()));
            json.put("alias",alias);
            StringEntity paramsTemp = new StringEntity(json.toString());

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8089/api/sign-pdf"))
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(EntityUtils.toString(paramsTemp)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("response from pdf func:" + response.body().toString());
            System.out.println("truncated response hash: " + response.body().substring(69,response.body().toString().length()-2));
            //System.out.println("Enter your string: \n");

            //Scanner scanner = new Scanner(System.in);
            //String str = scanner.nextLine();
            byte[] bytes = Utils.fromBase64(escapeCharFromSignature(response.body().substring(69,response.body().toString().length()-2)));
            SignatureValue signatureValue = new SignatureValue(parameters.getSignatureAlgorithm(), bytes);

            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
            signedDocument.save("C:\\Users\\Ahad\\Downloads\\deliverables-V1\\signed-demo.pdf");

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void signPdfDemo(String filePath, String password, String alias) {
        try {
            File file = new File(filePath);
            DSSDocument toSignDocument = new FileDocument(file);
            String pkcs12TokenFile = "C:\\Users\\Ahad\\Downloads\\deliverables-V1\\SampleAhad.p12.pfx";
            SignatureTokenConnection signingToken = new Pkcs12SignatureToken(pkcs12TokenFile, new KeyStore.PasswordProtection("bccca".toCharArray()));
            DSSPrivateKeyEntry privateKey = signingToken.getKeys().get(0);
            PAdESSignatureParameters parameters = new PAdESSignatureParameters();
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(privateKey.getCertificate());
            parameters.setCertificateChain(privateKey.getCertificateChain());


            SignatureImageParameters imageParameters = new SignatureImageParameters();
            SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
            DSSFont font = new DSSJavaFont(Font.SERIF);
            font.setSize(8); // Specifies the text size value (the default font size is 12pt)
            textParameters.setFont(font);
            textParameters.setTextColor(Color.BLUE);
            textParameters.setText(privateKey.getCertificate().getCertificate().getSubjectX500Principal().getName().substring(3,24) + "\n" +
                    "My signature");
            imageParameters.setTextParameters(textParameters);
            SignatureFieldParameters fieldParameters = new SignatureFieldParameters();
            imageParameters.setFieldParameters(fieldParameters);
            fieldParameters.setOriginX(200);
            fieldParameters.setOriginY(600);
            //fieldParameters.setFieldId("ExistingSignatureField");


            parameters.setImageParameters(imageParameters);
            CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
            PAdESService service = new PAdESService(commonCertificateVerifier);
            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

            DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
            //Digest digest = new Digest(digestAlgorithm, addPadding(DSSUtils.digest(digestAlgorithm, dataToSign.getBytes())));
            //Digest digest = new Digest(digestAlgorithm, addPadding(DSSUtils.digest(digestAlgorithm, dataToSign.getBytes())));
            //Digest digest = new Digest(digestAlgorithm,DSSUtils.digest(digestAlgorithm, dataToSign.getBytes()));
            System.out.println("raw hash: " + Base64.getEncoder().encodeToString(dataToSign.getBytes()));
            //Digest digest = new Digest(digestAlgorithm, addPadding(DSSUtils.digest(digestAlgorithm,dataToSign.getBytes())));
            Digest digest = new Digest(DigestAlgorithm.SHA256, addPadding(DSSUtils.digest(DigestAlgorithm.SHA256, dataToSign.getBytes())));
            JSONObject json = new JSONObject();
            json.put("password", password);
            json.put("pdfHash", Base64.getEncoder().encodeToString(digest.getValue()));
            json.put("alias",alias);
            StringEntity params = new StringEntity(json.toString());

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8089/api/sign-pdf"))
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(EntityUtils.toString(params)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("response from pdf func:" + response.body().toString());
            System.out.println("truncated response hash: " + response.body().substring(69,response.body().toString().length()-2));
            //System.out.println("Enter your string: \n");

            //Scanner scanner = new Scanner(System.in);
            //String str = scanner.nextLine();
            byte[] bytes = Utils.fromBase64(escapeCharFromSignature(response.body().substring(69,response.body().toString().length()-2)));
            SignatureValue signatureValue = new SignatureValue(parameters.getSignatureAlgorithm(), bytes);

            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
            signedDocument.save("C:\\Users\\Ahad\\Downloads\\deliverables-V1\\signed-demo.pdf");

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void signXmlDemo(String filePath, String password, String alias) {
        try {
            File file = new File(filePathFinal);
            DSSDocument toSignDocument = new FileDocument(file);

            String pkcs12TokenFile = "C:\\Users\\Ahad\\Downloads\\deliverables-V1\\SampleAhad.p12.pfx";
            SignatureTokenConnection signingToken = new Pkcs12SignatureToken(pkcs12TokenFile, new KeyStore.PasswordProtection("bccca".toCharArray()));
            DSSPrivateKeyEntry privateKey = signingToken.getKeys().get(0);
            XAdESSignatureParameters parameters = new XAdESSignatureParameters();
            parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
            parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED); // signature part of xml
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(privateKey.getCertificate());
            parameters.setCertificateChain(privateKey.getCertificateChain());

            CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
            XAdESService service = new XAdESService(commonCertificateVerifier);
            OnlineTSPSource tspSource = new OnlineTSPSource("http://tsa.belgium.be/connect");
            service.setTspSource(tspSource);

            DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();

            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);
            Digest digest = new Digest(digestAlgorithm,
                    addPadding(DSSUtils.digest(digestAlgorithm, dataToSign.getBytes())));
            //Digest digest = new Digest(digestAlgorithm,
                    //addPadding(DSSUtils.digest(digestAlgorithm, dataToSign.getBytes())));
            String digestBase64 = Base64.getEncoder().encodeToString(DSSUtils.digest(digestAlgorithm, dataToSign.getBytes()));
            System.out.println("digest base64: \n" + digestBase64);

            JSONObject json = new JSONObject();
            json.put("password", password);
            json.put("xmlHash", digestBase64);
            json.put("alias",alias);
            StringEntity params = new StringEntity(json.toString());

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8089/api/sign-xml"))
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(EntityUtils.toString(params)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("response :" + response.body().toString());

            byte[] bytes = Utils.fromBase64(escapeCharFromSignature(response.body().substring(53,response.body().toString().length())));

            SignatureValue signatureValue = new SignatureValue(parameters.getSignatureAlgorithm(), bytes);
            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
            signedDocument.save("C:\\Users\\Ahad\\Downloads\\deliverables-V1\\signed-demo.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }   catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String signPdfDemoPrompt(String filePath, String alias) {
        try {
            File file = new File(filePath);
            DSSDocument toSignDocument = new FileDocument(file);
            String pkcs12TokenFile = "/Users/bccca/Downloads/deliverables-V1/sample.p12";
            SignatureTokenConnection signingToken = new Pkcs12SignatureToken(pkcs12TokenFile, new KeyStore.PasswordProtection("bccca".toCharArray()));
            DSSPrivateKeyEntry privateKey = signingToken.getKeys().get(0);
            PAdESSignatureParameters parameters = new PAdESSignatureParameters();
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(privateKey.getCertificate());
            parameters.setCertificateChain(privateKey.getCertificateChain());


            SignatureImageParameters imageParameters = new SignatureImageParameters();
            SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
            DSSFont font = new DSSJavaFont(Font.SERIF);
            font.setSize(8); // Specifies the text size value (the default font size is 12pt)
            textParameters.setFont(font);
            textParameters.setTextColor(Color.BLUE);
            textParameters.setText(privateKey.getCertificate().getCertificate().getSubjectX500Principal().getName().substring(3,24) + "\n" +
                    "My signature");
            imageParameters.setTextParameters(textParameters);
            SignatureFieldParameters fieldParameters = new SignatureFieldParameters();
            fieldParameters.setOriginX(200);
            fieldParameters.setOriginY(1200);
            fieldParameters.setFieldId("ExistingSignatureField");

            parameters.setImageParameters(imageParameters);
            CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
            PAdESService service = new PAdESService(commonCertificateVerifier);
            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

            DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
            Digest digest = new Digest(digestAlgorithm, addPadding(DSSUtils.digest(digestAlgorithm, dataToSign.getBytes())));

            JSONObject json = new JSONObject();
            json.put("pdfHash", Base64.getEncoder().encodeToString(digest.getValue()));
            json.put("alias",alias);
            StringEntity params = new StringEntity(json.toString());

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8089/api/sign-pdf"))
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(EntityUtils.toString(params)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().toString();

            //System.out.println("response :" + response.body().toString());
            /*byte[] bytes = Utils.fromBase64(escapeCharFromSignature(response.body().substring(53,response.body().toString().length())));
            SignatureValue signatureValue = new SignatureValue(parameters.getSignatureAlgorithm(), bytes);

            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
            signedDocument.save("/Users/bccca/Downloads/deliverables-V1/signed-demo.pdf");
*/
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public String signXmlDemoPrompt(String filePath, String alias) {
        try {
            File file = new File(filePathFinal);
            DSSDocument toSignDocument = new FileDocument(file);

            String pkcs12TokenFile = "/Users/bccca/Downloads/deliverables-V1/samplebd.pfx";
            SignatureTokenConnection signingToken = new Pkcs12SignatureToken(pkcs12TokenFile, new KeyStore.PasswordProtection("bccca".toCharArray()));
            DSSPrivateKeyEntry privateKey = signingToken.getKeys().get(0);
            XAdESSignatureParameters parameters = new XAdESSignatureParameters();
            parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
            parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED); // signature part of xml
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(privateKey.getCertificate());
            parameters.setCertificateChain(privateKey.getCertificateChain());

            CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
            XAdESService service = new XAdESService(commonCertificateVerifier);
            OnlineTSPSource tspSource = new OnlineTSPSource("http://tsa.belgium.be/connect");
            service.setTspSource(tspSource);

            DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
            //System.out.println("digest base64: \n" + Base64.getEncoder().encodeToString(digest.getValue()));

            JSONObject json = new JSONObject();
            //json.put("xmlHash", Base64.getEncoder().encodeToString(digest.getValue()));
            json.put("alias",alias);
            StringEntity params = new StringEntity(json.toString());

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8089/api/sign-xml-prompt"))
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(EntityUtils.toString(params)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().toString();
            //System.out.println("response :" + response.body().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }   catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] addPadding(byte[] digest) {
        //		digestAlgorithm.
        byte[] sha256bytes = new byte[] { 0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03,
                0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20 };
        return ArrayUtils.addAll(sha256bytes, digest); // should find the prefix by checking digest length?
    }

    public DSSPrivateKeyEntry getDSSPrivateKeyEntry(X509Certificate certificate) {
        PasswordInputCallback passwordInputCallback = new PasswordInputCallback() {
            @Override
            public char[] getPassword() {
                return "aSdf1234***".toCharArray();
            }
        };
        try(Pkcs11SignatureToken token = new Pkcs11SignatureToken("/usr/local/lib/libeTPkcs11.dylib",passwordInputCallback)) {
            List<DSSPrivateKeyEntry> keys = token.getKeys();
            for(DSSPrivateKeyEntry entry: keys) {
                if(entry.getCertificate().getCertificate().equals(certificate)) {
                    return entry;
                }
            }

        }
        return null;

    }
    private static String escapeCharFromSignature(String str) {
        return str.replace("\\r\\n", "");
    }

    public static void main(String[] args) {
        launch(args);
    }
}