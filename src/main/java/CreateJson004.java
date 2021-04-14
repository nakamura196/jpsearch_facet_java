

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreateJson004 {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ

		String OUTPUT_DIR = "data/004_json_rdf";

		String inputPath = args[0];

		ObjectMapper mapper = new ObjectMapper();

		ObjectNode properties = mapper.createObjectNode();
		properties.put("schema:creator", "agential");
		properties.put("schema:publisher", "agential");
		properties.put("schema:contirbutor", "agential");
		properties.put("schema:temporal", "temporal");
		properties.put("schema:spatial", "spatial");
		properties.put("schema:inLanguage", "inLanguage");
		properties.put("schema:about", "about");
		properties.put("schema:relatedLink", "relatedLink");

		try {
			JsonNode node = mapper.readTree(new File(inputPath));

			JsonNode graphs = node.get("@graph");

			// 各グラフ（アイテム）に対して
			for (JsonNode graph : graphs) {

				ObjectNode item = mapper.createObjectNode();

				Iterator<String> fieldNames = graph.fieldNames();
		        while (fieldNames.hasNext()) {
		            String fieldName = fieldNames.next();
		            JsonNode metadataValues = graph.get(fieldName);

		            if(properties.has(fieldName)) { //先にリストアップしたプロパティの場合

		            	String propertyLabel = properties.get(fieldName).asText();

		            	ArrayNode valueArray = mapper.createObjectNode().arrayNode();

		            	item.put(propertyLabel, valueArray);

		            	if(metadataValues.isArray()) { //配列の場合
		            		for(JsonNode n : metadataValues) {
		            			valueArray.add(replacePrefix2Uri(n.asText()));
		            		}
		            	} else {
		            		valueArray.add(replacePrefix2Uri(metadataValues.asText()));
		            	}
		            } else if(fieldName.equals("@type")) { //タイプならば
		            	ArrayNode valueArray = mapper.createObjectNode().arrayNode();
		            	valueArray.add(replacePrefix2Uri(graph.get("@type").asText()));
		            	item.put("type", valueArray);

		            } else if(fieldName.equals("jps:sourceInfo")) { //ソースならば
		            	ArrayNode valueArray = mapper.createObjectNode().arrayNode();
		            	valueArray.add(replacePrefix2Uri(metadataValues.get("schema:provider").asText()));
		            	item.put("source", valueArray);
		            } else if(fieldName.equals("jps:accessInfo")) { //アクセスならば
		            	ArrayNode valueArray = mapper.createObjectNode().arrayNode();
		            	valueArray.add(replacePrefix2Uri(metadataValues.get("schema:provider").asText()));
		            	item.put("access", valueArray);
		            }

		        }

		        // JPSのアイテムではない場合
		        if(!item.has("access")) {
		        	return;
		        }

		        // 出力

		        String uri = graph.get("@id").asText();

		        String[] bits = uri.split("/");
				String id = bits[bits.length - 1];
				String dir = id.split("-")[0];

		        File directory = new File(OUTPUT_DIR + "/"+dir);
			    if (! directory.exists()){
			        directory.mkdirs();
			    }

			    String path = OUTPUT_DIR + "/"+dir+"/"+id+".json";

		        ObjectMapper mapper2 = new ObjectMapper();
		        String prettyJson = mapper2.writerWithDefaultPrettyPrinter().writeValueAsString(item);

		        PrintWriter out = new PrintWriter(path);
		        out.println(prettyJson);
		        out.close();
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static String replacePrefix2Uri(String v){
		v = v.replace("chname:", "https://jpsearch.go.jp/entity/chname/");
		v = v.replace("ncname:", "https://jpsearch.go.jp/entity/ncname/");
		v = v.replace("iso639-2:", "http://id.loc.gov/vocabulary/iso639-2/");

		v = v.replace("keyword:", "https://jpsearch.go.jp/term/keyword/");
		v = v.replace("type:", "https://jpsearch.go.jp/term/type/");
		v = v.replace("place:", "https://jpsearch.go.jp/entity/place/");

		return v;
	}

}
