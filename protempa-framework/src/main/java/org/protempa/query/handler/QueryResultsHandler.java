package org.protempa.query.handler;

import java.util.List;

import org.protempa.FinderException;
import org.protempa.proposition.Proposition;

/**
 * Interface defining the operations for handling a single result from a 
 * Protempa query.
 * 
 * @author Michel Mansour
 *
 */
public interface QueryResultsHandler {
	
	/**
	 * Performs all initialization functions to prepare the handler. This
	 * method is guaranteed to be called by Protempa before any
	 * query result processing is done.
	 * 
	 * @throws FinderException if any exceptions occur at a lower level
	 */
	public void init() throws FinderException;
	
	/**
	 * Performs all clean-up functions for the handler. This method is
	 * guaranteed to be called by Protempa when query result processing is
	 * complete.
	 *  
	 * @throws FinderException if any exceptions occur at a lower level
	 */
	public void finish() throws FinderException;
	
	/**
	 * Handles a single query result, which is the list of propositions
	 * associated with the given key.
	 * 
	 * @param key the identifying key for the result
	 * @param propositions the proposition results for the given key
	 */
	public void handleQueryResult(String key, List<Proposition> propositions) throws FinderException;
}