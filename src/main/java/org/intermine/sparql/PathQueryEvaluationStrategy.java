package org.intermine.sparql;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;

public class PathQueryEvaluationStrategy extends EvaluationStrategyImpl {

	public PathQueryEvaluationStrategy(PathQuerySource tripleSource) {
		super(tripleSource);
	}

	/*
	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			Join join, BindingSet bindings
			) throws QueryEvaluationException {
		if (joinable(join)) {
			return ((PathQuerySource) tripleSource).getStatements(bindings, join);
		} else {
			return super.evaluate(join, bindings);
		}
	}
	*/
	
	private boolean joinable(Join join) throws QueryEvaluationException {
		TupleExpr left = join.getLeftArg();
		TupleExpr right = join.getRightArg();
		// TODO - actually join.
		return false;
	}
}
