package com.ilirium.license.xmllicense;

import com.ilirium.license.xmllicense.utils.IO;
import com.ilirium.license.xmllicense.utils.Strings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import static java.util.Collections.singletonList;
import java.util.List;
import java.util.UUID;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import static javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import static javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import static javax.xml.crypto.dsig.XMLSignature.XMLNS;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Simple class for generating XML License
 * 
 * @author DoDo <dopoljak@gmail.com>
 */
public class XMLLicenseUtil
{
    private final XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
    private PrivateKeyEntry keyEntry;
    private PublicKey pubKey;

    public XMLLicenseUtil()
    {
    }
    
    /**
     * Load keys from keystore
     */
    public XMLLicenseUtil(String pathToKeyStore, String keyStorePass, String privKeyPass) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException
    {
        // LOAD KEY STORE - CERTIFICATE
        final KeyStore keyStore = KeyStore.getInstance("pkcs12"); // P12
        final FileInputStream keystoreStream = new FileInputStream(pathToKeyStore);
        keyStore.load(keystoreStream, keyStorePass.toCharArray());
        
        // LOAD PRIVATE KEY
        final String alias = keyStore.aliases().nextElement();
        keyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, new PasswordProtection(privKeyPass.toCharArray()));
        pubKey = keyEntry.getCertificate().getPublicKey();
    }
    
    /**
     * Validate XML document signature using provided PublicKey
     */
    public boolean validate(String signedXML, PublicKey publicKey) throws ParserConfigurationException, SAXException, IOException, MarshalException, XMLSignatureException
    {
        final ByteArrayInputStream bais = new ByteArrayInputStream(signedXML.getBytes());
        
        // LOAD SIGNED DOCUMENT
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        //documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        final Document document = documentBuilderFactory.newDocumentBuilder().parse(bais);
        
        // GET SIGNATURE ELEMENT
        final NodeList nodeList = document.getElementsByTagNameNS(XMLNS, "Signature");
        if (nodeList.getLength() == 0) {
            throw new IllegalArgumentException("Signature element is not found");
        }
        final Node item = nodeList.item(0);
        
        // VALIDATE SIGNATURE        
        final DOMValidateContext validateContext = new DOMValidateContext(publicKey, item);
        final XMLSignature signature = factory.unmarshalXMLSignature(validateContext);
        return signature.validate(validateContext);
    }
    
    /**
     * Validate XML document signature
     */
    public boolean validate(String signedXML) throws ParserConfigurationException, SAXException, IOException, MarshalException, XMLSignatureException
    {
        return this.validate(signedXML, pubKey);
    }
    
    /**
     * Sign XML document
     * @param xml
     * @return
     * @throws Exception 
     */
    public String sign(String xml) throws Exception
    {
        final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

        // LOAD DOCUMENT
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        final Document document = documentBuilderFactory.newDocumentBuilder().parse(bais);

        // CREATE SIGNER INFO                
        final DigestMethod digestMethod = factory.newDigestMethod(DigestMethod.SHA256, null);
        final Transform transform = factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        final Reference reference = factory.newReference("", digestMethod, singletonList(transform), null, null);
        final SignatureMethod signatureMethod = factory.newSignatureMethod(RSA_SHA1, null);
        final CanonicalizationMethod canonicalizationMethod = factory.newCanonicalizationMethod(INCLUSIVE, (C14NMethodParameterSpec) null);
        final SignedInfo signedInfo = factory.newSignedInfo(canonicalizationMethod, signatureMethod, singletonList(reference));

        // CREATE KEYS        
        final X509Certificate certificate = (X509Certificate) keyEntry.getCertificate();
        final List<Serializable> x509Content = new ArrayList<Serializable>();
        x509Content.add(certificate.getSubjectX500Principal().getName());
        x509Content.add(certificate);
        final KeyInfoFactory keyInfoFactory = factory.getKeyInfoFactory();
        final X509Data data = keyInfoFactory.newX509Data(x509Content);
        final KeyInfo keyInfo = keyInfoFactory.newKeyInfo(singletonList(data));
        
        // CREATE SIGNATURE
        final DOMSignContext signContext = new DOMSignContext(keyEntry.getPrivateKey(), document.getDocumentElement());
        final XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo);
        signature.sign(signContext);
        
        // WRITE SIGNED DOCUMENT TO DISK
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();        
        //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");        
        transformer.transform(new DOMSource(document), new StreamResult(baos));
        return baos.toString();
    }

    public static void main(String[] args) throws Exception
    {
        if(args == null || args.length < 4)
        {
            System.out.println("");
            System.out.println("");
            System.out.println("application [PATH-TO-P12-CERTIFICATE] [P12 PASSWORD] [PRIVATE KEY PASSWORD] [PATH-TO-UNSIGNED-XML]");
            System.out.println("example: application C:\\my_keystore.p12 my_password my_password C:\\unsigned_license.xml");
            System.out.println("signed document will be saved in same directory where unsigned file is located under name 'signed.xml'");
            System.out.println("");
            System.out.println("");
            return;
        }
        
        // get arguments
        final String pathKeyStore = args[0];
        final String passKeyStore = args[1];
        final String passPrivKey = args[2];
        final File pathToUnsignedXML = new File(args[3]);
        
        // load XML
        final String xmlFile = new String(IO.readFile(pathToUnsignedXML.getAbsoluteFile().toString()));
        
        // validate XML
        final XMLLicense xmlLicense = XMLLicense.parse(xmlFile);
        
        // generate new UUID
        if(xmlLicense.getUuid() == null || xmlLicense.getUuid().isEmpty()) {
            System.out.println("Generating new UUID for license ...");
            xmlLicense.setUuid(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
        }
        
        // serialize to XML
        final String parsedXML = xmlLicense.toXML();
        System.out.println("Parsed licence = " + parsedXML);
        
        // init signature
        final XMLLicenseUtil xml = new XMLLicenseUtil(pathKeyStore, passKeyStore, passPrivKey);     
        final String publicKeyHEX = Strings.toHEX(xml.pubKey.getEncoded());

        // sign document
        final String signedFile = xml.sign(parsedXML);
        final boolean isValid = xml.validate(signedFile);
        System.out.println("is signature valid = " + isValid);
        
        final String parent_path = pathToUnsignedXML.getParent() != null ? pathToUnsignedXML.getParent() + "\\" : "";
        
        // save signed
        IO.saveFile(parent_path + "signed.xml", signedFile.getBytes());
        IO.saveFile(parent_path + "publicKey.hex.txt", publicKeyHEX.getBytes());
    }
}
