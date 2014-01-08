package edu.holycross.shot.hocuspocus

import static org.junit.Assert.*
import org.junit.Test

import edu.harvard.chs.cite.TextInventory
import edu.harvard.chs.cite.CtsUrn

/**
*/
class TestCorpusOnline extends GroovyTestCase {

    String testTIFile = "testdata/testCorpusTextInv.xml"
    String testOnlineTIFile = "testdata/testOnlineCorpusTextInv.xml"


    /** Tests contents of TextInventory against
    * contents of archive.
    */
    @Test void testOnline() {
        def expectedInvOnline = ["urn:cts:greekLit:tlg0012.tlg001.msA", "urn:cts:greekLit:tlg0012.tlg001.msB"]

        def relativeBase = "testdata/archive/"
        File inv = new File(testTIFile)
        File dir = new File(relativeBase)
        Corpus c = new Corpus(inv, dir)

        assert  c.inventory.allOnline() == expectedInvOnline

        def expectedInArchive = [ 'A_Iliad_p5uc.xml', 'tier2/B_Iliad_p5.xml'] as Set
        def filesInArchive = c.filesInArchive() as Set

        assert expectedInArchive == filesInArchive
        assert c.filesAndInventoryMatch()
    }


    /** Tests methods reporting file names and URNs of documents
    * the archive.
    */
    @Test void testOnlineMethods() {

        File invFile = new File(testOnlineTIFile)
        File dir = new File("testdata/testArchive")
        Corpus c = new Corpus(invFile, dir)

        def expectedSet = ["A_Iliad_testlines.xml", "B_Iliad_test2.xml", "no-namespace.xml"] as Set
        def actualSet = c.filesInInventory() as Set
        assert expectedSet == actualSet

        def expectedUrns =  ["urn:cts:greekLit:tlg0012.tlg001.testlines", "urn:cts:greekLit:tlg0012.tlg001.test2", "urn:cts:test1:test.unclassified.unittest"] as Set
        def actualUrns = c.urnsInInventory() as Set
        assert actualUrns == expectedUrns
    }

}
