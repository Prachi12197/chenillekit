/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2008 by chenillekit.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.chenillekit.lucene.services.impl;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.chenillekit.lucene.ChenilleKitLuceneRuntimeException;
import org.chenillekit.lucene.services.IndexSource;
import org.chenillekit.lucene.services.IndexerService;
import org.slf4j.Logger;

/**
 * Implements indexer based on <a href="http://lucene.apache.org/java/docs/index.html">lucene</a>.
 *
 * @version $Id$
 */
public class IndexerServiceImpl implements IndexerService, ThreadCleanupListener
{
    private final Logger logger;

    private final IndexSource indexSource;

    public IndexerServiceImpl(final Logger logger, IndexSource indexSource)
    {
    	this.logger = logger;
    	this.indexSource = indexSource;
    }


    /*
     * (non-Javadoc)
     * @see org.chenillekit.lucene.services.IndexerService#addDocument(org.apache.lucene.document.Document)
     */
    public void addDocument(Document document)
    {
        addDocument(indexSource.getIndexWriter(), document);
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.chenillekit.lucene.services.IndexerService#addDocument(float, org.apache.lucene.document.Fieldable[])
	 */
	public void addDocument(Float boost, Fieldable... fields)
	{
		Document document = new Document();
		
		if (boost != null)
			document.setBoost(boost.floatValue());
		
		for (Fieldable field : fields)
		{
			document.add(field);
		}
		
		addDocument(indexSource.getIndexWriter(), document);
	}

	/**
     * delete documents by the given field name and the query.
     *
     * @param field       name of the field
     * @param queryString
     */
    public void delDocuments(String field, String queryString)
    {
        try
        {
        	IndexWriter indexWriter = indexSource.getIndexWriter();
            indexWriter.deleteDocuments(new Term(field, queryString));
        }
        catch (IOException ioe)
        {
        	this.logger.error(String.format("Unable to access the index for deleting docs: '%s'", ioe.getMessage()), ioe);
			throw new ChenilleKitLuceneRuntimeException(ioe);
        }
    }

    /**
     * add a document to the given index.
     *
     * @param indexWriter
     * @param document
     */
    private void addDocument(IndexWriter indexWriter, Document document)
    {
        try
        {
            if (logger.isDebugEnabled())
                logger.debug("adding document '%s', to writer", document);

            indexWriter.addDocument(document);
        }
        catch (IOException ioe)
        {
        	this.logger.error(String.format("Unable to access the index for updating doc '%s', reason: '%s'", document.toString(), ioe.getMessage()), ioe);
			throw new ChenilleKitLuceneRuntimeException(ioe);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.chenillekit.lucene.services.IndexerService#getDocCount()
     */
    public int getDocCount()
    {
        return indexSource.getIndexWriter().maxDoc();
    }
    
    /* (non-Javadoc)
	 * @see org.chenillekit.lucene.services.IndexerService#getDocCountWithDeletions()
	 */
	public int getDocCountWithDeletions()
	{
		try
		{
			return indexSource.getIndexWriter().numDocs();
		}
		catch (IOException ioe)
		{
			logger.error("Unable to perform numDocs count returning zero", ioe);
			return 0;
		}
	}
    

    /*
     * (non-Javadoc)
     * @see org.chenillekit.lucene.services.IndexerService#commit()
     */
    public void commit()
    {
    	try
    	{
    		indexSource.getIndexWriter().commit();
		}
    	catch (CorruptIndexException cie)
    	{
    		this.logger.error(String.format("The index result corrupted: '%s'", cie.getMessage()), cie);
			throw new ChenilleKitLuceneRuntimeException(cie);
		}
    	catch (IOException ioe)
    	{
    		this.logger.error(String.format("Unable to access the index for committing changes, reason: '%s'", ioe.getMessage()), ioe);
			throw new ChenilleKitLuceneRuntimeException(ioe);
		}
	}


	/**
     * Invoked by {@link org.apache.tapestry5.ioc.services.PerthreadManager} service when a thread performs and
     * end-of-request cleanup.
     */
    public void threadDidCleanup()
    {
    	// Commit changes
    	commit();
    }
}
