# Functional Requirements License Creator

## Background
At certain moments in time datasets require a new license. This is mainly when a dataset is first ingested (both via the front-end via `WebUI` or via the backend using `EASY-Ingest` and `EASY-Deposit`) or when something in the dataset's metadata changes (using one of the tools written for specific tasks). EASY-License-Creator facilitates the generation of a license according to a given dataset, but does *not* ingest the license into the database.

### Former version
The main reason for creating this separate module is to replace the old license generator in the business logic, as this version turns out to not be particularly usable in the various modules that require the generation of a new license. The old generator takes the *dataset*, *depositor data* and an `OutputStream` to which the output is written as its arguments and returns `Unit`.

## New design
The new design of the License Creator consists of (1) an API which can be called from within other modules that are dependents of this module and (2) a command line tool. In case the latter is used, we assume that the dataset as well as the depositor data are already present in EASY. Again notice that this command line tool is not intended to ingest the newly generated license into EASY!

The input and output of both parts of the License Creator are as follows:
  * *input (via command line):* dataset identifier, output file location
  * *input (via API call):* either one of
      * dataset identifier, `OutputStream`
      * dataset object, `OutputStream`
      * TODO other forms of input
  * *output (via command line):* pdf document with the license agreement
  * *output (via API call):* `Unit`

### Functionality
  * Generate a license agreement according to the dataset and the corresponding depositor data.
  * From the dataset mainly the EMD and the list of files are used.
  * From the depositor the contact information is used.
  * The interface of this module will comply with the current interface that is used in the business layer.

### Non-functionality
  * The License Creator will *not* add the newly created license agreement to EASY.
  * The License Creator wil *not* send emails to depositors whose datasets are modified or newly ingested.
  * No data is written to the databases; this module only reads data!

### Additions relative to the former version
  * In the list of files in the dataset the access category for each file needs to be included.
  * An explaination of the distinct access categories from the previous item.

## Resources
The license is generated from a series of template files with placeholders. Using the resources listed below this module can resolve these placeholders and transform the whole text into a pdf.

### Template files
  * `Appendix.html` - an appendix with more information about the CC0 access category
  * `Body.html` - the main content of the license agreement text
  * `dans_logo.jpg` - the Dans logo to be displayed in the header of each page
  * `Embargo.html` - an optional text in case the dataset is under embargo
  * `FileTable.html` - an overview of all the files in the dataset, showing the file path, checksum and access category
  * `License.html` - the main file in which all the other html files are merged together
  * `LicenseVersion.txt` - the version of the license to be displayed in the footer of the license
  * `MetadataTerms.properties` - a mapping between terms from the metadata and the text to be displayed in the license
  * `Table.html` - an overview of the metadata of the dataset

### Data resources
  * *Fedora* - metadata of the dataset is stored in Fedora. The EMD datastream dissemination contains the metadata of the dataset itself, the AMD datastream dissemination contains the depositor identifier, the EASY_FILE datastream and EASY_FILE_METADATA datastream dissemination contain the data of the files in the dataset.
  * *LDAP* - the depositor data required for the license is stored in LDAP.
  * *RiSearch* - this is part of Fedora and provides the relation between the dataset and the files.

### Required data in the template


+benodigde data voor de templates+
||Data||Gebruikt in||Waar te vinden||
|Dataset.getDmoStoreId()|code {{LicenseComposer.java:205}}|-{{dc:identifier}} of {{foxml:digitalObject@PID}} of- *+ingegeven in applicatie+*|
|Dataset.getDansManagedDoi|template {{Body.html}}|{{emd:identifier // dc:identifier}}|
|Dataset.getEncodedDansManagedDoi|template {{Body.html}}, see the link at {{Body.html:3}}|{{let id = emd:identifier // dc:identifier in (id@eas:identification-system ++ "/" ++ id.value)}}|
|DatasetDates.getDateSubmitted|template {{Body.html}}| |
|Dataset.getDateSubmitted|code {{LicenseComposer.java:83}}|{{emd:date // eas:dateSubmitted}}|
|Dataset.getPreferredTitle|template {{Body.html}}|{{emd:title // dc:title}}|
|Dataset.getAccessCategory|code {{LicenseComposer.java:192}}|{{emd:rights // dct:accessRights}} of {{dc:rights}} ({color:#d04437}*deze zijn altijd hetzelfde, alleen verschillend (export) schema, dus kunnen we altijd de waarde uit EMD gebruiken (zodat zo min mogelijk Fedora requests gemaakt hoeven te worden!)*{color})|
|Dataset.isUnderEmbargo|code {{LicenseComposer.java:193}}|berekenen op basis van de huidige datum en {{Dataset.getDateAvailable}}|
|DatasetDates.getDateAvailable|template {{Embargo.html}}| |
|Dataset.getDateAvailable|code {{LicenseComposer.java:87}}|{{emd:date // eas:available}}|
|String.toString|template {{Tail.html}}, this is the timestamp of creating the license: {{new org.joda.time.DateTime().toString("YYYY-MM-dd HH:mm:ss"))}}|wordt op runtime berekend|
|EasyUser.getDisplayName|template {{Body.html}}|LDAP user database - {{(givenName <> initials)? + dansPrefixes? + sn?}}|
|EasyUser.getOrganization|template {{Body.html}}|LDAP user database - {{o}}|
|EasyUser.getAddress|template {{Body.html}}|LDAP user database - {{postalAddress}}|
|EasyUser.getPostalCode|template {{Body.html}}|LDAP user database - {{postalCode}}|
|EasyUser.getCity|template {{Body.html}}|LDAP user database - {{l}}|
|EasyUser.getCountry|template {{Body.html}}|LDAP user database - {{st}}|
|EasyUser.getTelephone|template {{Body.html}}|LDAP user database - {{telephoneNumber}}|
|EasyUser.getEmail|template {{Body.html}}|LDAP user database - {{mail}}|
