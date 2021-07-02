# BM25 Lucene Service
This project provides a lightweight service that wrap Lucene java implementation, for use from non-java languages.<br/>
The API provides access via HTTP calls to Lucene strong indexing and BM25 search capabilities. Specially, for querying a large scale documents or passages collection.

## Table Of Contents

- [Requirements](https://github.com/AlonEirew/bm25-lucene-service#Requirements)
- [Build and Run](https://github.com/AlonEirew/bm25-lucene-service#Build-and-Run)
- [API](https://github.com/AlonEirew/bm25-lucene-service#API)
    - [Create](https://github.com/AlonEirew/bm25-lucene-service#Create)
    - [Search](https://github.com/AlonEirew/bm25-lucene-service#Search)
    - [Delete](https://github.com/AlonEirew/bm25-lucene-service#Delete)
- [Python Example](https://github.com/AlonEirew/bm25-lucene-service#Python-Example)

***

## Requirements
- Java 11

## Build and Run
### Building the service executable
This will create a portable independent jar file in `build/libs/bm25-lucene-service-1.0-SNAPSHOT.jar'
```
#>git clone https://github.com/AlonEirew/bm25-lucene-service.git
#>./gradlew clean build -x test
```

### Running the service
```
#>java -jar bm25-lucene-service-1.0-SNAPSHOT.jar
```

## API
### Create
HTTP *PUT* method to create Lucene index <br/>
URL - http://localhost:8085/createLuceneIndex
#### Create Request Example
Json:
```json
{
  "inputPath": "src/test/resources/passages.tsv",
  "indexPath": "tempIndex/test"
}
```
Where:
- `inputPath` - A tab separated value (tsv) file location in the format of:<br/> 
  `<Passage ID> <TAB> <Passage Text>`
- `indexPath` - The folder path to create the index in

#### Create Response Example
Json:
```json
{
  "added": 5,
  "indexId": "3e441154-aaf5-480f-854b-a1691569bad8",
  "message": "Index created successfully"
}
```
Where:
- `added` - Number of passages/documents added to index
- `indexId` - Created index id for search requests
- `message` - Log information in case of failure 

### Search 
HTTP *POST* method to search a Lucene index using BM25 method<br/>
URL - http://localhost:8085/bm25Search

#### Search Request Example
Json:
```json
{
  "indexId": "3e441154-aaf5-480f-854b-a1691569bad8",
  "queryId": "1",
  "queryText": "Hajuron Jamiri",
  "topK": 5
}
```
Where:
- `indexId` - The index id given when creating the index
- `queryId` - The query id for tracking
- `queryText` - Query text

#### Search Response Example
Json:
```json
{
  "queryId": "1",
  "rankedPassageIds": [
    "122791"
  ],
  "message": "Done successfully"
}
```
Where:
- `queryId` - The query id that yield this result
- `rankedPassageIds` - Retrieved passages/documents ids ordered by rank
- `message` - Log information in case of failure

### Delete 
HTTP *DELETE* method to delete a Lucene index <br/>
URL - http://localhost:8085/deleteLuceneIndex
#### Delete Request Example
String:<br/>
`3e441154-aaf5-480f-854b-a1691569bad8`

Where:
Index id to delete

#### Delete Response Example
Json:
```json
{
  "deleted": "true",
  "message": "Index deleted successfully"
}
```

Where:
- `deleted` - Boolean indicates of success or failure
- `message` - Log information in case of failure 

## Python Example
Simple python client code snippet at [example/py_code.py](https://github.com/AlonEirew/bm25-lucene-service/blob/master/example/py_code.py)
