package org.intermine.sparql;

import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.Path;
import org.intermine.pathquery.PathException;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.exceptions.ServiceException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;

public class PathQueryBuilder {

	private ServiceFactory services;
	private String ns;

	public PathQueryBuilder(ServiceFactory services) {
		this.services = services;
	}
	
	public String getNS() throws QueryEvaluationException {
		if (ns == null) {
			try {
				ns = services.getRootUrl() + "rdf/"
						+ services.getQueryService().getRelease();
			} catch (ServiceException e) {
				throw new QueryEvaluationException("Error retrieving release", e);
			}
		}
		return ns;
	}

	public PathQuery build(Resource subj, URI pred, Value obj)
			throws QueryEvaluationException {
		if (pred == null) {
			throw new QueryEvaluationException("Can't handle null predicates yet.");
		}
		String ns = pred.getNamespace();
		if (ns == null || !ns.equals(getNS())) {
			return null;
		}
		Path relationship;
		try {
			relationship = new Path(services.getModel(), pred.getLocalName());
		} catch (PathException e) {
			return null;
		}
		Path root = relationship.getPrefix();
		PathQuery pq = new PathQuery(services.getModel());
		pq.addViews(relationship.toStringNoConstraints(), relationship.getPrefix() + ".id");
		if (subj != null) {
			pq.addConstraint(Constraints.eq(root + ".id", ((URI) subj).getLocalName()));
		}
		if (obj != null) {
			if (relationship.endIsAttribute()) {
				pq.addConstraint(Constraints.eq(relationship.toStringNoConstraints(), obj.stringValue()));
			} else {
				pq.addConstraint(Constraints.eq(relationship + ".id", ((URI) obj).getLocalName()));
			}
		}
		return pq;
	}
}
