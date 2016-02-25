# Data Services Technical Overview

TODO:  KC/Gretchen to include section describing use cases, what it does and why.

## Table of Contents
0. [How it Works](#how-it-works)
0. [ETL Designer Guide](#etl-designer-guide)
0. [User Guide](#user-guide)
0. [Limitations](#limitations)
0. [Troubleshooting](#troubleshooting)

## How it Works

Data Services is implemented as a set of OSGi plugins and is included in all core Pentaho products. It allows data to be processed in Pentaho Data Integration and used in another tool in the form of a virtual database table.

Any Transformation can be used as a virtual table. Here's out it works:
  0. In Spoon, an ETL designer [creates a Data Service](https://help.pentaho.com/Documentation/6.0/0L0/0Y0/090/020#Create_a_Pentaho_Data_Service) on the step which will generate the virtual table's rows.
  0. Meta data is saved to the transformation describing the the virtual table's name and any optimizations applied. Optimizations are optional should never affect the Data Service's output. They serve only to speed up processing.
  0. On saving the transformation to a repository, a table in the repository's MetaStore maps the virtual table to the transformation in which it is defined. A Carte or DI Server must be connected to this repository and running for any Data Services to be accessible.
  0. A user with a JDBC client [connects to the server](https://help.pentaho.com/Documentation/6.0/0L0/0Y0/090/040). The client can list tables, view table structure, and submit SQL SELECT queries.
  0. When the server receives a SQL query, the table name is resolved and the user-defined **Service Transformation** is loaded from the repository. The SQL is parsed and a second **Generated Transformation** is created, containing all of the query operations (grouping, sorting, filtering).  
  0. Optimizations are applied to the Service Transformation in a best-effort fashion, depending on the user's query and constraints of the optimization type. These optimizations are intended to reduce the number of rows processed during query execution.
  0. Both transformations execute. Output from the service transformation is injected into the generated transformation, and output of the generated transformation is returned to the user as a Result Set.

A vast majority of the work here is done by the [pdi-dataservice-server-plugin](https://github.com/pentaho/pdi-dataservice-server-plugin). It is located in Karaf assembly in data-integration and data-integration-server.

The [pdi-dataservice-client-plugin](https://github.com/pentaho/pdi-dataservice-plugin/tree/master/pdi-dataservice-client/) contains the code for connecting to a server, issuing queries, and receiving result sets. It also contains the [DatabaseMetaPlugin](https://github.com/pentaho/pdi-dataservice-plugin/blob/master/pdi-dataservice-client/src/main/java/org/pentaho/di/trans/dataservice/client/DataServiceClientPlugin.java) which adds 'Pentaho Data Services' as a supported database connection type. It is included in the karaf assemblies of all supported Pentaho products and can be added to the classpath (along with a few dependencies) of other tools using JDBC connections.

## ETL Designer Guide
  - Transformation Design
  - Optimizations
    * Caching
     - *TEST*
       * Result set limitations
       * Cache capacity
    * Parameter Generation
    * Parameter Pushdown
  - Testing
    * Logging
  - Hosting
    * DI Server
    * Carte
    * Local

## User Guide
  - Queries
  - PDI
  - Analyzer <-- top use case
    * Modeling (workbench, dsw, SDR)
      * *TEST*
        - Properties
        - parent/child
        - AggTables
        - Big Filters
        - Compound slicers
        - count distinct
    * Shared dimensions
     
### Reporting

#### PRD

For the most part PRD reports will work well with Data Services. Parameterized reports in particular can benefit from the optimization features of Data Services:

**Query Pushdown.**  If the underlying datasources in a Data Service include Table Input or MongoDB Input, including Query Pushdown optimizations will allow the parameters selected within the report to be included in the queries to the source data, which can greatly limit the amount of processing the service needs to perform.  Query Pushdown can handle relatively complex WHERE clauses, and can work well with both Single-Select style parameters as well as Multi-Select.

**Parameter Pushdown.**  For other input sources (e.g. a REST input), Parameter Pushdown can be leveraged to make parameterized PRD reports more efficient.  Single values selected within one or more report parameters can be pushed down and used to limit the underlying data source.  Note that this won’t work with Multi-Select parameters, since Parameter Pushdown does not support IN lists.

There are two current limitations to be aware of when creating PRD reports.

1.  When defining your SQL query, the SQL Query Designer is able to load and display the virtual tables and fields available from a data service.  The filenames set in the editor, however, use the full path (e.g. “Kettle”.”VirtualTable”.’VirtualField”).  Data Services SQL does not support prefixing the “Kettle” schema name within column references (PRD-5560).  The workaround is to manually edit the query to remove the “Kettle”.

2.  Including parameters in your query can cause design-time issues, since PRD will place NULL values in parameters when doing things like preview or listing the available columns in the Query tree in the Data Sets pane ([PRD-5662](http://jira.pentaho.com/browse/PRD-5662)).  The workaround for this is to not use parameters while doing report design, swapping them in at the point you’re ready to test the report locally or publish.  This design time limitation should not impact successful execution of reports with parameters.

The one place the above limitation can have run-time impact is if the PRD parameter is set to "Validate Values" and can have a NULL (or N/A) value.  PRD will attempt to validate by passing a NULL in the JDBC call, which hits the same error as [PRD-5662](http://jira.pentaho.com/browse/PRD-5662) describes.  This error doesn't actually prevent running the report, but does display a validation error below the prompt.

Another potential “gotcha” with PRD/Data Services report construction is that the datatypes from the transformation in the data service may not translate to what you expect in the virtual table.  For example, an Integer field in a transformation will be widened to a Long in the resultset.  Make sure to check the datatype as displayed in the Data Set tree when defining parameter datatypes.

Also, virtual table names in SQL are case sensitive, so make sure to match the casing from the defined service.

#### PIR
    * *TEST*
### External tools

## Limitations
  - Multi tenancy
  - Performance

## Troubleshooting
  - Local/Remote Files
