package edu.holycross.shot.hocuspocus

import edu.harvard.chs.cite.TextInventory
import edu.harvard.chs.cite.CtsUrn

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import org.apache.commons.io.FilenameUtils

/**
* Fundamental class representing the archival version of atext corpus 
* as a collection of XML documents cataloged by a CTS TextInventory.
*/
class Corpus {

    int debug = 0

    /** Character encoding to use for all file output. */
    String charEnc = "UTF-8"

    /** TTL prefix declaration for HMT namespace. */
    String prefix = "@prefix hmt:        <http://www.homermultitext.org/hmt/rdf/> .\n@prefix cts:        <http://www.homermultitext.org/cts/rdf/> .\n@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n@prefix dcterms: <http://purl.org/dc/terms/> .\n"



    /** TextInventory with entries for all documents in the corpus. 
    */
    TextInventory  inventory

    /** XML serialization of TextInventory information, in a File.
    */
    File  inventoryXml

    /** Root directory of file system containing archival files.
    */
    File baseDirectory


    /** Constructor using a local File object for the corpus' TextInventory.
    * @param invFile File with the corpus' TextInventory.
    * @baseDir Root directory of file system containing archival files.
    * @throws Exception if invFile is not a valid TextInventory, or if
    * archive directory is not readable
    */
    Corpus(File invFile, File baseDir) 
    throws Exception{
        try{
            this.inventory = new TextInventory(invFile)
        } catch (Exception e) {
            throw e
        }

        if (!baseDir.canRead()) {
            throw new Exception("Corpus: cannot read directory ${baseDir}")
        }
        this.baseDirectory = baseDir
        this.inventoryXml = invFile

        try {
            validateInventory()

        } catch (Exception invException) {
            throw invException
        }
    }


    /** Creates a Tabulator object and uses it to tabulate
    * a given document.
    * @param f The source file in the archive to tabulate.
    * @param urn The URN for the file to tabulate.
    * @param outputDir A writeable directory for output files.
    */
    void tabulateFile(File f, CtsUrn urn, File outputDir) {
        Tabulator tab = new Tabulator()
        tab.tabulate(urn, inventory, f, outputDir)
    }


    /** Creates a tabular representation of every document
    * in the corpus.
    * @param ouputDir A writeable directory where tabulated files
    * will be written.
    */
    void tabulateRepository(File outputDir) {
        urnsInInventory().each { u ->
            CtsUrn urn = new CtsUrn(u)
            File f = new File(baseDirectory, inventory.onlineDocname(urn))
            if (debug > 0) {
                System.err.println "Tabulating ${urn} with file ${f}..."
            }

            tabulateFile(f, urn, outputDir)
       }
    }



    /** Cycles through all tabular files in a directory,
    * turtleizing each file.
    * Output is written to a file named "cts.ttl" in the same directory.
    * @param outputDir Directory containing tabular format files with names
    * ending in 'txt'.  Must be a writable directory.
    */
    void turtleizeTabs(File outputDir) {
        turtleizeTabs(outputDir, false)
    }


    /** Cycles through all tabular files in a directory,
    * first turtleizing each file.  If destructive is true, it
    * then deletes the source file.
    * Output is written to a file named "cts.ttl" in the same directory.
    * @param outputDir Directory containing tabular format files with names
    * ending in 'txt'.  Must be a writable directory.
    * @param destructive True if tab files should be deleted
    * after turtelizing.
    */
    void turtleizeTabs(File outputDir, boolean destructive) {
        turtleizeTabs(outputDir, "${outputDir}/cts.ttl", destructive)
    }

    /** Cycles through all tabular files in a directory,
    * first turtleizing each file.  If destructive is true, it
    * then deletes the source file.
    * Output is written ttlFileName.
    * @param outputDir Directory containing tabular format files with names
    * ending in 'txt'.  Must be a readable directory.
    * @param ttlFileName Name of output file.
    * @param destructive True if tab files should be deleted
    * after turtelizing.
    */ 
    void turtleizeTabs(File outputDir, String ttlFileName, boolean destructive) {
        File  ttl = new File(ttlFileName)
        turtleizeTabs(outputDir, ttl, destructive)
    }

    /** Cycles through all tabular files in a directory,
    * first turtleizing each file.  If destructive is true, it
    * then deletes the source file.
    * Output is written to a file named "cts.ttl" in the same directory.
    * @param outputDir Directory containing tabular format files with names
    * ending in 'txt'.  Must be a writable directory.
    * @param ttl Output file.
    * @param destructive True if tab files should be deleted
    * after turtelizing.
    */ 
    void turtleizeTabs(File outputDir, File ttl, boolean destructive) {

        CtsTtl turtler = new CtsTtl(this.inventory)
        Integer fileCount = 0
        outputDir.eachFileMatch(~/.*.txt/) { tab ->  
            fileCount++;
            if (fileCount == 1) {
                ttl.append(turtler.turtleizeTabs(tab, false), charEnc)
            } else {
                ttl.append(turtler.turtleizeTabs(tab, false), charEnc)
            }
            if (destructive) { tab.delete() }
        }
    }


    /** Writes a RDF TTL representation of the entire CTS repository
    * to a file. First generates TTL for the repository's TextInventory,
    * then tabulates all files in the repository, and turtleizes
    * the resulting tab files. 
    * @param ttlFile Writable output file.
    * @param tabDir Writable directory for generated tab files.
    */
    void ttl(File outputFile, File tabDir) {
        ttl(outputFile, false, tabDir)
    }

    /** Writes a RDF TTL representation of the entire CTS repository
    * to a file. First generates TTL for the repository's TextInventory,
    * then tabulates all files in the repository, and turtleizes
    * the resulting tab files.  All output is written to outputFile.
    * @param ttlFile Writable output file.
    * @param includePrefix Whether or not to including prefix statements
    * in the output RDF.
    * @param tabDir Writable directory for generated tab files.
    */
    void ttl(File ttlFile, boolean includePrefix, File tabDir) {
        if (debug > 0) {
            System.err.println "Ttl'ing to ${ttlFile} after tabbing to ${tabDir}"
        }
        if (includePrefix) {
            ttlFile.append(prefix, charEnc)
        }

        CtsTtl turtler = new CtsTtl(this.inventory)        
        ttlFile.append( turtler.turtleizeInv(), charEnc)

        tabulateRepository(tabDir)
        turtleizeTabs(tabDir, ttlFile, false)
    }


    /** First tabulates the entire inventory, then uses the resulting
    * tabulated files to tokenize the inventory using the specified
    * Tokenization system, and writes resulting RDF TTL in outputDir.
    * @param tokenSystem TokenizationSystem to apply to the inventory.
    * @param outputDir A writable directory where the output will be created.
    */
    void tokenizeInventory(TokenizationSystem tokenSystem, File outputDir) {
        tokenizeInventory(tokenSystem, outputDir, "#")
    }

    /** First tabulates the entire inventory, then uses the resulting
    * tabulated files to tokenize the inventory using the specified
    * Tokenization system, and writes resulting RDF TTL in outputDir.
    * Tabulated files are deleted after tokenization.
    * @param tokenSystem TokenizationSystem to apply to the inventory.
    * @param outputDir A writable directory where the output will be created.
    * @param separatorString String value used to separator fields of 
    * tabulated files.
    */
    void tokenizeInventory(TokenizationSystem tokenSystem, File outputDir, String separatorString) {
        File tokensFile = new File(outputDir, "tokens.txt")
        tabulateRepository(outputDir)
        outputDir.eachFileMatch(~/.*.txt/) { tab ->  
            def tokenData = tokenSystem.tokenize(tab, separatorString)
            tokenData.each {  pair ->
                tokensFile.append( "${pair[0]}\t${pair[1]}\n", "UTF-8")
            }
            tab.delete()
        }
    }


    /** Validates the XML serialization of the corpus's TextInventory 
    * against the published schema for a CITE TextInventory.
    * @throws Exception if the XML does not validate.
    */
    void validateInventory() 
    throws Exception {
        // as an alternative, allow a local copy of schmea ...
        URL TextInvSchema = new URL("http://www.homermultitext.org/hmtschemas/TextInventory.rng")

//        URL TextInvSchema = new URL("file://./testdata/TextInventory.rng")
        System.setProperty("javax.xml.validation.SchemaFactory:"+XMLConstants.RELAXNG_NS_URI,
    "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory");

        def factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI)
        def schema = factory.newSchema(TextInvSchema)
        def validator = schema.newValidator()
        try {
            validator.validate(inventoryXml)
        } catch (Exception e) {
            throw e
        }
    }

    /** Determines if the set of online documents in the corpus'
    * TextInventory has a one-to-one correspondence with the set of
    * .xml files in the corpus' file storage.
    * @returns True if the two sets are equal, otherwise false.
    */
    boolean filesAndInventoryMatch() {
        def invList = []
        try {
            this.inventory.allOnline().each { urn ->
                invList.add(this.inventory.onlineDocname(urn))
            }
            def invSet = invList as Set
            def fileSet = this.filesInArchive() as Set
            return (invSet == fileSet)
        } catch (Exception e) {
            throw new Exception("")
        }
    }

    /** Creates a list of all files categorized as "online" in the
    * corpus TextInventory, but not appearing as a file in the file system.
    * @returns A (possibly empty) list of file names.
    */
    ArrayList inventoriedMissingFromArchive() {
        def missing = []
        def fileSet = this.filesInArchive()
        def invList = []
        this.inventory.allOnline().each { urn ->
            invList.add(this.inventory.onlineDocname(urn))
        }

        invList.each { urn ->
            if (urn in fileSet) {
            } else {
                missing.add(urn)
            }
        }
        return missing
    }


    /** Creates a list of all .xml files in the archive lacking a 
    * corresponding "online" entry in the corpus TextInventory.
    * @returns A (possibly empty) list of file names.
    */
    ArrayList inventoriedMissingFromInventory() {
        def missing = []
        def invList = []
        this.inventory.allOnline().each { urn ->
            invList.add(this.inventory.onlineDocname(urn))
        }

        this.filesInArchive().each { urn ->
            if (urn in invList) {
            } else {
                missing.add(urn)
            }
        }
        return missing
    }

    /** Creates a list of file paths relative to the archive root
    * for documents marked in the corpus text inventory as online.
    */
    ArrayList filesInInventory() {
        def onlineList = []
        this.inventory.allOnline().each { urn ->
            onlineList.add(this.inventory.onlineDocname(urn))
        }
        return onlineList
    }

    /** Creates a list of CTS URNs for documents marked in the 
    * corpus text inventory as online.
    */
    ArrayList urnsInInventory() {
        def onlineList = []
        this.inventory.allOnline().each {
            onlineList.add(it)
        }
        return onlineList
    }

    /**  Recursively walks through the file system where archival
    * files are kepts, and finds all finds with names ending in '.xml'.
    * @returns A list of file names, with paths relative to the 
    * base directory of this corpus' file storage.
    */
    ArrayList filesInArchive() {
        def fileList = []
        def relativeBase = baseDirectory.toString()
        
        baseDirectory.eachFileMatch(~/.*.xml/) { file ->  
            def stripped = file.toString().replaceFirst(relativeBase,'')
            if (stripped[0] == '/') {
                stripped = stripped.replaceFirst('/','')
            }
            fileList.add(stripped)
        }

        baseDirectory.eachDirRecurse() { d ->  
            d.eachFileMatch(~/.*.xml/) { file ->  
                def stripped = file.toString().replaceFirst(relativeBase,'')
                if (stripped[0] == '/') {
                    stripped = stripped.replaceFirst('/','')
                }
                fileList.add(stripped)
            }  
        }  
        return fileList
    }

}
