package fi.helsinki.eduview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
        if(init) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> root = cus;
        for(File file : new File("test/").listFiles()) {
            if(!file.getName().contains("course-units")) {
                continue;
            }
            ArrayNode fileTree = (ArrayNode)mapper.readTree(Files.readAllBytes(file.toPath()));
            for(JsonNode child : fileTree) {
                root.add(child);
            }
        }
        init = true;
    }

    public String getCUNamesByIds(List<String> idList, String lv) throws IOException {
        ArrayNode array = mapper.createArrayNode();
        for(JsonNode cu : cus) {
            if(idList.contains(cu.get("groupId").asText())) {
                array.add(cu);
            }
        }
        JsonNode filtered = filterResultsByLv(array, lv);
        array = mapper.createArrayNode();
        for(JsonNode cu : filtered) {
            array.add(cu.get("name"));
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(array);
    }
}
