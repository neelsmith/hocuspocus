# hocuspocus#

`hocuspocus` is a system for managing a corpus of citable texts.  It is implemented in groovy, 
and uses gradle for its build system. You can retrieve binary jars, and zip files with source and documentation directly from the Nexus reposistory at [http://beta.hpcc.uh.edu/nexus](http://beta.hpcc.uh.edu/nexus) or by using the maven coordinates group `edu.holycross.shot`, name `hocuspocus`.

Additional documentation in multimarkdown format is in the `docs` directory.

##Usage and prerequisites##

The hocus-pocus library sets the system property `javax.xml.validation.SchemaFactory` to use `com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory` so `jing` must be available on the class path to use the hocus-pocus library.

##Naive assumptions##

XML abbreviations will be unique in a corpus.

