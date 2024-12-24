# DB compare
A tool to compare databases

## Usage
Compares the schemas of two databases and generates SQL scripts for syncing to the target database

```shell
java -jar dbcompare.jar \
-t schema \
-ss db1 \
-ts db2 \
-u "user:password@jdbc:mysql://localhost:3306/db1 \
,user:password@jdbc:mysql://localhost:3306/db2" \
-o compare.sql
```

Execute scripts on multiple databases

```shell
java -jar dbcompare.jar \
-t exec \
-i compare.sql \
-u "user:password@jdbc:mysql://test1:3306/db1 \
,user:password@jdbc:mysql://test2:3306/db2 \
,user:password@jdbc:mysql://test3:3306/db3"
```


## Options
```shell
usage: java -jar dbcompare.jar
 -h,--help                   print usage
 -i,--input <arg>            input file path or sql
 -o,--output <arg>           output file path: default console
 -ss,--schema <arg>          compare source schema
 -st,--tables <arg>          compare source tables, such as tb1,tb2
 -t,--type <arg>             operate type: schema|data|exec, schema
                             compare|data compare|execute sql
 -ts,--target-schema <arg>   compare target schema
 -u,--urls <arg>             jdbc urls like:
                             user:password@jdbc:mysql://localhost:3306,...
```