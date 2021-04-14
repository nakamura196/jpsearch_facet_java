

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class DownloadRdf005 {

	public static void main(String[] args) throws FileNotFoundException, JsonProcessingException {

		String INPUT_DIR = "data/004_json_rdf";
		String OUTPUT_DIR = "data/005_entity";

		String collectionId = args[0];

		File directory = new File(OUTPUT_DIR);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }

	    String service = "https://jpsearch.go.jp/rdf/sparql";

		// TODO 自動生成されたメソッド・スタブ
		File dir = new File(INPUT_DIR + "/"+collectionId);
		File[] fileList = dir.listFiles();

		ObjectMapper mapper = new ObjectMapper();

		ArrayNode uris = mapper.createObjectNode().arrayNode();

		for(int i = 0; i < fileList.length; i++) {
			// Java object to JSON file
		    try {
				JsonNode data = mapper.readTree(fileList[i]);

				Iterator<String> fieldNames = data.fieldNames();
		        while (fieldNames.hasNext()) {
		            String fieldName = fieldNames.next();
		            JsonNode node2 = data.get(fieldName);

		            for(JsonNode node : node2) {
		            	uris.add(node);
		            }
		        }
			} catch (JsonGenerationException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			} catch (JsonMappingException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}

		}

	    for(JsonNode node : uris){

	    	String uri = node.asText();

	    	System.out.println(uri);

	    	//ファイル名用にmd5値を取得
	    	String hash = DigestUtils.md5Hex(uri);

	    	String path = OUTPUT_DIR + "/"+hash+".json";

	    	File file = new File(path);

	    	if (!file.exists()) {

		    	// SPARQLクエリ
		        String queryString = " PREFIX jps: <https://jpsearch.go.jp/term/property#> " +
		        " PREFIX schema: <http://schema.org/> " +
		        " select * where { " +
		        " <" + uri + "> ?p ?o " +
		        " } ";

		        Query query = QueryFactory.create(queryString);
		        QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		        ResultSet results = qe.execSelect();

		        Map<String, List<Map<String, String>>> resultMap = new HashMap<String, List<Map<String, String>>>();

		        while (results.hasNext()) { // returns false
		            QuerySolution querySolution = results.next();
		            String p = querySolution.getResource("p").getURI();

		            RDFNode oNode = querySolution.get("o");
		            if(!oNode.isLiteral()) {
		            	continue;
		            }
		            String lang = oNode.asLiteral().getLanguage();
		            String o = oNode.asLiteral().getString();

		            if(!resultMap.containsKey(p)) {
		            	resultMap.put(p, new ArrayList<Map<String, String>>());
		            }

		            Map<String, String> v = new HashMap<String, String>();
		            v.put("value", o);

		            // 言語指定がある場合
		            if(lang != "") {
		            	v.put("lang", lang);
		            }

		            resultMap.get(p).add(v);
		        }

		        String label = "";

		        // RDFS.label が存在する場合は、1つ目を採用
	            if(resultMap.containsKey("http://www.w3.org/2000/01/rdf-schema#label")) {
	            	label = resultMap.get("http://www.w3.org/2000/01/rdf-schema#label").get(0).get("value");
	            }

	            String label_en = "";

	            // schema.name が存在し、かつlang=enが存在する場合
	            if(resultMap.containsKey("http://schema.org/name")) {
	            	List<Map<String, String>> values = resultMap.get("http://schema.org/name");
	            	for(Map<String, String> v : values) {
	            		if(v.containsKey("lang") && v.get("lang").equals("en")) {
	            			label_en = v.get("value");
	            		}
	            	}
	            }

	            // 英語ラベルが与えられなかった場合、日本語ラベルを英語ラベルとする
	            if(label_en.equals("")) {
	            	label_en = label;
	            }

	            Map<String, String> entity = new HashMap<String, String>();
	    	    entity.put("uri", uri);
	    	    entity.put("ja", label);
	    	    entity.put("en", label_en);

		        ObjectMapper mapper2 = new ObjectMapper();
		        String prettyJson = mapper2.writerWithDefaultPrettyPrinter().writeValueAsString(entity);

		        PrintWriter out = new PrintWriter(path);
		        out.println(prettyJson);
		        out.close();
	    	}

		}
	}

}
