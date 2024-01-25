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

## How to compile modules and whole project
### Build individual modules
- `mvn clean package` in `vlo-commons` module, with `-DskipTests=True` if you want to skip tests. 

### Building whole project
The following line will build a war file of VLO and it is **not** depending on your local `java` and `maven` version.

- `CLEAN_CACHE=true ./build.sh -DskipTests=true -Pdocker` in root folder of `VLO`. 

### Building docker image
The following line will build a docker image. With the latest version of the `VLO` project which just being built locally.
- Run `./build.sh -b -l` in [`docker-vlo-beta`](https://gitlab.com/CLARIN-ERIC/docker-vlo-beta) folder.

Some of the shell scripts have to be adapted to your local environment.
- In `copy_data.sh`, source `copy_data_env.dev.sh` instead of `copy_data_env.sh`
  - Variable `SRC_FILE` at around line 22 might need modification
- In `copy_data_dev.env.sh`, 
  - variable `VLO_VERSION` should be correspondent to your compiled war file
  - variable `REMOTE_RELEASE_URL` should be the local location of your war file, i.e, `file:///home/username/git/VLO/vlo-web-app/target/vlo-web-app-4.7.1-SNAPSHOT.war`

After running the script, you should have a docker image named like `vlo-${VLO_VERSION}-docker`, i.e, `vlo-4.7.1-1-docker`.

### Running VLO
In folder `clariah-vlo`, where all the docker compose files are located, run `docker-compose up -d`. Optionally you can add -f <location of docker compose file> to specify which docker compose file to use.  
Also make sure that the `docker-compose.yaml` uses the correct Docker image (#LN41). Adjust if needed.

### Run importer and see the VLO portal
- First start the apps by starting them from `datasets-vlo`:   
```$ ./control.sh -s -v start```   
The (empty) VLO should be visible at: `http://localhost:38081`.
Wicket might throw some internal error here, because the 'custom' facet field is not yet available to the app. This can be solved by running the importer in tghe next step.   
- Now you can import the (CMDI) records:   
```$ ./control.sh -s -v run-import```   
- To stop the service:   
```$ ./control.sh -s stop```   

When starting the docker image, some file-mounts are also created.  
F.i. some (wicket) properties files are mounted into the war.  
Example: in `/datasets-vlo/clarin/clariah.yml`
`./clariah/pages/FacetedSearchPage.properties:/opt/vlo/war/vlo/WEB-INF/classes/eu/clarin/cmdi/vlo/wicket/pages/FacetedSearchPage.properties`
