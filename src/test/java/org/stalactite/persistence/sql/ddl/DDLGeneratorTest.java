package org.stalactite.persistence.sql.ddl;

import org.stalactite.lang.collection.KeepOrderSet;
import org.stalactite.persistence.structure.ForeignKey;
import org.stalactite.persistence.structure.Index;
import org.stalactite.persistence.structure.Table;
import org.stalactite.persistence.structure.Table.Column;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DDLGeneratorTest {
	
	@Test
	public void testGenerateCreateTable() throws Exception {
		DDLGenerator testInstance = new DDLGenerator(null) {
			@Override
			protected String getSqlType(Column column) {
				return "type";
			}
		};
		
		Table t = new Table(null, "Toto");
		
		t.new Column("A", String.class);
		String generatedCreateTable = testInstance.generateCreateTable(t);
		Assert.assertEquals(generatedCreateTable, "create table Toto(A type)");
		
		t.new Column("B", String.class);
		generatedCreateTable = testInstance.generateCreateTable(t);
		Assert.assertEquals(generatedCreateTable, "create table Toto(A type, B type)");
		
		t.new Column("C", String.class).setPrimaryKey(true);
		generatedCreateTable = testInstance.generateCreateTable(t);
		Assert.assertEquals(generatedCreateTable, "create table Toto(A type, B type, C type primary key)");
		
		t.new Column("D", Integer.TYPE);	// test isNullable
		generatedCreateTable = testInstance.generateCreateTable(t);
		Assert.assertEquals(generatedCreateTable, "create table Toto(A type, B type, C type primary key, D type not null)");
	}
	
	@Test
	public void testGenerateCreateIndex() throws Exception {
		DDLGenerator testInstance = new DDLGenerator(null);
		
		Table t = new Table(null, "Toto");
		Column colA = t.new Column("A", String.class);
		Column colB = t.new Column("B", String.class);
		
		Index idx1 = new Index(colA, "Idx1");
		String generatedCreateIndex = testInstance.generateCreateIndex(idx1);
		Assert.assertEquals(generatedCreateIndex, "create index Idx1 on Toto(A)");
		
		Index idx2 = new Index(new KeepOrderSet<>(colA, colB), "Idx2");
		generatedCreateIndex = testInstance.generateCreateIndex(idx2);
		Assert.assertEquals(generatedCreateIndex, "create index Idx2 on Toto(A, B)");
	}
	
	@Test
	public void testGenerateCreateForeignKey() throws Exception {
		DDLGenerator testInstance = new DDLGenerator(null);
		
		Table toto = new Table(null, "Toto");
		Column colA = toto.new Column("A", String.class);
		Column colB = toto.new Column("B", String.class);
		
		Table titi = new Table(null, "Titi");
		Column colA2 = titi.new Column("A", String.class);
		Column colB2 = titi.new Column("B", String.class);
		
		ForeignKey foreignKey = new ForeignKey(colA, "FK1", colA2);
		String generatedCreateIndex = testInstance.generateCreateForeignKey(foreignKey);
		Assert.assertEquals(generatedCreateIndex, "alter table Titi add constraint FK1 foreign key(A) references Titi(A)");
		
		foreignKey = new ForeignKey(new KeepOrderSet<>(colA, colB), "FK1", new KeepOrderSet<>(colA2, colB2));
		generatedCreateIndex = testInstance.generateCreateForeignKey(foreignKey);
		Assert.assertEquals(generatedCreateIndex, "alter table Titi add constraint FK1 foreign key(A, B) references Titi(A, B)");
	}
}