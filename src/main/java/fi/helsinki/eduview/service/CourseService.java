package fi.helsinki.eduview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: hpr
 * @date: 14/01/2018
 */
@Service
public class CourseService extends AbstractService {

    private boolean init = false;
    private ObjectMapper mapper = new ObjectMapper();
    private List<JsonNode> cus = new ArrayList<>();


    @PostConstruct
    private void init() throws IOException {
//        if(init) {
//            return;
//        }
//        ObjectMapper mapper = new ObjectMapper();
//        List<JsonNode> root = cus;
//        String courseUnitsFileName = env.getProperty("course-units", "course-units");
//        for(File file : new File(env.getProperty("data-location", "backup/")).listFiles()) {
//            if(!file.getName().contains(courseUnitsFileName)) {
//                continue;
//            }
//            ArrayNode fileTree = (ArrayNode)mapper.readTree(Files.readAllBytes(file.toPath()));
//            for(JsonNode child : fileTree) {
//                root.add(child);
//            }
//        }
        init = true;
    }

    private JsonNode openFile(String id) throws IOException {
        File file = new File(env.getProperty("data-location") + env.getProperty("course-units-dir") + "/" + id + ".json");
        if(file.isFile()) {
            return mapper.readTree(Files.readAllBytes(file.toPath()));
        }
        return mapper.createArrayNode();
    }

    public String getCUNamesByIds(List<String> idList, String lv) throws IOException {
        ArrayNode array = mapper.createArrayNode();
        for(String id : idList) {
            JsonNode root = openFile(id);
            if(root.isArray()) {
                array.addAll((ArrayNode)root);
            }
        }
        JsonNode filtered = filterResultsByLv(array, lv);
        array = mapper.createArrayNode();
        for(JsonNode cu : filtered) {
            array.add(cu);
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(array);
    }

//    public String getCUNamesByIds(List<String> idList, String lv) throws IOException {
//        ArrayNode array = mapper.createArrayNode();
//        for(JsonNode cu : cus) {
//            if(idList.contains(cu.get("groupId").asText())) {
//                array.add(cu);
//            }
//        }
//        JsonNode filtered = filterResultsByLv(array, lv);
//        array = mapper.createArrayNode();
//        for(JsonNode cu : filtered) {
//            array.add(cu.get("name"));
//        }
//        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(array);
//    }
}
