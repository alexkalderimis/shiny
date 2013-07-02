package org.intermine.sparql;

import info.aduna.iteration.CloseableIteration;

import java.util.Iterator;
import java.util.List;

import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.services.QueryService;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;

public class PathQueryStatementIteration implements
		CloseableIteration<Statement, QueryEvaluationException> {

	private PathQuery pq;
	private QueryService service;
	private transient Iterator<List<Object>> results = null;
	private BindingInfo bindingInfo;
	private ValueFactory valueFactory;

	public PathQueryStatementIteration(
			PathQuery pq, QueryService queryService, ValueFactory valueFactory, 
			BindingInfo bindingInfo) {
		this.pq = pq;
		this.service = queryService;
		this.bindingInfo = bindingInfo;
		this.valueFactory = valueFactory;
	}
	
	private void run() {
		//System.out.println("Running query");
		results = service.getRowListIterator(pq);
	}

	@Override
	public boolean hasNext() throws QueryEvaluationException {
		if (results == null) {
			run();
		}
		return results.hasNext();
	}

	@Override
	public Statement next() throws QueryEvaluationException {
		if (results == null) {
			run();
		}
		return bind(results.next());
	}
	
	private Statement bind(List<Object> row) {
		//System.out.println("Got row: " + row);
		Resource subj = bindingInfo.getSubject(row);
		URI pred = bindingInfo.getPredicate(row);
		Value obj = bindingInfo.getObject(row);
		return valueFactory.createStatement(subj, pred, obj);
	}

	@Override
	public void remove() throws QueryEvaluationException {
		throw new QueryEvaluationException("Cannot remove");
	}

	@Override
	public void close() throws QueryEvaluationException {
		// TODO: is there anything to do here??
	}

}
