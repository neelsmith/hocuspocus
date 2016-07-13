package edu.holycross.shot.hocuspocus

import edu.harvard.chs.cite.CtsUrn
import edu.harvard.chs.cite.TextInventory

import org.junit.Test
import static groovy.test.GroovyAssert.shouldFail


/** */
class TestTablesUtilExport {


  @Test
  void testTablesUtilIndexSeq() {
    File tabs = new File("testdata/il17-tabs.txt")

    def expectedSize = 760


    TablesUtil tu = new TablesUtil()
    def seqIdx = tu.getSequenceIndex(tabs)
    assert seqIdx.size() == expectedSize


    // tst conversion to 82XF of single line
    // tst conversion to 82XF of initial line
    // tst conversion to 82XF of final line
    // tst that namespace decl is ignored
    // thro error otherwise?

    // test indexing of urn by seq


  }



}
