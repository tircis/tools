package org.stalactite.persistence.structure;

import org.stalactite.lang.collection.KeepOrderSet;
import org.stalactite.persistence.structure.Table.Column;

/**
 * @author mary
 */
public class ForeignKey {
	private KeepOrderSet<Column> columns;
	private String name;
	private KeepOrderSet<Column> targetColumns;

	public ForeignKey(Column column, String name, Column targetColumn) {
		this(new KeepOrderSet<Column>(column), name, new KeepOrderSet<Column>(targetColumn));
	}

	public ForeignKey(KeepOrderSet<Column> columns, String name, KeepOrderSet<Column> targetColumns) {
		this.columns = columns;
		this.name = name;
		this.targetColumns = targetColumns;
	}

	public KeepOrderSet<Column> getColumns() {
		return columns;
	}

	public String getName() {
		return name;
	}

	public KeepOrderSet<Column> getTargetColumns() {
		return targetColumns;
	}
}
