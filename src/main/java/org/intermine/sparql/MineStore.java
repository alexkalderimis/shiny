package org.intermine.sparql;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.Path;
import org.intermine.pathquery.PathException;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;

public class MineStore extends SailBase {

	private String uri;
	private ServiceFactory serviceFactory;
	final private String prefix;

	public MineStore(String serviceUri) {
		this.prefix = serviceUri + "/classes/";
		this.uri = serviceUri;
		this.serviceFactory = new ServiceFactory(uri);
	}
	
	@Override
	public ValueFactory getValueFactory() {
		// TODO: we need an actual mine native value factory.
		return new ValueFactoryImpl() {
			@Override
			public URI createURI(String uriStr) {
				return super.createURI(prefix + uriStr);
			}
		};
	}

	@Override
	public boolean isWritable() throws SailException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected SailConnection getConnectionInternal() throws SailException {
		return new InterMineConnection(uri, uri, getValueFactory());
	}

	@Override
	protected void shutDownInternal() throws SailException {
		// TODO Auto-generated method stub
		
	}


}
