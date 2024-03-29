## NEW REPOSITORY
https://framagit.org/mcib3d/tapas-core

# tapas-core
The tapas-core library is the core for the core functionnalities and plugins of TAPAS, a system for automation for processing and analysis with ImageJ and OMERO. It can also work with local files if Omero is not isntalled.

Please follow these instructions to download and compile the source code : 

`git clone https://github.com/mcib3d/tapas-core.git`

`mvn package`

Altetnatively you should be able to open the project in Netbeans or Eclipse. 

If you want to include this library as part as your maven project, use this dependency : 

`<dependency>`

		<groupId>com.github.mcib3d</groupId>
		
		<artifactId>tapas-core</artifactId>
		
		<version>0.6.3</version>
		
`</dependency>`

and 

`<repository>`
			
	<id>jitpack.io</id>
	
	<url>https://jitpack.io</url>
	
`</repository>`

  
  If you use TAPAS in your experiments, please cite : 
  
  J. Ollion, J. Cochennec, F. Loll, C. Escudé, and T. Boudier (2013). 
  TANGO: a generic tool for high-throughput 3D image analysis for studying nuclear organization.
  Bioinformatics 29(14):1840-1. doi: 10.1093/bioinformatics/btt276.
  
  and 
  
  Whitehead et al.(2018).
  Towards an Automated Processing and Analysis System for multi-dimensional light-sheet microscopy big data using ImageJ and OMERO.
  IMC'19, Sydney, Australia.
  
  Documentations are available on the Imagej wiki website : 
  http://imagejdocu.list.lu/doku.php?id=plugin:utilities:tapas_:integrated_framework_for_automated_processing_and_analysis:start
