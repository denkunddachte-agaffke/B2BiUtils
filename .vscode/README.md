# BPML code snippets for Visual Studio Code

The `bpml.code-snippets` provides autocompletion for many commonly used service/adapter calls on Sterling B2B Integrator.
Put this file into `[ProjectDir]/.vscode` folder of your project and quickly create common operations by typping `bp` and choosing the service/adapter call you want to add to your code.

## Notes

* Install the following extensions to help with XML/BPML files:
  * XML extension by RedHat to add language support for XML
  * XML Tools extension by Josh Johnson (add support for XPath)
  * Auto Close Tag extension by Jun Han
  * Auto Close Tag extension, also by Jun Han
* Make sure to assign the `.bpml` file extension to the XML file format in VSCode (search for "files associatons" in settings and add `*.bpml` &rarr; `XML` mapping)

When working with XML files (like ProcessData), it is helpful to have the XPath tools (get/evaluate XPath) in the context menu. To add these menu items, look for the `pacakge.json` file for the `XML Tools` extension (e.g. in `[VSCodeDirectory]/data/extensions/dotjoshjohnson.xml-2.5.1/package.json`) and add the commands `xmlTools.getCurrentXPath` and `xmlTools.evaluateXPath` like:

```json
...
"editor/context": [
    {
      "command": "xmlTools.getCurrentXPath",
      "group": "1_modification@100",
      "when": "editorLangId == xml"
     },
     {
      "command": "xmlTools.evaluateXPath",
      "group": "1_modification@100",
      "when": "editorLangId == xml"
     },
     {
     "command": "xmlTools.minifyXml",
     "group": "1_modification@100",
     "when": "editorLangId == 'xml'"
    }
   ]
...
```

## B2B Integrator services and adpaters

Some snippets address local adapter/service configurations with prefix `DD_` like

* `DD_JavaTaskService` (empty JavaTask service)
* `DD_JDBC_ADAPTER_01` (Lighweight JDBC Adapter)
* `DD_CLA_LOCAL` (local Commandline Adapter 2 instance)
