package edu.holycross.shot.hocuspocus

import org.apache.commons.io.FilenameUtils
import edu.harvard.chs.f1k.GreekNode

/*
Do we want to check for?
 <unclear>, <supplied>
*/


/** Class to tokenize text following Homer Multitext project conventions for
*   definition of character set and markup.
*/
class HmtGreekTokenization implements TokenizationSystem {


    /** Empty constructor.
    */
    HmtGreekTokenization() {
    }


    /** Gets a short phrase describing the kind of tokenization
    * done by this implementation of TokenizationSystem.
    * @returns A descriptive String.
    */
    String getDescription() {
        return "HMT Greek tokenization."
    }

    /** Closure tokenizes a String on white space.
    * @param str The String to tokenize.
    * @returns An ArrayList of Strings.
    */
    def tokenizeString = { str ->
        def tokes = str.split(/[\s]+/)
        tokes
    }    



    /** Recursively tokenizes a well-formed fragment of a document following HMT project
    * editorial conventions.  Tokenization considers both markup and type of characters.
    * @param node The root node of the fragment to tokenize.
    * @urnBase The CTS URN value, as a String, of the citable node this XML fragment
    * belongs to.
    * @tokenType A String, possibly empty, classifying the tokens belonging to this 
    * node.
    * @returns A List of pairings.  Each pairing is an ArrayList containing two Strings.
    * The first is the String value of a CTS URN including subreference, identifying 
    * the token; the second is one of the following values for token class: 
    * "lexical", "namedEntity", "numeric", "waw", "sic".
    */
    ArrayList tokenizeElement(Object node, String urnBase, String tokenType) {

        def returnVal = []
        if (node instanceof java.lang.String) {
            tokenizeString(node).each { t ->
                def pairing
                if (tokenType.size() > 0) {
                    pairing = ["${urnBase}@${t}", tokenType]
                } else {
                    pairing = ["${urnBase}@${t}", "lexical"]
                }
                returnVal.add(pairing)
            }

        } else {
            String nodeName
            if (node.name() instanceof java.lang.String) {
                nodeName = node.name()
            } else {
                nodeName = node.name().getLocalPart()
            }


            switch (nodeName) {
                case "del":
                case "abbr":
                case "figDesc":
                    // Completely ignore contents
                    break

                case "num" :
                node.children().each { child ->
                    tokenizeElement(child, urnBase, "numeric").each { tokenList ->
                        returnVal.add(tokenList)
                    }
                }

                break



                case "sic":
                    GreekNode n = new GreekNode(node)
                String wd = n.collectText()
                wd = wd.replaceAll(~/\s/, "") // generalize to all white space
                if (tokenType.size() > 0) {
                    returnVal.add(["${urnBase}@${wd}", "sic"])
                } 
                break


                case "w":
                    GreekNode n = new GreekNode(node)
                String wd = n.collectText()
                wd = wd.replaceAll(~/\s/, "") // generalize to all white space
                if (tokenType.size() > 0) {
                    returnVal.add(["${urnBase}@${wd}", tokenType])
                } else {
                    returnVal.add(["${urnBase}@${wd}", "lexical"])
                }
                break

                case "persName":
                case "placeName":
                    GreekNode n = new GreekNode(node)
                String wd = n.collectText()
                    returnVal.add(["${urnBase}@${wd}", "namedEntity:${node.'@n'}"])
                break

                case "rs":
                    if (node.'@type' == "waw") {
                    node.children().each { child ->
                    tokenizeElement(child, urnBase, "waw").each { tokenList ->
                        returnVal.add(tokenList)
                    }
                    } }
                    break

                case "ref":
                case "note":
                   /* Do not process these metadata elements.*/
                break

                default:
                node.children().each { child ->
                    tokenizeElement(child, urnBase, "").each { tokenList ->
                        returnVal.add(tokenList)
                    }
                }
                break
            }

        }
        return returnVal
    }

    /** Tokenizes the contents of a text in tabular form.
    * @param inputFile A document in tabular format to tokenize.
    * @param separatorStr The String value used to separate columns of
    * the tabular file.  Default value is "#".
    * @returns A List of pairings.  Each pairing is an ArrayList containing two Strings.
    * The first is the String value of a CTS URN including subreference, identifying 
    * the token; the second is one of the following values for token class: 
    * "lexical", "namedEntity", "numeric", "waw".
    * @throws Exception if last column of tabular file cannot be parsed as a
    * well-formed XML fragment.
    */
    ArrayList tokenize(File inputFile, String separatorStr) 
    throws Exception {
    System.err.println "Tokenizing input file " + inputFile
        def replyList = []
        inputFile.eachLine { l ->
            def cols = l.split(/${separatorStr}/)
            def urnBase = cols[0]
            if (cols.size() > 5) {
                try {
                    String str = cols[5]
                    def root = new XmlParser().parseText(str)
                    tokenizeElement(root, urnBase, "").each { t ->
                        replyList.add(t)
                    }

                } catch (Exception e) {
                    System.err.println "HmtGreekTokenization:tokenize: exception"
                    System.err.println "FAILED TO PARSE LINE: ${l}"
                    throw e
                }

            } else {
                System.err.println "HmtGreekTokenization: omit input line ${l}"
            }
        }

        return replyList

    }

}
