//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.04 at 02:05:46 PM CEST 
//


package playground.gregor.grips.jaxb.inspire.geographicalnames;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PronunciationOfNamePropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PronunciationOfNamePropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:x-inspire:specification:gmlas:GeographicalNames:3.0}PronunciationOfName"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PronunciationOfNamePropertyType", propOrder = {
    "pronunciationOfName"
})
public class PronunciationOfNamePropertyType {

    @XmlElement(name = "PronunciationOfName", required = true)
    protected PronunciationOfNameType pronunciationOfName;

    /**
     * Gets the value of the pronunciationOfName property.
     * 
     * @return
     *     possible object is
     *     {@link PronunciationOfNameType }
     *     
     */
    public PronunciationOfNameType getPronunciationOfName() {
        return pronunciationOfName;
    }

    /**
     * Sets the value of the pronunciationOfName property.
     * 
     * @param value
     *     allowed object is
     *     {@link PronunciationOfNameType }
     *     
     */
    public void setPronunciationOfName(PronunciationOfNameType value) {
        this.pronunciationOfName = value;
    }

    public boolean isSetPronunciationOfName() {
        return (this.pronunciationOfName!= null);
    }

}