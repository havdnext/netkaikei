package kaikei.jasperreport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JsonDataSource;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * Java object to convert posted json string
 * @author HungVN1234567vuha
 * @since 2015/02/11
 */
public class CommonReportData {

	@SuppressWarnings("rawtypes")
	private Map parameters;

	@SuppressWarnings("rawtypes")
	public Map getParameters() {
		return parameters;
	}

	@SuppressWarnings("rawtypes")
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	private String jsonDataSource;

	public String getJsonDataSource() {
		return jsonDataSource;
	}

	public void setJsonDataSource(String jsonDataSource) {
		this.jsonDataSource = jsonDataSource;
	}

	@Override
	public String toString() {
		return "CommonReportData [parameters=" + parameters
				+ ", jsonDataSource=" + jsonDataSource + "]";
	}

	/**
	 * Convert json to Map of CommonRepportData
	 * @param strJson
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static LinkedHashMap<String, CommonReportData> getReportDataFromJson(String strJson, Map options) throws JsonParseException, JsonMappingException, IOException {
		LinkedHashMap<String, CommonReportData> commonReportDataList = new LinkedHashMap<String, CommonReportData>();
		Map<String, String> allReportDataMap = new HashMap<String, String>();
		ObjectMapper mapper = new ObjectMapper();
		allReportDataMap = mapper.readValue(strJson, new TypeReference<HashMap<String, String>>() {});
		if (!allReportDataMap.isEmpty()) {
			ArrayList<String> reportIdsList = null;
			LinkedHashMap<String, Map<String, Object>> parametersMap = null;
			LinkedHashMap<String, List<HashMap<String, String>>> dataMap = null;
			Map<String, List<Map<String, String>>> subDataMap = null;
			Map<String, Object> optionsMap = null;
			if (allReportDataMap.containsKey("options") && allReportDataMap.get("options") != null && !"".equals(allReportDataMap.get("options"))) {
				optionsMap = mapper.readValue(allReportDataMap.get("options"), new TypeReference<HashMap<String, Object>>() {});
				options.putAll(optionsMap);
				reportIdsList = (ArrayList)optionsMap.get("includes");
			}
			if (allReportDataMap.containsKey("parameters") && allReportDataMap.get("parameters") != null && !"".equals(allReportDataMap.get("parameters"))) {
				parametersMap = mapper.readValue(allReportDataMap.get("parameters"), new TypeReference<LinkedHashMap<String, HashMap<String, String>>>() {});
			}
			if (allReportDataMap.containsKey("data") && allReportDataMap.get("data") != null && !"".equals(allReportDataMap.get("data"))) {
				dataMap = mapper.readValue(allReportDataMap.get("data"), new TypeReference<LinkedHashMap<String, List<HashMap<String, String>>>>() {});
			}

			if (reportIdsList == null) {
				if (!parametersMap.isEmpty()) {
					reportIdsList = new ArrayList<String>(parametersMap.keySet());
				} else if (!dataMap.isEmpty()) {
					reportIdsList = new ArrayList<String>(dataMap.keySet());
				}
			}
			if (allReportDataMap.containsKey("subdata") && allReportDataMap.get("subdata") != null && !"".equals(allReportDataMap.get("subdata"))) {
				subDataMap = mapper.readValue(allReportDataMap.get("subdata"), new TypeReference<HashMap<String, Map<String, List<HashMap<String, Object>>>>>() {});
				if (parametersMap == null) {
					parametersMap = new LinkedHashMap<String, Map<String,Object>>();
				}
				InputStream jsonInputStream;
				Iterator it = subDataMap.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					Map currentParams = new HashMap<String, Object>();
					if (parametersMap.containsKey(entry.getKey().toString())) {
						currentParams.putAll(parametersMap.get(entry.getKey().toString()));
					}

					Iterator itCurrentReportSubdata = ((Map)entry.getValue()).entrySet().iterator();
					while (itCurrentReportSubdata.hasNext()) {
						Map.Entry entryCurrentSubdata = (Map.Entry) itCurrentReportSubdata.next();
						jsonInputStream = new ByteArrayInputStream(mapper.writeValueAsBytes(entryCurrentSubdata.getValue()));
						try {
							currentParams.put(entryCurrentSubdata.getKey().toString(), new JsonDataSource(jsonInputStream));
						} catch (JRException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						jsonInputStream.close();
					}
					parametersMap.put(entry.getKey().toString(), currentParams);
				}
			}
			if (reportIdsList != null && reportIdsList.size() > 0) {
				commonReportDataList = new LinkedHashMap<String, CommonReportData>();
				for(String reportIds : reportIdsList) {
					CommonReportData commonReportData = new CommonReportData();
					if (parametersMap != null) {
						commonReportData.setParameters(parametersMap.get(reportIds));
					}
					if (dataMap != null) {
						if (dataMap.containsKey(reportIds)) {
							commonReportData.setJsonDataSource(mapper.writeValueAsString(dataMap.get(reportIds)));
						}
					}
					commonReportDataList.put(reportIds, commonReportData);
				}
			}
		}
		return commonReportDataList;
	}

}