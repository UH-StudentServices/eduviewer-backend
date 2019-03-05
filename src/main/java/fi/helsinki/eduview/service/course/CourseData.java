package fi.helsinki.eduview.service.course;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author: hpr
 * @date: 05/03/2019
 */
public interface CourseData {
    JsonNode getCourseUnitByGroupId(String id, String lv) throws Exception;
}
