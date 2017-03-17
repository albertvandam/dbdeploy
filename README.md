# Database Deployer

An utility to deploy a database between environments without needing to run any SQL scripts

## Why this utility?

My team started with a new project, heavily dependent on Stored Procedures and Triggers, which had to be deployed to a DB2 UDB database. This deployment had to be automated and we were not allowed to run SQL scripts in production. The deployment is handled by the IT Operations team who were used to deploying RPG programs, files and logicals using a tool.

The tool in use did not like the SQL based source files. After spending around three weeks with various configurations of the tool, we were still unable to deploy the objects from the development environment to a test environment. In some instances triggers were deployed prior to tables, in others tables in incorrect order resulting in foreign key constraints, in others the deployment was successful but failed on first use. By now our project was delayed by three weeks, deadlines loomed and business was extremely unhappy with a lack of progress.

We investigated tools from [Redgate](http://www.red-gate.com/) and [DBmaestro](http://www.dbmaestro.com/) but at the time (and possibly still) they did not support DB2 UDB.

Left with the possibility of having to jump through hoops to convince risk, compliance and audit that there are no alternative but to run SQL scripts in production, I spent a weekend and created an utility which allowed us to achieve automated deployment of the database, as part of our continuous integration processes.

This utility is very much a work in progress but it gets the job done.

## Objective

The utility sets out to perform the following on a DB2 UDB database:

* Import existing database structure, stored procedures and triggers from a DB2 UDB or MySQL database
* Compare existing database structure, stored procedures, triggers and data with a version controlled source
* Apply incremental changes on a target database

## Safety

* All changes on the DB2 UDB database is performed in a transaction. If any of the changes fail, all changes are rolled back to leave the database in the state prior attempting to apply changes.

## Known issues

* When creating a new record in the database, and a NULL value is required in one of the columns, the value is incorrectly populated. The current workaround is to not use NULL values.
* The comparison finds differences between source configuration and target database even if there are no differences. This is not consistent. This does not introduce issues on the target database, but does increase the execution time as unnecessary changes are applied.
* Applying changes to a MySQL database has not been tested
* All objects (tables, stored procedures, triggers, indexes, etc) which do not exist in the source configuration will be removed from the target database. There is no option to retain objects in the target database.
* All records which do not exist in the source configuration will result in a value of D being set in a STATUS column in that table.
* Originally there was an idea to allow the utility to test the changes prior to commit, some code was implemented for this but it was never tested or used.
* All object names are case sensitive

## Usage

The build will created a JAR containing all dependencies.

```shell
# java -jar dbdeploy.jar <option>
```

Valid options:

| Option | Description |
|--------|-------------|
| help | Show help |
| help databaseformat | Generate an XML definition (XSD) for the database configuration |
| init | Initialise configuration |
| import | Import database from source |
| importdata &lt;tablename&gt; | Import static data from &lt;tablename&gt; in source |
| compare &lt;target&gt; | Show changes to be applied on &lt;target&gt; when compared to source |
| apply &lt;target&gt; |  Apply changes on &lt;target&gt; when compared to source |
| importtxt &lt;table&gt; &lt;filename&gt; | Import static data for &lt;table&gt; from &lt;filename&gt; in tab seperated format |

### Getting started

The _sample_ folder contains a sample database configuration.

Initialise the configuration:

```shell
# java -jar dbdeploy.jar init
```

This will asks some questions:

* Where to find a source database to import
* Targets to deploy to

A default configuration will be created in _conf/config.xml_.

You can now import an existing database:

```shell
# java -jar dbdeploy.jar import
```

This will create the configuration files as follows:

| File | Description |
|------|-------------|
| conf/SOURCE.xml | Source database configuration |
| sql_source/stored_procedures/*.sql | Stored procedure source files |
| sql_source/triggers/*.sql | Trigger source files |

If you have existing data, you can import the data from the database as well:

```shell
# java -jar dbdeploy.jar importdata <tablename>
```