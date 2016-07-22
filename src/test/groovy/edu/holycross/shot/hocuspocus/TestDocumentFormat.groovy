package edu.holycross.shot.hocuspocus

import org.junit.Test
import static groovy.test.GroovyAssert.shouldFail





/**
*/
class TestDocumentFormat  {



    def expectedLabels = ["xml", "2cols", "82xf", "markdown"]

    @Test
    void testDocFormatEnum() {
      ArrayList testList = DocumentFormat.values()  as ArrayList
      testList.eachWithIndex { n, i ->
        assert n.getLabel() == expectedLabels[i]
      }
    }


    @Test
    void testIndex() {
      assert DocumentFormat.getByLabel("xml") == DocumentFormat.XML
      assert DocumentFormat.getByLabel("82xf") == DocumentFormat.O2XF
      assert DocumentFormat.getByLabel("2cols") == DocumentFormat.TWO_COLS
      assert DocumentFormat.getByLabel("markdown") == DocumentFormat.MARKDOWN
    }

}
