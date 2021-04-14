

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModifyRdf006 {

	static String INPUT_DIR = "data/004_json_rdf";
	static String ENTITY_DIR = "data/005_entity";
	static String OUTPUT_DIR = "data/006_es_rdf";

	public static void main(String[] args) {



		// TODO 自動生成されたメソッド・スタブ
		File dir = new File(INPUT_DIR + "/"+args[0]);
		File[] fileList = dir.listFiles();

		// 対象データセットのURIの一覧を取得
		ObjectMapper mapper = new ObjectMapper();
		List<String> uris = new ArrayList<String>();

		for(int i = 0; i < fileList.length; i++) {
			// Java object to JSON file
		    try {
				JsonNode data = mapper.readTree(fileList[i]);

				Iterator<String> fieldNames = data.fieldNames();
		        while (fieldNames.hasNext()) {
		            String fieldName = fieldNames.next();
		            JsonNode node2 = data.get(fieldName);

		            for(JsonNode node : node2) {
		            	String uri = node.asText();
		            	if(uris.indexOf(uri) == -1) {
		            		uris.add(uri);
		            	}

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

		// Entityの一覧を取得
		Map<String, Map<String, String>> entities = new HashMap<String, Map<String, String>>();

		for(int i = 0; i < uris.size(); i++) {
			String uri = uris.get(i);
			String hash = DigestUtils.md5Hex(uri);

			String path = ENTITY_DIR+"/"+hash+".json";

	    	File file = new File(path);
	    	JsonNode data;
			try {
				data = mapper.readTree(file);
				Map<String, String> entity = new HashMap<String, String>();
		    	Iterator<String> fieldNames = data.fieldNames();
		        while (fieldNames.hasNext()) {
		            String fieldName = fieldNames.next();
		            JsonNode value = data.get(fieldName);
		            entity.put(fieldName, value.asText());
		        }

		        entities.put(uri, entity);
			} catch (JsonProcessingException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}


		}

		//各ファイルに対して、変換処理を加える
		for(int i = 0; i < fileList.length; i++) {
			exec(fileList[i], entities);
		}
	}

	public static void exec(File targetFile, Map<String, Map<String, String>> entities) {
		ObjectMapper mapper = new ObjectMapper();

		String name = targetFile.getName();
		String[] bits = name.split("/");
		String id = bits[bits.length - 1].split("\\.")[0];
		String dir = id.split("-")[0];
		System.out.println(id);

		File directory = new File(OUTPUT_DIR + "/"+dir);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }

		String path = OUTPUT_DIR + "/"+dir+"/" + id+".json";

		HashMap<String, List<String>> newItem = new HashMap<String, List<String>>();

		try {
			JsonNode currentItenJsonNode = mapper.readTree(targetFile);

			Iterator<String> fieldNames = currentItenJsonNode.fieldNames();

			while (fieldNames.hasNext()) {
	            String fieldName = fieldNames.next();
	            JsonNode valueJsonNode = currentItenJsonNode.get(fieldName);

	            for(JsonNode value : valueJsonNode) {
	            	String uri = value.asText();

	            	if(entities.containsKey(uri)) {

	            		if(!newItem.containsKey(fieldName+"_ja")) {
	            			newItem.put(fieldName+"_ja", new ArrayList<String>());
	            			newItem.put(fieldName+"_en", new ArrayList<String>());
	            			newItem.put(fieldName+"_uri", new ArrayList<String>());
	            		}

	            		Map<String, String> entity = entities.get(uri);
		            	for(String key : entity.keySet()) {
		            		newItem.get(fieldName+"_"+key).add(entity.get(key));
		            	}
	            	}else {
	            		System.err.println(uri);
	            	}

        		}

			}

			ObjectMapper mapper2 = new ObjectMapper();
	        String prettyJson = mapper2.writerWithDefaultPrettyPrinter().writeValueAsString(newItem);

	        PrintWriter out = new PrintWriter(path);
	        out.println(prettyJson);
	        out.close();

		} catch (JsonProcessingException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}

