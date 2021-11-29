package router.usage.statistics.java.servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import router.usage.statistics.java.model.Model;
import router.usage.statistics.java.model.ModelResponse;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.time.LocalDate.now;
import static java.time.ZoneId.of;
import static java.util.Collections.singletonList;
import static router.usage.statistics.java.service.Service.*;
import static router.usage.statistics.java.servlet.HtmlDisplay.getDisplay;
import static router.usage.statistics.java.util.Util.TIME_ZONE;
import static router.usage.statistics.java.util.Util.getSystemEnvProperty;

@Slf4j
public class Servlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        boolean toJson = Boolean.parseBoolean(request.getParameter("toJson"));
        String selected = request.getParameter("selected");
        boolean isCheckOnly = Boolean.parseBoolean(request.getParameter("isCheck"));

        log.info("In Servlet:: toJson: {} | selected: {} | isCheckOnly: {}", toJson, selected, isCheckOnly);

        if (isCheckOnly) {
            response.setContentType("text/html");
            response.getWriter().print(returnDataCheck());
        } else {
            if (selected == null || selected.isEmpty()) {
                String timeZone = getSystemEnvProperty(TIME_ZONE);
                int monthValue = now(of(timeZone)).getMonthValue();
                if (monthValue < 10) {
                    selected = now(of(timeZone)).getYear() + "-0" + now().getMonthValue();
                } else {
                    selected = now(of(timeZone)).getYear() + "-" + now().getMonthValue();
                }
            }

            String[] selectedYearMonth = selected.split("-");
            List<String> selectedYear = singletonList(selectedYearMonth[0]);
            List<String> selectedMonth = singletonList(selectedYearMonth[1]);

            Set<String> yearMonthSet = retrieveUniqueDatesOnly();
            List<Model> modelList = retrieveDataUsages(selectedYear, selectedMonth);
            Model modelTotal = calculateTotalDataUsage(modelList);

            if (toJson) {
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.setContentType("application/json");
                ModelResponse modelResponse = getModelResponse(yearMonthSet, modelList, modelTotal);
                String jsonToDisplay = new Gson().toJson(modelResponse);
                response.getWriter().print(jsonToDisplay);
            } else {
                response.setContentType("text/html");
                String htmlToDisplay = getDisplay(modelList, modelTotal, selected, yearMonthSet);
                response.getWriter().print(htmlToDisplay);
            }
        }
    }
}
