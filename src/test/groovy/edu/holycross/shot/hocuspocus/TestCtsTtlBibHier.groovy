package edu.holycross.shot.hocuspocus

import edu.harvard.chs.cite.CtsUrn
import edu.harvard.chs.cite.TextInventory

import org.junit.Test
import static groovy.test.GroovyAssert.shouldFail


/** */
class TestCtsTtlBibHier {

  CitationConfigurationFileReader confFile = new CitationConfigurationFileReader(new File("testdata/conf2016/citationconfig3.xml"))

  TextInventory ti = new TextInventory(new File("testdata/testcorpus2016/testinventory-2016.xml"))

  @Test
  void testExemplar() {
    String ttl = CtsTtl.turtleizeInv(ti, confFile, false)
    // 3 texts online:

    System.err.println "\nTestCtsTtlBibHier: \n" +  ttl
  }

}
