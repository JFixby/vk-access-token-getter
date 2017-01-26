
package com.druidkuma.vk;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.log.L;

/** Created by Iurii Miedviediev
 *
 * @author DruidKuma
 * @version 1.0.0
 * @since 12/23/15 */
public final class VKTokenGetter {

	private VKTokenGetter () {
	}

	// Login URL
	public static final String VK_URL = "https://m.vk.com";

	// OAuth authorization URL
	public static final String AUTH_URL = "http://oauth.vk.com/oauth/authorize?redirect_uri=http://oauth.vk.com/blank.html&response_type=token&client_id=%s&scope=%s&display=wap";

	// Cookie name to resolve invalid credentials flow
	private static final String CHECKED_PROPERTIES = "p";

	/** Imitates browser VK login activity
	 *
	 * TODO: Handle additional login flow checks (e.g. captcha)
	 *
	 * @param login user login (phone or email)
	 * @param password user password
	 * @return collection of retrieved cookies after successful login
	 * @throws IOException */
	private static Map<String, String> login (final String login, final String password) throws IOException {
		Map<String, String> cookies;

		// retrieve login page html
		Connection.Response connection = Jsoup.connect(VK_URL).execute();

		// get login action URL
		final String url = getFormActionFromPage(connection);

		// get cookies from first login page retrieving
		cookies = Collections.newMap(connection.cookies());

		// prepare and send credentials along with got cookies to the login action
		final Map<String, String> data = Collections.newMap();
		data.put("email", login);
		data.put("pass", password);
		connection = Jsoup.connect(url).cookies(cookies.toJavaMap()).data(data.toJavaMap()).execute();

		// get cookies after login
		cookies = Collections.newMap(connection.cookies());

		// check the invalid credentials flow
		if (!cookies.containsKey(CHECKED_PROPERTIES)) {
			throw new RuntimeException("Invalid login/password");
		}
		return cookies;
	}

	/** Retrieves Access Token, valid within 24 hours
	 * @param appId VK application ID
	 * @param scope scopes with comma delimeter
	 * @param login user login (email or phone)
	 * @param password user password
	 * @return VK access token
	 * @throws IOException */
	public static String getAccessToken (final String appId, final String scope, final String login, final String password)
		throws IOException {

		// login and get cookies
		final Map<String, String> loginCookies = login(login, password);
		loginCookies.print("loginCookies");
		// imitate the oauth authorization flow, assuming user is logged in

		final String connectString = String.format(AUTH_URL, appId, scope);
		L.d("connectString", connectString);
		Connection.Response response = Jsoup.connect(connectString).cookies(loginCookies.toJavaMap()).execute();

		/*
		 * If access token is still not in the location of the retrieved page, it means that application is being authorized for the
		 * first time, and user have to grant access to it, according to the requested scopes (Imitates clicking on the "Permit"
		 * button)
		 */
		if (response.url().getRef() == null) {
			final String grantAccessAction = getFormActionFromPage(response);
			response = Jsoup.connect(grantAccessAction).cookies(loginCookies.toJavaMap()).execute();
		}

		// response url ref contains all token info (time until expiration and user id), we need only token value itself
		return response.url().getRef().split("&")[0].split("=")[1];
	}

	/** Given Response with page HTML, find form element and retrieves its action URL
	 * @param pageResponse response with page HTML
	 * @return form action URL
	 * @throws IOException */
	private static String getFormActionFromPage (final Connection.Response pageResponse) throws IOException {
		final Document document = pageResponse.parse();
		final Element form = document.getElementsByTag("form").get(0);
		return form.attr("action");
	}
}
