package org.example.beans;

import java.util.Calendar;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.onehippo.cms7.essentials.components.rest.adapters.HippoHtmlAdapter;
import org.onehippo.cms7.essentials.components.rest.adapters.HippoGalleryImageAdapter;

@XmlRootElement(name = "newsdocument")
@XmlAccessorType(XmlAccessType.NONE)
@HippoEssentialsGenerated(internalName = "myhippoproject:newsdocument")
@Node(jcrType = "myhippoproject:newsdocument")
public class NewsDocument extends HippoDocument {
    /** 
     * The document type of the news document.
     */
    public final static String DOCUMENT_TYPE = "myhippoproject:newsdocument";
    private final static String TITLE = "myhippoproject:title";
    private final static String DATE = "myhippoproject:date";
    private final static String INTRODUCTION = "myhippoproject:introduction";
    private final static String IMAGE = "myhippoproject:image";
    private final static String CONTENT = "myhippoproject:content";
    private final static String LOCATION = "myhippoproject:location";
    private final static String AUTHOR = "myhippoproject:author";
    private final static String SOURCE = "myhippoproject:source";

    /** 
     * Get the title of the document.
     * @return the title
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:title")
    public String getTitle() {
        return getProperty(TITLE);
    }

    /** 
     * Get the date of the document.
     * @return the date
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:date")
    public Calendar getDate() {
        return getProperty(DATE);
    }

    /** 
     * Get the introduction of the document.
     * @return the introduction
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:introduction")
    public String getIntroduction() {
        return getProperty(INTRODUCTION);
    }

    /** 
     * Get the image of the document.
     * @return the image
     */
    @XmlJavaTypeAdapter(HippoGalleryImageAdapter.class)
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:image")
    public HippoGalleryImageSet getImage() {
        return getLinkedBean(IMAGE, HippoGalleryImageSet.class);
    }

    /** 
     * Get the main content of the document.
     * @return the content
     */
    @XmlJavaTypeAdapter(HippoHtmlAdapter.class)
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:content")
    public HippoHtml getContent() {
        return getHippoHtml(CONTENT);
    }

    /** 
     * Get the location of the document.
     * @return the location
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:location")
    public String getLocation() {
        return getProperty(LOCATION);
    }

    /** 
     * Get the author of the document.
     * @return the author
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:author")
    public String getAuthor() {
        return getProperty(AUTHOR);
    }

    /** 
     * Get the source of the document.
     * @return the source
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:source")
    public String getSource() {
        return getProperty(SOURCE);
    }
}
