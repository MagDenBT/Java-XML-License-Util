package com.ilirium.license.xmllicense;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author DoDo <dopoljak@gmail.com>
 */
@XmlRootElement(name = "licence")
public class XMLLicense
{
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(XMLLicense.class);
        }
        catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }
    
    private String uuid;    
    private String institution;
    private Date expires;

    public XMLLicense()
    {
    }

    @XmlElement(name = "uuid")
    public String getUuid()
    {
        return uuid;
    }
    
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    @XmlElement(name = "institution")
    public String getInstitution()
    {
        return institution;
    }

    public void setInstitution(String institution)
    {
        this.institution = institution;
    }
    
    

    @XmlElement(name = "expires")
    @XmlJavaTypeAdapter(DateFormatterAdapter.class)
    public Date getExpires()
    {
        return expires;
    }

    public void setExpires(Date expires)
    {
        this.expires = expires;
    }
    
    

    /**
     * Parse XML document to POJO class
     *
     * @param xml
     * @return
     * @throws JAXBException
     */
    public static XMLLicense parse(String xml) throws JAXBException
    {
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler()
        {
            public boolean handleEvent(ValidationEvent event)
            {
                if (event.getMessage().toLowerCase().contains("unexpected element")) {
                    return true;
                }
                return false;
            }
        });
        final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        return (XMLLicense) unmarshaller.unmarshal(bais);
    }

    /**
     * Serialize POJO object to XML String
     *
     * @return
     * @throws JAXBException
     */
    public String toXML() throws JAXBException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        try {
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
        }
        catch (Exception e) {
        }
        marshaller.marshal(this, out);
        return out.toString();
    }

    private static class DateFormatterAdapter extends XmlAdapter<String, Date>
    {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Date unmarshal(final String v) throws Exception
        {
            return dateFormat.parse(v);
        }

        @Override
        public String marshal(final Date v) throws Exception
        {
            return dateFormat.format(v);
        }
    }
}
