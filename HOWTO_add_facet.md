# Howto add new facet to VLO

This howto serves as a poc for adding a new facet to the VLO. The final goal is to display the added facets on a 
separate tab in VLO portal. 

Most of the current work is done in `vlo-commons` and `vlo-importer` module.  



## Files modified in order to add new facet to VLO 
Files to be modified in `vlo-commons` module, path: `vlo-commons/src/main/java/eu/clarin/cmdi/vlo/
- `./FieldKey.java`: Field is only added to VLO when it's defined here. It is **not** yet a facet. 
- `resources/VloConfig.xml` **TODO** It seems that the field added here becomes a facet in VLO and put through post-process, need verification. 
- `resources/VloConfig.xsd` Needs the key here to pass validation. 

Files to be modified in `vlo-importer` module, path: `vlo-importer/src/main/java/eu/clarin/cmdi/vlo/importer/`
- `Metadataimporter.java`: Defines which post-processor to use for each field. This is also the entrypoint for checking fairness of the field. (line 262)
- `processor/FacetProcessorVTDXML.java`: Line 97 ~ 120, not sure if this is the best place for calling code for combining value given by processors/post-processors. **TODO** need better code 
- `normalizer/CommunicationProtocolPostNormalizer.java`: Defines function for assessing fairness of the field. 

Files to be modified in `vlo-web-app` module, path: `vlo-web/src/main/resources/`
- `./fieldNames.properties`: *This file defines label to be shown on the web page for each associated field*
- **TODO** Actually add the facet to page on new tab using JSF. 

## Adding new facet to VLO
Ideally to add a new facet to VLO, you need to do the following steps:
1. Add the new field to `FieldKey.java` in `vlo-commons` module
2. Add the new field to `VloConfig.xml` in `vlo-commons` module
3. Add the new field to `VloConfig.xsd` in `vlo-commons` module
4. Add the new field to `fieldNames.properties` in `vlo-web-app` module
5. Add the new field to `Metadataimporter.java` in `vlo-importer` module
6. Around line line 99 in `FacetProcessorVTDXML.java` in `vlo-importer` module, add a new case for the new field

> **NOTE** **TODO**: Still investigating how field rewriting should be properly (systematically) done.
