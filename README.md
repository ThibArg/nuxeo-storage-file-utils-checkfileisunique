nuxeo-storage-file-utils-checkfileisunique
==========================================

Checks if the file of a document is unique when the doc is created/modifed. The document not created/modified if the file is not unique, and an error is displayed to the user, "Duplicate file detected".

The plug-in checks and handles "unicity" of the file by doing the following:

* Install an event listener for "documentCreted" and "documentModified"
* In this listener, if the document has the <code>file</code> schema and a non-null blob in <code>file:content</code>, then:
  * A NXQL query is done, querying for documents having the same digest (and not the same id, so we ignore current document)
  * If at least one document is found, the listener throws a <code>RecoverableClientException</code>
 

##Build/Install
Build with maven. You will do the usual cd path/to/this/repository, then mvn package


##Important

* To check the binary is unique, the plug-in handles deleted document, proxies and version, so documents in the trash, versions and proxies are not taken into account

* The plug-in only checks <code>file:content</code>. It does not handle the <code>files</code> schema (plural form, "attachments")

* There is a (small?) problem. When the duplicate is created by drag-drop, the UI in the browser displays an "Unknown server error" alert, and the Document is not created. So, all is good on the business rule point of view, but not on the UI requirement.

* The plug-in implements 2 ways for querying for duplicate files: using NXQL or using SQL. Look at the code to see how it is done. We recommend using the NXQL way since it handles the underlying joins/relations for us ()It is then safer for future version of Nuxeo.)

  The current code uses NXQL of course. Just comment/uncomment the apropriate parts to test the different ways.
* I also commented the log parts which were usefull during development. Just uncomment, or change log level, etc...

* **About performance**: I don't know what will be the performance if the repository contains hundred thousands/millions of documents. I suspect it will the n be a good idea to index the content.data field.


## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep) and packaged applications for [document management](http://www.nuxeo.com/en/products/document-management), [digital asset management](http://www.nuxeo.com/en/products/dam) and [case management](http://www.nuxeo.com/en/products/case-management). Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information on: <http://www.nuxeo.com/>
