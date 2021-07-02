import json
import urllib
from urllib import request


def send_request(url, payload, method):
    req = urllib.request.Request(url, method=method)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    payload = payload.encode('utf-8')
    with urllib.request.urlopen(req, payload) as response:
        return json.loads(response.read())


def create():
    url_create = "http://localhost:8085/createLuceneIndex"
    create_request = json.dumps({"inputPath": "resources/test/passages.tsv", "indexPath": "tempIndex/test"})
    create_response = send_request(url_create, create_request, "PUT")
    print(create_response)
    return create_response["indexId"]


def search(index_id):
    url_search = "http://localhost:8085/bm25Search"
    search_request = json.dumps({"indexId": index_id, "queryId": "1", "queryText": "Hajuron Jamiri", "topK": 5})
    search_resonse = send_request(url_search, search_request, "POST")
    print(search_resonse)


def delete(index_id):
    url_delete = " http://localhost:8085/deleteLuceneIndex"
    delete_response = send_request(url_delete, index_id, "DELETE")
    print(delete_response)


def main():
    index_id = create()
    search(index_id)
    delete(index_id)


if __name__ == "__main__":
    main()
