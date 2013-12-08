package com.ilirium.license.xmllicense;

import com.ilirium.license.xmllicense.utils.IO;
import com.ilirium.license.xmllicense.utils.Keys;
import com.ilirium.license.xmllicense.utils.Strings;
import java.security.PublicKey;
import junit.framework.TestCase;

/**
 *
 * @author DoDo <dopoljak@gmail.com>
 */
public class TestXMLLicense extends TestCase
{
    public void test1_encode() throws Exception
    {
        // generate CERTIFICATE only once using keytool, store public key in application & validate signatures over generated license ...
        // keytool.exe -genkeypair -alias my_certificate -keystore my_keystore.pfx -storepass my_password -validity 365 -keyalg RSA -keysize 2048 -storetype pkcs12

        // generate license
        String argsaa[] = new String[] { "src/main/resources/my_keystore.pfx", "my_password", "my_password", "src/main/resources/unsigned_license.xml" };
        XMLLicenseUtil.main(argsaa);

        // load licence
        final String signedXML = new String( IO.readFile( "src/main/resources/signed.xml" ) );

        // load public key        
        final byte[] publiKeyBytes = Strings.fromHEX(new String(IO.readFile("src/main/resources/publicKey.hex.txt")));
        final PublicKey pubKey = Keys.convertToPublicKey(publiKeyBytes);
        
        final boolean isValid = new XMLLicenseUtil().validate(signedXML, pubKey);
        System.out.println("Signed is valid = " + isValid);
        assertTrue(isValid);
         
        // parse licence to POJO & load data (IP address, institution, etc ... )
        final XMLLicense licence = XMLLicense.parse(signedXML);
        System.out.println("XMLLicense = " + licence.toXML());     
    }
}
