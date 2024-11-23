package com.example.soap.config;

import com.example.soap.modele.TypeCompte;
import com.example.soap.modele.Compte;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class SoapClient {

    private static final String NAMESPACE = "http://MacBook-Pro-de-Saad.local:8085/service/ws"; // Web service namespace
    private static final String URL = "http://MacBook-Pro-de-Saad.local:8085/service/ws"; // URL for your backend endpoint (not the WSDL)
    private static final String SOAP_ACTION = "http://MacBook-Pro-de-Saad.local:8085/service/ws"; // SOAP action URL
    private static final String METHOD_CREATE = "createCompte"; // Method for creating a compte
    private static final String METHOD_GET = "getCompteById"; // Method for getting a compte by ID


    // Method for creating a compte
    public static Compte createCompte(Double solde, TypeCompte type) {
        try {
            // Create SOAP request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_CREATE);

            // Add parameters to the request
            PropertyInfo soldeProperty = new PropertyInfo();
            soldeProperty.name = "Sold";
            soldeProperty.type = Double.class;
            soldeProperty.setValue(solde);
            request.addProperty(soldeProperty);

            PropertyInfo typeProperty = new PropertyInfo();
            typeProperty.name = "Type";
            typeProperty.type = TypeCompte.class;
            typeProperty.setValue(type);
            request.addProperty(typeProperty);

            // Create SOAP envelope
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);

            // Send the SOAP request
            HttpTransportSE httpTransport = new HttpTransportSE(URL);
            httpTransport.call(SOAP_ACTION + METHOD_CREATE, envelope);

            // Get the response
            SoapObject response = (SoapObject) envelope.getResponse();
            Double responseSolde = Double.valueOf(response.getProperty("Sold").toString());
            TypeCompte responseType = TypeCompte.valueOf(response.getProperty("Type").toString());

            // Return the created Compte object
            return new Compte(responseSolde, responseType);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method for getting a compte by ID
    public static Compte getCompteById(Long id) {
        try {
            // Create SOAP request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_GET);

            // Add parameter to the request
            PropertyInfo idProperty = new PropertyInfo();
            idProperty.name = "id";
            idProperty.type = Long.class;
            idProperty.setValue(id);
            request.addProperty(idProperty);

            // Create SOAP envelope
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);

            // Send the SOAP request
            HttpTransportSE httpTransport = new HttpTransportSE(URL);
            httpTransport.call(SOAP_ACTION + METHOD_GET, envelope);

            // Get the response
            SoapObject response = (SoapObject) envelope.getResponse();
            Double responseSolde = Double.valueOf(response.getProperty("Sold").toString());
            TypeCompte responseType = TypeCompte.valueOf(response.getProperty("Type").toString());

            // Return the fetched Compte object
            return new Compte(responseSolde, responseType);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
