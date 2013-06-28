package org.intermine.sparql;

import java.util.Date;
import java.util.List;

import org.intermine.metadata.FieldDescriptor;
import org.intermine.pathquery.Path;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

public class BindingInfo {

	private ValueFactory vf;
	private Path subjPath;
	private int subjIdx;
	private URI pred;
	private Path objPath;
	private int objIdx;
	private FieldDescriptor idField;

	public BindingInfo(
			ValueFactory vf,
			Path subjPath, int subjIdx,
			URI pred,
			Path objPath, int objIdx) {
		this.vf = vf;
		this.subjPath = subjPath;
		this.subjIdx = subjIdx;
		this.pred = pred;
		this.objPath = objPath;
		this.objIdx = objIdx;
		this.idField =
				objPath.getModel().getClassDescriptorByName("InterMineObject")
				.getFieldDescriptorByName("id");
	}
	
	public URI getPredicate(List<Object> row) {
		return pred; // TODO: make this cleverer
	}
	
	public Resource getSubject(List<Object> row) {
		String localName = subjPath.getLastClassDescriptor().getUnqualifiedName()
				+ "/" + row.get(subjIdx);
		return vf.createURI(localName);
	}
	
	public Value getObject(List<Object> row) {
		Object val = row.get(objIdx);
		if (idField == objPath.getEndFieldDescriptor()) {
			String localName =
				objPath.getLastClassDescriptor().getUnqualifiedName() + "/" + val;
			return vf.createURI(localName);
		} else {
			Class<?> valType = objPath.getEndType();
			if (valType == String.class) {
				return vf.createLiteral(String.valueOf(val));
			} else if (valType == Integer.class || valType == int.class) {
				return vf.createLiteral((Integer) val);
			} else if (valType == Float.class || valType == float.class) {
				return vf.createLiteral((Float) val);
			} else if (valType == Long.class || valType == long.class) {
				return vf.createLiteral((Long) val);
			} else if (valType == Double.class || valType == double.class) {
				return vf.createLiteral((Double) val);
			} else if (valType == Date.class) {
				return vf.createLiteral((Date) val);
			} else {
				throw new RuntimeException("Unrepresentable value");
			}
		}
	}
}
