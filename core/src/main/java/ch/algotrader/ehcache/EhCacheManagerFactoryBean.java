package ch.algotrader.ehcache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class based on spring's EhCacheManagerFactoryBean class.
 * Its aim is to merge ehcache configs getting them in jar files
 * @author Pierre Le Roux
 */
public class EhCacheManagerFactoryBean implements FactoryBean<CacheManager>, InitializingBean, DisposableBean {

    protected Resource[] configLocations;

    private boolean shared = false;

    private String cacheManagerName;

    private CacheManager cacheManager;

    /**
     * Set the location of the EHCache config files.
     * The config files included in itcb-cache-manager imports all files that match the pattern :
     * classpath*:WEB-INF/*ehcache.xml
     * Wild card is allowed.
     * ConfigLocations XML files are merged.
     * @throws IOException
     * @see net.sf.ehcache.CacheManager#create(java.io.InputStream)
     * @see net.sf.ehcache.CacheManager#CacheManager(java.io.InputStream)
     */
    public void setConfigLocations(String configLocations) throws IOException {
        PathMatchingResourcePatternResolver c = new PathMatchingResourcePatternResolver();
        this.configLocations = c.getResources(configLocations);
    }

    /**
     * Set whether the EHCache CacheManager should be shared (as a singleton at the VM level)
     * or independent (typically local within the application). Default is "false", creating
     * an independent instance.
     * @see net.sf.ehcache.CacheManager#create()
     * @see net.sf.ehcache.CacheManager#CacheManager()
     */
    public void setShared(boolean shared) {
        this.shared = shared;
    }

    /**
     * Set the name of the EHCache CacheManager (if a specific name is desired).
     * @see net.sf.ehcache.CacheManager#setName(String)
     */
    public void setCacheManagerName(String cacheManagerName) {
        this.cacheManagerName = cacheManagerName;
    }

    @Override
    public void afterPropertiesSet() throws IOException, CacheException, ParserConfigurationException, SAXException, TransformerException {
        if (this.shared) {
            // Shared CacheManager singleton at the VM level.
            if (this.configLocations != null) {
                this.cacheManager = CacheManager.create(getInputStream());
            } else {
                this.cacheManager = CacheManager.create();
            }
        } else {
            // Independent CacheManager instance (the default).
            if (this.configLocations != null) {
                this.cacheManager = new CacheManager(getInputStream());
            } else {
                this.cacheManager = new CacheManager();
            }
        }
        if (this.cacheManagerName != null) {
            this.cacheManager.setName(this.cacheManagerName);
        }
    }

    /**
     * This method merge ehcache xml configuration files as an inputstream
     * @return the inputStream
     * @throws XmlMappingException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @throws TransformerConfigurationException
     */
    private InputStream getInputStream() throws IOException, ParserConfigurationException, SAXException, TransformerException, TransformerFactoryConfigurationError {

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document baseDoc = null;
        Element baseElement = null;
        for (Resource ehcacheXml : this.configLocations) {

            if (baseDoc == null || baseElement == null) {
                baseDoc = db.parse(ehcacheXml.getInputStream());
                baseElement = baseDoc.getDocumentElement();
            } else {
                NodeList list = db.parse(ehcacheXml.getInputStream()).getDocumentElement().getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node child = list.item(i);
                    baseElement.appendChild(baseDoc.importNode(child, true));
                }
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(baseDoc);
        Result outputTarget = new StreamResult(outputStream);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @Override
    public CacheManager getObject() {
        return this.cacheManager;
    }

    @Override
    public Class<? extends CacheManager> getObjectType() {
        return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() {
        this.cacheManager.shutdown();
    }
}

