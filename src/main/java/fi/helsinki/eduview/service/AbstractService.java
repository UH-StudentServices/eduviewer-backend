package fi.helsinki.eduview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author: hpr
 * @date: 14/01/2018
 */
@Service
public abstract class AbstractService {

    @Autowired
    protected Environment env;
    protected ObjectMapper mapper = new ObjectMapper();

    private JsonNode filterResultByLv(JsonNode response, String lv) throws JsonProcessingException {
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
        ArrayNode filteredResults = mapper.createArrayNode();

        if(lv == null || lv.isEmpty()) {
            return filteredResults;
        }

        if(results.isObject()) {
            return filterResultByLv(results, lv);
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

    public String getLvNames() throws JsonProcessingException {
        ObjectNode lvs = mapper.createObjectNode();
        lvs.put("hy-lv-1", "1950-51");
        lvs.put("hy-lv-2", "1951-52");
        lvs.put("hy-lv-3", "1952-53");
        lvs.put("hy-lv-4", "1953-54");
        lvs.put("hy-lv-5", "1954-55");
        lvs.put("hy-lv-6", "1955-56");
        lvs.put("hy-lv-7", "1956-57");
        lvs.put("hy-lv-8", "1957-58");
        lvs.put("hy-lv-9", "1958-59");
        lvs.put("hy-lv-10",  "1959-60");
        lvs.put("hy-lv-11",  "1960-61");
        lvs.put("hy-lv-12",  "1961-62");
        lvs.put("hy-lv-13",  "1962-63");
        lvs.put("hy-lv-14",  "1963-64");
        lvs.put("hy-lv-15",  "1964-65");
        lvs.put("hy-lv-16",  "1965-66");
        lvs.put("hy-lv-17",  "1966-67");
        lvs.put("hy-lv-18",  "1967-68");
        lvs.put("hy-lv-19",  "1968-69");
        lvs.put("hy-lv-20",  "1969-70");
        lvs.put("hy-lv-21",  "1970-71");
        lvs.put("hy-lv-22",  "1971-72");
        lvs.put("hy-lv-23",  "1972-73");
        lvs.put("hy-lv-24",  "1973-74");
        lvs.put("hy-lv-25",  "1974-75");
        lvs.put("hy-lv-26",  "1975-76");
        lvs.put("hy-lv-27",  "1976-77");
        lvs.put("hy-lv-28",  "1977-78");
        lvs.put("hy-lv-29",  "1978-79");
        lvs.put("hy-lv-30",  "1979-80");
        lvs.put("hy-lv-31",  "1980-81");
        lvs.put("hy-lv-32",  "1981-82");
        lvs.put("hy-lv-33",  "1982-83");
        lvs.put("hy-lv-34",  "1983-84");
        lvs.put("hy-lv-35",  "1984-85");
        lvs.put("hy-lv-36",  "1985-86");
        lvs.put("hy-lv-37",  "1986-87");
        lvs.put("hy-lv-38",  "1987-88");
        lvs.put("hy-lv-39",  "1988-89");
        lvs.put("hy-lv-40",  "1989-90");
        lvs.put("hy-lv-41",  "1990-91");
        lvs.put("hy-lv-42",  "1991-92");
        lvs.put("hy-lv-43",  "1992-93");
        lvs.put("hy-lv-44",  "1993-94");
        lvs.put("hy-lv-45",  "1994-95");
        lvs.put("hy-lv-46",  "1995-96");
        lvs.put("hy-lv-47",  "1996-97");
        lvs.put("hy-lv-48",  "1997-98");
        lvs.put("hy-lv-49",  "1998-99");
        lvs.put("hy-lv-50",  "1999-00");
        lvs.put("hy-lv-51",  "2000-01");
        lvs.put("hy-lv-52",  "2001-02");
        lvs.put("hy-lv-53",  "2002-03");
        lvs.put("hy-lv-54",  "2003-04");
        lvs.put("hy-lv-55",  "2004-05");
        lvs.put("hy-lv-56",  "2005-06");
        lvs.put("hy-lv-57",  "2006-07");
        lvs.put("hy-lv-58",  "2007-08");
        lvs.put("hy-lv-59",  "2008-09");
        lvs.put("hy-lv-60",  "2009-10");
        lvs.put("hy-lv-61",  "2010-11");
        lvs.put("hy-lv-62",  "2011-12");
        lvs.put("hy-lv-63",  "2012-13");
        lvs.put("hy-lv-64",  "2013-14");
        lvs.put("hy-lv-65",  "2014-15");
        lvs.put("hy-lv-66",  "2015-16");
        lvs.put("hy-lv-67",  "2016-17");
        lvs.put("hy-lv-68",  "2017-18");
        lvs.put("hy-lv-69",  "2018-19");
        lvs.put("hy-lv-70",  "2019-20");
        lvs.put("hy-lv-71",  "2020-21");
        lvs.put("hy-lv-72",  "2021-22");
        lvs.put("hy-lv-73",  "2022-23");
        lvs.put("hy-lv-74",  "2023-24");
        lvs.put("hy-lv-75",  "2024-25");
        lvs.put("hy-lv-76",  "2025-26");
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lvs);
    }
}
