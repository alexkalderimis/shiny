package org.intermine.sparql;

import info.aduna.iteration.CloseableIteration;

import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;

public class PathQuerySource implements TripleSource {

	private ServiceFactory services;
	private ValueFactory values;
	private PathQueryBuilder builder;

	public PathQuerySource(ServiceFactory services, ValueFactory values) {
		this.services = services;
		this.values = values;
	}
	
	private PathQueryBuilder getBuilder() {
		if (builder == null) {
			builder = new PathQueryBuilder(services);
		}
		return builder;
	}

	@Override
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(
			Resource subj, URI pred, Value obj, Resource... contexts)
			throws QueryEvaluationException {
		PathQuery pq = getBuilder().build(subj, pred, obj);
		return new PathQueryIteration(pq, services.getQueryService());
	}

	@Override
	public ValueFactory getValueFactory() {
		return values;
	}
	
	/*
	public CloseableIteration<BindingSet, QueryEvaluationException> getStatements(
			BindingSet bindings, Join join) {
		return PathQueryBinder();
	}
	*/

}
