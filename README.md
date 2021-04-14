# Japan Search Facet Generator （Java）

ジャパンサーチの利活用スキーマから、ファセット検索用のデータを作成するプログラムです。（Java版）

## jsonldファイルからの構造化対象データの抽出

jsonldファイルへのパス（例. XXXX/michi-001.jsonld）を引数として、`CreateJson004.java` を実行してください。

`data/004_json_rdf` にcollectionのID（例. michi） 別のファイルが出力されます。

## RDFストアからの構造化データの情報取得

collectionのID（例. michi）を引数として、`DownloadRdf005.java` を実行してください。

`data/005_entity` に構造化データのURIのmd5値をファイル名とするファイルが出力されます。

## 構造化対象データをESのインデックス登録用に置換

collectionのID（例. michi）を引数として、`ModifyRdf006.java` を実行してください。

`data/006_es_rdf` に変換されたファイルが出力されます。
