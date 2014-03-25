package cn.onboard.android.app.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.onboard.api.dto.Activity;
import com.onboard.api.dto.Attachment;
import com.onboard.api.dto.Comment;
import com.onboard.api.dto.Company;
import com.onboard.api.dto.Discussion;
import com.onboard.api.dto.Document;
import com.onboard.api.dto.Project;
import com.onboard.api.dto.Todo;
import com.onboard.api.dto.Todolist;
import com.onboard.api.dto.Topic;
import com.onboard.api.dto.Upload;
import com.onboard.api.dto.User;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.codehaus.jackson.type.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.bean.Update;
import cn.onboard.android.app.common.HttpStreamToObject;

/**
 * API客户端接口：用于访问网络数据
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ApiClient {

	public static final String UTF_8 = "UTF-8";
	public static final String DESC = "descend";
	public static final String ASC = "ascend";

	private final static int TIMEOUT_CONNECTION = 20000;
	private final static int TIMEOUT_SOCKET = 20000;
	private final static int RETRY_TIME = 3;

	private static String appCookie;
	private static String appUserAgent;

	public static void cleanCookie() {
		appCookie = "";
	}

	private static String getCookie(AppContext appContext) {
		if (appCookie == null || appCookie == "") {
			appCookie = appContext.getProperty("cookie");
		}
		return appCookie;
	}

	private static String getUserAgent(AppContext appContext) {
		if (appUserAgent == null || appUserAgent == "") {
			StringBuilder ua = new StringBuilder("Onboard");
			ua.append('/' + appContext.getPackageInfo().versionName + '_'
					+ appContext.getPackageInfo().versionCode);// App版本
			ua.append("/Android");// 手机系统平台
			ua.append("/" + android.os.Build.VERSION.RELEASE);// 手机系统版本
			ua.append("/" + android.os.Build.MODEL); // 手机型号
			ua.append("/" + appContext.getAppId());// 客户端唯一标识
			appUserAgent = ua.toString();
		}
		return appUserAgent;
	}

	private static HttpClient getHttpClient() {
		HttpClient httpClient = new HttpClient();
		// 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		// 设置 默认的超时重试处理策略
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		// 设置 连接超时时间
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(TIMEOUT_CONNECTION);
		// 设置 读数据超时时间
		httpClient.getHttpConnectionManager().getParams()
				.setSoTimeout(TIMEOUT_SOCKET);
		// 设置 字符集
		httpClient.getParams().setContentCharset(UTF_8);
		return httpClient;
	}

	private static GetMethod getHttpGet(String url, String cookie,
			String userAgent) {
		GetMethod httpGet = new GetMethod(url);
		// 设置 请求超时时间
		httpGet.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpGet.setRequestHeader("Host", URLs.HOST);
		httpGet.setRequestHeader("Connection", "Keep-Alive");
		httpGet.setRequestHeader("Cookie", cookie);
		httpGet.setRequestHeader("User-Agent", userAgent);
		return httpGet;
	}

	private static PostMethod getHttpPost(String url, String cookie,
			String userAgent) {
		PostMethod httpPost = new PostMethod(url);
		// 设置 请求超时时间
		httpPost.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpPost.setRequestHeader("Host", URLs.HOST);
		httpPost.setRequestHeader("Connection", "Keep-Alive");
		httpPost.setRequestHeader("Cookie", cookie);
		httpPost.setRequestHeader("User-Agent", userAgent);
		return httpPost;
	}

	// private static String _MakeURL(String p_url, Map<String, Object> params)
	// {
	// StringBuilder url = new StringBuilder(p_url);
	// if(url.indexOf("?")<0)
	// url.append('?');
	//
	// for(String name : params.keySet()){
	// url.append('&');
	// url.append(name);
	// url.append('=');
	// url.append(String.valueOf(params.get(name)));
	// //不做URLEncoder处理
	// //url.append(URLEncoder.encode(String.valueOf(params.get(name)), UTF_8));
	// }
	//
	// return url.toString().replace("?&", "?");
	// }

    /**
     *
     * @param appContext
     * @param url
     * @return
     * @throws AppException
     */
	private static InputStream http_get(AppContext appContext, String url)
			throws AppException {
		// System.out.println("get_url==> "+url);
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		GetMethod httpGet = null;

		String responseBody = "";
		int time = 0;
		int statusCode;
		do {   
			try {
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, cookie, userAgent);
				statusCode = httpClient.executeMethod(httpGet);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
				responseBody = httpGet.getResponseBodyAsString();
				// System.out.println("XMLDATA=====>"+responseBody);
				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			}  finally {
				// 释放连接
				httpGet.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);

		responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
		return new ByteArrayInputStream(responseBody.getBytes());
	}

	/**
	 * 公用post方法
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @throws AppException
	 */
	private static InputStream _post(AppContext appContext, String url,
			Map<String, Object> params, Map<String, File> files)
			throws AppException {
		// System.out.println("post_url==> "+url);
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		PostMethod httpPost = null;

		// post表单参数处理
		int length = (params == null ? 0 : params.size())
				+ (files == null ? 0 : files.size());
		Part[] parts = new Part[length];
		int i = 0;
		if (params != null)
			for (String name : params.keySet()) {
				parts[i++] = new StringPart(name, String.valueOf(params
						.get(name)), UTF_8);
				// System.out.println("post_key==> "+name+"    value==>"+String.valueOf(params.get(name)));
			}
		if (files != null)
			for (String file : files.keySet()) {
				try {
					parts[i++] = new FilePart(file, files.get(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				// System.out.println("post_key_file==> "+file);
			}

		String responseBody = "";
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				// httpClient.getParams().setAuthenticationPreemptive(true);
				// httpClient.getState().setCredentials(AuthScope.ANY, new
				// UsernamePasswordCredentials("xuchen109@gmail.com",
				// "12345678"));
				httpPost = getHttpPost(url, cookie, userAgent);
				for (String name : params.keySet()) {
					httpPost.setParameter(name,
							String.valueOf(params.get(name)));
				}
				// httpPost.setRequestEntity(new
				// MultipartRequestEntity(parts,httpPost.getParams()));

				int statusCode = httpClient.executeMethod(httpPost);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				} else if (statusCode == HttpStatus.SC_OK) {
					Cookie[] cookies = httpClient.getState().getCookies();
					String tmpcookies = "";
					for (Cookie ck : cookies) {
						tmpcookies += ck.toString() + ";";
					}
					// 保存cookie
					if (appContext != null && tmpcookies != "") {
						appContext.setProperty("cookie", tmpcookies);
						appCookie = tmpcookies;
					}
				}
				responseBody = httpPost.getResponseBodyAsString();
				// System.out.println("XMLDATA=====>"+responseBody);
				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpPost.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);

		responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
		return new ByteArrayInputStream(responseBody.getBytes());
	}

	/**
	 * post请求URL
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @throws AppException
	 * @throws IOException
	 * @throws
	 */
	// private static Result http_post(AppContext appContext, String url,
	// Map<String, Object> params, Map<String,File> files) throws AppException,
	// IOException {
	// return Result.parseUser(_post(appContext, url, params, files));
	// }

	/**
	 * 获取网络图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getNetBitmap(String url) throws AppException {
		// System.out.println("image_url==> "+url);
		HttpClient httpClient = null;
		GetMethod httpGet = null;
		Bitmap bitmap = null;
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, null, null);
				int statusCode = httpClient.executeMethod(httpGet);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
				InputStream inStream = httpGet.getResponseBodyAsStream();
				bitmap = BitmapFactory.decodeStream(inStream);
				inStream.close();
				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpGet.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		return bitmap;
	}


	public static Update checkVersion(AppContext appContext)
			throws AppException {
		try {
			return Update.parse(http_get(appContext, URLs.UPDATE_VERSION));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	// /**
	// * 登录， 自动处理cookie
	// * @param url
	// * @param username
	// * @param pwd
	// * @return
	// * @throws AppException
	// */
	// public static User login(AppContext appContext, String username, String
	// pwd) throws AppException {
	// Map<String,Object> params = new HashMap<String,Object>();
	// // params.put("username", username);
	// // params.put("pwd", pwd);
	// // params.put("keep_login", 1);
	//
	// // String loginurl = URLs.LOGIN_VALIDATE_HTTP;
	// String loginurl = "http://192.168.1.102:8080/api/";
	// if(appContext.isHttpsLogin()){
	// loginurl = URLs.LOGIN_VALIDATE_HTTPS;
	// }
	//
	// try{
	// List<Company> companyList=Company.parse(http_get(appContext,loginurl));
	// return null;
	// }catch(Exception e){
	// if(e instanceof AppException)
	// throw (AppException)e;
	// throw AppException.network(e);
	// }
	// }


	public static User login(AppContext appContext, String username, String pwd)
			throws AppException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("email", username);
		params.put("password", pwd);
		params.put("remember", 1);

		String loginurl = URLs.LOGIN_VALIDATE_HTTP;
		try {
			return HttpStreamToObject.inputStreamToObject(
                    new TypeReference<User>() {
                    }, _post(appContext, loginurl, params, null));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 获得公司列表
	 * 
	 * @return
	 * @throws AppException
	 */
	public static List<Company> getCompanyList(AppContext appContext)
			throws AppException {
		String newUrl = URLs.COMPANY_LIST_HTTP;
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Company>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static List<Project> getProjectByCompanyId(AppContext appContext,
			int companyId) throws AppException {
		String newUrl = URLs.PROJECT_LIST_HTTP.replaceAll("companyId",
				companyId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Project>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}

	}

	public static List<Todolist> getTodoListByProjectId(AppContext appContext,
			int companyId, int projectId) throws AppException {
		String newUrl = URLs.TODOLIST_LIST_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("projectId", projectId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Todolist>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}

	}

	public static List<Topic> getTopicsByProjectId(AppContext appContext,
			int companyId, int projectId) throws AppException {
		String newUrl = URLs.TOPIC_LIST_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("projectId", projectId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Topic>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}

	}

	public static List<Document> getDocumentsByProjectId(AppContext appContext,
			int companyId, int projectId) throws AppException {
		String newUrl = URLs.DOCUMENT__LIST_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("projectId", projectId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Document>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

    public static List<Todolist> getTodolistsByProjectId(AppContext appContext,
                                                         int companyId, int projectId) throws AppException {
        String newUrl = URLs.TODOLIST__LIST_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("projectId", projectId + "");
        try {
            return HttpStreamToObject.inputStreamToObject(
                    new TypeReference<List<Todolist>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Discussion getDiscussionById(AppContext appContext,
			int companyId, int projectId, int discussionId) throws AppException {
		String newUrl = URLs.DISCUSSION_HTTP_STRING
				.replaceAll("companyId", companyId + "")
				.replaceAll("projectId", projectId + "")
				.replaceAll("discussionId", discussionId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<Discussion>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}

	}

	public static Document getDocumentById(AppContext appContext,
			int companyId, int projectId, int documentId) throws AppException {
		String newUrl = URLs.DOCUMENT_HTTP_STRING
				.replaceAll("companyId", companyId + "")
				.replaceAll("projectId", projectId + "")
				.replaceAll("documentId", documentId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<Document>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}

	}

	public static List<Discussion> getDiscussionsByProjectId(
			AppContext appContext, int companyId, int projectId)
			throws AppException {
		String newUrl = URLs.DISCUSSION__LIST_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("projectId", projectId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Discussion>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static List<Activity> getActivitiesByProjectId(
			AppContext appContext, int companyId, int projectId,int page)
			throws AppException {
		String newUrl = URLs.ACTIVITY__LIST_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("projectId", projectId + "")+"?page="+page;
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Activity>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

//	public static List<Repository> getRepositoryByProjectId(
//			AppContext appContext, int companyId, int projectId)
//			throws AppException {
//		String newUrl = URLs.REPOSITY__LIST_HTTP.replaceAll("companyId",
//				companyId + "").replaceAll("projectId", projectId + "");
//		try {
//			return HttpStreamToObject.inputStreamToObject(
//					new TypeReference<List<Repository>>() {
//					}, http_get(appContext, newUrl));
//		} catch (Exception e) {
//			if (e instanceof AppException)
//				throw (AppException) e;
//			throw AppException.network(e);
//		}
//
//	}

	public static List<Upload> getUploadByProjectId(AppContext appContext,
			int companyId, int projectId) throws AppException {
		String newUrl = URLs.UPLOAD__LIST_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("projectId", projectId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Upload>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

    public static Upload getUploadById(AppContext appContext, int companyId, int projectId, int uploadId) throws AppException {
        String newUrl = URLs.UPLOAD_HTTP.replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "")
                .replaceAll("uploadId", uploadId + "");
        try {
            return HttpStreamToObject.inputStreamToObject(new TypeReference<Upload>() {
            }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }
	
	public static List<Attachment> getAttachmentsByProjectId(AppContext appContext,
			int companyId, int projectId) throws AppException {
		String newUrl = URLs.ATTACHMENT_LIST_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("projectId", projectId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Attachment>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static List<User> getUsersByProjectId(AppContext appContext,
			int companyId, int projectId) throws AppException {
		String newUrl = URLs.USERS__LIST_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("projectId", projectId + "");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<User>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

    public static Map<String, List<User>> getDepartmentNameUserMapByCompanyId(AppContext appContext, int companyId) throws AppException {
        String newUrl = URLs.DEPARTMENT_NAME_USER_MAP_HTTP.replaceAll("companyId", companyId + "");
        try {
            return HttpStreamToObject.inputStreamToObject(
                    new TypeReference<Map<String, List<User>>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
                if (e instanceof AppException)
                    throw (AppException) e;
                throw AppException.network(e);
            }
    }

	public static Comment publishComment(AppContext appContext, Comment comment)
			throws AppException {
		String url = URLs.PUBLISH_COMMENT_HTTP.replaceAll("companyId", comment.getCompanyId()+"").replaceAll("projectId", comment.getProjectId()+"").replaceAll("attachType", comment.getAttachType()).replaceAll("attachId", comment.getAttachId()+"");
		Map<String,Object> params = HttpStreamToObject.objectToMap(comment);
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<Comment>() {
					}, _post(appContext, url, params, null));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static Discussion createDiscussion(AppContext appContext, Discussion discussion)
			throws AppException {
		String url = URLs.DISCUSSION__LIST_HTTP.replaceAll("companyId", discussion.getCompanyId()+"").replaceAll("projectId", discussion.getProjectId()+"");
		Map<String,Object> params = HttpStreamToObject.objectToMap(discussion);
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<Discussion>() {
					}, _post(appContext, url, params, null));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static Document createDocument(AppContext appContext, Document document)
			throws AppException {
		String url = URLs.DOCUMENT__LIST_HTTP.replaceAll("companyId", document.getCompanyId()+"").replaceAll("projectId", document.getProjectId()+"");
		Map<String,Object> params = HttpStreamToObject.objectToMap(document);
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<Document>() {
					}, _post(appContext, url, params, null));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static List<Todo> getCalendarTodos(AppContext appContext,int companyId,long startTime,long endTime) throws AppException
	{
		String newUrl = URLs.CALENDAR_TODO_HTTP.replaceAll("companyId",
				companyId + "").replaceAll("startTime", startTime + "").replaceAll("endTime", endTime+"");
		try {
			return HttpStreamToObject.inputStreamToObject(
					new TypeReference<List<Todo>>() {
					}, http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
		
	}

    public static Todo getTodoById(AppContext appContext, int companyId, int projectId, int todoId) throws AppException {
        String newUrl = URLs.CALENDAR_TODO_HTTP.replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "")
                .replaceAll("todoId", todoId + "");
        try {
            return HttpStreamToObject.inputStreamToObject(new TypeReference<Todo>() {
            }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }
}
