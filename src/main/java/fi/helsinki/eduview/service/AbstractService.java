package fi.helsinki.eduview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;

/**
 * @author: hpr
 * @date: 14/01/2018
 */
@Service
public abstract class AbstractService {

    protected ObjectMapper mapper = new ObjectMapper();

    private JsonNode filterResultByLv(JsonNode response) throws JsonProcessingException {
        String lv = getLv();
        if(!response.has("curriculumPeriodIds")) {
            return response;
        }
        for(JsonNode lvNode : response.get("curriculumPeriodIds")) {
            if(lvNode.asText().equals(lv)) {
                return response;
            }
        }
        return mapper.createObjectNode();
    }

    protected JsonNode filterResultsByLv(JsonNode results, String lv) throws JsonProcessingException {
        if(results.isObject()) {
            return filterResultByLv(results);
        }
        ArrayNode filteredResults = mapper.createArrayNode();

        if(lv == null || lv.isEmpty()) {
            return results;
        }

        for(JsonNode node : results) {
            if(!node.has("curriculumPeriodIds")) {
                filteredResults.add(node);
                continue;
            }
            if(node.get("curriculumPeriodIds").size() == 0) {
                filteredResults.add(node);
            }
            for(JsonNode lvNode : node.get("curriculumPeriodIds")) {
                if(lvNode.asText().equals(lv)) {
                    filteredResults.add(node);
                    break;
                }
            }
        }
        return filteredResults;
    }

    protected String filterResultsByLvAndPrint(JsonNode results, String lv) throws IOException {
        JsonNode result = filterResultsByLv(results, lv);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }

    private String getLv() {
        return (String) RequestContextHolder.getRequestAttributes().getAttribute("lv", RequestAttributes.SCOPE_SESSION);
    }
}
