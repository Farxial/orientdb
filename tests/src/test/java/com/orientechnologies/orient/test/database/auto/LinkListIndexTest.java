package com.orientechnologies.orient.test.database.auto;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @since 21.03.12
 */
@Test(groups = { "index" })
public class LinkListIndexTest extends DocumentDBBaseTest {

	@Parameters(value = "url")
	public LinkListIndexTest(@Optional String url) {
		super(url);
	}


  @BeforeClass
  public void setupSchema() {
    final OClass linkListIndexTestClass = database.getMetadata().getSchema().createClass("LinkListIndexTestClass");

    linkListIndexTestClass.createProperty("linkCollection", OType.LINKLIST);

    linkListIndexTestClass.createIndex("linkCollectionIndex", OClass.INDEX_TYPE.NOTUNIQUE, "linkCollection");
    database.getMetadata().getSchema().save();
  }

  @AfterClass
  public void destroySchema() {
		database.open("admin", "admin");
    database.getMetadata().getSchema().dropClass("LinkListIndexTestClass");
  }


  @AfterMethod
  public void afterMethod() throws Exception {
    database.command(new OCommandSQL("DELETE FROM LinkListIndexTestClass")).execute();

    List<ODocument> result = database.command(new OCommandSQL("select from LinkListIndexTestClass")).execute();
    Assert.assertEquals(result.size(), 0);

    result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();
    Assert.assertEquals(result.size(), 0);

		super.afterMethod();
  }

  public void testIndexCollection() {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docTwo.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionInTx() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    try {
      database.begin();
      final ODocument document = new ODocument("LinkListIndexTestClass");
      document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
      document.save();
      database.commit();
    } catch (Exception e) {
      database.rollback();
      throw e;
    }

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docTwo.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdate() {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docThree = new ODocument();
    docThree.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docThree.getIdentity())));
    document.save();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docThree.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdateInTx() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docThree = new ODocument();
    docThree.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    try {
      database.begin();
      document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docThree.getIdentity())));
      document.save();
      database.commit();
    } catch (Exception e) {
      database.rollback();
      throw e;
    }

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docThree.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdateInTxRollback() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docThree = new ODocument();
    docThree.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    database.begin();
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docThree.getIdentity())));
    document.save();
    database.rollback();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docTwo.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdateAddItem() {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docThree = new ODocument();
    docThree.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    database.command(new OCommandSQL("UPDATE " + document.getIdentity() + " add linkCollection = " + docThree.getIdentity()))
        .execute();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 3);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docTwo.getIdentity())
          && !d.field("key").equals(docThree.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdateAddItemInTx() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docThree = new ODocument();
    docThree.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    try {
      database.begin();
      ODocument loadedDocument = database.load(document.getIdentity());
      loadedDocument.<List> field("linkCollection").add(docThree.getIdentity());
      document.save();
      database.commit();
    } catch (Exception e) {
      database.rollback();
      throw e;
    }

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 3);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docTwo.getIdentity())
          && !d.field("key").equals(docThree.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdateAddItemInTxRollback() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docThree = new ODocument();
    docThree.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    database.begin();
    ODocument loadedDocument = database.load(document.getIdentity());
    loadedDocument.<List> field("linkCollection").add(docThree.getIdentity());
    loadedDocument.save();
    database.rollback();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docTwo.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdateRemoveItemInTx() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    try {
      database.begin();
      ODocument loadedDocument = database.load(document.getIdentity());
      loadedDocument.<List> field("linkCollection").remove(docTwo.getIdentity());
      loadedDocument.save();
      database.commit();
    } catch (Exception e) {
      database.rollback();
      throw e;
    }

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 1);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdateRemoveItemInTxRollback() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    database.begin();
    ODocument loadedDocument = database.load(document.getIdentity());
    loadedDocument.<List> field("linkCollection").remove(docTwo.getIdentity());
    loadedDocument.save();
    database.rollback();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docTwo.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionUpdateRemoveItem() {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    database.command(new OCommandSQL("UPDATE " + document.getIdentity() + " remove linkCollection = " + docTwo.getIdentity()))
        .execute();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 1);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionRemove() {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();
    document.delete();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 0);
  }

  public void testIndexCollectionRemoveInTx() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();
    try {
      database.begin();
      document.delete();
      database.commit();
    } catch (Exception e) {
      database.rollback();
      throw e;
    }

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 0);
  }

  public void testIndexCollectionRemoveInTxRollback() throws Exception {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    database.begin();
    document.delete();
    database.rollback();

    List<ODocument> result = database.command(new OCommandSQL("select key, rid from index:linkCollectionIndex")).execute();

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    for (ODocument d : result) {
      Assert.assertTrue(d.containsField("key"));
      Assert.assertTrue(d.containsField("rid"));

      if (!d.field("key").equals(docOne.getIdentity()) && !d.field("key").equals(docTwo.getIdentity())) {
        Assert.fail("Unknown key found: " + d.field("key"));
      }
    }
  }

  public void testIndexCollectionSQL() {
    final ODocument docOne = new ODocument();
    docOne.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument docTwo = new ODocument();
    docTwo.save(database.getClusterNameById(database.getDefaultClusterId()));

    final ODocument document = new ODocument("LinkListIndexTestClass");
    document.field("linkCollection", new ArrayList<ORID>(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity())));
    document.save();

    List<ODocument> result = database.query(new OSQLSynchQuery<ODocument>(
        "select * from LinkListIndexTestClass where linkCollection contains ?"), docOne.getIdentity());
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(Arrays.asList(docOne.getIdentity(), docTwo.getIdentity()), result.get(0).<List> field("linkCollection"));
  }
}
