package cn.onboard.android.app.common;


import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import cn.onboard.android.app.AppException;

public class HttpStreamToObject {
	public static <T> T inputStreamToObject(final TypeReference<T> type,
			InputStream inputStream) throws IOException, AppException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "/n");
			}
			T data = null;
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			data = mapper.readValue(sb.toString(), type);
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public static Map<String, Object> objectToMap (Object object)
	{
		ObjectMapper m = new ObjectMapper();
		m.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
		Map<String,Object> params = m.convertValue(object, Map.class);
		return params;
	}
	
	
}
