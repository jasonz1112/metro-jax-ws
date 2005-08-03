/*
 * $Id: RuntimeEndpointInfo.java,v 1.26 2005-08-03 22:54:08 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import com.sun.xml.ws.spi.runtime.Binding;
import javax.xml.ws.Provider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.transform.Source;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.ws.BeginService;
import javax.xml.ws.EndService;


/**
 * modeled after the javax.xml.ws.Endpoint class in API. 
 * Contains all the information about Binding, handler chain, Implementor object, 
 * WSDL & Schema Metadata
 * @author WS Development Team
 */
public class RuntimeEndpointInfo
    implements com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo {
    
    private String name;
    private Exception exception;
    private QName portName;
    private QName serviceName;
    private String wsdlFileName;
    private boolean deployed;
    private String urlPattern;
    private List<Source> metadata;
    private Binding binding;
    private RuntimeModel runtimeModel;
    private Object implementor;
    private Object implementorProxy;
    private Map<String, DocInfo> docs;      // /WEB-INF/wsdl/xxx.wsdl -> DocInfo
    private Map<String, DocInfo> query2Doc;     // (wsdl=a) --> DocInfo
    private boolean enableMtom;
    private WebServiceContext wsContext;
    private boolean beginService;
    private boolean endService;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception e) {
        exception = e;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public String getWSDLFileName() {
        return wsdlFileName;
    }

    public void setWSDLFileName(String s) {
        wsdlFileName = s;
    }

    /**
     * Enable Mtom processing
     * @param enable enables the use of MTOM
     */
    public void setMtomEnabled(boolean enable){
        this.enableMtom = enable;
    }

    public boolean isDeployed() {
        return deployed;
    }
    
    public void createModel() {
        // Create runtime model for non Provider endpoints            
        RuntimeModeler rap = new RuntimeModeler(getImplementor().getClass(),
                ((BindingImpl)binding).getBindingId());
        runtimeModel = rap.buildRuntimeModel();     
    }
    
    /**
     * creates a RuntimeModel using @link com.sun.xml.ws.modeler.RuntimeModeler. 
     * The modeler creates the model by reading annotations on Implementor object. 
     * RuntimeModel is read only and is accessed from multiple threads afterwards.
     * Generates the WSDL and XML Schemam for the endpoint if necessary
     */
    public void deploy() {
        if (implementor == null) {
            // TODO throw exception
        }
        
        // setting a default binding
        if (binding == null) {
            setBinding(new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING));
        }
        
        if (implementor instanceof Provider) {
            // No runtime model is required
            if (getWSDLFileName() == null) {
                // TODO throw exception
            } else {
                if (serviceName == null) {
                    // If WSDL has only one service, then it is okay
                    // else error
                } else {
                    if (portName == null) {
                        // If service has only one port, then it is okay
                        // else error
                    }
                }
            }
        } else {
            // Create runtime model for non Provider endpoints    
            createModel();
            if (getWSDLFileName() == null) {
                // Generate WSDL and schema documents using runtime model
                WSDLGenResolver wsdlResolver = new WSDLGenResolver();
                WSDLGenerator wsdlGen = new WSDLGenerator(runtimeModel, wsdlResolver,
                        ((BindingImpl)binding).getBindingId());
                try {
                    wsdlGen.doGeneration();
                } catch(Exception e) {
                    // TODO
                    e.printStackTrace();
                }
                setMetadata(wsdlResolver.getDocs());
                setWSDLFileName(wsdlResolver.getWSDLFile());      
                setServiceName(runtimeModel.getServiceQName());       
                setPortName(runtimeModel.getPortQName());
            } else {
                if (serviceName == null) {
                    // If WSDL has only one service, then it is okay
                    // else error
                } else {
                    if (portName == null) {
                        // If service has only one port, then it is okay
                        // else error
                    } else {
                        // WSD
                    }
                }
            }
            
            if (getBinding().getHandlerChain() == null) {
                HandlerAnnotationInfo chainInfo =
                    HandlerAnnotationProcessor.buildHandlerInfo(
                        getImplementor().getClass());
                if (chainInfo != null) {
                    getBinding().setHandlerChain(chainInfo.getHandlers());
                    if (getBinding() instanceof SOAPBinding) {
                        ((SOAPBinding) getBinding()).setRoles(
                            chainInfo.getRoles());
                    }
                }
            }
            
            //set Tmomt processing
            runtimeModel.enableMtom(enableMtom);
        }
        deployed = true;
    }

    public QName getPortName() {
        return portName;
    }

    public void setPortName(QName n) {
        portName = n;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName n) {
        serviceName = n;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String s) {
        urlPattern = s;
    }

    public void setBinding(Binding binding){
        this.binding = binding;
    }

    public Binding getBinding() {
        return binding;
    }
    
    public java.util.List<Source> getMetadata() {
        return metadata;
    }
        
    public void setMetadata(java.util.List<Source> metadata) {

        this.metadata = metadata;
    }

    public RuntimeModel getRuntimeModel() {
        return runtimeModel;
    }
    
    public Object getImplementor() {
        return implementor;
    }
    
    public void setImplementor(Object implementor) {
        this.implementor = implementor;
    }
    
    public Object getImplementorProxy() {
        return implementorProxy;
    }
    
    public void setImplementorProxy(Object implementorProxy) {
        this.implementorProxy = implementorProxy;
    }
    
    public void setMetadata(Map<String, DocInfo> docs) {
        this.docs = docs;
        // update uri-->DocInfo map
        if (query2Doc != null) {
            query2Doc.clear();
        } else {
            query2Doc = new HashMap<String, DocInfo>();
        }
        Set<Map.Entry<String, DocInfo>> entries = docs.entrySet();
        for(Map.Entry<String, DocInfo> entry : entries) {
            DocInfo docInfo = entry.getValue();
            query2Doc.put(docInfo.getQueryString(), docInfo);
        }
    }
    
    public WebServiceContext getWebServiceContext() {
        return wsContext;
    }
    
    public void setWebServiceContext(WebServiceContext wsContext) {
        this.wsContext = wsContext;
    }
     
    
    /*
     * key - /WEB-INF/wsdl/xxx.wsdl
     */
    public Map<String, DocInfo> getDocMetadata() {
        return docs;
    }
    
    /*
     * path - /WEB-INF/wsdl/xxx.wsdl
     * return - xsd=a | wsdl | wsdl=b etc
     */
    public String getQueryString(String path) {
        DocInfo docInfo = docs.get(path);
        return (docInfo == null) ? null : docInfo.getQueryString();
    }
    
    /*
     * queryString - xsd=a | wsdl | wsdl=b etc
     * return - /WEB-INF/wsdl/xxx.wsdl
     */
    public String getPath(String queryString) {
        DocInfo docInfo = query2Doc.get(queryString);
        return (docInfo == null) ? null : docInfo.getPath();
    }
    
    /*
     * Calls the first method in the implementor object that has @BeginService
     * annotation. Servlet.init(), or Endpoint.publish() may call this. Used
     * synchronized because multiple servlet instances may call this in their
     * init()
     */
    public synchronized void beginService()
    throws IllegalAccessException, InvocationTargetException {
        if (!beginService) {
            Class c = implementor.getClass();
            Method[] methods = c.getMethods();
            for(Method method : methods) {
                if (method.getAnnotation(BeginService.class) != null) {
                    method.invoke(implementor, new Object[]{ });
                    break;
                }
            }
            beginService = true;
        }
    }
    
    /*
     * Calls the first method in the implementor object that has @EndService
     * annotation. Servlet.destory(), or Endpoint.stop() may call this. Used
     * synchronized because multiple servlet instances may call this in their
     * destroy()
     */
    public synchronized void endService()
    throws IllegalAccessException, InvocationTargetException {
        if (!endService) {
            Class c = implementor.getClass();
            Method[] methods = c.getMethods();
            for(Method method : methods) {
                if (method.getAnnotation(EndService.class) != null) {
                    method.invoke(implementor, new Object[]{ });
                    break;
                }
            }
            endService = true;
        }
    }

}
