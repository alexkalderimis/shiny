package org.intermine.sparql;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.intermine.metadata.AttributeDescriptor;
import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.CollectionDescriptor;
import org.intermine.metadata.Model;
import org.intermine.metadata.ReferenceDescriptor;
import org.intermine.webservice.client.core.ServiceFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.SailException;

public class CLI {

	private enum Command { ontology, query }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> arguments = Arrays.asList(args);
		Command cmd = Command.valueOf(arguments.get(0));
		switch (cmd) {
		case ontology:
			getOntology(arguments.subList(1, arguments.size()));
			break;
		case query:
			runQuery(arguments.subList(1, arguments.size()));
			break;
		}
	}
	
	private static void getOntology(List<String> arguments) {
		final PrintStream out = System.out;
		final String servicePrefix = arguments.get(0);
		final ServiceFactory sf = new ServiceFactory(servicePrefix);
		final Model m = sf.getModel();
		out.println("Prefix(:=<" + servicePrefix + "classes/>)");
		out.println("Ontology(<" + servicePrefix + "classes/>");
		for (ClassDescriptor cd: m.getClassDescriptors()) {
			final String cName = cd.getSimpleName();
			out.println("  Declaration( Class( :" + cName + " ))");
			for (ClassDescriptor parent: cd.getSuperDescriptors()) {
				out.println("  SubClassOf( :" + cName + " :" + parent.getSimpleName() + " )");
			}
			for (AttributeDescriptor a: cd.getAttributeDescriptors()) {
				final String propName = cd.getSimpleName() + "." + a.getName();
				out.println("  Declaration( ObjectProperty( :" + propName + " ))");
				out.println("  ObjectPropertyDomain( :" + propName + " :" + cName + " )");
				out.println("  ObjectProperyRange( :" + propName + " " + getDataType(a.getType()) + " )");
			}
			for (ReferenceDescriptor r: cd.getReferenceDescriptors()) {
				final String propName = cd.getSimpleName() + "." + r.getName();
				out.println("  Declaration( ObjectProperty( :" + propName + " ))");
				out.println("  ObjectPropertyDomain( :" + propName + " :" + cName + " )");
				out.println("  ObjectProperyRange( :" + propName + " :" + r.getReferencedClassDescriptor().getSimpleName() + " )");
			}
			for (CollectionDescriptor c: cd.getCollectionDescriptors()) {
				final String propName = cd.getSimpleName() + "." + c.getName();
				out.println("  Declaration( ObjectProperty( :" + propName + " ))");
				out.println("  ObjectPropertyDomain( :" + propName + " :" + cName + " )");
				out.println("  ObjectProperyRange( :" + propName + " :" + c.getReferencedClassDescriptor().getSimpleName() + " )");
				
			}
		}
		out.println(")");
	}
	
	private static String getDataType(String attributeType) {
		if ("java.lang.Integer".equals(attributeType) || "int".equals(attributeType)) {
			return "xsd:integer";
		} else if ("java.lang.String".equals(attributeType)) {
			return "xsd:string";
		} else if ("java.lang.Double".equals(attributeType) || "double".equals(attributeType)) {
			return "xsd:double";
		} else if ("java.lang.Short".equals(attributeType) || "short".equals(attributeType)) {
			return "xsd:short";
		} else if ("java.lang.Long".equals(attributeType) || "long".equals(attributeType)) {
			return "xsd:long";
		} else if ("java.lang.Float".equals(attributeType) || "float".equals(attributeType)) {
			return "xsd:float";
		} else if ("java.lang.Date".equals(attributeType)) {
			return "xsd:datetime";
		} else if ("java.lang.Boolean".equals(attributeType) || "boolean".equals(attributeType)) {
			return "xsd:boolean";
		}
		return attributeType;
	}
	
	private static void runQuery(List<String> arguments) {
		final String serviceUrl = arguments.get(0);
		final String query = arguments.get(1);

		System.out.println("Mine is " + serviceUrl);
		System.out.println("Query is " + query);
		
		MineStore store = new MineStore(serviceUrl);
		try {
			SailRepository rep = new SailRepository(store);
			store.initialize();
			Query probTQ = rep.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
			if (probTQ instanceof TupleQuery) {
				SPARQLResultsCSVWriter handler = new SPARQLResultsCSVWriter(System.out);
				((TupleQuery) probTQ).evaluate(handler);
			} else if (probTQ instanceof GraphQuery) {
				RDFHandler createWriter = new TurtleWriter(System.out);
				((GraphQuery) probTQ).evaluate(createWriter);
			} else if (probTQ instanceof BooleanQuery) {
				BooleanQueryResultWriter createWriter = QueryResultIO
						.createWriter(BooleanQueryResultFormat.TEXT, System.out);
				boolean evaluate = ((BooleanQuery) probTQ).evaluate();
				createWriter.handleBoolean(evaluate);
			}
		} catch (SailException e) {
			System.err.println("ERROR:");
			e.printStackTrace(System.err);
		} catch (MalformedQueryException e) {
			System.err.println("ERROR:");
			e.printStackTrace(System.err);
		} catch (RepositoryException e) {
			System.err.println("ERROR:");
			e.printStackTrace(System.err);
		} catch (TupleQueryResultHandlerException e) {
			System.err.println("ERROR:");
			e.printStackTrace(System.err);
		} catch (QueryEvaluationException e) {
			System.err.println("ERROR:");
			e.printStackTrace(System.err);
		} catch (RDFHandlerException e) {
			System.err.println("ERROR:");
			e.printStackTrace(System.err);
		} catch (Throwable t) {
			System.err.println("ERROR:");
			t.printStackTrace(System.err);
		} finally {
			System.out.println("done");
			System.exit(0);
		}
	}

}
