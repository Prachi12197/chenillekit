/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2008-2010 by chenillekit.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.chenillekit.template.services.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import org.apache.tapestry5.ioc.Resource;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.chenillekit.template.services.TemplateService;
import org.slf4j.Logger;

/**
 * template service based on <a href="http://freemarker.sourceforge.net">FreeMarker</a> framework.
 *
 * @version $Id$
 */
public class FreeMarkerServiceImpl implements TemplateService
{
    public static final String CONFIG_RESOURCE_KEY = "freemarker.configuration";

    private Configuration configuration;
    private Logger serviceLog;

    /**
     * Standard-Konstruktor.
     *
     * @param serviceLog
     * @param configuration
     */
    public FreeMarkerServiceImpl(Logger serviceLog, Configuration configuration)
    {
        this.serviceLog = serviceLog;
        this.configuration = configuration;
    }

    /**
     * merge data from parameter map with template resource into given output stream.
     *
     * @param template     name of the template file
     * @param outputStream where to write in
     * @param parameterMap parameters for template
     */
    public void mergeDataWithResource(Resource template, OutputStream outputStream, Map parameterMap)
    {
        mergeDataWithResource(template, outputStream, parameterMap, (Collection) null);
    }

    /**
     * merge data from parameter map and collection of elements with template resource into given output stream.
     *
     * @param template     name of the template file
     * @param outputStream where to write in
     * @param parameterMap parameters for template
     * @param elements     collection of elements
     */
    public void mergeDataWithResource(Resource template, OutputStream outputStream, Map parameterMap, Collection elements)
    {
        mergeDataWithResource(template, outputStream, parameterMap, elements != null ? elements.toArray() : null);
    }

    /**
     * merge data from parameter map and array of elements with template resource into given output stream.
     *
     * @param template     name of the template file
     * @param outputStream where to write in
     * @param parameterMap parameters for template
     * @param elements     array of elements
     */
    public void mergeDataWithResource(Resource template, OutputStream outputStream, Map parameterMap, Object[] elements)
    {
        try
        {
            if (configuration == null)
            {
                configuration = new Configuration();

                // Specify how templates will see the data model. This is an advanced topic...
                // but just use this:
                configuration.setObjectWrapper(new DefaultObjectWrapper());
                configuration.setClassForTemplateLoading(Resource.class, "/");
            }

            //noinspection unchecked
            parameterMap.put("elementList", elements);

            if (serviceLog.isInfoEnabled())
                serviceLog.info("processing template resource '" + template.getPath() + "'");

            String path = template.getPath();
            Template freeMarkerTemplate = configuration.getTemplate(path);
            Writer out = new OutputStreamWriter(outputStream);
            freeMarkerTemplate.process(parameterMap, out);
            out.flush();

            if (serviceLog.isInfoEnabled())
                serviceLog.info("processing template file '" + template.getPath() + "' finished");

        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * merge data from parameter map with template resource into given output stream.
     *
     * @param templateStream template stream
     * @param outputStream   where to write in
     * @param parameterMap   parameters for template
     */
    public void mergeDataWithStream(InputStream templateStream, OutputStream outputStream, Map parameterMap)
    {
        mergeDataWithStream(templateStream, outputStream, parameterMap, (Collection) null);
    }

    /**
     * merge data from parameter map and array of elements with template stream into given output stream.
     *
     * @param templateStream template stream
     * @param outputStream   where to write in
     * @param parameterMap   parameters for template
     * @param elements       collection of elements
     */
    public void mergeDataWithStream(InputStream templateStream, OutputStream outputStream, Map parameterMap, Collection elements)
    {
        mergeDataWithStream(templateStream, outputStream, parameterMap, elements != null ? elements.toArray() : null);
    }

    /**
     * merge data from parameter map and array of elements with template stream into given output stream.
     *
     * @param templateStream template stream
     * @param outputStream   where to write in
     * @param parameterMap   parameters for template
     * @param elements       array of elements
     */
    public void mergeDataWithStream(InputStream templateStream, OutputStream outputStream, Map parameterMap, Object[] elements)
    {
        try
        {
            if (configuration == null)
            {
                configuration = new Configuration();

                // Specify how templates will see the data model. This is an advanced topic...
                // but just use this:
                configuration.setObjectWrapper(new DefaultObjectWrapper());
                configuration.setClassForTemplateLoading(Resource.class, "/");
            }

            //noinspection unchecked
            parameterMap.put("elementList", elements);

            if (serviceLog.isInfoEnabled())
                serviceLog.info("processing template stream");

            Template freeMarkerTemplate = new Template("Freemarker Template", new InputStreamReader(templateStream), configuration);
			String encoding = freeMarkerTemplate.getEncoding();
			if (encoding == null)
				encoding = Charset.defaultCharset().name();

            Writer out = new OutputStreamWriter(outputStream, encoding);
            freeMarkerTemplate.process(parameterMap, out);
            out.flush();

            if (serviceLog.isInfoEnabled())
                serviceLog.info("processing template finished");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }
}