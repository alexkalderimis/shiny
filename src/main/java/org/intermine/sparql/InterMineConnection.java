package org.intermine.sparql;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;

import java.util.Collections;
import java.util.Iterator;

import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.Path;
import org.intermine.pathquery.PathException;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.exceptions.ServiceException;
import org.intermine.webservice.client.services.QueryService;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UnknownSailTransactionStateException;
import org.openrdf.sail.UpdateContext;

public class InterMineConnection implements SailConnection {

	private final ServiceFactory services;
	private final ValueFactory values;
	private String prefix, ns;
	
	public InterMineConnection(String prefix, String serviceUrl, ValueFactory vf) {
		ServiceFactory sf = new ServiceFactory(serviceUrl);
		this.prefix = prefix;
		this.services = sf;
		this.values = vf;
	}
	
	private String getNS() throws SailException {
		if (ns == null) {
			try {
				ns = services.getRootUrl()
						+ "rdf/"
						+ services.getQueryService().getRelease();
			} catch (ServiceException e) {
				throw new SailException("Error retrieving release", e);
			}
		}
		return ns;
	}

	@Override
	public void addStatement(Resource arg0, URI arg1, Value arg2,
			Resource... arg3) throws SailException {
		// In the future this can be used for list operations...
		throw new SailException("InterMine services are read only");
	}

	@Override
	public void addStatement(UpdateContext arg0, Resource arg1, URI arg2,
			Value arg3, Resource... arg4) throws SailException {
		// In the future this can be used for list operations...
		throw new SailException("InterMine services are read only");
	}

	@Override
	public void begin() throws SailException {
		throw new SailException("InterMine services are read only");
	}

	@Override
	public void clear(Resource... arg0) throws SailException {
		throw new SailException("InterMine services are read only");
	}

	@Override
	public void clearNamespaces() throws SailException {
		throw new SailException("InterMine services are read only");
	}

	@Override
	public void close() throws SailException {
		throw new SailException("InterMine services are read only");
	}

	@Override
	public void commit() throws SailException {
		throw new SailException("InterMine services are read only");
	}

	@Override
	public void endUpdate(UpdateContext arg0) throws SailException {
		throw new SailException("InterMine services are read only");
	}

	@Override
	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred) throws SailException {
		try {
			PathQuerySource tripleSource = new PathQuerySource(services, values);
			EvaluationStrategy strategy = new PathQueryEvaluationStrategy(
					tripleSource);
			
			// Section copied from Jerven, who copied it from Sesame...
			tupleExpr = tupleExpr.clone();
			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset,
					bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset,
					bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset,
					bindings);
			new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);

			// new SubSelectJoinOptimizer().optimize(tupleExpr, dataset,
			// bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset,
					bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
		} catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	@Override
	public CloseableIteration<? extends Resource, SailException> getContextIDs()
			throws SailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNamespace(String prefix) throws SailException {
		if (this.prefix != null && this.prefix.equals(prefix)) return getNS();
		return null;
	}

	@Override
	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
			throws SailException {

		return new CloseableIteratorIteration<Namespace, SailException>() {
			private Namespace mineNS = new NamespaceImpl(prefix, getNS());
			private Iterator<Namespace> namespaces = Collections.singleton(mineNS).iterator();

			@Override
			public boolean hasNext() throws SailException {
				return namespaces.hasNext();
			}

			@Override
			public Namespace next() throws SailException {
				return namespaces.next();
			};
		};
	}
	private QueryService getQueryService() {
		return services.getQueryService();
	}
	
	private PathQuery buildPathQuery(Resource subj, URI pred, Value obj)
		throws SailException {
		if (pred == null) {
			throw new SailException("Can't handle null predicates yet.");
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
	
	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(
			Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
			throws SailException {
		PathQuery pq = buildPathQuery(subj, pred, obj);
		BindingInfo bi;
		try {
			bi = BindingInfo.create(values, pq, subj, pred, obj);
		} catch (PathException e) {
			throw new SailException(e);
		}
		final PathQueryStatementIteration pqi = new PathQueryStatementIteration(pq, getQueryService(), values, bi);
		return new CloseableIteratorIteration<Statement, SailException>() {
			
			@Override
			public boolean hasNext() throws SailException {
				try {
					return pqi.hasNext();
				} catch (QueryEvaluationException e) {
					throw new SailException(e);
				}
			}
			
			@Override
			public Statement next() throws SailException {
				try {
					return pqi.next();
				} catch (QueryEvaluationException e) {
					throw new SailException(e);
				}
			}
			
			@Override
			protected void handleClose() throws SailException {
				try {
					pqi.close();
				} catch (QueryEvaluationException e) {
					throw new SailException(e);
				}
			}
		};
	}

	@Override
	public boolean isActive() throws UnknownSailTransactionStateException {
		return false;
	}

	@Override
	public boolean isOpen() throws SailException {
		return false;
	}

	@Override
	public void prepare() throws SailException {
		throw new SailException("InterMine repositories do not support transactions");
	}

	@Override
	public void removeNamespace(String arg0) throws SailException {
		throw new SailException("InterMine repositories are read only");
	}

	@Override
	public void removeStatement(UpdateContext arg0, Resource arg1, URI arg2,
			Value arg3, Resource... arg4) throws SailException {
		throw new SailException("InterMine repositories are read only");	
	}

	@Override
	public void removeStatements(Resource arg0, URI arg1, Value arg2,
			Resource... arg3) throws SailException {
		throw new SailException("InterMine repositories are read only");	
	}

	@Override
	public void rollback() throws SailException {
		throw new SailException("InterMine repositories do not support transactions");
	}

	@Override
	public void setNamespace(String arg0, String arg1) throws SailException {
		throw new SailException("InterMine repositories are read only");
	}

	@Override
	public long size(Resource... arg0) throws SailException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startUpdate(UpdateContext arg0) throws SailException {
		throw new SailException("InterMine repositories do not support transactions");
	}

}
